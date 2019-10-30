package com.digitrinity.alarmdeamon.mailprocess;

import java.sql.Connection;

import com.digitrinity.alarmdeamon.dbutil.DataBaseOperation;

public class CloseAlarmMailThread extends Thread {

	public void run() {

		try {
			while(true) {
				
				System.out.println("close alarm thread");
				fetchCloseAlarmRecord();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void fetchCloseAlarmRecord() throws Exception {

		System.out.println("fetchCloseAlarmRecord");
		DataBaseOperation dbOp = new DataBaseOperation();
		dbOp.fetchCloseAlarmRecord();

	}
}
