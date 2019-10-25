package com.dignity.alarmdeamon.model;


public class AlarmData {
	
	private long alrOpenTime;

	public long getAlrOpenTime() {
		return alrOpenTime;
	}


	public void setAlrOpenTime(long alrOpenTime) {
		this.alrOpenTime = alrOpenTime;
	}


	public String getSiteCode() {
		return siteCode;
	}


	public void setSiteCode(String siteCode) {
		this.siteCode = siteCode;
	}


	private String siteCode;
	
	
	public AlarmData(long alrOpenTime, String siteCode) {
		super();
		this.alrOpenTime = alrOpenTime;
		this.siteCode = siteCode;
	}
}
