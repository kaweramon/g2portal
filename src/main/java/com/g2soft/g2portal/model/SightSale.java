package com.g2soft.g2portal.model;

import java.sql.Date;
import java.sql.Timestamp;

public class SightSale {

	private Integer id;

	private Timestamp sellDate;
	
	private String nfeKey;
	
	private String status;
	
	private boolean uploaded;
	
	private String nfeStatus;
	
	private String onlineShipping;
	
	private Date onlineShippingDate;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Timestamp getSellDate() {
		return sellDate;
	}

	public void setSellDate(Timestamp sellDate) {
		this.sellDate = sellDate;
	}

	public String getNfeKey() {
		return nfeKey;
	}

	public void setNfeKey(String nfeKey) {
		this.nfeKey = nfeKey;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isUploaded() {
		return uploaded;
	}

	public void setUploaded(boolean uploaded) {
		this.uploaded = uploaded;
	}

	public String getNfeStatus() {
		return nfeStatus;
	}

	public void setNfeStatus(String nfeStatus) {
		this.nfeStatus = nfeStatus;
	}

	public String getOnlineShipping() {
		return onlineShipping;
	}

	public void setOnlineShipping(String onlineShipping) {
		this.onlineShipping = onlineShipping;
	}

	public Date getOnlineShippingDate() {
		return onlineShippingDate;
	}

	public void setOnlineShippingDate(Date onlineShippingDate) {
		this.onlineShippingDate = onlineShippingDate;
	}
	
}
