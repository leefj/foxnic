package com.github.foxnic.commons.concurrent;

/**
 * @author leefangjie
 * */
public enum ThreadStopWay {
	/**
	 * 不处理
	 * */
	NONE,
	/**
	 * thread.interrupt()中断
	 * */
	INTERRUPT,
	/**
	 * thread.stop()强行终止
	 * */
	STOP;
}
