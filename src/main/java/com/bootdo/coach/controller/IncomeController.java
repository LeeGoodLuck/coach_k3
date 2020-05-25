package com.bootdo.coach.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bootdo.coach.domain.TabAgent;
import com.bootdo.coach.service.TabAgentService;
import com.bootdo.coach.service.TabCoachService;
import com.bootdo.coach.service.TabSeparateAccountService;
import com.bootdo.coach.vo.TabSeparateAccountVo;
import com.bootdo.common.controller.BaseController;
import com.bootdo.common.utils.PageUtils;
import com.bootdo.common.utils.Query;

import io.swagger.annotations.ApiOperation;

/**
 * 学员端
 * @author Administrator
 *
 */
@RequestMapping("/coach/income")
@Controller
public class IncomeController extends BaseController{
	private String prefix = "coach/income";
	@Autowired
	private TabSeparateAccountService separateAccountService;

	@Autowired
	private TabAgentService agentService;
	
	@Autowired
	private TabCoachService coachService;

	@RequiresPermissions("coach:income:income")
	@GetMapping("income")
	String test(Model model) {
		BigDecimal totalMoney = null;
		BigDecimal dayRevenue = null;
		BigDecimal sevenRevenue = null;
		BigDecimal monthRevenue = null;
		Map<String, Object> revenue = new HashMap<>();
		// 找出当前用户的代理商
		String bg_account = getUser().getUsername();
		TabAgent curAgent = agentService.findAgent(bg_account, null);
		if (curAgent != null) {
			Set<Long> agentIdList = new HashSet<Long>();
			agentIdList.add(curAgent.getAgentId());
			Set<Long> tempList = null;
			switch (curAgent.getLevel()) {
			case 0:
				// 找出下级
				tempList = agentService.findAgentIdList(agentIdList);
				if (tempList != null) {
					agentIdList.addAll(tempList);
					// 找出下下级
					tempList = agentService.findAgentIdList(agentIdList);
					if (tempList != null) {
						agentIdList.addAll(tempList);
					}
				}
				break;
			case 1:
				// 找出下级
				tempList = agentService.findAgentIdList(agentIdList);
				if (tempList != null) {
					agentIdList.addAll(tempList);
				}
				break;

			default:
				break;
			}
			// 找出代理商下的教练
			Set<Long> coachIdList = coachService.findCoachIdList(agentIdList);
			
			// 查询列表数据
			if (coachIdList.size() != 0) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("coachIdList", coachIdList);
				totalMoney = separateAccountService.getAgentTotalMoney(params);
				//本月
				monthRevenue = separateAccountService.findMonthRevenue(params);
				//近七天
				sevenRevenue = separateAccountService.findSevenRevenue(params);
				//本日
				dayRevenue = separateAccountService.findDayRevenue(params);
			}
		}
		if (totalMoney == null) {
			totalMoney = new BigDecimal(0.00);
		}
		if (sevenRevenue == null) {
			sevenRevenue = new BigDecimal(0.00);
		}
		if (dayRevenue == null) {
			dayRevenue = new BigDecimal(0.00);
		}
		if (monthRevenue == null) {
			monthRevenue = new BigDecimal(0.00);
		}
		totalMoney = totalMoney.setScale(2, BigDecimal.ROUND_HALF_UP);
		sevenRevenue = sevenRevenue.setScale(2, BigDecimal.ROUND_HALF_UP);
		dayRevenue = dayRevenue.setScale(2, BigDecimal.ROUND_HALF_UP);
		monthRevenue = monthRevenue.setScale(2, BigDecimal.ROUND_HALF_UP);
		revenue.put("dayRevenue", dayRevenue);
		revenue.put("sevenRevenue", sevenRevenue);
		revenue.put("monthRevenue", monthRevenue);
		revenue.put("totalMoney", totalMoney);
		model.addAttribute("revenue", revenue);
		return prefix + "/income";
	}

	@ApiOperation(value = "获取收入列表", notes = "")
	@ResponseBody
	@GetMapping("/incomeList")
	@RequiresPermissions("coach:income:income")
	public PageUtils testList(@RequestParam Map<String, Object> params) {
		// 找出当前用户的代理商
		String bg_account = getUser().getUsername();
		TabAgent curAgent = agentService.findAgent(bg_account, null);
		if (curAgent == null) {
			PageUtils pageUtil = new PageUtils(new ArrayList<TabSeparateAccountVo>(), 0);
			return pageUtil;
		}
		Set<Long> agentIdList = new HashSet<Long>();
		agentIdList.add(curAgent.getAgentId());
		Set<Long> tempList = null;
		switch (curAgent.getLevel()) {
		case 0:
			// 找出下级
			tempList = agentService.findAgentIdList(agentIdList);
			if (tempList != null) {
				agentIdList.addAll(tempList);
				// 找出下下级
				tempList = agentService.findAgentIdList(agentIdList);
				if (tempList != null) {
					agentIdList.addAll(tempList);
				}
			}
			break;
		case 1:
			// 找出下级
			tempList = agentService.findAgentIdList(agentIdList);
			if (tempList != null) {
				agentIdList.addAll(tempList);
			}
			break;

		default:
			break;
		}
		// 找出代理商下的教练
		Set<Long> coachIdList = coachService.findCoachIdList(agentIdList);
		
		// 查询列表数据
		Query query = new Query(params);
		query.put("coachIdList", coachIdList);
		List<TabSeparateAccountVo> list = separateAccountService.list(query);
		if (curAgent.getLevel() == 0) {
			// 一级代理商的，把list中3级代理商的记录找出上级代理商
			if (list != null) {
				for (TabSeparateAccountVo tabSeparateAccountVo : list) {
					if (tabSeparateAccountVo.getLevel() == 2) {
						TabAgent agent3 = agentService.selectByPrimaryKey(tabSeparateAccountVo.getAgentId());
						TabAgent agent2 = agentService.selectByPrimaryKey(agent3.getHigherAgentId());
						tabSeparateAccountVo.setAgentName(agent2.getAgentName());
					}
				}
			}
		}
		int total = separateAccountService.count(query);
		PageUtils pageUtil = new PageUtils(list, total);
		return pageUtil;
	}
}