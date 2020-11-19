package com.github.foxnic.dao;

import com.github.foxnic.sql.data.DataNameFormat;

/**
 * 用于全局参数设置
 */
public class GlobalSettings {

	/**
	 * 固定包名
	 * */
	public static final String PACKAGE = "com.github.foxnic.dao";
 
	
	/***
	 * 在转格式化的时候字段名大小写规则，默认按数据库返回的字段名不变
	 */
	public static DataNameFormat DEFAULT_DATA_NAME_FORMAT = DataNameFormat.POJO_PROPERTY;
	 
}
