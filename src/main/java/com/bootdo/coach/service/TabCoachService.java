package com.bootdo.coach.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bootdo.coach.domain.TabCoach;
import com.bootdo.coach.vo.TabAgentVo;
import com.bootdo.coach.vo.TabCoachVo;
import com.bootdo.common.utils.Query;
public interface TabCoachService {
	
    int deleteByPrimaryKey(Long coachId);

    int insert(TabCoach record);

    int insertSelective(TabCoach record);

    TabCoach selectByPrimaryKey(Long coachId);

    int updateByPrimaryKeySelective(TabCoach record);

    int updateByPrimaryKey(TabCoach record);

	TabCoach findCoachByAccount(String account);

	TabCoach findCoachByTel(String tel);

	List<Map<String, Object>> findCoachList(Map<String, Object> paramMap);

	void updateAgentId(Long agentId, Long higherAgentId);

	boolean exitCoachByAgentId(Long agentId);

	List<TabCoach> findCoachList(String agentId);

	List<TabCoachVo> list(Map<String, Object> params);

	List<TabCoachVo> listStatistics(Map<String, Object> params);

	int count(Map<String, Object> params);

	boolean exitCoachName(Map<String, Object> params);

//	List<TabCoach> findCoachList();

	Set<Long> findCoachIdList(Set<Long> agentIdList);

	TabCoach findCoachByOpenId(String openid);

	/**
	 * 申请提现，减少可用amount_desirable，增加冻结amount_freeze
	 * @param coach_id
	 * @param amount_apply
	 * @throws RuntimeException
	 */
	void applyWithdraw(Long coach_id, BigDecimal amount_apply) throws RuntimeException;
	
	/**
	 * 申请提现成功，减少冻结amount_freeze，增加已提现amount_withdraw
	 * @param applyId
	 * @throws RuntimeException
	 */
	void sucApplyWithdraw(Integer applyId) throws Exception;
	
	/**
	 * 申请提现失败，减少冻结amount_freeze，增加可用amount_desirable
	 * @param coach_id
	 * @param amount_apply
	 * @throws RuntimeException
	 */
	void failApplyWithdraw(Long coach_id, BigDecimal amount_apply) throws RuntimeException;

	/**
	 * 分账失败，增加amount_desirable可取金额
	 * @param coachId
	 * @param coachMoney
	 * @return
	 */
	boolean addAmountDesirable(Long coachId, BigDecimal coachMoney);
}
