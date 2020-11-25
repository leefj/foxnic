package com.github.foxnic.commons.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolTest {
	
	public static void main(String[] args) {
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		executorService.execute(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(1);
				
			}
		});
		
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println(2);
				
			}
		});
		System.out.println("shutdown-1");
		executorService.shutdown();
		System.out.println("shutdown-2");
		
	}
	
}
