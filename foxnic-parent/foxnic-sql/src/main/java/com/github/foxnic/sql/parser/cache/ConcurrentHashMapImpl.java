package com.github.foxnic.sql.parser.cache;

import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapImpl  implements SQLParserCache {

	private ConcurrentHashMap<String,Object> cache=new ConcurrentHashMap<>();
	
	@Override
	public Object get(String key) {
		return cache.get(key);
	}

	@Override
	public void put(String key, Object value) {
		cache.put(key, value);
	}
 
}
