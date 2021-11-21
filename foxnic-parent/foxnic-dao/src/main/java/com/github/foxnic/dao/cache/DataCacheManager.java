package com.github.foxnic.dao.cache;

import com.github.foxnic.commons.cache.DoubleCache;

public abstract class DataCacheManager {

    public static enum JoinCacheMode {
        local,remote,both,none;
    }

    public DoubleCache<String,Object> defineOrGetJoinCache(Class type) {
        return defineEntityCache(type,this.getJoinCacheMode(),this.getJoinLocalElements(),this.getRemoteExpire());
    }
    public abstract DoubleCache<String,Object> defineEntityCache(Class type,JoinCacheMode cacheMode,int localLimit,int expire) ;
    public abstract DoubleCache<String,Object> getEntityCache(Class type);

    private JoinCacheMode joinCacheMode=JoinCacheMode.both;
    private Integer joinLocalElements=1024;
    private Integer remoteExpire=1000 * 60 *20;

    public Integer getRemoteExpire() {
        return remoteExpire;
    }

    public void setRemoteExpire(Integer remoteExpire) {
        this.remoteExpire = remoteExpire;
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

    public Integer getJoinLocalElements() {
        return joinLocalElements;
    }

    public void setJoinLocalElements(Integer joinLocalElements) {
        this.joinLocalElements = joinLocalElements;
    }




}
