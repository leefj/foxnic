package com.github.foxnic.dao.cache;

import com.github.foxnic.commons.cache.DoubleCache;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;

import java.util.HashMap;
import java.util.Map;

public abstract class DataCacheManager {

    public static enum JoinCacheMode {
        local,remote,both,none;
    }

    private CacheProperties cacheProperties;
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

    /**
     * 按po获得缓存策略
     * */
    public Map<String,CacheStrategy> getStrategies(Class<? extends Entity> poType) {
        Map<String,CacheStrategy>  strategies=poStrategies.get(poType);
        if(strategies==null) {
            strategies=new HashMap<>();
            poStrategies.put(poType,strategies);
        }
        return strategies;
    }


    public abstract DoubleCache<String,Object> getEntityCache(Class type);

    public CacheProperties getCacheProperties() {
        return cacheProperties;
    }

    public void setCacheProperties(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
        //初始化自定义缓存策略
        for (CacheProperties.PoCacheProperty poCacheProperty : this.cacheProperties.getPoCachePropertyMap().values()) {
            Map<String,CacheStrategy>  strategies=getStrategies(poCacheProperty.getPoType());
            for (CacheStrategy cacheStrategy : poCacheProperty.getCacheStrategyMap().values()) {
                strategies.put(cacheStrategy.getName(),cacheStrategy);
            }
        }
    }

    /**
     * 使匹配到的精准缓存失效
     * */
    public void invalidateAccurateCache(Entity entity){
        if(entity==null) return;
        Class poType=EntityContext.getProxyType(entity.getClass());
        DoubleCache cache=this.getEntityCache(poType);
        if(cache==null) return;
        String key=null;
        Map<String,CacheStrategy> map=this.getStrategies(poType);
        for (CacheStrategy cacheStrategy : map.values()) {
            if(!cacheStrategy.isAccurate()) continue;
            key=cacheStrategy.makeKey(entity);
            cache.remove(key);
            cache.removeKeyStarts(key);
        }
    }

    public boolean hasCache(Class poType) {
        poType=EntityContext.getProxyType(poType);
        DoubleCache cache=this.getEntityCache(poType);
        if(cache==null) return false;
        Map<String,CacheStrategy> map=this.getStrategies(poType);
        return !map.isEmpty();
    }


}
