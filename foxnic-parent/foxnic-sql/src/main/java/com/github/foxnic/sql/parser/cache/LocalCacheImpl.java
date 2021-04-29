package com.github.foxnic.sql.parser.cache;

import com.github.foxnic.commons.cache.LocalCache;

public class LocalCacheImpl  implements SQLParserCache {

	private LocalCache<String,Object> cache=new LocalCache<>();
	
	@Override
	public Object get(String key) {
		return cache.get(key);
	}

	@Override
	public void put(String key, Object value) {
		cache.put(key, value);
	}
 
}
