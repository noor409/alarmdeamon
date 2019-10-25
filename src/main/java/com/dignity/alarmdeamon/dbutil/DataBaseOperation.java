package com.dignity.alarmdeamon.dbutil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.sql.DataSource;

import com.dignity.alarmdeamon.model.AlarmData;

public class DataBaseOperation {

	public void fetchDeviceRawData() throws Exception {

		Connection connObj = null;
		PreparedStatement pstmtObj = null;
		ConnectionPool jdbcObj = new ConnectionPool();
		DataSource dataSource = jdbcObj.setUpPool();
		ArrayList<AlarmData> alarmDataList = new ArrayList<AlarmData>();

		try {   

			jdbcObj.printDbStatus();

			// Performing Database Operation!
			System.out.println("\n=====Making A New Connection Object For Db Transaction=====\n");
			connObj = dataSource.getConnection();
			jdbcObj.printDbStatus(); 
			pstmtObj = connObj.prepareStatement("Select * from trans_alarmrecords  where alrCloseTime=0 and alrDemonProcessed!=1");

			ResultSet rsObj = pstmtObj.executeQuery();
			rsObj.next();
			long alrOpenTime;
			String siteCode;
			
			while(rsObj.next()){

				alrOpenTime = rsObj.getInt("alrOpenTime");
				siteCode = rsObj.getString("smSiteCode");
				
				AlarmData alarmData = new AlarmData(alrOpenTime,siteCode);
				alarmDataList.add(alarmData);
			}
			
			/**
			 * Process Alarm for Mail
			 */
			SendMail sendMail = new SendMail();
			sendMail.processAlarm(alarmDataList);
			

			rsObj.close();
			connObj.close();
			pstmtObj.close();

			System.out.println("\n=====Releasing Connection Object To Pool=====\n");            
		} catch(Exception sqlException) {
			sqlException.printStackTrace();
		} finally {
			try {
				// Closing PreparedStatement Object
				if(pstmtObj != null) {
					pstmtObj.close();
					pstmtObj = null;
				}
			} catch(Exception sqlException) {
				sqlException.printStackTrace();
			}
		}
		jdbcObj.printDbStatus();
	}
}

