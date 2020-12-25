package com.github.foxnic.commons.task;

import com.github.foxnic.commons.concurrent.task.QuartzTaskManager;

public class QuartzTaskMgrTest {

	public static void main(String[] args) {
		test_parse();
	}
 
	public static void test_parse()
	{
		QuartzTaskManager tm=new QuartzTaskManager(4,"AAA");
		
		tm.doCronTask(new Runnable() {
			
			@Override
			public void run() {
				long t=System.currentTimeMillis();
				t=(t/1000)%10;
				System.out.println("T1-"+Thread.currentThread().getName()+"," + t);
			}
		}, "1/5 * * * * ? *");
		
		tm.doCronTask(new Runnable() {
			
			@Override
			public void run() {
				long t=System.currentTimeMillis();
				t=(t/1000)%10;
				System.out.println("T2-"+Thread.currentThread().getName()+"," + t);
			}
		}, "1/3 * * * * ? *");
		
	}
	
}
