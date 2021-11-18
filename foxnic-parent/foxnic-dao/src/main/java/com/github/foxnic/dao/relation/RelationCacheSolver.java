package com.github.foxnic.dao.relation;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.cache.DoubleCache;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.meta.DBField;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RelationCacheSolver {

    private PropertyRoute route;
    private Class targetType;
    private boolean forJoin;
    private DAO dao;
    private  DBTableMeta tableMeta;
    private  DoubleCache<String,Object> cache;
    public  RelationCacheSolver(DAO dao,PropertyRoute route,boolean forJoin) {
        this.dao=dao;
        this.route=route;
        this.targetType=route.getTargetPoType();
        this.forJoin=forJoin;
        this.tableMeta=dao.getTableMeta(route.getTargetTable().name());
        if(this.forJoin) {
            //拿到这个cache，如果没有就创建
            cache=dao.getDataCacheManager().defineEntityCache(route.getTargetPoType(),1024,-1);
        }
    }

    /**
     * 在构建 in 语句时处理数据
     * */
    public void handleForIn(BuildingResult result, DBField[] usingProps, Set<Object> values) {
        Map<Object,Object> cachedTargetPoMap = new HashMap<>();
        Map<Object, JSONObject> cachedTargetPoRcd = new HashMap<>();
        //单一主键的情况
        if (this.tableMeta.getPKColumnCount() == 1 && this.tableMeta.isPK(usingProps[0].name())) {

            //按主键值从缓存取出实体
            Set<Object> removes = new HashSet<>();
            for (Object value : values) {
                Object po = cache.get("join:entity:" + value);
                if (po != null) {
                    JSONObject rcd= (JSONObject)cache.get("join:record:" + value);
                    cachedTargetPoMap.put(value, po);
                    cachedTargetPoRcd.put(value,rcd);
                    removes.add(value);
                }
            }
            //移除无需 in 的元素
            values.removeAll(removes);
            //设置结果模式
            result.setCacheMode(RelationSolver.JoinCacheMode.SIMPLE_PRIMARY_KEY);
            result.setCachedTargetPoMap(cachedTargetPoMap);
            result.setCachedTargetPoRcd(cachedTargetPoRcd);
            result.setTargetTableSimplePrimaryField(usingProps[0].name());

        }

    }
}
