package com.dignity.alarmdeamon.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailUtil {

	public void sendMail(final String toMailId, String fromMailId, String host, String port) {

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
				return new PasswordAuthentication(toMailId, "Jimi@14071992");
			}
		});
		
		 try {
	         // Create a default MimeMessage object.
	         MimeMessage message = new MimeMessage(session);

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(fromMailId));

	         // Set To: header field of the header.
	         message.addRecipient(Message.RecipientType.TO, new InternetAddress(toMailId));

	         // Set Subject: header field
	         message.setSubject("This is the Subject Line!");

	         String bodymail = "<i>Greetings!</i><br>";
	         bodymail += "<b>Wish you a nice day!</b><br>";
	         bodymail += "<font color=red>Duke</font>";
	         // Now set the actual message
//	         message.setText(bodymail);
	         message.setContent(bodymail, "text/html");

	         // Send message
	         Transport.send(message);
	         System.out.println("Sent message successfully....");
	      } catch (MessagingException mex) {
	         mex.printStackTrace();
	      }
	}




}
