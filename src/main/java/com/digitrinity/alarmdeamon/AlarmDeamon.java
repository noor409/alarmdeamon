package com.digitrinity.alarmdeamon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.digitrinity.alarmdeamon.mailprocess.CloseAlarmMailThread;
import com.digitrinity.alarmdeamon.mailprocess.OpenAlarmMailThread;

public class AlarmDeamon {
	
	private static Logger logger = LogManager.getLogger(AlarmDeamon.class.getName());
	
    public static void main( String[] args ) {
    	
//    	ExecutorService executor = null;
//    	
//    	try{
//    	logger.info("Alarm Deamon Started");
//		executor = Executors.newFixedThreadPool(1);
////		executor.execute(new OpenAlarmMailThread());
//		logger.info("Open Alarm Thread Ended");
//		executor.submit(new CloseAlarmMailThread());
//		logger.info("Close Alarm Thread Ended");
//    	} catch(Exception ex) {
//    		ex.printStackTrace();
//    	} finally {
//    		executor.shutdown();
//    	}
    	
    	OpenAlarmMailThread thread1 = new OpenAlarmMailThread();
    	thread1.start();
    	
//    	CloseAlarmMailThread thread2 = new CloseAlarmMailThread();
//    	thread2.start();
    }
}
