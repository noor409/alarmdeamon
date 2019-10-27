package com.digitrinity.alarmdeamon;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.digitrinity.alarmdeamon.mailprocess.CloseAlarmMailThread;
import com.digitrinity.alarmdeamon.mailprocess.OpenAlarmMailThread;

/**
 * Hello world!
 *
 */
public class AlarmDeamon {
	
    public static void main( String[] args ) {
    	
		ExecutorService executor = Executors.newFixedThreadPool(2);
		executor.execute(new OpenAlarmMailThread());
		executor.execute(new CloseAlarmMailThread());
    }
}
