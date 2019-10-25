package com.dignity.alarmdeamon;

import java.util.ResourceBundle;

import com.dignity.alarmdeamon.util.MailUtil;

/**
 * Hello world!
 *
 */
public class AlarmDeamon {
	
    public static void main( String[] args ) {
        
    	ResourceBundle mybundle = ResourceBundle.getBundle("application");
		String toMailId = mybundle.getString("to.mailid");
		String fromMailId = mybundle.getString("from.mailid");
		String host = mybundle.getString("mail.host");
		String port = mybundle.getString("smtp.port");
		
		/**
		 * Start Thread.
		 * 
		 */
		MailThread parserThread = new MailThread();
    	parserThread.start();
    	
    	MailUtil mailUtil = new MailUtil();
    	mailUtil.sendMail(toMailId,fromMailId,host,port);
    }
}
