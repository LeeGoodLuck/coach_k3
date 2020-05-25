package com.bootdo.coach.dao.ext;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bootdo.coach.domain.TabAgent;
import com.bootdo.coach.domain.TabCoach;
import com.bootdo.coach.vo.TabAgentVo;

public interface ExtTabAgentMapper {
	TabAgent findAgent(Map<String, Object> paramMap);

	int count(Map<String, Object> query);

	List<TabAgentVo> list(Map<String, Object> query);

	List<TabAgentVo> listStatistics1(Map<String, Object> query);
	List<TabAgentVo> listStatistics2(Map<String, Object> query);
	List<TabAgentVo> listStatistics3(Map<String, Object> query);

	void updateAgentLevelAndHigherAgentId(Map<String, Object> paramMap);

	void updateAgentHigherAgentId(Map<String, Object> paramMap);

	void updatePwd(Map<String, Object> paramMap);

	Set<Long> findAgentIdList(Map<String, Object> paramMap);

	List<TabCoach> getCoachList(String agentId);

	TabAgent findAgentByAccount(String account);

	TabAgent findAgentByOpenId(String openid);

	int addFreezeAndReduceDesirable(Map<String, Object> paramMap);

	int reduceFreezeAndAddAmountWithdraw(Map<String, Object> paramMap);

	int reduceFreezeAndAddAmountDesirable(Map<String, Object> paramMap);

	int addAmountDesirable(Map<String, Object> paramMap);

	List<TabAgent> getAgentList(Map<String, Object> params);

	//通过手机号码查询代理信息
	TabAgent getAgentByTel(String tel);

}