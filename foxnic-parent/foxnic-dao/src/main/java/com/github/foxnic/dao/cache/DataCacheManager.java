package com.github.foxnic.dao.cache;

import com.github.foxnic.commons.cache.DoubleCache;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.relation.PropertyRoute;
import com.github.foxnic.dao.relation.RelationManager;
import com.github.foxnic.sql.entity.EntityUtil;
import com.github.foxnic.sql.meta.DBTable;

import java.util.*;

public abstract class DataCacheManager {

    public static enum JoinCacheMode {
        local,remote,both,none;
    }

    private CacheProperties cacheProperties;
    private RelationManager relationManager;
    private Map<Class<? extends Entity>,Map<String,CacheStrategy>> poStrategies=new HashMap<>();
    private int nameIndex=0;
    private Set<String> strategyKeys=new HashSet<>();

    /**
     * 注册缓存策略
     * @param isAccurate  是否精确匹配
     * @param cacheEmptyResult 是否缓存空的集合对象
     * @param conditionProperty 属性清单
     * */
    public void registStrategyIf(Class<? extends Entity> poType, boolean isAccurate, boolean cacheEmptyResult, String... conditionProperty) {

        String key=poType.getName() + ":" + StringUtil.join(conditionProperty);
        if(strategyKeys.contains(key)) {
           return;
        }

        strategyKeys.add(key);
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

    public void setRelationManager(RelationManager relationManager) {
        this.relationManager = relationManager;
    }

    public RelationManager getRelationManager() {
        return relationManager;
    }

    public void invalidateAccurateCache(Entity entity){
        invalidateAccurateCache(entity,entity);
    }
    /**
     * 使匹配到的精准缓存失效
     * @param master 属性的所有者
     * @param slave 属性值
     * */
    public void invalidateAccurateCache(Entity master,Entity slave){
        if(slave==null) return;
        Class poType=this.findPoType(slave.getClass());
        DoubleCache cache=this.getEntityCache(poType);
        if(cache==null) return;
        String key=null;
        Map<String,CacheStrategy> map=this.getStrategies(poType);
        for (CacheStrategy cacheStrategy : map.values()) {
            if(!cacheStrategy.isAccurate()) continue;
            key=cacheStrategy.makeKey(master);
            cache.remove(key);
        }
        //
        invalidateRelatedAccurateCache(master,slave);
    }

    public void invalidateAccurateCache(List<? extends Entity> entities){
        invalidateAccurateCache(null,entities);
    }

    public void invalidateAccurateCache(Entity master,List<? extends Entity> slaves){

        Class poType=null;
        for (Entity entity : slaves) {
            if (entity == null) continue;
            if (poType == null) {
                poType = this.findPoType(entity.getClass());
                break;
            }
        }
        DoubleCache cache=this.getEntityCache(poType);
        if(cache==null) return;
        String key=null;
        Map<String,CacheStrategy> map=this.getStrategies(poType);

        for (Entity entity : slaves) {
            if(entity==null) continue;
            if(poType==null) {
                poType = this.findPoType(entity.getClass());
            }
            for (CacheStrategy cacheStrategy : map.values()) {
                if(!cacheStrategy.isAccurate()) continue;
                if(master!=null) {
                    key = cacheStrategy.makeKey(master);
                } else {
                    key = cacheStrategy.makeKey(entity);
                }
                cache.remove(key);
            }
        }
        //
        invalidateRelatedAccurateCache(master,slaves);
    }

    /**
     * 使关联关系缓存失效
     * */
    public void invalidateRelatedAccurateCache(Entity master,Entity slave){
        if(slave==null) return;
        Class poType=findPoType(slave.getClass());
        List<PropertyRoute> routes=getRelationManager().findPropertyRoutes(poType);
        String[] keys=null;
        for (PropertyRoute route : routes) {
            DoubleCache cache=this.getEntityCache(route.getTargetPoType());
            if(cache==null) continue;
            Map<String,CacheStrategy> map=this.getStrategies(route.getTargetPoType());
            for (CacheStrategy cacheStrategy : map.values()) {
                if(!cacheStrategy.isAccurate()) continue;
                keys=cacheStrategy.makeRelatedKeys(route,master==null?slave:master);
                for (String key : keys) {
                    if(key==null) continue;
                    if(key.endsWith(":**:**")) {
                        key=key.substring(0,key.length()-6);
                        cache.removeKeysStartWith(key);
                    } else {
                        cache.remove(key);
                    }
                }
            }
        }
    }

    /**
     * 使关联关系缓存失效
     * */
    public void invalidateRelatedAccurateCache(Entity master,List<? extends Entity> slaves){
        for (Entity entity : slaves) {
            invalidateRelatedAccurateCache(master==null?entity:master,entity);
        }
    }



    public Class findPoType(Class type) {
        DBTable table = null;
        while(true) {
            table= EntityUtil.getDBTable(type);
            if(table!=null) {
                break;
            }
            type=type.getSuperclass();
            if(type==null) break;
        }
        return type;
    }



    public boolean isSupportCache(Class poType) {
        poType = findPoType(poType);
        DoubleCache cache=this.getEntityCache(poType);
        if(cache==null) return false;
        Map<String,CacheStrategy> map=this.getStrategies(poType);
        return !map.isEmpty();
    }


}
