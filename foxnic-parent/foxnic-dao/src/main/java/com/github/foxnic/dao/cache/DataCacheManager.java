package com.github.foxnic.dao.cache;

import com.github.foxnic.commons.cache.DoubleCache;

public abstract class DataCacheManager {

    public DoubleCache<String,Object> defineEntityCache(Class type) {
        return defineEntityCache(type,1024,-1);
    }
    public abstract DoubleCache<String,Object> defineEntityCache(Class type,int localLimit,int expire) ;
    public abstract DoubleCache<String,Object> getEntityCache(Class type);


}
