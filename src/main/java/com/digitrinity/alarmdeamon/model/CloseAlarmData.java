package com.digitrinity.alarmdeamon.model;

public class CloseAlarmData {

	private long alrOpenTime;

	private String siteCode;

	private String alrPinNumber;

	private int alrTTEscalatedLevel;

	private int siteId;


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


	public CloseAlarmData(long alrOpenTime, String siteCode, String alrPinNumber, int alrTTEscalatedLevel, int siteId) {
		super();
		this.alrOpenTime = alrOpenTime;
		this.siteCode = siteCode;
		this.alrPinNumber = alrPinNumber;
	}
}
