package com.digitrinity.alarmdeamon.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
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

	public void sendCloseMail(String emailId) throws Exception {
		
		ResourceBundle mybundle = ResourceBundle.getBundle("application");
		String fromMailId = mybundle.getString("from.mailid");
		String host = mybundle.getString("mail.host");
		String port = mybundle.getString("smtp.port");
		
		Connection connObj = null;
		PreparedStatement pstmtObj = null;
		ConnectionPool jdbcObj = new ConnectionPool();
		DataSource dataSource = jdbcObj.setUpPool();
		connObj = dataSource.getConnection();
		
		for(CloseAlarmData closeAlarmData: alarmDataList) {
			
			ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
			MailUtil mailUtil = new MailUtil(emailId, fromMailId, host, port);
			executorService.execute(mailUtil);
			
			/* Update alrIsSentMail to 1 after sending mail*/
			pstmtObj = connObj.prepareStatement("Update  trans_alarmrecords set alrIsSentMail =1  where smSiteID="+closeAlarmData.getSiteId()+" and alrPinNumber=='"+closeAlarmData.getAlrPinNumber()+"' and alrOpenTime= ="+closeAlarmData.getAlrOpenTime()+"");
			pstmtObj.executeUpdate();
			
		}
		
		connObj.close();
		pstmtObj.close();
	}
}
