package com.bootdo.coach.domain;

import java.math.BigDecimal;

public class TabAgent {
    private Long agentId;
    private String agentName;

    private String contacts;

    private String tel;

    private String account;

    private String bgAccount;

    private String bgPassword;

    private Integer level;

    private Long higherAgentId;

    private BigDecimal divide;

    private String remarks;

    private Integer status;

    private Integer isDelete;

    private String openid;

    private String unionid;

    private String wechatName;

    private String headimg;

    private Integer countAgentDevice;

    private BigDecimal amountDesirable;

    private BigDecimal amountFreeze;

    private BigDecimal amountWithdraw;

    public Integer getCountAgentDevice() {
        return countAgentDevice;
    }

    public void setCountAgentDevice(Integer countAgentDevice) {
        this.countAgentDevice = countAgentDevice;
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
        this.agentName = agentName == null ? null : agentName.trim();
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts == null ? null : contacts.trim();
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel == null ? null : tel.trim();
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account == null ? null : account.trim();
    }

    public String getBgAccount() {
        return bgAccount;
    }

    public void setBgAccount(String bgAccount) {
        this.bgAccount = bgAccount == null ? null : bgAccount.trim();
    }

    public String getBgPassword() {
        return bgPassword;
    }

    public void setBgPassword(String bgPassword) {
        this.bgPassword = bgPassword == null ? null : bgPassword.trim();
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Long getHigherAgentId() {
        return higherAgentId;
    }

    public void setHigherAgentId(Long higherAgentId) {
        this.higherAgentId = higherAgentId;
    }

    public BigDecimal getDivide() {
        return divide;
    }

    public void setDivide(BigDecimal divide) {
        this.divide = divide;
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

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
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

    public String getHeadimg() {
        return headimg;
    }

    public void setHeadimg(String headimg) {
        this.headimg = headimg == null ? null : headimg.trim();
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
}