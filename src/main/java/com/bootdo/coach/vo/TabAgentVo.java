package com.bootdo.coach.vo;

import com.bootdo.coach.domain.TabAgent;

import java.math.BigDecimal;

public class TabAgentVo extends TabAgent{

    private String higherAgentName;

	private BigDecimal incomeAmount;

	private BigDecimal agentOneProfit;

	private BigDecimal agentTwoProfit;

	private BigDecimal agentThreeProfit;

	public BigDecimal getAgentOneProfit() {
		return agentOneProfit;
	}

	public void setAgentOneProfit(BigDecimal agentOneProfit) {
		this.agentOneProfit = agentOneProfit;
	}

	public BigDecimal getAgentTwoProfit() {
		return agentTwoProfit;
	}

	public void setAgentTwoProfit(BigDecimal agentTwoProfit) {
		this.agentTwoProfit = agentTwoProfit;
	}

	public BigDecimal getAgentThreeProfit() {
		return agentThreeProfit;
	}

	public void setAgentThreeProfit(BigDecimal agentThreeProfit) {
		this.agentThreeProfit = agentThreeProfit;
	}

	public BigDecimal getIncomeAmount() {
		return incomeAmount;
	}

	public void setIncomeAmount(BigDecimal incomeAmount) {
		this.incomeAmount = incomeAmount;
	}

	public String getHigherAgentName() {
		return higherAgentName;
	}

	public void setHigherAgentName(String higherAgentName) {
		this.higherAgentName = higherAgentName;
	}

    
}