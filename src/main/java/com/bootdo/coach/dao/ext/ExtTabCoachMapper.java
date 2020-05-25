package com.bootdo.coach.dao.ext;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bootdo.coach.domain.TabCoach;
import com.bootdo.coach.vo.TabCoachVo;

public interface ExtTabCoachMapper {
	List<TabCoach> findCoachListByAccount(String account);

	List<TabCoach> findCoachListByTel(String tel);

	List<Map<String, Object>> findCoachList(Map<String, Object> paramMap);

	void updateAgentId(Map<String, Object> paramMap);

	List<TabCoach> findCoachListByAgentId(Long agentId);

	int count(Map<String, Object> query);

	List<TabCoachVo> list(Map<String, Object> query);

	List<TabCoachVo> listStatistics(Map<String, Object> query);

	List<TabCoach> getCoachList(String agentId);

//	List<TabCoach> getCoachList();

	Set<Long> findCoachIdList(Map<String, Object> paramMap);

	TabCoach findCoachByOpenId(String openid);

	int addFreezeAndReduceDesirable(Map<String, Object> paramMap);

	int reduceFreezeAndAddAmountWithdraw(Map<String, Object> paramMap);

	int reduceFreezeAndAddAmountDesirable(Map<String, Object> paramMap);

	int addAmountDesirable(Map<String, Object> paramMap);

}