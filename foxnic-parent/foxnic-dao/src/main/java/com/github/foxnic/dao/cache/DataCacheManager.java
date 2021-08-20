package com.github.foxnic.dao.cache;

import com.github.foxnic.commons.cache.DoubleCache;

public abstract class DataCacheManager {

    public abstract DoubleCache<String,Object> defineEntityCache(Class type,int localLimit,int expire) ;
    public abstract DoubleCache<String,Object> getEntityCache(Class type);


}
