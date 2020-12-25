package com.github.foxnic.commons.concurrent.task;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.github.foxnic.commons.concurrent.BasicThreadFactory;
import com.github.foxnic.commons.concurrent.ThreadStopWay;
import com.github.foxnic.commons.log.Logger;

/**
 * @author lifangjie
 * */
public class SimpleTaskManager extends TaskManager {

	private ScheduledExecutorService scheduler=null;
	
	/**
	 * @param threadPoolSize 线程池大小
	 * @param namingPattern 线程名称样式
	 * */
	public SimpleTaskManager(int threadPoolSize,final String namingPattern)
	{
		BasicThreadFactory.Builder builder=new BasicThreadFactory.Builder();
		builder.namingPattern(namingPattern);
		ThreadFactory factory=builder.build(); 
		scheduler=new ScheduledThreadPoolExecutor(threadPoolSize,factory);
	}
	
	public SimpleTaskManager(int threadPoolSize)
	{
		this(threadPoolSize,"simple-taskmgr");
	}

	public SimpleTaskManager()
	{
		this(4);
	}
	
	/**
	 * 所有的调度器
	 * */
	private ConcurrentHashMap<Integer, ScheduledFuture<?>> futures = new ConcurrentHashMap<Integer, ScheduledFuture<?>>();
	private ConcurrentHashMap<Integer, Thread> threads = new ConcurrentHashMap<Integer, Thread>();
 
	/**
	 * 执行一个固定周期的任务，上一任务若未执行完毕，不等待
	 * @param task 任务
	 * @param interval 执行周期，毫秒
	 * */
	@Override
	public synchronized int doIntervalTask(Runnable task, long interval) {
		futureIndex++;
		final int index=futureIndex.intValue();
		ScheduledFuture<?>  f=scheduler.scheduleAtFixedRate(new InternalTask(this, index, task, 0), 16,interval,TimeUnit.MILLISECONDS);
		futures.put(futureIndex, f);
		return index;
	}
	
	private static class InternalTask implements Runnable
	{
		private Runnable task;
		private int taskId;
		private SimpleTaskManager mgr;
		private int mode;
		public InternalTask(SimpleTaskManager mgr,int taskId,Runnable task,int mode)
		{
			this.mgr=mgr;
			this.taskId=taskId;
			this.task=task;
			this.mode=mode;
		}
		
		@Override
		public void run() {
			mgr.threads.put(this.taskId,Thread.currentThread());
			try {
				task.run();
			} catch (Exception e) {
				Logger.exception("task error", e);
			}
			if(mode==1)
			{
				mgr.clearTask(taskId);
			}
		}
	}
	
	/**
	 * 执行一个延迟任务
	 * @param task 任务
	 * @param delay 延迟时长，毫秒
	 * */
	@Override
	public int doDelayTask(Runnable task, long delay)
	{
		futureIndex++;
		final int index=futureIndex.intValue();
		ScheduledFuture<?>  f=scheduler.schedule(new InternalTask(this, index, task, 1), delay, TimeUnit.MILLISECONDS);
		futures.put(futureIndex, f);
		return index;
	}
	
	
	private static ScheduledExecutorService parallelTaskScheduler=null;
	/**
	 * 执行并行任务
	 * @param task 任务
	 * @return ScheduledFuture
	 * */
	public static ScheduledFuture<?> doParallelTask(Runnable task)
	{
		if(task==null) {
			return null;
		}
		
		if(parallelTaskScheduler==null) {
			
			BasicThreadFactory.Builder builder=new BasicThreadFactory.Builder();
			builder.namingPattern("simple-parallel-task");
			ThreadFactory factory=builder.build(); 
			parallelTaskScheduler=new ScheduledThreadPoolExecutor(4,factory);
			
		}
		
		ScheduledFuture<?>  f=parallelTaskScheduler.schedule(new Runnable() {
			@Override
			public void run() {
				task.run();
			}
		}, 0, TimeUnit.MILLISECONDS);
		
		return f;
 
	}
	
	
	/**
	 * 清除任务
	 * @param id 任务ID
	 * @param immediately 是否立即停止任务
	 * @return 是否成功
	 * */
	@Override
	public synchronized boolean clearTask(int id,boolean immediately,ThreadStopWay stopWay)
	{
		ScheduledFuture<?> future=futures.remove(id);
		if(future!=null) 
		{
			future.cancel(immediately);
		}
		
		Thread thread=threads.remove(id);
		if(thread!=null)
		{
			stopThread(thread,stopWay);
		}
		 
		return true;
	}
	
	
	

	/**
	 * 停止所有任务调度
	 * @param immediately 是否立即停止
	 * @return 是否成功
	 * */
	@Override
	public synchronized boolean shutdown(boolean immediately)
	{
		try {
			if(immediately) {
				scheduler.shutdownNow();
			} else {
				scheduler.shutdown();
			}
			return true;
		} catch (Exception e) {
			 Logger.exception(e);
			 return false;
		}
	}

	
	
	@Override
	public boolean clearTask(int id, boolean immediately) {
		return clearTask(id,immediately,ThreadStopWay.NONE);
	}
	
	
	
	
	
	
}
