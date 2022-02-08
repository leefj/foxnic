package com.github.foxnic.dao.relation.cache;

import com.github.foxnic.commons.cache.DoubleCache;
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
        for (Long unitId : unitIds) {
            keys.add("tabled-meta:metas:"+unitId);
        }
        Map<String, CacheMeta> map=cache.getAll(keys);
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

    public void addUnit(String table,Long unitId) {
        Set<Long> metas=getCacheMetaUnits(table);
        metas.add(unitId);
    }

    public void addCacheMeta(CacheMeta cacheMeta) {
        try {
            unsavedMetas.put(cacheMeta);
        } catch (InterruptedException e) {
            Logger.exception("put error ",e);
        }
    }



    public void save() {

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

        Map<String,Set<Long>> units=new HashMap<>();
        for (Map.Entry<String,Set<Long>> e : metaUnits.entrySet()) {
            units.put("tabled-meta:units:"+e.getKey(),e.getValue());
        }
        cache.putAll(units);


    }

    public void remove(String table, CacheMeta meta) {
//        metaUnits.remove(table);
        this.getCacheMetaUnits(table).remove(meta.getId());
        //cache.remove("tabled-meta:units:"+table);
        cache.remove("tabled-meta:metas:"+meta.getId());
    }


    public void reset() {
        this.cache.clear();
        metaUnits.clear();
        this.unsavedMetas.clear();
    }
}
