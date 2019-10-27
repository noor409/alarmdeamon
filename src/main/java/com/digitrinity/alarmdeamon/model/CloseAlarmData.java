package com.digitrinity.alarmdeamon.model;

public class CloseAlarmData {
	
	private String escaltedMail;
	
	private int siteId;
	
	String alrPinNumber;
	
	long alrOpenTime;

	public int getSiteId() {
		return siteId;
	}

	public void setSiteId(int siteId) {
		this.siteId = siteId;
	}

	public String getAlrPinNumber() {
		return alrPinNumber;
	}

	public void setAlrPinNumber(String alrPinNumber) {
		this.alrPinNumber = alrPinNumber;
	}

	public long getAlrOpenTime() {
		return alrOpenTime;
	}

	public void setAlrOpenTime(long alrOpenTime) {
		this.alrOpenTime = alrOpenTime;
	}

	public CloseAlarmData(String escaltedMail, int siteId, String alrPinNumber, long alrOpenTime) {
		super();
		this.escaltedMail = escaltedMail;
	}

	public String getEscaltedMail() {
		return escaltedMail;
	}

	public void setEscaltedMail(String escaltedMail) {
		this.escaltedMail = escaltedMail;
	}
	
}
