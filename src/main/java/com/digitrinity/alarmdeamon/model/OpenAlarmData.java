package com.digitrinity.alarmdeamon.model;


public class OpenAlarmData {
	
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
	
	private String alrPinNumber;
	
	private int alrTTEscalatedLevel;
	
	private int siteId;
	
	
	public int getSiteId() {
		return siteId;
	}


	public void setSiteId(int siteId) {
		this.siteId = siteId;
	}


	public int getAlrTTEscalatedLevel() {
		return alrTTEscalatedLevel;
	}


	public void setAlrTTEscalatedLevel(int alrTTEscalatedLevel) {
		this.alrTTEscalatedLevel = alrTTEscalatedLevel;
	}


	public String getAlrPinNumber() {
		return alrPinNumber;
	}


	public void setAlrPinNumber(String alrPinNumber) {
		this.alrPinNumber = alrPinNumber;
	}


	public OpenAlarmData(long alrOpenTime, String siteCode, String alrPinNumber, int alrTTEscalatedLevel, int siteId) {
		super();
		this.alrOpenTime = alrOpenTime;
		this.siteCode = siteCode;
		this.alrPinNumber = alrPinNumber;
	}
}
