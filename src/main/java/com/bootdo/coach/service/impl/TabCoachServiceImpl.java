package com.bootdo.coach.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bootdo.coach.service.TabOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bootdo.coach.dao.TabCoachMapper;
import com.bootdo.coach.dao.ext.ExtTabCoachMapper;
import com.bootdo.coach.domain.TabCoach;
import com.bootdo.coach.domain.TabWithdrawApply;
import com.bootdo.coach.service.TabCoachService;
import com.bootdo.coach.service.TabWithdrawApplyService;
import com.bootdo.coach.vo.TabCoachVo;

@Service
public class TabCoachServiceImpl implements TabCoachService {
    @Autowired
    private TabCoachMapper mapper;

    @Autowired
    private ExtTabCoachMapper extMapper;
    @Autowired
    private TabWithdrawApplyService withdrawApplyService;

	@Autowired
	private TabOrderService tabOrderService;

	@Override
	public int deleteByPrimaryKey(Long coachId) {
		return mapper.deleteByPrimaryKey(coachId);
	}

	@Override
	public int insert(TabCoach record) {
		return mapper.insert(record);
	}

	@Override
	public int insertSelective(TabCoach record) {
		return mapper.insertSelective(record);
	}

	@Override
	public TabCoach selectByPrimaryKey(Long coachId) {
		return mapper.selectByPrimaryKey(coachId);
	}

	@Override
	public int updateByPrimaryKeySelective(TabCoach record) {
		return mapper.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(TabCoach record) {
		return mapper.updateByPrimaryKey(record);
	}

    @Override
    public TabCoach findCoachByAccount(String account) {
    	List<TabCoach> list = extMapper.findCoachListByAccount(account);
    	if (list == null || list.isEmpty()) {
    		return null;
		}
    	return list.get(0);
    }

    @Override
    public TabCoach findCoachByTel(String tel) {
    	List<TabCoach> list = extMapper.findCoachListByTel(tel);
    	if (list == null || list.isEmpty()) {
    		return null;
		}
    	return list.get(0);
    }

	@Override
	public List<Map<String, Object>> findCoachList(Map<String, Object> paramMap) {
		return extMapper.findCoachList(paramMap);
	}

	@Override
	public List<TabCoachVo> listStatistics(Map<String, Object> query) {
		return extMapper.listStatistics(query);
	}

	@Override
	public void updateAgentId(Long agentId, Long higherAgentId) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("higherAgentId", higherAgentId);
		paramMap.put("agentId", agentId);
		extMapper.updateAgentId(paramMap);
	}
	
	@Override
	public boolean exitCoachByAgentId(Long agentId) {
		List<TabCoach> list = extMapper.findCoachListByAgentId(agentId);
    	if (list == null || list.isEmpty()) {
    		return false;
		}
    	return true;
	}
	@Override
	public List<TabCoach> findCoachList(String agentId) {
		return extMapper.getCoachList(agentId);
	}
	
	@Override
	public int count(Map<String, Object> query) {
		return extMapper.count(query);
	}

	@Override
	public List<TabCoachVo> list(Map<String, Object> query) {
		return extMapper.list(query);
	}

	@Override
	public boolean exitCoachName(Map<String, Object> params) {
		boolean exit;
        exit = extMapper.list(params).size() > 0;
        return exit;
	}

//	@Override
//	public List<TabCoach> findCoachList() {
//		return extMapper.getCoachList();
//	}

	@Override
	public Set<Long> findCoachIdList(Set<Long> agentIdList) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("agentIdList", agentIdList);
		return extMapper.findCoachIdList(paramMap);
	}
	
	@Override
	public TabCoach findCoachByOpenId(String openid) {
		return extMapper.findCoachByOpenId(openid);
	}
	
	@Override
	@Transactional
	public void applyWithdraw(Long coach_id, BigDecimal amount_apply) throws RuntimeException {

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("coach_id", coach_id);
		paramMap.put("amount_apply", amount_apply);
		//申请提现，减少可用amount_desirable，增加冻结amount_freeze
		if (extMapper.addFreezeAndReduceDesirable(paramMap) <= 0) {
			throw new RuntimeException("修改教练金额失败");
		}

		if(!withdrawApplyService.applyWithdraw(0, amount_apply, coach_id, "教练申请提现")){
			throw new RuntimeException("插入提现日志失败");
		}
	}
	
	@Override
	public void failApplyWithdraw(Long coach_id, BigDecimal amount_apply) throws RuntimeException {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("coach_id", coach_id);
		paramMap.put("amount_apply", amount_apply);
		//申请提现成功，减少冻结amount_freeze，增加已提现amount_withdraw
		if (extMapper.reduceFreezeAndAddAmountWithdraw(paramMap) <= 0) {
			throw new RuntimeException("修改教练金额失败");
		}
	}
	
	@Override
	public void sucApplyWithdraw(Integer applyId) throws Exception {
		tabOrderService.widthdraw(applyId);

//		Map<String, Object> paramMap = new HashMap<String, Object>();
//		paramMap.put("coach_id", coach_id);
//		paramMap.put("amount_apply", amount_apply);
//		//申请提现失败，减少冻结amount_freeze，增加可用amount_desirable
//		if (extMapper.reduceFreezeAndAddAmountDesirable(paramMap) <= 0) {
//			throw new RuntimeException("修改教练金额失败");
//		}
	}
	
	@Override
	public boolean addAmountDesirable(Long coachId, BigDecimal coachMoney) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("coach_id", coachId);
		paramMap.put("amount_desirable", coachMoney);
		//分账失败，增加可用amount_desirable
		if (extMapper.addAmountDesirable(paramMap) <= 0) {
			return false;
		}
		return true;
	}
}
