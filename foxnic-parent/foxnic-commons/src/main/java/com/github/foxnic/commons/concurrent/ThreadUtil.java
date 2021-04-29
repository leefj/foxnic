package com.github.foxnic.commons.concurrent;

import com.github.foxnic.commons.log.Logger;

/**
 * @author lifangjie
 * */
public class ThreadUtil {

	/**
	 * 当前线程，Sleep随机的指定时间
	 * @param base 线程sleep的时间，单位毫秒
	 * */
	public static void sleep(long base)
	{
		sleep(base,0);
	}
	 
	/**
	 * Sleep随机的指定时间 最终sleep时间 = base + random * seed
	 * @param base 基础时间(毫秒)
	 * @param seed 随机时间的种子
	 * */
	public static void sleep(long base,long seed)
	{
		long t=base+(long)(Math.random()*seed);
		try {
			Thread.currentThread().sleep(t);
		} catch (InterruptedException e) {
			Logger.error("线程Sleep错误",e);
		}
	}
	
	/**
	 * 输出线信息
	 * */
	 public static void checkThread()
	    {
	        ThreadGroup group = Thread.currentThread().getThreadGroup();
	        ThreadGroup topGroup = group;
	        // 遍历线程组树，获取根线程组
	        while (group != null) {
	            topGroup = group;
	            group = group.getParent();
	        }
	        // 激活的线程数加倍
	        int estimatedSize = topGroup.activeCount() * 2;
	        Thread[] slackList = new Thread[estimatedSize];
	        // 获取根线程组的所有线程
	        int actualSize = topGroup.enumerate(slackList);
	        // copy into a list that is the exact size
	        Thread[] list = new Thread[actualSize];
	        System.arraycopy(slackList, 0, list, 0, actualSize);
	        Logger.info("Thread list size == " + list.length);
	        for (Thread thread : list) {
	        	Logger.info(thread.getName()+":"+thread.toString());
	        }
	    }
}
