package com.github.foxnic.commons.concurrent;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.foxnic.commons.concurrent.pool.SimpleTaskManager;

public class SimpleTaskManagerTest {

	public static void main(String[] args) {
		(new SimpleTaskManagerTest()).test_delayTask();
	}
	
	private int tag;
	
	@Test
	public void test_delayTask()
	{
		
		tag=1;
		
		SimpleTaskManager tm=new SimpleTaskManager();
		tm.doDelayTask(new Runnable() {
			
			@Override
			public void run() {
				
				tag=2;
				
			}
		}, 1000);
		
		tm.doIntervalTask(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.out.println("hahah");
			}
		}, 1000);
		
		ThreadUtil.sleep(1200);
		
		assertTrue(tag==2);
		
		ThreadUtil.sleep(100);
		
	}
	
	@Test
	public void test_parallel() {
		tag=1;
		SimpleTaskManager.doParallelTask(new Runnable() {
			@Override
			public void run() {
				tag=10;
			}
		});
		ThreadUtil.sleep(100);
		assertTrue(tag==10);
	}
	
	
}
