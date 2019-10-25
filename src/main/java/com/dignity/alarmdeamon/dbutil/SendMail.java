package com.dignity.alarmdeamon.dbutil;

import java.util.ArrayList;

import com.dignity.alarmdeamon.dateutil.DateTime;
import com.dignity.alarmdeamon.model.AlarmData;

public class SendMail {

	public void processAlarm(ArrayList<AlarmData> alarmDataList) {

		long currentTime 	= DateTime.getCurrentTimeSec();
		for(AlarmData alarmData: alarmDataList) {
			
			long alrOpenTime = alarmData.getAlrOpenTime();
			long timeDiff = currentTime - alrOpenTime; //30mnt 1800000ms
			
			/**
			 * Level 1 : 
			 */
			/*
			 * Query time difference from table
			 * Based on time configured, send to level 2 or 3.
			 * Update 'alrDemonProcessed' to 1 after sending mail.
			 */
			
			/**
			 * Get the mail id from employee 
			 */
			/* for level 1 get the mail id from trans_alarmrecord*/
			/* level 2: based on internal site id fetch clustre id from sitemaster.
			 * fetch employee master table based on clustre id( got employee id) , 
			 * level 3: go escalation check duration, check role.
			 * 
			 */
			
		}
	}

}
