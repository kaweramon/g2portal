package com.g2soft.g2portal.model;

import java.util.Date;

public class Apps {

	private Integer id;
	private String name;
	private Integer currentVersion;
	private Integer versionUp;
	private Date addedDate;
	private String link;
	
	public Apps() {}
	
	public Apps(Integer id, String name, Integer currentVersion, Integer versionUp, Date addedDate, String link) {
		super();
		this.id = id;
		this.name = name;
		this.currentVersion = currentVersion;
		this.versionUp = versionUp;
		this.addedDate = addedDate;
		this.link = link;
	}

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getCurrentVersion() {
		return currentVersion;
	}
	public void setCurrentVersion(Integer currentVersion) {
		this.currentVersion = currentVersion;
	}
	public Integer getVersionUp() {
		return versionUp;
	}
	public void setVersionUp(Integer versionUp) {
		this.versionUp = versionUp;
	}
	public Date getAddedDate() {
		return addedDate;
	}
	public void setAddedDate(Date addedDate) {
		this.addedDate = addedDate;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}

	@Override
	public String toString() {
		return "Apps [id=" + id + ", name=" + name + ", currentVersion=" + currentVersion + ", versionUp=" + versionUp
				+ ", addedDate=" + addedDate + ", link=" + link + "]";
	}
	
}
