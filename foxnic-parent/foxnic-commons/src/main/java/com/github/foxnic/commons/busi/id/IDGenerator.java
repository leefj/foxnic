package com.github.foxnic.commons.busi.id;

import java.util.UUID;

import com.github.foxnic.commons.encrypt.MD5Util;

public class IDGenerator {
	 
	/**
	 * 生成短的UUID，由于MD5和裁切，性能一般，默认用减号隔开
	 * @return 短UUID
	 * */
	public static String getSUID()
	{
		return getSUID(true);
	}
	
	/**
	 * 生成短的UUID，由于MD5和裁切，性能一般
	 * @param divided 是否用减号隔开
	 * @return 短UUID
	 * */
	public static String getSUID(boolean divided) {
		String id=UUID.randomUUID().toString();
		id=MD5Util.encrypt16(id).toLowerCase();
		id=id.substring(0, 2)+(divided?"-":"")+id.substring(2,12)+(divided?"-":"")+id.substring(12);
		return id;
	}
	
	/**
	 * 用减号隔开
	 * @return UUID
	 * */
	public static String getUUID() {
		return getUUID(true);
	}
	
	/**
	 * @param divided 是否用减号隔开
	 * @return UUID
	 * */
	public static String getUUID(boolean divided) {
		String id= UUID.randomUUID().toString();
		if(!divided)
		{
			id=id.replaceAll("-", "");
		}
		return id;
	}
	
	
	private static SnowflakeIdWorker snowflakeidWorker = null;

	/**
	 * 生成雪花ID，建议单节点使用，多节点时建议配置 SnowflakeIdWorker 使用
	 * 默认 workerId，workerCenterId 均为 0
	 * @return 雪花ID
	 * */
	public static long getSnowflakeId() {
		if (snowflakeidWorker == null) {
			snowflakeidWorker = SnowflakeIdWorker.getInstance();
		}
		return snowflakeidWorker.nextId();
	}
	
	/**
	 * 生成雪花ID，建议单节点使用，多节点时建议配置 SnowflakeIdWorker 使用
	 * 默认 workerId，workerCenterId 均为 0
	 * @return 雪花ID
	 * */
	public static String getSnowflakeIdString() {
		return getSnowflakeId()+"";
	}
	
	
	
}