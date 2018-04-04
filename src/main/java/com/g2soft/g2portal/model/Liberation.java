package com.g2soft.g2portal.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Liberation {

	private long id;
	private String clientSystemVersion;
	private Date systemLiberationDate;
	private Timestamp verificationDate;
	private Date tempLiberationDate;
	private String operator;
	private String obs;
	@Override
	public String toString() {
		return "Liberation [id=" + id + ", clientSystemVersion=" + clientSystemVersion + ", systemLiberationDate="
				+ systemLiberationDate + ", verificationDate=" + verificationDate + ", tempLiberationDate="
				+ tempLiberationDate + ", operator=" + operator + ", obs=" + obs + ", clientId=" + clientId + "]";
	}
	private Long clientId;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getClientSystemVersion() {
		return clientSystemVersion;
	}
	public void setClientSystemVersion(String clientSystemVersion) {
		this.clientSystemVersion = clientSystemVersion;
	}
	public Date getSystemLiberationDate() {
		return systemLiberationDate;
	}
	public void setSystemLiberationDate(Date systemLiberationDate) {
		this.systemLiberationDate = systemLiberationDate;
	}
	public Timestamp getVerificationDate() {
		return verificationDate;
	}
	public void setVerificationDate(Timestamp verificationDate) {
		this.verificationDate = verificationDate;
	}
	public Date getTempLiberationDate() {
		return tempLiberationDate;
	}
	public void setTempLiberationDate(Date tempLiberationDate) {
		this.tempLiberationDate = tempLiberationDate;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getObs() {
		return obs;
	}
	public void setObs(String obs) {
		this.obs = obs;
	}
	public Long getClientId() {
		return clientId;
	}
	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}
	
}
