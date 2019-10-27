package com.digitrinity.alarmdeamon.mailprocess;

import com.digitrinity.alarmdeamon.dbutil.DataBaseOperation;

public class CloseAlarmMailThread implements Runnable {

	public void run() {

		try {
			fetchCloseAlarmRecord();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void fetchCloseAlarmRecord() throws Exception {

		DataBaseOperation dbOp = new DataBaseOperation();
		dbOp.fetchCloseAlarmRecord();
		
	}
}
