package com.github.foxnic.dao.cache;

import com.alibaba.fastjson.JSONArray;
import com.github.foxnic.commons.cache.DoubleCache;
import com.github.foxnic.commons.encrypt.MD5Util;

public abstract class DataCacheManager {

    public abstract DoubleCache<String,Object> defineEntityCache(Class type,int localLimit,int expire) ;
    public abstract DoubleCache<String,Object> getEntityCache(Class type) ;

    /**
     * 生成缓存键
     * */
    public String makeCacheKey(Object... param) {
        JSONArray array=new JSONArray();
        for (Object o : param) {
            array.add(o);
        }
        return MD5Util.encrypt32(array.toJSONString());
    }
}
