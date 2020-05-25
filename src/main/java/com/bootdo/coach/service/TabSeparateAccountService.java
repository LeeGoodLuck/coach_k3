package com.bootdo.coach.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.bootdo.coach.domain.TabSeparateAccount;
import com.bootdo.coach.vo.TabSeparateAccountVo;

public interface TabSeparateAccountService {
	
    int deleteByPrimaryKey(Long id);

    int insert(TabSeparateAccount record);

    int insertSelective(TabSeparateAccount record);

    TabSeparateAccount selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TabSeparateAccount record);

    int updateByPrimaryKey(TabSeparateAccount record);

	BigDecimal findCoachMoney(Map<String, Object> paramMap);

	int findCoachMoneyCnt(Long coach_id);

	List<Map<String, Object>> getCoachIncomeList(Map<String, Object> paramMap);

    List<Map<String, Object>> getStudentIncomeList(Map<String, Object> paramMap);

	List<TabSeparateAccountVo> list(Map<String, Object> query);

	int count(Map<String, Object> query);

	BigDecimal getAgentTotalMoney(Map<String, Object> params);

    int findAgentMoneyCnt(Long agent_id);

    List<Map<String, Object>> getAgentIncomeList(Map<String, Object> paramMap);

    //统计教练当日的收益
    BigDecimal findDayRevenue(Long coachId);

    //统计教练近7天的收益
    BigDecimal findSevenRevenue(Long coachId);

    //统计教练本月的收益
    BigDecimal findMonthRevenue(Long coachId);

    //统计教练总的收益
    BigDecimal findTotalRevenue(Long coachId);

    //本月
    BigDecimal findMonthRevenue(Map<String, Object> params);

    //近七天
    BigDecimal findSevenRevenue(Map<String, Object> params);

    //本日
    BigDecimal findDayRevenue(Map<String, Object> params);

    //查询代理或名下的代理的总输入
    BigDecimal finAgentTotalRevenue(Map<String, Object> params);

    List<TabSeparateAccount> oneAgentMoney(Long agentId);

    List<TabSeparateAccount> twoAgentMoney(Long agentId);

    List<TabSeparateAccount> threeAgentMoney(Long agentId);

    List<TabSeparateAccount> timeAgentMoney(TabSeparateAccountVo account);

    List<TabSeparateAccount> timeWholeMoney(Long typeId);

    List<TabSeparateAccount> wholeMoney();
}
