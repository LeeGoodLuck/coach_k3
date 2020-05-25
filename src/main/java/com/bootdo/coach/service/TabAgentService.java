package com.bootdo.coach.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bootdo.coach.domain.TabAgent;
import com.bootdo.coach.domain.TabCoach;
import com.bootdo.coach.vo.TabAgentVo;

public interface TabAgentService {
	
    int deleteByPrimaryKey(Long agentId);

    int insert(TabAgent record);

    int insertSelective(TabAgent record);

    TabAgent selectByPrimaryKey(Long agentId);

    int updateByPrimaryKeySelective(TabAgent record);

    int updateByPrimaryKey(TabAgent record);

	TabAgent findAgent(String bg_account, String bg_password);
	
	TabAgent findAgent(String bg_account, String bg_password,Integer status);

	List<TabAgentVo> list(Map<String, Object> query);

	List<TabAgentVo> listStatistics(Map<String, Object> query);

	int count(Map<String, Object> query);

	boolean exitAgentName(Map<String, Object> params);

	boolean exitBgAccount(Map<String, Object> params);

	List<TabCoach> findCoachList(String agentId);

	void updateAgentLevelAndHigherAgentId(Long agentId, Long higherAgentId);

	boolean exitAgentByAgentId(Long agentId);

	void updateAgentHigherAgentId(Long agentId, Long changeAgentId);

	void updatePwd(String bg_account, String bg_password);

	Set<Long> findAgentIdList(Set<Long> agentIdList);

	TabAgent findAgentByAccount(String account);

	TabAgent findAgentByOpenId(String openid);

	void applyWithdraw(Long agent_id, BigDecimal amount_apply) throws RuntimeException;

	void sucApplyWithdraw(Integer applyId) throws Exception;

	void failApplyWithdraw(Long agent_id, BigDecimal amountApply) throws RuntimeException;

	/**
	 * 分账失败，增加amount_desirable可取金额
	 * @param agentId
	 * @return
	 */
	boolean addAmountDesirable(Long agentId, BigDecimal agentMoney);

	List<TabAgent> findAgentList(Map<String, Object> params);

	//通过手机号码查询代理信息
	TabAgent findAgentByTel(String tel);
}
