package com.digitrinity.alarmdeamon.dbutil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.digitrinity.alarmdeamon.constants.Constants;
import com.digitrinity.alarmdeamon.model.CloseAlarmData;
import com.digitrinity.alarmdeamon.model.OpenAlarmData;
import com.digitrinity.alarmdeamon.util.MailUtil;
import com.digitrinity.alarmdeamon.util.SendCloseMail;

public class DataBaseOperation {

	private static Logger logger = LogManager.getLogger(DataBaseOperation.class.getName());

	public ArrayList<OpenAlarmData> fetchOpenAlarmRecords() throws Exception {

		logger.info("Inside fetchOpenAlarmRecords");

		Connection connObj = null;
		PreparedStatement pstmtObj = null;
		ConnectionPool jdbcObj = new ConnectionPool();
			DataSource dataSource = jdbcObj.setUpPool();

		ArrayList<OpenAlarmData> openAlarmList = new ArrayList<OpenAlarmData>();

		String openAlarmQuery="Select ttlstescalatedmailid,alrID,alrOpenTime,smSiteCode,alrPinNumber,alrTTEscalatedLevel,smSiteID from trans_alarmrecords  where alrCloseTime=0 and "
				+ "ttStatus=1";

		try {   

			connObj = dataSource.getConnection();
			pstmtObj = connObj.prepareStatement(openAlarmQuery);
			/*Fetch Result*/
			ResultSet rsObj = pstmtObj.executeQuery();

			long alrOpenTime;
			String siteCode;
			String alrPinNumber;
			int alrTTEscalatedLevel;
			int siteId = 0;
			int alrID=0;
			String mailId="";

			while(rsObj.next()){

				alrOpenTime = rsObj.getInt("alrOpenTime");
				siteCode = rsObj.getString("smSiteCode");
				alrPinNumber = rsObj.getString("alrPinNumber");
				alrTTEscalatedLevel = rsObj.getInt("alrTTEscalatedLevel");
				siteId = rsObj.getInt("smSiteID");
				alrID = rsObj.getInt("alrID");
				mailId = rsObj.getString("ttlstescalatedmailid");

				/*Set Open Alarm Records*/
				OpenAlarmData alarmData = new OpenAlarmData(alrOpenTime,siteCode,alrPinNumber,
						alrTTEscalatedLevel,siteId,alrID,mailId);
				openAlarmList.add(alarmData);
			}

			connObj.close();
			rsObj.close();
			pstmtObj.close();

			System.out.println("\n=====Releasing Connection Object To Pool=====\n");            
		} catch(Exception sqlException) {
			sqlException.printStackTrace();
		} finally {
			try {
				pstmtObj.close();;
			} catch(Exception sqlException) {
				sqlException.printStackTrace();
			}
		}
		return openAlarmList;
	}

	public void fetchTTEscalationMaster(ArrayList<OpenAlarmData> alarmDataList) throws Exception {

		Connection connObj = null;
		PreparedStatement pstmtObj = null;
		ConnectionPool jdbcObj = new ConnectionPool();
			DataSource dataSource = jdbcObj.setUpPool();

		for(OpenAlarmData alarmData: alarmDataList) {

			System.out.println(alarmData);
			int ttEscalationLvlMasterID=0;
			int eroleId= 0;
			int smTechEmpid =0;
			String employeeEmail="";
			long ttEscalationLvlDuration =0;

			try {   

				connObj = dataSource.getConnection();
				/* Verify Alarm Pin is configured or not in 'ttescalationlevelmaster'*/
				pstmtObj = connObj.prepareStatement("Select * from ttescalationlevelmaster where alPinID ='"+alarmData.getAlrPinNumber()+"'");
				ResultSet rsObj2 = pstmtObj.executeQuery();
				System.out.println("\n=====After Connection2=====\n");
				/* Pin is configured*/
				if(rsObj2.next()){

					pstmtObj.close();
					pstmtObj = connObj.prepareStatement("Select * from ttescalationlevelmaster where ttEscalationLevel ="+alarmData.getAlrTTEscalatedLevel()+1+" and alPinID ='"+alarmData.getAlrPinNumber()+"'");
					ResultSet rsObj3 = pstmtObj.executeQuery();
					while(rsObj3.next()){

						ttEscalationLvlMasterID = rsObj3.getInt("ttEscalationLvlMasterID");
						ttEscalationLvlDuration = rsObj3.getInt("ttEscalationLvlDuration");

					}
					rsObj2.close();
					rsObj3.close();
					pstmtObj.close();
					pstmtObj = connObj.prepareStatement("Select erRoleID from ttescalationemployeerole where ttEscalationLvlMasterID ="+ttEscalationLvlMasterID+"");
					ResultSet rsObj4 = pstmtObj.executeQuery();

					while(rsObj4.next()){

						eroleId = rsObj4.getInt("erRoleID");

						if(eroleId==Constants.ESCALATION_LEVEL1) {

							pstmtObj = connObj.prepareStatement("Select smTechEmpid from sitemaster where smSiteID ="+alarmData.getSiteId()+"");
							ResultSet rsObj5 = pstmtObj.executeQuery();
							while(rsObj5.next()){
								smTechEmpid= rsObj5.getInt("smTechEmpid");
							}
							
							pstmtObj.close();
							pstmtObj = connObj.prepareStatement("Select emEmail from employeemaster where emEmpID ="+smTechEmpid+"");
							rsObj5 = pstmtObj.executeQuery();
							while(rsObj5.next()){
								employeeEmail= rsObj5.getString("emEmail");
							}
							rsObj5.close();
							/* Send Mail*/
							
							ResourceBundle mybundle = ResourceBundle.getBundle("application");
							String fromMailId = mybundle.getString("from.mailid");
							String host = mybundle.getString("mail.host");
							String port = mybundle.getString("smtp.port");


							ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
							MailUtil mailUtil = new MailUtil(employeeEmail, fromMailId, host, port,alarmData.getSiteId(),alarmData.getAlrID(),1,connObj);
							executorService.schedule(mailUtil, 5 , TimeUnit.SECONDS);

							/*After Sending Mail Update alrEscalatedLevel in Trans_AlarmRecords*/

//							updateAlrEscalatedLevel(alarmData.getSiteId(),alarmData.getAlrPinNumber(),alarmData.getAlrTTEscalatedLevel()+1,alarmData.getAlrOpenTime(),employeeEmail);
							
							try {   

								/* Before updating mail id , fetch the previous mail id and append the new one*/
								String updatedMailIdList ="";
								if(alarmData.getMailId() != null && !alarmData.getMailId().isEmpty()) {
									updatedMailIdList = alarmData.getMailId()+","+employeeEmail;
								} else {
									updatedMailIdList = employeeEmail;
								}
								connObj=dataSource.getConnection();
								pstmtObj = connObj.prepareStatement("Update trans_alarmrecords Set alrTTEscalatedLevel= "+alarmData.getAlrTTEscalatedLevel()+1+ " , ttlstescalatedmailid= '"+updatedMailIdList+"' where smSiteID= "
										+ ""+alarmData.getSiteId()+" and alrID= "+alarmData.getAlrID()+"");

								System.out.println("Update query---"+pstmtObj);
								pstmtObj.executeUpdate();
//								DBConnectionUtil.closeConnection(connObj);
								pstmtObj.close();

								System.out.println("\n=====Releasing Connection Object To Pool=====\n");            
							} catch(Exception sqlException) {
								sqlException.printStackTrace();
							} finally {
								try {
									// Closing PreparedStatement Object
									pstmtObj.close();
								} catch(Exception sqlException) {
									sqlException.printStackTrace();
								}
							}

						} else if((eroleId==Constants.ESCALATION_LEVEL4)||
								(eroleId==Constants.ESCALATION_LEVEL5)||(eroleId==Constants.ESCALATION_LEVEL6)) {

							employeeEmail = fetchEmailId(pstmtObj,connObj,alarmData.getSiteId());

							ResourceBundle mybundle = ResourceBundle.getBundle("application");
							String fromMailId = mybundle.getString("from.mailid");
							String host = mybundle.getString("mail.host");
							String port = mybundle.getString("smtp.port");


							ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
							MailUtil mailUtil = new MailUtil(employeeEmail, fromMailId, host, port,alarmData.getSiteId(),alarmData.getAlrID(),1,connObj);
							executorService.schedule(mailUtil, 5 , TimeUnit.SECONDS);
							
							try {   

								pstmtObj = connObj.prepareStatement("Update trans_alarmrecords Set alrTTEscalatedLevel= "+alarmData.getAlrTTEscalatedLevel()+1+ " , ttlstescalatedmailid= '"+employeeEmail+"' where smSiteID= "
										+ ""+alarmData.getSiteId()+" and alrID= "+alarmData.getAlrID()+"");

								System.out.println("Update query---"+pstmtObj);
								pstmtObj.executeUpdate();
//								DBConnectionUtil.closeConnection(connObj);
								pstmtObj.close();

								System.out.println("\n=====Releasing Connection Object To Pool=====\n");            
							} catch(Exception sqlException) {
								sqlException.printStackTrace();
							} finally {
								try {
									// Closing PreparedStatement Object
									pstmtObj.close();
								} catch(Exception sqlException) {
									sqlException.printStackTrace();
								}
							}

							/*After Sending Mail Update alrEscalatedLevel in Trans_AlarmRecords*/

//							updateAlrEscalatedLevel(alarmData.getSiteId(),alarmData.getAlrPinNumber(),alarmData.getAlrTTEscalatedLevel()+1,alarmData.getAlrOpenTime(),employeeEmail);
						}
					}
//					DBConnectionUtil.closeResultSet(rsObj4);
				}

				pstmtObj.close();

				System.out.println("\n=====Releasing Connection Object To Pool=====\n");            
			} catch(Exception sqlException) {
				sqlException.printStackTrace();
			} finally {
				try {
					pstmtObj.close();
				} catch(Exception sqlException) {
					sqlException.printStackTrace();
				}
			}
		}
	}

//	private void updateAlrEscalatedLevel(int siteId, String alrPinNumber, int escalationLevel, long alrOpenTime, String employeeId) throws Exception {
//
//		DBConnectionUtil dbConnUtil = new DBConnectionUtil();
//		Connection connObj = dbConnUtil.getConnection();
//		PreparedStatement pstmtObj = null;
//
//		try {   
//
//			pstmtObj = connObj.prepareStatement("Update trans_alarmrecords Set alrTTEscalatedLevel="+escalationLevel+ " where smSiteID="+siteId+"and alrOpenTime= "+alrOpenTime+"and ttlstescalatedmailid="+employeeId+"");
//
//			pstmtObj.executeUpdate();
//			DBConnectionUtil.closeConnection(connObj);
//			DBConnectionUtil.closePreparedStatement(pstmtObj);
//
//			System.out.println("\n=====Releasing Connection Object To Pool=====\n");            
//		} catch(Exception sqlException) {
//			sqlException.printStackTrace();
//		} finally {
//			try {
//				// Closing PreparedStatement Object
//				DBConnectionUtil.closePreparedStatement(pstmtObj);
//			} catch(Exception sqlException) {
//				sqlException.printStackTrace();
//			}
//		}
//	}

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
		rsObj5.close();
		pstmtObj.close();
		pstmtObj = connObj.prepareStatement("Select znZoneID from clustermaster where crClusterID ="+crClusterID+"");
		rsObj5 = pstmtObj.executeQuery();
		while(rsObj5.next()){
			znZoneID = rsObj5.getInt("znZoneID");
		}
		rsObj5.close();
		pstmtObj.close();
		pstmtObj = connObj.prepareStatement("Select rgRegionID from zonemaster where znZoneID ="+znZoneID+"");

		rsObj5 = pstmtObj.executeQuery();

		while(rsObj5.next()){
			rgRegionID = rsObj5.getInt("rgRegionID");
		}

		pstmtObj = connObj.prepareStatement("Select emEmail from employeemaster where znZoneID ="+znZoneID+" and rgRegionID="+rgRegionID+" and erRoleID=3");
		rsObj5 = pstmtObj.executeQuery();
		while(rsObj5.next()){

			employeeId = rsObj5.getString("emEmail");
		}
		rsObj5.close();
		connObj.close();
		pstmtObj.close();
		return employeeId;
	}

	public void fetchCloseAlarmRecord() throws Exception {

		System.out.println("inside fetchCloseAlarmRecord");
		Connection connObj = null;
		PreparedStatement pstmtObj = null;
		ConnectionPool jdbcObj = new ConnectionPool();
			DataSource dataSource = jdbcObj.setUpPool();

		ArrayList<CloseAlarmData> alarmDataList = new ArrayList<CloseAlarmData>();
		int siteId= 0;
		String alrPinNumber="";
		long alrOpenTime=0L;
		int alrId=0;
		int ttEscalationLevel=0;

		try {   

			connObj = dataSource.getConnection();
			pstmtObj = connObj.prepareStatement("Select * from trans_alarmrecords  where alrCloseTime!=0 and alrDemonProcessed=1 and ttClosedMailSent=0");

			ResultSet rsObj = pstmtObj.executeQuery();
			String emailId ="";

			while(rsObj.next()){

				emailId = rsObj.getString("ttlstescalatedmailid");
				siteId = rsObj.getInt("smSiteID");
				alrPinNumber = rsObj.getString("alrPinNumber");
				alrOpenTime = rsObj.getLong("alrOpenTime");
				alrId = rsObj.getInt("alrID");
				ttEscalationLevel = rsObj.getInt("ttEscalationLevel");

				CloseAlarmData closeAlarmData = new CloseAlarmData(emailId,siteId,alrPinNumber,alrOpenTime,alrId,ttEscalationLevel);
				alarmDataList.add(closeAlarmData);
			}
			
			System.out.println("After fetching");
			System.out.println("Before Arraylist:  "+Arrays.toString(alarmDataList.toArray()));
			SendCloseMail sendCloseMail = new SendCloseMail(alarmDataList);
			sendCloseMail.sendCloseMail(emailId,connObj);

//			DBConnectionUtil.closeConnection(connObj);
			rsObj.close();
			pstmtObj.close();

			/* Send Mail for Close Alarm*/

			System.out.println("\n=====Releasing Connection Object To Pool=====\n");            
		} catch(Exception sqlException) {
			sqlException.printStackTrace();
		} finally {
			try {
				pstmtObj.close();
			} catch(Exception sqlException) {
				sqlException.printStackTrace();
			}
		}
	}
}

