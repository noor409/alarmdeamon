package com.digitrinity.alarmdeamon.mailprocess;

import java.util.ArrayList;

import com.digitrinity.alarmdeamon.dbutil.DataBaseOperation;
import com.digitrinity.alarmdeamon.model.OpenAlarmData;

public class OpenAlarmMailThread implements Runnable  {

	public void run() {

		try {
			fetchAlarmRecord();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void fetchAlarmRecord() throws Exception {

		DataBaseOperation dbData = new DataBaseOperation();
		ArrayList<OpenAlarmData> alarmDataList = dbData.fetchOpenAlarmRecords();
		dbData.fetchTTEscalationMaster(alarmDataList);
	}
}
