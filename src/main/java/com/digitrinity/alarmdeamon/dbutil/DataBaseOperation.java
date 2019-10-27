package com.digitrinity.alarmdeamon.dbutil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import com.digitrinity.alarmdeamon.model.CloseAlarmData;
import com.digitrinity.alarmdeamon.model.OpenAlarmData;
import com.digitrinity.alarmdeamon.util.MailUtil;
import com.digitrinity.alarmdeamon.util.SendCloseMail;

public class DataBaseOperation {

	public ArrayList<OpenAlarmData> fetchOpenAlarmRecords() throws Exception {

		Connection connObj = null;
		PreparedStatement pstmtObj = null;
		ConnectionPool jdbcObj = new ConnectionPool();
		DataSource dataSource = jdbcObj.setUpPool();
		ArrayList<OpenAlarmData> alarmDataList = new ArrayList<OpenAlarmData>();

		try {   

			jdbcObj.printDbStatus();

			// Performing Database Operation!
			System.out.println("\n=====Making A New Connection Object For Db Transaction=====\n");
			connObj = dataSource.getConnection();
			jdbcObj.printDbStatus(); 
			pstmtObj = connObj.prepareStatement("Select * from trans_alarmrecords  where alrCloseTime=0 and alrDemonProcessed!=1");

			ResultSet rsObj = pstmtObj.executeQuery();
			long alrOpenTime;
			String siteCode;
			String alrPinNumber;
			int alrTTEscalatedLevel;
			int siteId = 0;

			while(rsObj.next()){

				alrOpenTime = rsObj.getInt("alrOpenTime");
				siteCode = rsObj.getString("smSiteCode");
				alrPinNumber = rsObj.getString("alrPinNumber");
				alrTTEscalatedLevel = rsObj.getInt("alrTTEscalatedLevel");
				siteId = rsObj.getInt("smSiteID");

				OpenAlarmData alarmData = new OpenAlarmData(alrOpenTime,siteCode,alrPinNumber,alrTTEscalatedLevel,siteId);
				alarmDataList.add(alarmData);
			}
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
		return alarmDataList;
	}

	public void fetchTTEscalationMaster(ArrayList<OpenAlarmData> alarmDataList) throws Exception {


		for(OpenAlarmData alarmData: alarmDataList) {

			Connection connObj = null;
			PreparedStatement pstmtObj = null;
			ConnectionPool jdbcObj = new ConnectionPool();
			DataSource dataSource = jdbcObj.setUpPool();
			int ttEscalationLvlMasterID=0;
			int eroleId= 0;
			String smTechEmpid ="";
			long ttEscalationLvlDuration =0;

			try {   

				System.out.println("\n=====Making A New Connection Object For Db Transaction=====\n");
				connObj = dataSource.getConnection();
				System.out.println("\n=====After Connection=====\n");

				/* Verify Alarm Pin is configured or not in 'ttescalationlevelmaster'*/
				pstmtObj = connObj.prepareStatement("Select * from ttescalationlevelmaster where alPinID ='"+alarmData.getAlrPinNumber()+"'");
				ResultSet rsObj2 = pstmtObj.executeQuery();
				System.out.println("\n=====After Connection2=====\n");
				while(rsObj2.next()){

					pstmtObj = connObj.prepareStatement("Select * from ttescalationlevelmaster where ttEscalationLevel ="+alarmData.getAlrTTEscalatedLevel()+1+" and alPinID ='"+alarmData.getAlrPinNumber()+"'");
					ResultSet rsObj3 = pstmtObj.executeQuery();
					while(rsObj3.next()){

						ttEscalationLvlMasterID = rsObj3.getInt("ttEscalationLvlMasterID");
						ttEscalationLvlDuration = rsObj3.getInt("ttEscalationLvlDuration");

					}

					pstmtObj.close();
					pstmtObj = connObj.prepareStatement("Select erRoleID from ttescalationemployeerole where ttEscalationLvlempRoleID ="+ttEscalationLvlMasterID+"");
					ResultSet rsObj4 = pstmtObj.executeQuery();

					while(rsObj4.next()){

						eroleId = rsObj4.getInt("erRoleID");

						if(eroleId==1) {

							pstmtObj = connObj.prepareStatement("Select smTechEmpid from sitemaster where smSiteID ="+alarmData.getSiteId()+"");
							pstmtObj.executeQuery();
							/* Send Mail*/

						} else if((eroleId==3)||(eroleId==4)||(eroleId==5)||(eroleId==6)) {

							smTechEmpid = fetchEmailId(pstmtObj,connObj,alarmData.getSiteId());

							ResourceBundle mybundle = ResourceBundle.getBundle("application");
							String toMailId = mybundle.getString("to.mailid");
							String fromMailId = mybundle.getString("from.mailid");
							String host = mybundle.getString("mail.host");
							String port = mybundle.getString("smtp.port");


							ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
							MailUtil mailUtil = new MailUtil(toMailId, fromMailId, host, port);
							executorService.schedule(mailUtil, 5 , TimeUnit.SECONDS);
							
							/*After Sending Mail Update alrEscalatedLevel in Trans_AlarmRecords*/
							
							updateAlrEscalatedLevel(alarmData.getSiteId(),alarmData.getAlrPinNumber(),alarmData.getAlrTTEscalatedLevel()+1,alarmData.getAlrOpenTime(),smTechEmpid);
						}
					}
				}

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

	private void updateAlrEscalatedLevel(int siteId, String alrPinNumber, int escalationLevel, long alrOpenTime, String smTechEmpid) throws Exception {

		Connection connObj = null;
		PreparedStatement pstmtObj = null;
		ConnectionPool jdbcObj = new ConnectionPool();
		DataSource dataSource = jdbcObj.setUpPool();

		try {   
			
			jdbcObj.printDbStatus();

			// Performing Database Operation!
			System.out.println("\n=====Making A New Connection Object For Db Transaction=====\n");
			connObj = dataSource.getConnection();
			jdbcObj.printDbStatus(); 
			pstmtObj = connObj.prepareStatement("Update trans_alarmrecords Set alrEscalatedLevel="+escalationLevel+ " where smSiteID="+siteId+"and alrOpenTime= "+alrOpenTime+"and alrEscalateMail="+smTechEmpid+"");

			pstmtObj.executeUpdate();
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

	private String fetchEmailId(PreparedStatement pstmtObj, Connection connObj, int siteId) throws SQLException {

		int crClusterID=0;
		int znZoneID=0;
		int rgRegionID=0;
		String employeeId="";

		pstmtObj = connObj.prepareStatement("Select crClusterID from sitemaster where smSiteID ="+siteId+"");
		ResultSet rsObj5 = pstmtObj.executeQuery();

		while(rsObj5.next()){
			crClusterID = rsObj5.getInt("crClusterID");
		}

		pstmtObj.close();
		pstmtObj = connObj.prepareStatement("Select znZoneID from clustermaster where crClusterID ="+crClusterID+"");
		ResultSet rsObj6 = pstmtObj.executeQuery();
		while(rsObj6.next()){
			znZoneID = rsObj6.getInt("znZoneID");
		}

		pstmtObj.close();
		pstmtObj = connObj.prepareStatement("Select rgRegionID from zonemaster where znZoneID ="+znZoneID+"");

		ResultSet rsObj7 = pstmtObj.executeQuery();

		while(rsObj7.next()){
			rgRegionID = rsObj6.getInt("rgRegionID");
		}

		pstmtObj = connObj.prepareStatement("Select emEmail from employeemaster where znZoneID ="+znZoneID+" and rgRegionID="+rgRegionID+" and erRoleID=3");
		ResultSet rsObj8 = pstmtObj.executeQuery();
		while(rsObj8.next()){

			employeeId = rsObj6.getString("emEmail");
		}
		return employeeId;
	}

	public void fetchCloseAlarmRecord() throws Exception {

		Connection connObj = null;
		PreparedStatement pstmtObj = null;
		ConnectionPool jdbcObj = new ConnectionPool();
		DataSource dataSource = jdbcObj.setUpPool();
		ArrayList<CloseAlarmData> alarmDataList = new ArrayList<CloseAlarmData>();
		int siteId= 0;
		String alrPinNumber="";
		long alrOpenTime=0L;

		try {   

			jdbcObj.printDbStatus();

			// Performing Database Operation!
			System.out.println("\n=====Making A New Connection Object For Db Transaction=====\n");
			connObj = dataSource.getConnection();
			jdbcObj.printDbStatus(); 
			pstmtObj = connObj.prepareStatement("Select * from trans_alarmrecords  where alrCloseTime!=0 and alrDemonProcessed=1 and alrIsSentMail=0");

			ResultSet rsObj = pstmtObj.executeQuery();
			String emailId ="";
			
			while(rsObj.next()){

				emailId = rsObj.getString("alrEscalateMail");
				siteId = rsObj.getInt("smSiteID");
				alrPinNumber = rsObj.getString("alrPinNumber");
				alrOpenTime = rsObj.getLong("alrOpenTime");
				
				CloseAlarmData closeAlarmData = new CloseAlarmData(emailId,siteId,alrPinNumber,alrOpenTime);
				alarmDataList.add(closeAlarmData);
			}
			
			SendCloseMail sendCloseMail = new SendCloseMail(alarmDataList);
			sendCloseMail.sendCloseMail(emailId);
			rsObj.close();
			connObj.close();
			pstmtObj.close();

			/* Send Mail for Close Alarm*/
			
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

