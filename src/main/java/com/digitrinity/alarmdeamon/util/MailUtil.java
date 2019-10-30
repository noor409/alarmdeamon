package com.digitrinity.alarmdeamon.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailUtil implements Runnable {


	String toMailId;
	String fromMailId;
	String host;
	String port;

	int siteID;
	int alrID;
	int alarmStatus;
	
	Connection connObj;

	public MailUtil(String toMailId, String fromMailId, String host, String port,int siteID,int alrID, int alarmStatus, Connection connObj) {
		super();
		this.toMailId = toMailId;
		this.fromMailId = fromMailId;
		this.host = host;
		this.port = port;
		this.siteID = siteID;
		this.alrID = alrID;
		this.alarmStatus = alarmStatus;
		this.connObj = connObj;
	}

	public String getToMailId() {
		return toMailId;
	}

	public void setToMailId(String toMailId) {
		this.toMailId = toMailId;
	}

	public String getFromMailId() {
		return fromMailId;
	}

	public void setFromMailId(String fromMailId) {
		this.fromMailId = fromMailId;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}



	public void run() {

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", host);
		properties.put("mail.smtp.port", port);
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("smtp.UseDefaultCredentials", "false");
		properties.put("mail.smtp.auth", "true");

		Session session = Session.getInstance(properties,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("projalin@gmail.com", "Jimi@14071992");
			}
		});

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(fromMailId));

			// Set To: header field of the header.
			System.out.println("toMailId---"+toMailId);
			//			message.addRecipient(Message.RecipientType.TO, new InternetAddress(toMailId));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(toMailId));

			// Set Subject: header field
			message.setSubject("This is the Subject Line!");

			String bodymail = "<i>Dummy Message</i><br>";
			bodymail += "<b>Wish you a nice day!</b><br>";
			bodymail += "<font color=red>Duke</font>";
			// Now set the actual message
			//		         message.setText(bodymail);
			message.setContent(bodymail, "text/html");

//			DBConnectionUtil dbConnUtil = new DBConnectionUtil();
//			Connection connObj = dbConnUtil.getConnection();
			PreparedStatement pstmtObj = null;
			int ttEscalationLevel=0;
			int ttStatus=0;

			try {   

				// Performing Database Operation!
				System.out.println("\n=====Making A New Connection Object For Db Transaction=====\n");
				pstmtObj = connObj.prepareStatement("Select ttStatus from trans_alarmrecords  where smSiteID="+siteID+" and alrID="+alrID+"");

				ResultSet rsObj = pstmtObj.executeQuery();

				while(rsObj.next()){

					if(alarmStatus==0) {
					ttEscalationLevel = rsObj.getInt("ttEscalationLevel"); 
					} else if(alarmStatus==1) {
						ttStatus=rsObj.getInt("ttStatus");
					}
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

			if(ttEscalationLevel!=0) {

				// Send message
				Transport.send(message);
				System.out.println("Sent message successfully....");
			}
		} catch (MessagingException mex) {
			mex.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
