package com.github.foxnic.commons.cache;

/**
 * 超时模式
 * @author leefangjie
 * */
public enum ExpireType {
	/**
	 * 按空闲时长记超时时长，存取操作都会延长超时时间
	 * */
	IDLE,
	/**
	 * 按存活时间即超时时长，生成后超时时间不再变化
	 * */
	LIVE;
}
