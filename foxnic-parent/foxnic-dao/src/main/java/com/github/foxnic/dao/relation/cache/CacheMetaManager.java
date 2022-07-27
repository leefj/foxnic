package com.github.foxnic.dao.relation.cache;

import com.github.foxnic.commons.cache.DoubleCache;
import com.github.foxnic.commons.concurrent.task.SimpleTaskManager;
import com.github.foxnic.commons.log.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

class CacheMetaManager {

    private Map<String, Set<Long>> metaUnits  = new ConcurrentHashMap<>();

    private DoubleCache cache=null;

    public CacheMetaManager(DoubleCache cache) {
        this.cache=cache;
    }


    public Collection<CacheMeta> getCacheMetas(String table) {
        Set<Long> unitIds = getCacheMetaUnits(table);
        if (unitIds == null || unitIds.isEmpty()) return new HashSet<>();
        Set<String> keys = new HashSet<>();
        Map<String, CacheMeta> map=new HashMap<>();
        String key=null;
        //  防止并发冲突
        Long[] unitIdArr=unitIds.toArray(new Long[0]);
        for (Long unitId : unitIdArr) {
            key="tabled-meta:metas:"+unitId;
            CacheMeta cacheMeta=unsavedMetasMap.get(key);
            if(cacheMeta!=null) {
                map.put(key,cacheMeta);
                continue;
            }
            keys.add(key);
        }
        map.putAll(cache.getAll(keys));

        List<CacheMeta> list=new ArrayList<>();
        for (CacheMeta value : map.values()) {
            if(value==null) continue;
            list.add(value);
        }
        return list;
    }

    public Set<Long> getCacheMetaUnits(String table) {
        Set<Long> unitList = metaUnits.get(table);
        if(unitList==null) {
            unitList=(Set<Long>)cache.get("tabled-meta:units:"+table);
        }
        if(unitList==null) {
            unitList=new HashSet<>();
            metaUnits.put(table,unitList);
        }
        return unitList;
    }

    private LinkedBlockingQueue<CacheMeta> unsavedMetas=new LinkedBlockingQueue<>();
    private Map<String,CacheMeta> unsavedMetasMap=new ConcurrentHashMap<>();


    private SimpleTaskManager unitSavingTaskManager=new SimpleTaskManager(2);
    private Set<String> unitSavingTables=null;


    public void addUnit(String table,Long unitId) {

        if(unitSavingTables==null) {
            unitSavingTables = new HashSet<>();
            unitSavingTaskManager.doIntervalTask(new Runnable() {
                @Override
                public void run() {
                    Set<String> tables=new HashSet<>(unitSavingTables);
                    unitSavingTables.clear();
                    for (String unitSavingTable : tables) {
                        Set<Long> metas=getCacheMetaUnits(unitSavingTable);
                        try {
                            cache.put("tabled-meta:units:" + unitSavingTable, metas);
                        } catch (Exception e) {
                            Logger.exception(e);
                        }
                    }
                }
            },1000);
        }

        Set<Long> metas=getCacheMetaUnits(table);
        metas.add(unitId);

    }

    public void addCacheMeta(CacheMeta cacheMeta) {
        try {
            unsavedMetas.put(cacheMeta);
            unsavedMetasMap.put("tabled-meta:metas:"+cacheMeta.getId(),cacheMeta);
        } catch (InterruptedException e) {
            Logger.exception("put error ",e);
        }
    }

    private SimpleTaskManager savingTaskManager=new SimpleTaskManager(2);
    private Boolean doSave = null;

//    public synchronized void save() {
//
//        if(doSave==null) {
//            savingTaskManager.doIntervalTask(new Runnable() {
//                @Override
//                public void run() {
//                    if(doSave) {
//
//                        try {
//                            saveInternal();
//                            doSave = false;
//                            System.err.println("hha");
//                        } catch (Exception e) {
//
//                        }
//
//                    }
//                }
//            },5000);
//        }
//
//        // 标记为需要保存
//        doSave=true;
//    }

    public synchronized void save() {
        Map<String,CacheMeta> metas=new HashMap<>();
        while(!unsavedMetas.isEmpty()) {
            try {
                CacheMeta meta= unsavedMetas.take();
                metas.put("tabled-meta:metas:"+meta.getId(),meta);
            } catch (InterruptedException e) {
                Logger.exception("take error ",e);
            }
        }
        cache.putAll(metas);
        //
        for (CacheMeta meta : metas.values()) {
            unsavedMetasMap.remove("tabled-meta:metas:"+meta.getId());
        }


        Map<String,Set<Long>> units=new HashMap<>();
        for (Map.Entry<String,Set<Long>> e : metaUnits.entrySet()) {
            units.put("tabled-meta:units:"+e.getKey(),e.getValue());
        }
        cache.putAll(units);


    }

    public void remove(String table, CacheMeta meta) {
        //
        this.getCacheMetaUnits(table).remove(meta.getId());
        cache.remove("tabled-meta:metas:"+meta.getId());

        // 处理未保存到缓存的部分
        CacheMeta us= unsavedMetasMap.get("tabled-meta:metas:"+meta.getId());
        if(us!=null) {
            unsavedMetasMap.remove("tabled-meta:metas:"+meta.getId());
            unsavedMetas.remove(us);
        }
        unsavedMetas.remove(meta);
        // 立即同步
        this.save();
    }


    public void reset() {
        this.cache.clear();
        metaUnits.clear();
        this.unsavedMetas.clear();
    }
}
