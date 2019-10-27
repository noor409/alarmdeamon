package com.digitrinity.alarmdeamon.util;

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
	public MailUtil(String toMailId, String fromMailId, String host, String port) {
		super();
		this.toMailId = toMailId;
		this.fromMailId = fromMailId;
		this.host = host;
		this.port = port;
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
			//		         message.setText(bodymail);
			message.setContent(bodymail, "text/html");

			// Send message
			Transport.send(message);
			System.out.println("Sent message successfully....");
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}

	}
}