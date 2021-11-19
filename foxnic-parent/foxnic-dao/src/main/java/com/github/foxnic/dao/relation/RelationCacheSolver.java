package com.github.foxnic.dao.relation;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.cache.DoubleCache;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.data.QueryMetaData;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.data.ExprRcd;
import com.github.foxnic.sql.meta.DBField;

import java.util.*;

public class RelationCacheSolver {

    public static final String ENTITY_KEY = "join:entity:";
    public static final String RECORD_KEY = "join:record:";
    public static final String META_KEY = "join:meta:";
    private PropertyRoute route;
    private Class targetType;
    private boolean forJoin;
    private DAO dao;
    private DBTableMeta tableMeta;
    private DoubleCache<String,Object> cache;
    private BuildingResult result;
    private QueryMetaData metaData;
    public  RelationCacheSolver(BuildingResult result, DAO dao,PropertyRoute route,boolean forJoin) {
        this.dao=dao;
        this.route=route;
        this.targetType=route.getTargetPoType();
        this.forJoin=forJoin;
        this.tableMeta=dao.getTableMeta(route.getTargetTable().name());
        this.result=result;
        if(this.forJoin) {
            //拿到这个 cache，如果没有就创建
            this.cache=dao.getDataCacheManager().defineEntityCache(route.getTargetPoType());
            String metaStr=(String) cache.get(META_KEY +route.hashCode());
            if(StringUtil.hasContent(metaStr)) {
                JSONObject meta = JSONObject.parseObject(metaStr);
                this.metaData = QueryMetaData.fromJSON(meta);
                System.out.println();
            }
        }
    }

    /**
     * 在构建 in 语句时处理数据
     * */
    public void handleForIn(DBField[] usingProps, Set<Object> values) {
        long t0=System.currentTimeMillis();
        Map<Object,Object> cachedTargetPoMap = new HashMap<>();
        Map<Object, Rcd> cachedTargetPoRcd = new HashMap<>();
        //单一主键的情况
        if (this.tableMeta.getPKColumnCount() == 1 && this.tableMeta.isPK(usingProps[0].name())) {

            //按主键值从缓存取出实体
            Set<Object> removes = new HashSet<>();
            for (Object value : values) {
                Object po = cache.get(ENTITY_KEY + value);
                if (po != null) {
                    Rcd rcd= (Rcd)cache.get(RECORD_KEY + value);
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
        System.err.println("handleForIn cost "+(System.currentTimeMillis()-t0));
    }

    /**
     * 追加缓存中的记录
     * */
    public void appendRecords(RcdSet targets) {
        return;
    }

    /**
     * 完全从缓存中的数据构建记录集
     * */
    public RcdSet buildRcdSet() {
        RcdSet rs=new RcdSet();
        return rs;
    }

    /**
     * 将来自缓存的数据回填至本次 join 的结果
     * */
    public void fillCachedResult(List entityList, Map<Object, ExprRcd> recordMap) {
        long t0=System.currentTimeMillis();
        if(result.getCacheMode()== RelationSolver.JoinCacheMode.SIMPLE_PRIMARY_KEY) {

            entityList.addAll(result.getCachedTargetPoMap().values());
            Object id=null;
            for (Object o : entityList) {
                id= BeanUtil.getFieldValue(o,result.getTargetTableSimplePrimaryField());
                Rcd rcd=result.getCachedTargetPoRcd().get(id);
                recordMap.put(id,rcd);
            }
        }
        System.err.println("fillCachedResult cost "+(System.currentTimeMillis()-t0));
    }

    /**
     * 把 join 的结果保存到缓存
     * */
    public void saveToCache(Object entity,Rcd rcd) {
        long t0=System.currentTimeMillis();
        if(result.getCacheMode()== RelationSolver.JoinCacheMode.SIMPLE_PRIMARY_KEY) {
            Object id= BeanUtil.getFieldValue(entity,result.getTargetTableSimplePrimaryField());
            cache.put(ENTITY_KEY + id,entity);
            cache.put(RECORD_KEY + id,rcd.toJSONObject());
        }
        if(this.metaData==null) {
            this.metaData=rcd.getOwnerSet().getMetaData();
            cache.put(META_KEY+route.hashCode(), JSON.toJSONString(this.metaData));
            System.out.println(META_KEY+route.hashCode()+":"+this.route.getTargetTable());
        }
        System.err.println("saveToCache cost "+(System.currentTimeMillis()-t0));
    }


}
