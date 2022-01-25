package com.github.foxnic.dao.relation.cache2;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.dao.entity.Entity;

import java.util.Map;

public class CacheInvalidDecider {
    //
    private CacheInvalidEventType eventType;
    private CacheMeta meta;
    private String table;
    private Class poType;
    private Entity valueBefore;
    private Entity valueAfter;
    //
    public CacheInvalidDecider(CacheInvalidEventType eventType, CacheMeta meta, String table, Class poType , Entity valueBefore, Entity valueAfter) {
        this.eventType=eventType;
        this.meta=meta;
        this.table=table;
        this.poType=poType;
        this.valueBefore=valueBefore;
        this.valueAfter=valueAfter;
    }

    /**
     * 判断是否需要使缓存失效失效
     * */
    public boolean decide() {
        // 如果是处理 master
        if(meta.getMasterType().equals(poType)) {
            // id 一致即失效
            if(idsEquals(meta.getMasterIds(),valueBefore) || idsEquals(meta.getMasterIds(),valueAfter)) {
                return true;
            }
        }
        // 如果不是处理 master
        else {

        }
        return false;
    }

    private boolean idsEquals(Map<String, Object> masterIds, Entity po) {
        if(valueBefore==null) return false;
        Object value=null;
        for (Map.Entry<String, Object> entry : masterIds.entrySet()) {
            value= BeanUtil.getFieldValue(po,entry.getKey());
            if(value==null) return false;
            if(!value.equals(entry.getValue())) return false;
        }
        return true;
    }

}
