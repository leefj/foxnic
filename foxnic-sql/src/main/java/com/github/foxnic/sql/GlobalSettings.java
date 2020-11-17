package com.github.foxnic.sql;

 

import com.github.foxnic.sql.data.DataNameFormat;
import com.github.foxnic.sql.dialect.SQLDialect;

/**
 * 用于全局参数设置
 */
public class GlobalSettings {

	/**
	 * 固定包名
	 * */
	public static final String SQL_PACKAGE = "com.github.foxnic.sql";

	/**
	 * 版本
	 * */
	public static final String VERSION = "0.2.0";

	/**
	 * 指定默认的SQL方言，默认 SQLDialect.MySQL
	 */
	public static SQLDialect DEFAULT_SQL_DIALECT = SQLDialect.MySQL;

	/***
	 * 在转格式化的时候字段名大小写规则，默认按数据库返回的字段名不变
	 */
	public static DataNameFormat DEFAULT_DATA_NAME_FORMAT = DataNameFormat.NONE;
	
	
	/**
	 * 是否在默认情况下使用主DAO(@Primary 注解的 DAO 或 第一个初始化的 DAO)<br>
	 * 默认为 false  
	 * */
	public static boolean USE_PRIMARY_DAO = false;
}
