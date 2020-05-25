package com.bootdo.coach.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bootdo.coach.domain.TabCoach;
import com.bootdo.coach.service.TabOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bootdo.coach.dao.TabAgentMapper;
import com.bootdo.coach.dao.ext.ExtTabAgentMapper;
import com.bootdo.coach.domain.TabAgent;
import com.bootdo.coach.service.TabAgentService;
import com.bootdo.coach.service.TabWithdrawApplyService;
import com.bootdo.coach.vo.TabAgentVo;

@Service
public class TabAgentServiceImpl implements TabAgentService {
    @Autowired
    private TabAgentMapper mapper;
    @Autowired
    private ExtTabAgentMapper extMapper;

    @Autowired
    private TabWithdrawApplyService withdrawApplyService;
	@Autowired
	private TabOrderService tabOrderService;

	@Override
	public int deleteByPrimaryKey(Long agentId) {
		return mapper.deleteByPrimaryKey(agentId);
	}

	@Override
	public int insert(TabAgent record) {
		return mapper.insert(record);
	}

	@Override
	public int insertSelective(TabAgent record) {
		return mapper.insertSelective(record);
	}

	@Override
	public TabAgent selectByPrimaryKey(Long agentId) {
		return mapper.selectByPrimaryKey(agentId);
	}

	@Override
	public int updateByPrimaryKeySelective(TabAgent record) {
		return mapper.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(TabAgent record) {
		return mapper.updateByPrimaryKey(record);
	}
	
	@Override
	public TabAgent findAgent(String bg_account, String bg_password) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("bg_account", bg_account);
		paramMap.put("bg_password", bg_password);
		return extMapper.findAgent(paramMap);
	}
	
	@Override
	public TabAgent findAgent(String bg_account, String bg_password,Integer status) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("bg_account", bg_account);
		paramMap.put("bg_password", bg_password);
		paramMap.put("status", status);
		return extMapper.findAgent(paramMap);
	}

	@Override
	public int count(Map<String, Object> query) {
		return extMapper.count(query);
	}

	@Override
	public List<TabAgentVo> list(Map<String, Object> query) {
		return extMapper.list(query);
	}

	public List<TabAgentVo> listStatistics(Map<String, Object> query) {
		String levelStr = (String) query.get("level");
		int level = Integer.valueOf(levelStr);
		if(level == 0){
			return extMapper.listStatistics1(query);
		}else if(level == 1){
			return extMapper.listStatistics2(query);
		}else {
			return extMapper.listStatistics3(query);
		}
	}
    
	@Override
	public boolean exitAgentName(Map<String, Object> params) {
		boolean exit;
        exit = extMapper.list(params).size() > 0;
        return exit;
	}
	
	@Override
	public boolean exitBgAccount(Map<String, Object> params) {
		boolean exit;
        exit = extMapper.list(params).size() > 0;
        return exit;
	}
	@Override
	public List<TabCoach> findCoachList(String agentId) {
		return extMapper.getCoachList(agentId);
	}

	@Override
	public void updateAgentLevelAndHigherAgentId(Long agentId, Long higherAgentId) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("higherAgentId", higherAgentId);
		paramMap.put("agentId", agentId);
		extMapper.updateAgentLevelAndHigherAgentId(paramMap);
	}

	@Override
	public boolean exitAgentByAgentId(Long agentId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("agentId", agentId);
		boolean exit;
        exit = extMapper.count(params) > 0;
        return exit;
	}
	
	@Override
	public void updateAgentHigherAgentId(Long agentId, Long changeAgentId) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("higherAgentId", changeAgentId);
		paramMap.put("agentId", agentId);
		extMapper.updateAgentHigherAgentId(paramMap);
	}
	
	@Override
	public void updatePwd(String bg_account, String bg_password) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("bg_account", bg_account);
		paramMap.put("bg_password", bg_password);
		extMapper.updatePwd(paramMap);
	}
	
	@Override
	public Set<Long> findAgentIdList(Set<Long> agentIdList) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("agentIdList", agentIdList);
		return extMapper.findAgentIdList(paramMap);
	}
	
	@Override
	public TabAgent findAgentByAccount(String account) {
		return extMapper.findAgentByAccount(account);
	}
	
	@Override
	public TabAgent findAgentByOpenId(String openid) {
		return extMapper.findAgentByOpenId(openid);
	}
	
	@Override
	@Transactional
	public void applyWithdraw(Long agent_id, BigDecimal amount_apply) throws RuntimeException {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("agent_id", agent_id);
		paramMap.put("amount_apply", amount_apply);
		if (extMapper.addFreezeAndReduceDesirable(paramMap) <= 0) {
			throw new RuntimeException("修改代理商金额失败");
		}

		if(!withdrawApplyService.applyWithdraw(1, amount_apply, agent_id, "代理商申请提现")){
			throw new RuntimeException("插入提现日志失败");
		}
	}
	
	@Override
	public void sucApplyWithdraw(Integer applyId) throws Exception{
		tabOrderService.widthdraw(applyId);
//		Map<String, Object> paramMap = new HashMap<String, Object>();
//		paramMap.put("agent_id", agent_id);
//		paramMap.put("amount_apply", amountApply);
//		if (extMapper.reduceFreezeAndAddAmountWithdraw(paramMap) <= 0) {
//			throw new RuntimeException("修改代理商金额失败");
//		}
	}
	
	@Override
	public void failApplyWithdraw(Long agent_id, BigDecimal amountApply) throws RuntimeException{
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("agent_id", agent_id);
		paramMap.put("amount_apply", amountApply);
		if (extMapper.reduceFreezeAndAddAmountDesirable(paramMap) <= 0) {
			throw new RuntimeException("修改代理商金额失败");
		}
	}
	
	@Override
	public boolean addAmountDesirable(Long agentId, BigDecimal agentMoney) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("agent_id", agentId);
		paramMap.put("amount_desirable", agentMoney);
		//分账失败，增加可用amount_desirable
		if (extMapper.addAmountDesirable(paramMap) <= 0) {
			return false;
		}
		return true;
	}
	@Override
	public List<TabAgent> findAgentList(Map<String, Object> params) {
		return extMapper.getAgentList(params);
	}

	@Override
	public TabAgent findAgentByTel(String tel) {
		return extMapper.getAgentByTel(tel);
	}
}
