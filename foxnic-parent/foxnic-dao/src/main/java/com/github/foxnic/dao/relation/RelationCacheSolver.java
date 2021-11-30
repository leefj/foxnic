package com.github.foxnic.dao.relation;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.cache.DoubleCache;
import com.github.foxnic.dao.cache.DataCacheManager;
import com.github.foxnic.dao.data.QueryMetaData;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RelationCacheSolver {



    public static final String RECORDS_KEY = "join:%s:%s:records:";
    public static final String RECORD_KEY = "join:%s:%s:record:";
    public static final String META_KEY = "join:%s:%s:meta";
    private PropertyRoute route;
    private Class targetType;
    private boolean forJoin;
    private DAO dao;
    private DBTableMeta tableMeta;
    private DoubleCache<String,Object> cache;
    private BuildingResult result;
    private QueryMetaData metaData;
    private DataCacheManager.JoinCacheMode cacheMode;
    private String[] groupFields;
    private Set<Object> values;



    public  RelationCacheSolver(BuildingResult result, DAO dao,PropertyRoute route,boolean forJoin) {
        this.dao=dao;
        this.route=route;
        this.targetType=route.getTargetPoType();
        this.forJoin=forJoin;
        this.tableMeta=dao.getTableMeta(route.getTargetTable().name());
        this.result=result;
        if(this.forJoin) {
            //拿到这个 cache，如果没有就创建
            this.cache=dao.getDataCacheManager().getEntityCache(route.getTargetPoType());
            this.cacheMode=dao.getDataCacheManager().getCacheProperties().getMode();
            if(this.cacheMode!= DataCacheManager.JoinCacheMode.none) {
                JSONObject meta = (JSONObject) cache.get(String.format(META_KEY,route.getKey(),route.getPropertyWithClass()));
                if (meta != null) {
                    this.metaData = QueryMetaData.fromJSON(meta);
                }
            }
        }
    }



    /**
     * 在构建 in 语句时处理数据
     * */
    public void handleForIn(String[] groupFields, String[] groupColumn,Set<Object> values) {
        if(this.cacheMode== DataCacheManager.JoinCacheMode.none) return;



        this.dao.getDataCacheManager().registStrategyIf(route.getTargetPoType(), true, true, groupColumn);

        this.groupFields=groupFields;
        this.values=values;
        //单字段
        if(groupFields.length==1) {
            //单主键
            if (this.tableMeta.getPKColumnCount() == 1 && this.tableMeta.isPK(groupColumn[0])) {
                handleForInWhenSinglePrimaryKey(groupFields,groupColumn,values);
            }
            //非主键
            else if (!this.tableMeta.isPK(groupFields[0])) {
                handleForInWhenSingleField(groupFields,groupColumn, values);
            }
        }
//        if(values.size()>0) {
//            System.err.printf(""+result.getCacheType().name());
//            System.err.println("");
//        }

    }

    private void handleForInWhenSingleField(String[] groupFields, String[] groupColumn,Set<Object> values) {

        Map<Object, Object> cachedTargetPoRcd = new HashMap<>();
        //按主键值从缓存取出实体
        Set<Object> removes = new HashSet<>();
        Set<String> recordKeys = new HashSet<>();
        for (Object value : values) {
            recordKeys.add(String.format(RECORDS_KEY,route.getKey(),route.getPropertyWithClass()) + value);
        }
        Map<String, JSONArray> recordMap = (Map)cache.getAll(recordKeys);
        //移除in语句中的元素，并搜集缓存元素
        for (Object value : values) {
            JSONArray rcds =  recordMap.get(String.format(RECORDS_KEY,route.getKey(),route.getPropertyWithClass()) + value);

            if (rcds != null) {
//                if(rcds.getJSONObject(0).containsKey("accessType")) {
//                    System.out.println();
//                };
                cachedTargetPoRcd.put(value, rcds);
                removes.add(value);
            }
        }
        //移除无需 in 的元素
        values.removeAll(removes);
        //设置结果模式
        result.setCacheType(RelationSolver.JoinCacheType.SINGLE_FIELD);
        result.setCachedTargetPoRecords(cachedTargetPoRcd);
//        if(values.size()>0) {
//            System.err.printf(""+result.getCacheType().name());
//            System.err.println("");
//        }
    }

    private void handleForInWhenSinglePrimaryKey(String[] usingProps, String[] groupColumn,Set<Object> values) {

        Map<Object, Object> cachedTargetPoRcd = new HashMap<>();
        //按主键值从缓存取出实体
        Set<Object> removes = new HashSet<>();
        Set<String> recordKeys = new HashSet<>();
        for (Object value : values) {
            recordKeys.add(String.format(RECORD_KEY,route.getKey(),route.getPropertyWithClass()) + value);
        }
        Map<String, Object> recordMap = cache.getAll(recordKeys);
        //移除in语句中的元素，并搜集缓存元素
        for (Object value : values) {
            JSONObject rcd = (JSONObject) recordMap.get(String.format(RECORD_KEY,route.getKey(),route.getPropertyWithClass()) + value);
//            if(rcd.containsKey("accessType")) {
//                System.out.println();
//            };
            if (rcd != null) {
                cachedTargetPoRcd.put(value, rcd);
                removes.add(value);
            }
        }
        //移除无需 in 的元素
        values.removeAll(removes);
        //设置结果模式
        result.setCacheType(RelationSolver.JoinCacheType.SINGLE_PRIMARY_KEY);
        result.setCachedTargetPoRecords(cachedTargetPoRcd);
        result.setTargetTableSimplePrimaryField(groupColumn[0]);

    }

    /**
     * 追加缓存中的记录
     * */
    public void appendRecords(RcdSet targets) {
        if(this.cacheMode== DataCacheManager.JoinCacheMode.none) return;
        //if(result.getCacheType()==null) return;
//        if(result.getCacheType()== RelationSolver.JoinCacheType.SINGLE_FIELD && result.getCachedTargetPoRecords()==null){
//            System.out.printf("");
//        }
//        if(result.getCacheType()== RelationSolver.JoinCacheType.SINGLE_PRIMARY_KEY) {
        if(result.getCachedTargetPoRecords()!=null) {

            for (Map.Entry<Object, Object> e : result.getCachedTargetPoRecords().entrySet()) {

                //缓存是数据就两种情况，要么是 JSONObject ，要么是 JSONArray
                if(e.getValue() instanceof JSONObject) {
                    JSONObject jr=(JSONObject) e.getValue();
                    if(jr==null || jr.isEmpty()) continue;
                    Rcd r = null;
                    try {
                        r = new Rcd(targets);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    for (String label : jr.keySet()) {
                        r.set(label, jr.get(label));
                    }
                    targets.add(r);
                }
                else if(e.getValue() instanceof JSONArray) {
                    JSONArray array=(JSONArray)e.getValue();
                    for (int i = 0; i < array.size(); i++) {
                        JSONObject jr=array.getJSONObject(i);
                        if(jr==null || jr.isEmpty()) continue;
//                        if(jr.containsKey("accessType")) {
//                            System.out.println();
//                        };
                        Rcd r = new Rcd(targets);
                        for (String label : jr.keySet()) {
                            r.set(label, jr.get(label));
                        }
                        targets.add(r);
                    }
                }
                else {
                    System.out.printf("break point");
                }

            }
        }

        if(this.metaData==null) {
            this.metaData=targets.getMetaData();
            JSONObject json=this.metaData.toJSONObject();
            cache.put(String.format(META_KEY,route.getKey(),route.getPropertyWithClass()), json);
        }

        return;
    }

    /**
     * 完全从缓存中的数据构建记录集
     * */
    public RcdSet buildRcdSet() {
        RcdSet rs=new RcdSet();
        BeanUtil.setFieldValue(rs,"metaData",this.metaData);
        appendRecords(rs);
        return rs;
    }

    /**
     * 把 join 的结果保存到缓存
     * */
    public void saveToCache(RcdSet rs) {
        if(this.cacheMode== DataCacheManager.JoinCacheMode.none) return;
        Object value=null;
        if(this.values==null)  this.values=new HashSet<>();
//        if(values.size()>0) {
//            System.err.printf(""+result.getCacheType().name());
//            System.err.println("");
//        }
        if(result.getCacheType()== RelationSolver.JoinCacheType.SINGLE_PRIMARY_KEY) {
            Map<String,Object> recordMap=new HashMap<>();
            for (Rcd r : rs) {
                value = r.getValue(this.groupFields[0]);
                recordMap.put(String.format(RECORD_KEY,route.getKey(),route.getPropertyWithClass()) + value,r.toJSONObject());
                values.remove(value);
            }
            //如果不存在，存入空对象
            for (Object key : values) {
                recordMap.put(String.format(RECORD_KEY,route.getKey(),route.getPropertyWithClass()) + key,new JSONObject());
            }
            cache.putAll(recordMap);
        }
        else if(result.getCacheType()== RelationSolver.JoinCacheType.SINGLE_FIELD) {
            JSONObject json=rs.getGroupedJSON(this.groupFields[0]);
            Map<String,Object> recordMap=new HashMap<>();
            for (String key : json.keySet()) {
                recordMap.put(String.format(RECORDS_KEY,route.getKey(),route.getPropertyWithClass()) + key,json.getJSONArray(key));
                values.remove(key);
            }
            //如果不存在，存入空数组
            for (Object key : values) {
                recordMap.put(String.format(RECORDS_KEY,route.getKey(),route.getPropertyWithClass()) + key,new JSONArray());
            }
            cache.putAll(recordMap);
        }



    }


}
