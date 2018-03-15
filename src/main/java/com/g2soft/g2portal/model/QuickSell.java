package com.g2soft.g2portal.model;

import java.sql.Date;
import java.sql.Timestamp;

public class QuickSell {

	private Integer id;
	private Date sellDate;	
	private boolean isCancel;
	private String status;
	private String transaction;
	private String nfceKey;
	private String nfceProtocol;
	private String nfeSituation;
	private String onlineShipping;
	private Timestamp onlineShippingDate;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Date getSellDate() {
		return sellDate;
	}
	public void setSellDate(Date sellDate) {
		this.sellDate = sellDate;
	}
	public boolean isCancel() {
		return isCancel;
	}
	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTransaction() {
		return transaction;
	}
	public void setTransaction(String transaction) {
		this.transaction = transaction;
	}
	public String getNfceKey() {
		return nfceKey;
	}
	public void setNfceKey(String nfceKey) {
		this.nfceKey = nfceKey;
	}
	public String getNfceProtocol() {
		return nfceProtocol;
	}
	public void setNfceProtocol(String nfceProtocol) {
		this.nfceProtocol = nfceProtocol;
	}
	public String getNfeSituation() {
		return nfeSituation;
	}
	public void setNfeSituation(String nfeSituation) {
		this.nfeSituation = nfeSituation;
	}
	public String getOnlineShipping() {
		return onlineShipping;
	}
	public void setOnlineShipping(String onlineShipping) {
		this.onlineShipping = onlineShipping;
	}
	public Timestamp getOnlineShippingDate() {
		return onlineShippingDate;
	}
	public void setOnlineShippingDate(Timestamp onlineShippingDate) {
		this.onlineShippingDate = onlineShippingDate;
	}
	
}
