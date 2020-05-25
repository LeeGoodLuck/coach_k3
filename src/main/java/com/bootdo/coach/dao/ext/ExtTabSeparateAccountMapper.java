package com.bootdo.coach.dao.ext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.bootdo.coach.domain.TabSeparateAccount;
import com.bootdo.coach.vo.TabCostVo;
import com.bootdo.coach.vo.TabSeparateAccountVo;
import org.apache.ibatis.annotations.Param;

public interface ExtTabSeparateAccountMapper {
	BigDecimal findCoachMoney(Map<String, Object> paramMap);

	int findCoachMoneyCnt(Long coach_id);

	List<Map<String, Object>> getCoachIncomeList(Map<String, Object> paramMap);

	List<Map<String, Object>> getstudentIncomeList(Map<String, Object> paramMap);

	int insertSelectiveAndReturnId(TabSeparateAccount separateAccount);

	int count(Map<String, Object> query);

	List<TabSeparateAccountVo> list(Map<String, Object> query);

	List<TabCostVo> costCoachList(Map<String, Object> query);
	List<TabCostVo> costAgent1List(Map<String, Object> query);
	List<TabCostVo> costAgent2List(Map<String, Object> query);
	List<TabCostVo> costAgent3List(Map<String, Object> query);

	List<TabSeparateAccountVo> getAgentTotalMoney(Map<String, Object> params);

	int findAgentMoneyCnt(Long agent_id);

	List<Map<String, Object>> getAgentIncomeList(Map<String, Object> paramMap);

	//统计教练当日的收益
	BigDecimal getDayRevenue(Long coachId);
	//统计教练近7天的收益
	BigDecimal getSevenRevenue(Long coachId);
	//统计教练本月的收益
	BigDecimal getMonthRevenue(Long coachId);
	//统计教练总的收益
	BigDecimal getTotalRevenue(Long coachId);

	//本月
	List<TabSeparateAccountVo> getMonthRevenues(Map<String, Object> params);
	//近七天
	List<TabSeparateAccountVo> getSevenRevenues(Map<String, Object> params);
	//本日
	List<TabSeparateAccountVo> getDayRevenues(Map<String, Object> params);

	//查询代理或名下的代理的总输入
	BigDecimal finAgentTotalRevenue(Map<String, Object> params);

	List<TabSeparateAccount> getOneAgentMoney(Long agentId);

	List<TabSeparateAccount> getTwoAgentMoney(Long agentId);

	List<TabSeparateAccount> getThreeAgentMoney(Long agentId);

	List<TabSeparateAccount> getWholeMoney();

	List<TabSeparateAccount> getTimeWholeMoney(@Param("typeId") Long typeId);

	List<TabSeparateAccount> getTimeAgentMoney(TabSeparateAccountVo account);

}