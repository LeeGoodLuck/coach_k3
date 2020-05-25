package com.bootdo.coach.vo;

import com.bootdo.coach.domain.TabCoach;

import java.math.BigDecimal;

public class TabCoachVo extends TabCoach{

	private String agentName;

	private BigDecimal incomeAmount;

	//虚拟字段统计设备数量
	private Integer countDevice;
	//当日收益
	private BigDecimal dayRevenue;
	//近七天收益
	private BigDecimal sevenRevenue;
	//本月收益
	private BigDecimal monthRevenue;
	//总收益
	private BigDecimal totalRevenue;

	public BigDecimal getIncomeAmount() {
		return incomeAmount;
	}

	public void setIncomeAmount(BigDecimal incomeAmount) {
		this.incomeAmount = incomeAmount;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public Integer getCountDevice() {
		return countDevice;
	}

	public void setCountDevice(Integer countDevice) {
		this.countDevice = countDevice;
	}

	public BigDecimal getDayRevenue() {
		return dayRevenue;
	}

	public void setDayRevenue(BigDecimal dayRevenue) {
		this.dayRevenue = dayRevenue;
	}
	public BigDecimal getSevenRevenue() {
		return sevenRevenue;
	}

	public void setSevenRevenue(BigDecimal sevenRevenue) {
		this.sevenRevenue = sevenRevenue;
	}
	public BigDecimal getMonthRevenue() {
		return monthRevenue;
	}

	public void setMonthRevenue(BigDecimal monthRevenue) {
		this.monthRevenue = monthRevenue;
	}

	public BigDecimal getTotalRevenue() {
		return totalRevenue;
	}

	public void setTotalRevenue(BigDecimal totalRevenue) {
		this.totalRevenue = totalRevenue;
	}

}