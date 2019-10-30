package com.digitrinity.alarmdeamon.mailprocess;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.digitrinity.alarmdeamon.dbutil.DataBaseOperation;
import com.digitrinity.alarmdeamon.model.OpenAlarmData;

public class OpenAlarmMailThread extends Thread  {

	private static Logger logger = LogManager.getLogger(OpenAlarmMailThread.class.getName());
	
	public void run() {

		try {
			
			while(true) {
			logger.info("OpenAlarmMailThread running");
			fetchAlarmRecord();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void fetchAlarmRecord() throws Exception {

		logger.info("Inside fetchAlarmRecord");
		
		DataBaseOperation dbData = new DataBaseOperation();
		ArrayList<OpenAlarmData> openAlarmList = dbData.fetchOpenAlarmRecords();
		dbData.fetchTTEscalationMaster(openAlarmList);
	}
}
