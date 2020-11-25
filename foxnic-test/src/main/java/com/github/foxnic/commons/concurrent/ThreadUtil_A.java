package com.github.foxnic.commons.concurrent;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ThreadUtil_A {

	@Test
	public void test_sleep()
	{
		long t0=System.currentTimeMillis();
		ThreadUtil.sleep(200);
		long t1=System.currentTimeMillis();
		assertTrue((t1-t0)>=200);
	}
	
	@Test
	public void test_sleep_r()
	{
		long t0=System.currentTimeMillis();
		ThreadUtil.sleep(200,100);
		long t1=System.currentTimeMillis();
		assertTrue((t1-t0)>=200);
		assertTrue((t1-t0)<=300);
	}
	
}
