package com.github.foxnic.sql.parser.cache;

/**
 * SQL解析结果缓存
 * @author fangjieli
 * */
public interface SQLParserCache {

	public Object get(String key);
	public void put(String key,Object value);
	
}
