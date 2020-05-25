package com.bootdo.coach.domain;

import java.math.BigDecimal;
import java.util.Date;

public class TabCoach {
    private Long coachId;
    private String account;

    private String coachName;

    private Integer sex;

    private String headimg;

    private String tel;

    private Long agentId;

    private Integer divideType;

    private BigDecimal divide;

    private Date createTime;

    private Date lastLoginTime;

    private String lastLoginIp;

    private String remarks;

    private Integer status;

    private String openid;

    private String unionid;

    private String wechatName;

    private BigDecimal amountDesirable;

    private BigDecimal amountFreeze;

    private BigDecimal amountWithdraw;

    private Integer chargeType;

    private BigDecimal chargeRate;

    public Long getCoachId() {
        return coachId;
    }

    public void setCoachId(Long coachId) {
        this.coachId = coachId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account == null ? null : account.trim();
    }

    public String getCoachName() {
        return coachName;
    }

    public void setCoachName(String coachName) {
        this.coachName = coachName == null ? null : coachName.trim();
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getHeadimg() {
        return headimg;
    }

    public void setHeadimg(String headimg) {
        this.headimg = headimg == null ? null : headimg.trim();
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel == null ? null : tel.trim();
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public Integer getDivideType() {
        return divideType;
    }

    public void setDivideType(Integer divideType) {
        this.divideType = divideType;
    }

    public BigDecimal getDivide() {
        return divide;
    }

    public void setDivide(BigDecimal divide) {
        this.divide = divide;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp == null ? null : lastLoginIp.trim();
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks == null ? null : remarks.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid == null ? null : openid.trim();
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid == null ? null : unionid.trim();
    }

    public String getWechatName() {
        return wechatName;
    }

    public void setWechatName(String wechatName) {
        this.wechatName = wechatName == null ? null : wechatName.trim();
    }

    public BigDecimal getAmountDesirable() {
        return amountDesirable;
    }

    public void setAmountDesirable(BigDecimal amountDesirable) {
        this.amountDesirable = amountDesirable;
    }

    public BigDecimal getAmountFreeze() {
        return amountFreeze;
    }

    public void setAmountFreeze(BigDecimal amountFreeze) {
        this.amountFreeze = amountFreeze;
    }

    public BigDecimal getAmountWithdraw() {
        return amountWithdraw;
    }

    public void setAmountWithdraw(BigDecimal amountWithdraw) {
        this.amountWithdraw = amountWithdraw;
    }

    public Integer getChargeType() {
        return chargeType;
    }

    public void setChargeType(Integer chargeType) {
        this.chargeType = chargeType;
    }

    public BigDecimal getChargeRate() {
        return chargeRate;
    }

    public void setChargeRate(BigDecimal chargeRate) {
        this.chargeRate = chargeRate;
    }
}