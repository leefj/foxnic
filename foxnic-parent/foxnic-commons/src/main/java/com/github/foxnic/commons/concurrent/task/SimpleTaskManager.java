package com.github.foxnic.commons.concurrent.task;

import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.concurrent.BasicThreadFactory;
import com.github.foxnic.commons.concurrent.ThreadStopWay;
import com.github.foxnic.commons.log.Logger;

import java.util.concurrent.*;

/**
 * @author lifangjie
 * */
public class SimpleTaskManager extends TaskManager {

	private static SimpleTaskManager  DEFAULT =null ;

	/**
	 * 获得一个默认的线程池
	 * */
	public static SimpleTaskManager getDefault() {
		if(DEFAULT!=null) return DEFAULT;
		DEFAULT = new SimpleTaskManager(16);
		return DEFAULT;
	}

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
		return doParallelTask(task,0);
	}
	/**
	 * 执行并行任务
	 * @param task 任务
	 * @param  delay 延迟执行的毫秒数
	 * @return ScheduledFuture
	 * */
	public static ScheduledFuture<?> doParallelTask(Runnable task,long delay)
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
		}, delay, TimeUnit.MILLISECONDS);

		return f;

	}

	public static interface RetryTask {
		Result run(int times) throws Exception ;
	}

	public static void retry(RetryTask task,long... delay) {
		if(delay.length==0) return;
		SimpleTaskManager taskManager=new SimpleTaskManager(1,"retry");
		retry(taskManager,task,System.currentTimeMillis(),0,delay[0],delay);
	}

	private static void retry(SimpleTaskManager taskManager,final  RetryTask task,long startPoint,int i,long currDelay,final long... delay) {


		long nextDelay=-1;
		if(delay.length>i+1) {
			nextDelay=delay[i+1];
		}

	 	final long next=nextDelay;
		taskManager.doDelayTask(new Runnable() {
			@Override
			public void run() {
				Boolean retry=false;
				try {
					Result result = task.run(i);
					if(result==null) {
						retry = true;
					} else {
						retry = result.failure();
					}
				} catch (Exception e) {
					retry = true;
				};

				if(retry && next>0) {
					long delayX=next-(System.currentTimeMillis()-startPoint);
					retry(taskManager,task,startPoint,i+1,delayX,delay);
				} else {
					taskManager.shutdown();
				}

			}
		},currDelay);

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
