package com.github.foxnic.commons.concurrent.task;

import com.github.foxnic.commons.concurrent.ThreadStopWay;
import com.github.foxnic.commons.log.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;



/**
 * https://www.cnblogs.com/tq1226112215/p/9639349.html
 * */
public class QuartzTaskManager extends TaskManager {

	private String id;
	private String jobGroupName;
	private String triggerGroupName;
	private String instanceName;
	
	protected int threadPriority=2;

	public int getThreadPriority() {
		return threadPriority;
	}

	public void setThreadPriority(int threadPriority) {
		this.threadPriority = threadPriority;
	}
	
	private  StdSchedulerFactory schedulerFactory =null;

	private Scheduler scheduler = null;
 
	/**
	 * @param threadPoolSize 线程池大小
	 * */
	public QuartzTaskManager(int threadPoolSize,String instanceName) {
		this.threadPoolSize=threadPoolSize;
		this.id = UUID.randomUUID().toString();
		jobGroupName = this.id + "-group";
		triggerGroupName = this.id + "-trigger";
		this.instanceName=instanceName;
	}
	
	public QuartzTaskManager(int threadPoolSize)
	{
		this(threadPoolSize,"quartz");
	}
	
	public QuartzTaskManager()
	{
		this(10,"quartz");
	}

	private void initScheduler() throws SchedulerException {

		if(schedulerFactory==null)
		{
			schedulerFactory = new StdSchedulerFactory();
		 
			Properties props = new Properties();
			props.put(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, "org.quartz.simpl.SimpleThreadPool"); // 线程池定义
			props.put("org.quartz.threadPool.threadCount", this.threadPoolSize+""); // 默认Scheduler的线程数
			props.put("org.quartz.threadPool.threadPriority", threadPriority+"");
			props.put("org.quartz.scheduler.instanceName", this.instanceName+"");
			props.put(StdSchedulerFactory.PROP_SCHED_JOB_FACTORY_CLASS, QuartzSimpleJobFactory.class.getName());
			try {
				schedulerFactory.initialize(props);
			} catch (SchedulerException e) {
				Logger.exception(e);
			}
		}
		
		if (scheduler == null) {
			scheduler = schedulerFactory.getScheduler();
		}
 
		if (!scheduler.isStarted()) {
			Logger.info("TaskScheduler Startup");
			scheduler.start();
		}
	}

	private Set<Runnable> executingTasks=new HashSet<>();
 
	/**
	 * @param mode  0:interval,1:delay
	 * */
	private Object createJobClass( final Runnable runable,final int taskId,int  mode)
	{
		class InternalJob implements InterruptableJob
		{
			private volatile Thread jobThread;
			private volatile Integer currentLoops=0; 
			private volatile int taskId=0; 
			@Override
			public void execute(JobExecutionContext context) throws JobExecutionException {

				if(mode==1) {
					synchronized (executingTasks) {
						if(executingTasks.contains(runable)) {
							return;
						}
					}
				}

				currentLoops++;
				if(mode==1  && currentLoops==1){
					return;
				}
				jobThread = Thread.currentThread();
				threads.put(taskId,jobThread);
				try {
					synchronized (executingTasks) {
						executingTasks.add(runable);
					}
					runable.run();
					synchronized (executingTasks) {
						executingTasks.remove(runable);
					}
				} catch (Exception e) {
					Logger.exception(e);
				}
				
				
				//如果是延迟任务，直接移除任务
				if(mode==1)
				{
					//delay 类型不做任何处理
					try {
						scheduler.deleteJob(context.getJobDetail().getKey());
						for (Entry <Integer,JobDetail> e : jobDetails.entrySet()) {
							if(e.getValue().equals(context.getJobDetail()))
							{
								jobDetails.remove(e.getKey());
								break;
							}
						}
					} catch (Exception e) {
						 Logger.exception(e);
					}
				}
			}

			@Override
			public void interrupt() throws UnableToInterruptJobException {
				if(jobThread!=null)
				{
					jobThread.interrupt();
				}
			}
		};
		
		return new InternalJob();
	}
	
	/**
	 * 执行一个固定周期的任务，上一任务若未执行完毕，不等待
	 * @param task 任务
	 * @param interval 执行周期，毫秒
	 * */
	@Override
	public synchronized int doIntervalTask(final Runnable task,long interval) {
		futureIndex++;
		final int index=futureIndex.intValue();
		Object instance=createJobClass(task,index,0);
		return doIntervalTask((Class<? extends Job>)instance.getClass(),index,interval,instance);
	}
	
	
	/**
	 * 执行一个固定周期的任务，上一任务若未执行完毕，不等待
	 * @param task 任务
	 * @param interval 执行周期，毫秒
	 * */
	public synchronized int doCronTask(final Runnable task,String cron) {
		futureIndex++;
		final int index=futureIndex.intValue();
		Object instance=createJobClass(task,index,0);
		return doCronTask((Class<? extends Job>)instance.getClass(),index,cron,instance);
	}
	
	/**
	 * 执行一个延迟任务
	 * @param task 任务
	 * @param delay 延迟时长，毫秒
	 * */
	@Override
	public synchronized int doDelayTask(Runnable task, long delay)
	{
		futureIndex++;
		final int index=futureIndex.intValue();
		Object instance=createJobClass(task,index,1);
		return doIntervalTask((Class<? extends Job>)instance.getClass(),index,delay,instance);
	}
	
	/**
	 * 所有的调度器
	 * */
	private ConcurrentHashMap<Integer, JobDetail> jobDetails = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, Thread> threads = new ConcurrentHashMap<Integer, Thread>();
	
	/**
	 * 重复执行一个任务
	 * @param interval 间隔执行的时间
	 * 	@return 返回任务ID
	 * */
	private synchronized int doIntervalTask(Class<? extends Job> jobType,final int index, long interval,Object instance) {
		
		String r=UUID.randomUUID().toString();
		String jobName=jobType.getName()+r+"-job";
		String triggerName=jobType.getName()+r+"-trigger";
		try {
			 JobDetail jobDetail=addIntervalJob(jobName, triggerName, jobType, interval,instance);
		 
			 jobDetails.put(index, jobDetail);
			return index;
		} catch (SchedulerException e) {
			 Logger.exception(e);
			 return -1;
		}
	}
	
	
	/**
	 * 重复执行一个任务
	 * @param interval 间隔执行的时间
	 * 	@return 返回任务ID
	 * */
	private synchronized int doCronTask(Class<? extends Job> jobType,final int index, String cron,Object instance) {
		
		String r=UUID.randomUUID().toString();
		String jobName=jobType.getName()+r+"-job";
		String triggerName=jobType.getName()+r+"-trigger";
		try {
			 JobDetail jobDetail=addCronJob(jobName, triggerName, jobType, cron,instance);
		 
			 jobDetails.put(index, jobDetail);
			return index;
		} catch (SchedulerException e) {
			 Logger.exception(e);
			 return -1;
		}
	}
	
	private void addJob(JobDetail job, Trigger trigger) throws SchedulerException {
		initScheduler();
		scheduler.scheduleJob(job, trigger);
	}

	private JobDetail addIntervalJob(String jobName, String triggerName, Class<? extends Job> jobClass, long interval,Object instance)
			throws SchedulerException {
		JobDataMap data=new JobDataMap();
		data.put("instance", instance);
		JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).setJobData(data).build();
		Trigger trigger = TriggerBuilder.newTrigger()// 创建一个新的TriggerBuilder来规范一个触发器
				.withIdentity(triggerName, triggerGroupName)// 给触发器起一个名字和组名
				.startNow()// 立即执行
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(interval)// 时间间隔 // 单位：毫秒
						.repeatForever()// 一直执行
				).build();// 产生触发器
		addJob(job, trigger);
 
		return job;
	}
	
	
	private JobDetail addCronJob(String jobName, String triggerName, Class<? extends Job> jobClass, String cronExpression,Object instance)
			throws SchedulerException {
		JobDataMap data=new JobDataMap();
		data.put("instance", instance);
		JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).setJobData(data).build();
		Trigger trigger = TriggerBuilder.newTrigger()// 创建一个新的TriggerBuilder来规范一个触发器
				.withIdentity(triggerName, triggerGroupName)// 给触发器起一个名字和组名
				.startNow()// 立即执行
				.withSchedule(
						CronScheduleBuilder.cronSchedule(cronExpression)
				).build();// 产生触发器
		addJob(job, trigger);
 
		return job;
	}

	/**
	 * 清除任务任务
	 * @param id 任务ID
	 * @param immediately 是否立即停止任务
	 * @return 是否成功
	 * */
	@Override
	public boolean clearTask(int id,boolean immediately)
	{
		return clearTask(id,immediately,ThreadStopWay.NONE);
	}
 
	/**
	 * 清除任务任务
	 * @param id 任务ID
	 * @param immediately 是否立即停止任务
	 * @param  stopWay  停止方式，ThreadStopWay的枚举值
	 * @return 是否成功
	 * */
	@Override
	public boolean clearTask(int id,boolean immediately,ThreadStopWay stopWay)
	{
		JobDetail jobDetail=jobDetails.get(id);
		if(jobDetail!=null)
		{
			try {
				scheduler.interrupt(jobDetail.getKey());
				scheduler.deleteJob(jobDetail.getKey());
				return true;
			} catch (Exception e) {
				 Logger.exception(e);
				 return false;
			}
		}
		
		Thread thread=threads.remove(id);
		if(thread!=null)
		{
			stopThread(thread,stopWay);
		}
		
		return false;
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
			scheduler.shutdown(!immediately);
			return true;
		} catch (SchedulerException e) {
			 Logger.exception(e);
			 return false;
		}
	}

	 
	
	
}



