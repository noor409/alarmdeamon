package com.digitrinity.alarmdeamon.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.sql.DataSource;

import com.digitrinity.alarmdeamon.dbutil.ConnectionPool;
import com.digitrinity.alarmdeamon.model.CloseAlarmData;

public class SendCloseMail {
	
	private ArrayList<CloseAlarmData> alarmDataList;

	public SendCloseMail(ArrayList<CloseAlarmData> alarmDataList) {
		super();
		this.alarmDataList = alarmDataList;
	}

	public void sendCloseMail(String emailId, Connection connObj) throws Exception {
		
		System.out.println("sendCloseMail");
		
		ResourceBundle mybundle = ResourceBundle.getBundle("application");
		String fromMailId = mybundle.getString("from.mailid");
		String host = mybundle.getString("mail.host");
		String port = mybundle.getString("smtp.port");
//		
//		Connection connObj = null;
		PreparedStatement pstmtObj = null;
//		ConnectionPool jdbcObj = new ConnectionPool();
//		DataSource dataSource = jdbcObj.setUpPool();
//		connObj = dataSource.getConnection();
		
		System.out.println("After Arraylist:  "+Arrays.toString(alarmDataList.toArray()));
		for(CloseAlarmData closeAlarmData: alarmDataList) {
			
			int siteId = closeAlarmData.getSiteId();
			int alrId = closeAlarmData.getAlrId();
			
			System.out.println("closeAlarmData.getAlrPinNumber()===="+closeAlarmData.getAlrPinNumber());
			System.out.println("closeAlarmData"+closeAlarmData.getAlrOpenTime());
			ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
			MailUtil mailUtil = new MailUtil(emailId, fromMailId, host, port,siteId,alrId,0,connObj);
			executorService.execute(mailUtil);
			
			/* Update alrIsSentMail to 1 after sending mail*/
			pstmtObj = connObj.prepareStatement("Update  trans_alarmrecords set ttStatus =1  where smSiteID="+closeAlarmData.getSiteId()+" and alrPinNumber='"+closeAlarmData.getAlrPinNumber()+"'");
			System.out.println(pstmtObj);
			pstmtObj.executeUpdate();
			System.out.println("updated");
			
		}
		connObj.close();
//		pstmtObj.close();
	}
}
