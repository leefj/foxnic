package com.github.foxnic.dao.cache;

import com.github.foxnic.commons.cache.DoubleCache;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public abstract class DataCacheManager {

    public static enum JoinCacheMode {
        local,remote,both,none;
    }

    private JoinCacheMode joinCacheMode=JoinCacheMode.both;
    private Integer joinLocalLimit =1024;
    private Integer joinLocalExpire =1000 * 60 *20;
    private Integer joinRemoteExpire =1000 * 60 *20;
    private String cacheDetailConfigPrefix=null;
    //
    private Map<Class<? extends Entity>,Map<String,CacheStrategy>> poStrategies=new HashMap<>();
    private int nameIndex=0;


    /**
     * 注册缓存策略
     * @param isAccurate  是否精确匹配
     * @param cacheEmptyResult 是否缓存空的集合对象
     * @param conditionProperty 属性清单
     * */
    public void registStrategy(Class<? extends Entity> poType, boolean isAccurate, boolean cacheEmptyResult, String... conditionProperty) {
        Map<String,CacheStrategy>  strategies=poStrategies.get(poType);
        if(strategies==null) {
            strategies=new HashMap<>();
            poStrategies.put(poType,strategies);
        }
        nameIndex++;
        strategies.put("strategy-"+nameIndex,new CacheStrategy(poType.getName(),isAccurate,cacheEmptyResult,conditionProperty));
    }

    public Map<String,CacheStrategy> getStrategies(Class<? extends Entity> poType) {
        Map<String,CacheStrategy>  strategies=poStrategies.get(poType);
        if(strategies==null) {
            strategies=new HashMap<>();
            poStrategies.put(poType,strategies);
        }
        return strategies;
    }






    public DoubleCache<String,Object> defineOrGetJoinCache(Class type) {
        return defineOrGetJoinCache(type,this.getJoinCacheMode(),this.getJoinLocalLimit(),this.getJoinLocalExpire(),this.getJoinRemoteExpire());
    }

    public abstract DoubleCache<String,Object> defineOrGetJoinCache(Class type,JoinCacheMode cacheMode,int localLimit,int localExpire,int remoteExpire) ;
    public abstract DoubleCache<String,Object> getEntityCache(Class type);


    public String getCacheDetailConfigPrefix() {
        return cacheDetailConfigPrefix;
    }

    public void setCacheDetailConfigPrefix(String cacheDetailConfigPrefix) {
        cacheDetailConfigPrefix=cacheDetailConfigPrefix.trim();
        cacheDetailConfigPrefix= StringUtil.trim(cacheDetailConfigPrefix,".");
        this.cacheDetailConfigPrefix = cacheDetailConfigPrefix;
    }

    public Integer getJoinLocalExpire() {
        return joinLocalExpire;
    }

    public void setJoinLocalExpire(Integer joinLocalExpire) {
        this.joinLocalExpire = joinLocalExpire;
    }

    public Integer getJoinRemoteExpire() {
        return joinRemoteExpire;
    }

    public void setJoinRemoteExpire(Integer joinRemoteExpire) {
        this.joinRemoteExpire = joinRemoteExpire;
    }

    public JoinCacheMode getJoinCacheMode() {
        return joinCacheMode;
    }

    public void setJoinCacheMode(JoinCacheMode joinCacheMode) {
        this.joinCacheMode = joinCacheMode;
    }

    public void setJoinCacheMode(String joinCacheMode) {
        this.joinCacheMode = JoinCacheMode.valueOf(joinCacheMode);
    }

    public Integer getJoinLocalLimit() {
        return joinLocalLimit;
    }

    public void setJoinLocalLimit(Integer joinLocalElements) {
        this.joinLocalLimit = joinLocalElements;
    }




}
