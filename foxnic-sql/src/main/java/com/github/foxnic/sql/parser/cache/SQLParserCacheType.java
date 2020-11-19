package com.github.foxnic.sql.parser.cache;

/**
 * 数据库类型
 * @author fangjieli
 *
 */
public enum SQLParserCacheType {
	/**
	 * 以本地 ConcurrentHashMap 为缓存方式
	 * */
	MAP,
	/**
	 * 以 CAFFEINE 支持的 LocalCache 为缓存方式
	 * */
	CAFFEINE;
	 
 
	
	public SQLParserCache createSQLParserCache() {
		switch (this) {
		case MAP:
			return new ConcurrentHashMapImpl();
		case CAFFEINE:
			 return new LocalCacheImpl();
		default:
			return new ConcurrentHashMapImpl();
		}
	}

}
