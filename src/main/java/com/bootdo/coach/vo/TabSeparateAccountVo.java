package com.bootdo.coach.vo;

import java.math.BigDecimal;

import com.bootdo.coach.domain.TabSeparateAccount;
public class TabSeparateAccountVo extends TabSeparateAccount{

	private BigDecimal money;
	private int level;
	private int separateStatus;
	private Long agentId;
	private String agentName;
	private String testDurationStr;

	private Long typeId;
	
	public BigDecimal getMoney() {
		switch (level) {
		case 0:
			money = getAgent1Money();
			break;
		case 1:
			money = getAgent2Money();
			break;
		case 2:
			money = getAgent3Money();
			break;

		default:
			break;
		}
		return money;
	}
	public void setMoney(BigDecimal money) {
		this.money = money;
	}
	
	
	public int getSeparateStatus() {
		switch (level) {
		case 0:
			separateStatus = getAgent1SeparateStatus();
			break;
		case 1:
			separateStatus = getAgent2SeparateStatus();
			break;
		case 2:
			separateStatus = getAgent3SeparateStatus();
			break;

		default:
			break;
		}
		return separateStatus;
	}
	public void setSeparateStatus(int separateStatus) {
		this.separateStatus = separateStatus;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public Long getAgentId() {
		return agentId;
	}
	public void setAgentId(Long agentId) {
		this.agentId = agentId;
	}
	public String getAgentName() {
		return agentName;
	}
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	public String getTestDurationStr() {
		return getTestDuration() > 0 ? ((getTestDuration()/60)+"0分钟"):"0分钟";
	}
	public void setTestDurationStr(String testDurationStr) {
		this.testDurationStr = testDurationStr;
	}
	public Long getTypeId() {
		return typeId;
	}

	public void setTypeId(Long typeId) {
		this.typeId = typeId;
	}
	
}
