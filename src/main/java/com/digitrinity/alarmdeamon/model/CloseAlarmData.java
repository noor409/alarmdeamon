package com.digitrinity.alarmdeamon.model;

public class CloseAlarmData {
	
	private String escaltedMail;
	
	private int siteId;
	
	private int alrId;
	
	private int ttEscalationLevel;
	
	public int getAlrId() {
		return alrId;
	}

	public void setAlrId(int alrId) {
		this.alrId = alrId;
	}

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

	public CloseAlarmData(String escaltedMail, int siteId, String alrPinNumber, long alrOpenTime, int alrId, int ttEscalationLevel) {
		super();
		this.escaltedMail = escaltedMail;
		this.siteId = siteId;
		this.alrPinNumber = alrPinNumber;
		this.alrOpenTime = alrOpenTime;
		this.alrId = alrId;
		this.ttEscalationLevel = ttEscalationLevel;
	}

	public String getEscaltedMail() {
		return escaltedMail;
	}

	public void setEscaltedMail(String escaltedMail) {
		this.escaltedMail = escaltedMail;
	}
	
}
