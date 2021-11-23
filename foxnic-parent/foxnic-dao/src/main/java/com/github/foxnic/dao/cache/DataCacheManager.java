package com.github.foxnic.dao.cache;

import com.github.foxnic.commons.cache.DoubleCache;
import com.github.foxnic.commons.lang.StringUtil;

public abstract class DataCacheManager {

    public static enum JoinCacheMode {
        local,remote,both,none;
    }

    public DoubleCache<String,Object> defineOrGetJoinCache(Class type) {
        return defineOrGetJoinCache(type,this.getJoinCacheMode(),this.getJoinLocalLimit(),this.getJoinLocalExpire(),this.getJoinRemoteExpire());
    }

    public abstract DoubleCache<String,Object> defineOrGetJoinCache(Class type,JoinCacheMode cacheMode,int localLimit,int localExpire,int remoteExpire) ;
    public abstract DoubleCache<String,Object> getEntityCache(Class type);

    private JoinCacheMode joinCacheMode=JoinCacheMode.both;
    private Integer joinLocalLimit =1024;
    private Integer joinLocalExpire =1000 * 60 *20;
    private Integer joinRemoteExpire =1000 * 60 *20;
    private String cacheDetailConfigPrefix=null;

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
