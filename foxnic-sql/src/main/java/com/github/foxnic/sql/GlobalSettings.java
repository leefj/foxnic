package com.github.foxnic.sql;

 

import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.parser.cache.SQLParserCacheType;

/**
 * 用于全局参数设置
 */
public class GlobalSettings {

	/**
	 * 固定包名
	 * */
	public static final String PACKAGE = "com.github.foxnic.sql";

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
	public static SQLParserCacheType SQL_PARSER_CACHE_TYPE = SQLParserCacheType.MAP;
	
	
	 
}
