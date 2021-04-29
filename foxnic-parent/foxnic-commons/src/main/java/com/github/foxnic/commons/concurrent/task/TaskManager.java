package com.github.foxnic.commons.concurrent.task;

import com.github.foxnic.commons.concurrent.ThreadStopWay;
import com.github.foxnic.commons.log.Logger;

public  abstract class TaskManager {

	/**
	 * ID序号
	 * */
	protected Integer futureIndex=0;
	
	 public int getThreadPoolSize() {
		return threadPoolSize;
	}

	protected int threadPoolSize=10;
	
	/**
	 * 按一定的时间间隔执行任务
	 * @param task 任务
	 * @param interval 时间间隔，单位毫秒
	 * @return 任务ID
	 * */
	public abstract int doIntervalTask(final Runnable task, final long interval);
	
	/**
	 * 延迟指定时间执行任务
	 * @param task 任务
	 * @param delay 时间延迟，单位毫秒
	 * @return 任务ID
	 * */
	public abstract int doDelayTask(Runnable task, long delay);
	
	/**
	 * 清除任务
	 * @param id 任务ID
	 * @param immediately 是否立即停止任务
	 * @return 是否成功
	 * */
	public abstract boolean clearTask(int id,boolean immediately);
	
	/**
	 * 清除任务
	 * @param id 任务ID 
	 * @return 是否成功
	 * */
	public boolean clearTask(int id)
	{
		return clearTask(id,false);
	}
	
	/**
	 * 停止所有任务调度
	 * @param  id  任务ID
	 * @param  immediately 是否立即清理
	 * @param stopWay ThreadStopWay 枚举值
	 * @return 是否成功
	 * */
	public abstract boolean clearTask(int id,boolean immediately,ThreadStopWay stopWay);
	/**
	 * 停止所有任务调度
	 * @param immediately 是否立即停止
	 * @return 是否成功
	 * */
	public abstract boolean shutdown(boolean immediately);
	
	/**
	 * 停止所有任务调度
	 * @return 是否成功
	 * */
	public boolean shutdown()
	{
		return shutdown(false);
	}
	
 
	@SuppressWarnings("deprecation")
	protected void stopThread(Thread thread,ThreadStopWay stopWay) {
		if(stopWay ==ThreadStopWay.INTERRUPT)
		{
			try {
				while(!thread.isInterrupted())
				{
					thread.interrupt();
				}
			} catch (Exception e) {
				 Logger.exception(e);
			}
		}
		else if(stopWay ==ThreadStopWay.STOP)
		{
			try {
				//强制停止，有一定的危险性(数据丢失等)
				 thread.stop();
			} catch (Exception e) {
				 Logger.exception(e);
			}
		}
	}
}
