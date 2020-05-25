package com.bootdo.coach.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bootdo.coach.service.TabSeparateAccountService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bootdo.coach.domain.TabAgent;
import com.bootdo.coach.domain.TabTopAgent;
import com.bootdo.coach.service.TabAgentService;
import com.bootdo.coach.service.TabCoachService;
import com.bootdo.coach.service.TabTopAgentService;
import com.bootdo.coach.vo.TabAgentVo;
import com.bootdo.common.annotation.Log;
import com.bootdo.common.controller.BaseController;
import com.bootdo.common.utils.AgentMD5;
import com.bootdo.common.utils.MD5Utils;
import com.bootdo.common.utils.PageUtils;
import com.bootdo.common.utils.Query;
import com.bootdo.common.utils.R;
import com.bootdo.system.domain.UserDO;
import com.bootdo.system.service.UserService;

import io.swagger.annotations.ApiOperation;

/**
 * 代理商
 * @author Administrator
 *
 */
@RequestMapping("/coach/agent")
@Controller
public class AgentController extends BaseController{
	private String prefix = "coach/agent";
	@Autowired
	private TabTopAgentService topAgentService;

	@Autowired
	private TabAgentService agentService;
	
	@Autowired
	private TabCoachService coachService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private TabSeparateAccountService tabSeparateAccountService;
	
	//顶级代理商=============>>>开始
	@RequiresPermissions("coach:agent:topAgent")
	@GetMapping("topAgent")
	String topAgent(Model model) {
		return prefix + "/topAgent";
	}

	@ApiOperation(value="获取顶级代理商列表", notes="")
	@ResponseBody
	@GetMapping("/topAgentList")
	@RequiresPermissions("coach:agent:topAgent")
	public PageUtils topAgentList(@RequestParam Map<String, Object> params) {
        //模糊查询
        if (params != null&&params.containsKey("topAgentName")&&!"".equals(params.get("topAgentName").toString())) {
            params.put("topAgentName", "%" + params.get("topAgentName").toString() + "%");
        }
        if (params != null&&params.containsKey("tel")&&!"".equals(params.get("tel").toString())) {
            params.put("tel", "%" + params.get("tel").toString() + "%");
        }
		// 查询列表数据
        Query query = new Query( params );
		List<TabTopAgent> topAgentList = topAgentService.list(query);
		int total = topAgentService.count(query);
		PageUtils pageUtil = new PageUtils(topAgentList, total);
		return pageUtil;
	}

	@RequiresPermissions("coach:agent:topAgentAdd")
	@Log("添加顶级代理商")
	@GetMapping("/topAgentAdd")
	String topAgentAdd() {
		return prefix + "/topAgentAdd";
	}

	@RequiresPermissions("coach:agent:topAgentEdit")
	@Log("编辑顶级代理商")
	@GetMapping("/topAgentEdit/{topAgentId}")
	String topAgentEdit(Model model, @PathVariable("topAgentId") Long topAgentId) {
		TabTopAgent topAgent = topAgentService.selectByPrimaryKey(topAgentId);
		model.addAttribute("topAgent", topAgent);
		return prefix+"/topAgentEdit";
	}

	@RequiresPermissions("coach:agent:topAgentAdd")
	@Log("保存顶级代理商")
	@PostMapping("/saveTopAgent")
	@ResponseBody
	R saveTopAgent(TabTopAgent topAgent) {
		if (topAgentService.insertSelective(topAgent) > 0) {
			return R.ok();
		}
		return R.error();
	}

	@RequiresPermissions("coach:agent:topAgentEdit")
	@Log("更新顶级代理商")
	@PostMapping("/updateTopAgent")
	@ResponseBody
	R updateTopAgent(TabTopAgent topAgent) {
		if (topAgentService.updateByPrimaryKeySelective(topAgent) > 0) {
			return R.ok();
		}
		return R.error();
	}

	@RequiresPermissions("coach:agent:topAgentRemove")
	@Log("删除顶级代理商")
	@PostMapping("/removeTopAgent")
	@ResponseBody
	R removeTopAgent(Long topAgentId) {
		if (topAgentService.deleteByPrimaryKey(topAgentId) > 0) {
			return R.ok();
		}
		return R.error();
	}

	@PostMapping("/exitTopAgentName")
	@ResponseBody
	boolean exitTopAgentName(@RequestParam Map<String, Object> params) {
		// 存在，不通过，false
		return !topAgentService.exitTopAgentName(params);
	}
	
	//顶级代理商=============>>>结束

	//一级代理商=============>>>开始
	
	@RequiresPermissions("coach:agent:agent1")
	@GetMapping("agent1")
	String agent1(Model model) {
		return prefix + "/agent1";
	}
	
	@ApiOperation(value="获取一级代理商列表", notes="")
	@ResponseBody
	@GetMapping("/agent1List")
	@RequiresPermissions("coach:agent:agent1")
	public PageUtils agent1List(@RequestParam Map<String, Object> params) {
        //模糊查询
        if (params != null&&params.containsKey("agentName")&&!"".equals(params.get("agentName").toString())) {
            params.put("agentName", "%" + params.get("agentName").toString() + "%");
        }
        if (params != null&&params.containsKey("tel")&&!"".equals(params.get("tel").toString())) {
            params.put("tel", "%" + params.get("tel").toString() + "%");
        }
		// 查询列表数据
		Query query = new Query( params );
		query.put("level", 0);
		List<TabAgentVo> agent1List = agentService.list(query);
		for (int i = 0; i <agent1List.size(); i++) {
			Map<String, Object> map = new HashMap<>();
			map.put("level", agent1List.get(i).getLevel());
			System.out.println("level==="+agent1List.get(i).getLevel());
			map.put("agentId", agent1List.get(i).getAgentId());
			System.out.println("agentId==="+agent1List.get(i).getAgentId());
			BigDecimal bigDecimal = tabSeparateAccountService.finAgentTotalRevenue(map);
			agent1List.get(i).setAgentOneProfit(bigDecimal);
		}
		int total = agentService.count(query);
		PageUtils pageUtil = new PageUtils(agent1List, total);
		return pageUtil;
	}
	
	@RequiresPermissions("coach:agent:agent1Add")
	@Log("添加一级代理商")
	@GetMapping("/agent1Add")
	String agent1Add() {
		return prefix + "/agent1Add";
	}
	
	@RequiresPermissions("coach:agent:agent1Edit")
	@Log("编辑一级代理商")
	@GetMapping("/agent1Edit/{agentId}")
	String agent1Edit(Model model, @PathVariable("agentId") Long agentId) {
		TabAgent agent = agentService.selectByPrimaryKey(agentId);
		model.addAttribute("agent", agent);
		return prefix+"/agent1Edit";
	}

	@RequiresPermissions("coach:agent:agent1Change")
	@Log("更换一级代理商")
	@GetMapping("/agent1Change/{agentId}")
	String agent1Change(Model model, @PathVariable("agentId") Long agentId) {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("notAgentId", agentId);
		query.put("level", 0);
		query.put("status", 1);
		query.put("isDelete", 0);
		List<TabAgentVo> agent1List = agentService.list(query);
		
		TabAgent agent = agentService.selectByPrimaryKey(agentId);
		model.addAttribute("agent", agent);
		model.addAttribute("agent1List", agent1List);
		return prefix+"/agent1Change";
	}
	
	@RequiresPermissions("coach:agent:agent1Change")
	@Log("更换一级代理商")
	@PostMapping("/changeAgent1")
	@ResponseBody
	R changeAgent1(Long agentId,Long changeAgentId) {
		// 把代理商下级代理商都转到他的上级代理商，下级代理商自动升级
		agentService.updateAgentHigherAgentId(agentId, changeAgentId);
		// 把代理商下的教练转到他的上级代理商
		coachService.updateAgentId(agentId, changeAgentId);
		
		return R.ok();
	}
	
	@RequiresPermissions("coach:agent:agent1Add")
	@Log("保存一级代理商")
	@PostMapping("/saveAgent1")
	@ResponseBody
	R saveAgent1(TabAgent agent) {
		agent.setLevel(0);// 设置级别
		String oldpwd = agent.getBgPassword();
		agent.setBgPassword(AgentMD5.md5(agent.getBgPassword()));
		agent.setHigherAgentId(0l);
		
		if (agentService.insertSelective(agent) > 0) {
			addUser(agent.getAgentName(), agent.getBgAccount(), oldpwd);
			return R.ok();
		}
		return R.error();
	}
	
	@RequiresPermissions("coach:agent:agent1Edit")
	@Log("更新一级代理商")
	@PostMapping("/updateAgent1")
	@ResponseBody
	R updateAgent1(TabAgent agent) {
		
		TabAgent temp = agentService.selectByPrimaryKey(agent.getAgentId());
		if (!temp.getBgPassword().equals(agent.getBgPassword())) {
			agent.setBgPassword(AgentMD5.md5(agent.getBgPassword()));
		}
		
		if (agentService.updateByPrimaryKeySelective(agent) > 0) {
			return R.ok();
		}
		return R.error();
	}
	
	@RequiresPermissions("coach:agent:agent1Remove")
	@Log("删除一级代理商")
	@PostMapping("/removeAgent1")
	@ResponseBody
	R removeAgent1(Long agentId) {
		if (agentService.deleteByPrimaryKey(agentId) > 0) {
			return R.ok();
		}
		return R.error();
	}
	
	@RequiresPermissions("coach:agent:agent1Quit")
	@Log("一级代理商离职")
	@PostMapping("/quitAgent1")
	@ResponseBody
	R quitAgent1(Long agentId) {
		// 一级代理商离职，先判断他下面是否有二级代理或者教练，如果有，则不给离职
		if (agentService.exitAgentByAgentId(agentId)) {
			return R.error("此一级代理还有下级代理，请先把下级代理转让");
		}
		if (coachService.exitCoachByAgentId(agentId)) {
			return R.error("此一级代理还有教练，请先把教练转让");
		}
		
		TabAgent agent = agentService.selectByPrimaryKey(agentId);
		agent.setIsDelete(1);
		agent.setStatus(0);
		
		if (agentService.updateByPrimaryKeySelective(agent) > 0) {
			return R.ok();
		}
		return R.error();
	}
	
	//一级代理商=============>>>结束
	
	//二级代理商=============>>>开始
	
	@RequiresPermissions("coach:agent:agent2")
	@GetMapping("agent2")
	String agent2(Model model) {
		return prefix + "/agent2";
	}
	
	@ApiOperation(value="获取二级代理商列表", notes="")
	@ResponseBody
	@GetMapping("/agent2List")
	@RequiresPermissions("coach:agent:agent2")
	public PageUtils agent2List(@RequestParam Map<String, Object> params) {
        //模糊查询
        if (params != null&&params.containsKey("agentName")&&!"".equals(params.get("agentName").toString())) {
            params.put("agentName", "%" + params.get("agentName").toString() + "%");
        }
        if (params != null&&params.containsKey("tel")&&!"".equals(params.get("tel").toString())) {
            params.put("tel", "%" + params.get("tel").toString() + "%");
        }
		// 查询列表数据
		Query query = new Query( params );
		query.put("level", 1);
		List<TabAgentVo> agent2List = agentService.list(query);
        for (int i = 0; i <agent2List.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("level", agent2List.get(i).getLevel());
            System.out.println("level==="+agent2List.get(i).getLevel());
            map.put("agentId", agent2List.get(i).getAgentId());
            System.out.println("agentId==="+agent2List.get(i).getAgentId());
            BigDecimal bigDecimal = tabSeparateAccountService.finAgentTotalRevenue(map);
            agent2List.get(i).setAgentTwoProfit(bigDecimal);
        }
		int total = agentService.count(query);
		PageUtils pageUtil = new PageUtils(agent2List, total);
		return pageUtil;
	}
	
	@RequiresPermissions("coach:agent:agent2Add")
	@Log("添加二级代理商")
	@GetMapping("/agent2Add")
	String agent2Add(Model model) {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("level", 0);
		query.put("status", 1);
		List<TabAgentVo> agent1List = agentService.list(query);
		if (agent1List == null) {
			agent1List = new ArrayList<TabAgentVo>();
		}
		TabAgentVo one = new TabAgentVo();
		one.setAgentId(0l);
		one.setAgentName("请选择");
		agent1List.add(0, one);
		model.addAttribute("agent1List", agent1List);
		return prefix + "/agent2Add";
	}
	
	@RequiresPermissions("coach:agent:agent2Edit")
	@Log("编辑二级代理商")
	@GetMapping("/agent2Edit/{agentId}")
	String agent2Edit(Model model, @PathVariable("agentId") Long agentId) {
		TabAgent agent = agentService.selectByPrimaryKey(agentId);
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("level", 0);
		query.put("status", 1);
		List<TabAgentVo> agent1List = agentService.list(query);
		
		if (agent1List == null) {
			agent1List = new ArrayList<TabAgentVo>();
		}
		TabAgentVo one = new TabAgentVo();
		one.setAgentId(0l);
		one.setAgentName("请选择");
		agent1List.add(0, one);
		
		model.addAttribute("agent1List", agent1List);
		model.addAttribute("agent", agent);
		return prefix+"/agent2Edit";
	}
	
	@RequiresPermissions("coach:agent:agent2Add")
	@Log("保存二级代理商")
	@PostMapping("/saveAgent2")
	@ResponseBody
	R saveAgent2(TabAgent agent) {
		if (agent.getHigherAgentId() == null || agent.getHigherAgentId() == 0l) {
			return R.error("请选择上级代理");
		}
		
		agent.setLevel(1);// 设置级别

		String oldpwd = agent.getBgPassword();
		agent.setBgPassword(AgentMD5.md5(agent.getBgPassword()));
		
		if (agentService.insertSelective(agent) > 0) {
			addUser(agent.getAgentName(), agent.getBgAccount(), oldpwd);
			return R.ok();
		}
		return R.error();
	}
	
	@RequiresPermissions("coach:agent:agent2Edit")
	@Log("更新二级代理商")
	@PostMapping("/updateAgent2")
	@ResponseBody
	R updateAgent2(TabAgent agent) {
		if (agent.getHigherAgentId() == null || agent.getHigherAgentId() == 0l) {
			return R.error("请选择上级代理");
		}
		
		TabAgent temp = agentService.selectByPrimaryKey(agent.getAgentId());
		if (!temp.getBgPassword().equals(agent.getBgPassword())) {
			agent.setBgPassword(AgentMD5.md5(agent.getBgPassword()));
		}
		
		if (agentService.updateByPrimaryKeySelective(agent) > 0) {
			return R.ok();
		}
		return R.error();
	}
	
//	@RequiresPermissions("coach:agent:agent2Remove")
//	@Log("删除二级代理商")
//	@PostMapping("/removeAgent2")
//	@ResponseBody
//	R removeAgent2(Long agentId) {
//		
//		if (agentService.deleteByPrimaryKey(agentId) > 0) {
//			return R.ok();
//		}
//		return R.error();
//	}
	
	@RequiresPermissions("coach:agent:agent2Quit")
	@Log("二级代理商离职")
	@PostMapping("/quitAgent2")
	@ResponseBody
	R quitAgent2(Long agentId) {
		
		TabAgent agent = agentService.selectByPrimaryKey(agentId);
		agent.setIsDelete(1);
		agent.setStatus(0);
		
		if (agentService.updateByPrimaryKeySelective(agent) > 0) {
			// 把代理商下级代理商都转到他的上级代理商，下级代理商自动升级
			agentService.updateAgentLevelAndHigherAgentId(agent.getAgentId(), agent.getHigherAgentId());
			// 把代理商下的教练转到他的上级代理商
			coachService.updateAgentId(agent.getAgentId(), agent.getHigherAgentId());
			
			return R.ok();
		}
		return R.error();
	}
	
	@RequiresPermissions("coach:agent:agent2Change")
	@Log("更换二级代理商")
	@GetMapping("/agent2Change/{agentId}")
	String agent2Change(Model model, @PathVariable("agentId") Long agentId) {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("notAgentId", agentId);
		query.put("level", 1);
		query.put("status", 1);
		query.put("isDelete", 0);
		List<TabAgentVo> agent2List = agentService.list(query);
		
		TabAgent agent = agentService.selectByPrimaryKey(agentId);
		model.addAttribute("agent", agent);
		model.addAttribute("agent2List", agent2List);
		return prefix+"/agent2Change";
	}
	
	@RequiresPermissions("coach:agent:agent2Change")
	@Log("更换二级代理商")
	@PostMapping("/changeAgent2")
	@ResponseBody
	R changeAgent2(Long agentId,Long changeAgentId) {
		// 把代理商下级代理商都转到他的上级代理商，下级代理商自动升级
		agentService.updateAgentHigherAgentId(agentId, changeAgentId);
		// 把代理商下的教练转到他的上级代理商
		coachService.updateAgentId(agentId, changeAgentId);
		
		return R.ok();
	}
	
	//二级代理商=============>>>结束
	
	//三级代理商=============>>>开始
	
	@RequiresPermissions("coach:agent:agent3")
	@GetMapping("agent3")
	String agent3(Model model) {
		return prefix + "/agent3";
	}
	
	@ApiOperation(value="获取三级代理商列表", notes="")
	@ResponseBody
	@GetMapping("/agent3List")
	@RequiresPermissions("coach:agent:agent3")
	public PageUtils agent3List(@RequestParam Map<String, Object> params) {
        //模糊查询
        if (params != null&&params.containsKey("agentName")&&!"".equals(params.get("agentName").toString())) {
            params.put("agentName", "%" + params.get("agentName").toString() + "%");
        }
        if (params != null&&params.containsKey("tel")&&!"".equals(params.get("tel").toString())) {
            params.put("tel", "%" + params.get("tel").toString() + "%");
        }
		// 查询列表数据
		Query query = new Query( params );
		query.put("level", 2);
		List<TabAgentVo> agent3List = agentService.list(query);
        for (int i = 0; i <agent3List.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("level", agent3List.get(i).getLevel());
            System.out.println("level==="+agent3List.get(i).getLevel());
            map.put("agentId", agent3List.get(i).getAgentId());
            System.out.println("agentId==="+agent3List.get(i).getAgentId());
            BigDecimal bigDecimal = tabSeparateAccountService.finAgentTotalRevenue(map);
            agent3List.get(i).setAgentThreeProfit(bigDecimal);
        }
		int total = agentService.count(query);
		PageUtils pageUtil = new PageUtils(agent3List, total);
		return pageUtil;
	}
	
	@RequiresPermissions("coach:agent:agent3Add")
	@Log("添加三级代理商")
	@GetMapping("/agent3Add")
	String agent3Add(Model model) {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("level", 1);
		query.put("status", 1);
		List<TabAgentVo> agent2List = agentService.list(query);
		if (agent2List == null) {
			agent2List = new ArrayList<TabAgentVo>();
		}
		TabAgentVo one = new TabAgentVo();
		one.setAgentId(0l);
		one.setAgentName("请选择");
		agent2List.add(0, one);
		model.addAttribute("agent2List", agent2List);
		return prefix + "/agent3Add";
	}
	
	@RequiresPermissions("coach:agent:agent3Edit")
	@Log("编辑三级代理商")
	@GetMapping("/agent3Edit/{agentId}")
	String agent3Edit(Model model, @PathVariable("agentId") Long agentId) {
		TabAgent agent = agentService.selectByPrimaryKey(agentId);
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("level", 1);
		query.put("status", 1);
		List<TabAgentVo> agent2List = agentService.list(query);
		
		if (agent2List == null) {
			agent2List = new ArrayList<TabAgentVo>();
		}
		TabAgentVo one = new TabAgentVo();
		one.setAgentId(0l);
		one.setAgentName("请选择");
		agent2List.add(0, one);
		
		model.addAttribute("agent2List", agent2List);
		model.addAttribute("agent", agent);
		return prefix+"/agent3Edit";
	}
	
	@RequiresPermissions("coach:agent:agent3Add")
	@Log("保存三级代理商")
	@PostMapping("/saveAgent3")
	@ResponseBody
	R saveAgent3(TabAgent agent) {
		if (agent.getHigherAgentId() == null || agent.getHigherAgentId() == 0l) {
			return R.error("请选择上级代理");
		}
		
		agent.setLevel(2);// 设置级别

		String oldpwd = agent.getBgPassword();
		agent.setBgPassword(AgentMD5.md5(agent.getBgPassword()));
		
		if (agentService.insertSelective(agent) > 0) {
			addUser(agent.getAgentName(), agent.getBgAccount(), oldpwd);
			return R.ok();
		}
		return R.error();
	}
	
	@RequiresPermissions("coach:agent:agent3Edit")
	@Log("更新三级代理商")
	@PostMapping("/updateAgent3")
	@ResponseBody
	R updateAgent3(TabAgent agent) {
		if (agent.getHigherAgentId() == null || agent.getHigherAgentId() == 0l) {
			return R.error("请选择上级代理");
		}
		
		TabAgent temp = agentService.selectByPrimaryKey(agent.getAgentId());
		if (!temp.getBgPassword().equals(agent.getBgPassword())) {
			agent.setBgPassword(AgentMD5.md5(agent.getBgPassword()));
		}
		
		if (agentService.updateByPrimaryKeySelective(agent) > 0) {
			return R.ok();
		}
		return R.error();
	}
	
//	@RequiresPermissions("coach:agent:agent3Remove")
//	@Log("删除三级代理商")
//	@PostMapping("/removeAgent3")
//	@ResponseBody
//	R removeAgent3(Long agentId) {
//		
//		if (agentService.deleteByPrimaryKey(agentId) > 0) {
//			return R.ok();
//		}
//		return R.error();
//	}
	
	@RequiresPermissions("coach:agent:agent3Quit")
	@Log("三级代理商离职")
	@PostMapping("/quitAgent3")
	@ResponseBody
	R quitAgent3(Long agentId) {
		
		TabAgent agent = agentService.selectByPrimaryKey(agentId);
		agent.setIsDelete(1);
		agent.setStatus(0);
		
		if (agentService.updateByPrimaryKeySelective(agent) > 0) {
//			// 把代理商下级代理商都转到他的上级代理商，下级代理商自动升级
//			agentService.updateAgentLevelAndHigherAgentId(agent.getAgentId(), agent.getHigherAgentId());
			// 把代理商下的教练转到他的上级代理商
			coachService.updateAgentId(agent.getAgentId(), agent.getHigherAgentId());
			
			return R.ok();
		}
		return R.error();
	}
	
	@RequiresPermissions("coach:agent:agent3Change")
	@Log("更换三级代理商")
	@GetMapping("/agent3Change/{agentId}")
	String agent3Change(Model model, @PathVariable("agentId") Long agentId) {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("notAgentId", agentId);
		query.put("level", 2);
		query.put("status", 1);
		query.put("isDelete", 0);
		List<TabAgentVo> agent3List = agentService.list(query);
		
		TabAgent agent = agentService.selectByPrimaryKey(agentId);
		model.addAttribute("agent", agent);
		model.addAttribute("agent3List", agent3List);
		return prefix+"/agent3Change";
	}
	
	@RequiresPermissions("coach:agent:agent3Change")
	@Log("更换三级代理商")
	@PostMapping("/changeAgent3")
	@ResponseBody
	R changeAgent3(Long agentId,Long changeAgentId) {
		// 把代理商下级代理商都转到他的上级代理商，下级代理商自动升级
		agentService.updateAgentHigherAgentId(agentId, changeAgentId);
		// 把代理商下的教练转到他的上级代理商
		coachService.updateAgentId(agentId, changeAgentId);
		
		return R.ok();
	}
	
	//三级代理商=============>>>结束
	

	/**
	 * 查询代理商名称是否存在
	 * @param params
	 * @return
	 */
	@PostMapping("/exitAgentName")
	@ResponseBody
	boolean exitAgentName(@RequestParam Map<String, Object> params) {
		// 存在，不通过，false
		return !agentService.exitAgentName(params);
	}
	
	/**
	 * 查询后台登录账号是否存在
	 * @param params
	 * @return
	 */
	@PostMapping("/exitBgAccount")
	@ResponseBody
	boolean exitBgAccount(@RequestParam Map<String, Object> params) {
		// 存在，不通过，false
		return !agentService.exitBgAccount(params);
	}
	
	/**
	 * 增加后台账号
	 * @param agentName
	 * @param userName
	 * @param password
	 */
	private void addUser(String agentName, String userName, String password){
		UserDO user = new UserDO();
		user.setUsername(userName);
		user.setPassword(MD5Utils.encrypt(userName, password));
		user.setName(agentName);
		List<Long> roleIds = new ArrayList<Long>();
		roleIds.add(60l);
		user.setRoleIds(roleIds);
		user.setStatus(1);
		//{"deptName":"","email":"lifaqiu@163.com","name":"测试","password":"25bb7c42604b0e7aba7bcae50e7762a9","roleIds":[60],"status":1,"username":"test"}
		userService.save(user);
	}
}
