package com.github.foxnic.dao.relation.cache;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.entity.Entity;

import java.util.Map;
import java.util.Set;

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
            // UPDATE 时，主键一致即失效
            if(eventType==CacheInvalidEventType.UPDATE) {
                if (idsEquals(meta.getMasterIds(), valueBefore) || idsEquals(meta.getMasterIds(), valueAfter)) {
                    return true;
                }
            } else if(eventType==CacheInvalidEventType.INSERT) {
                // 自关联情况暂缓考虑
                // throw new RuntimeException("自关联情况暂缓不支持");
            } else if(eventType==CacheInvalidEventType.DELETE) {
                if (idsEquals(meta.getMasterIds(), valueBefore)) {
                    return true;
                }
            }
        }
        // 如果不是处理 master
        else {
            Map pks=meta.getJoinedTablePks().get(table);
            if(pks==null || pks.isEmpty()) return false;
            Set pkVals = meta.getJoinedTablePkValues().get(table);

            if(pkVals==null || pkVals.isEmpty()) return false;
            //
            if(eventType==CacheInvalidEventType.UPDATE) {
                Object pkVal=getPkValue(pks,valueBefore);
                if (pkVals == null || pkVals.isEmpty()) return false;
                else {
                    if(pkVals.contains(pkVal)) return true;
                }
            } else if(eventType==CacheInvalidEventType.INSERT) {
                // 有限确认主键匹配
                Object pkVal=getPkValue(pks,valueAfter);
                if (pkVals == null || pkVals.isEmpty()) return false;
                else {
                    if(pkVals.contains(pkVal)) return true;
                }
                // 确认关联字段值匹配
                Map<String,Set> joinedFieldsValueMap=this.meta.getJoinedTableFieldValues().get(table);
                for (Map.Entry<String,Set> e : joinedFieldsValueMap.entrySet()) {
                    Object value=BeanUtil.getFieldValue(valueAfter,e.getKey());
                    if(e.getValue().contains(value)) return true;
                }
            } else if(eventType==CacheInvalidEventType.DELETE) {
                // 有限确认主键匹配
                Object pkVal=getPkValue(pks,valueBefore);
                if (pkVals == null || pkVals.isEmpty()) return false;
                else {
                    if(pkVals.contains(pkVal)) return true;
                }
                // 确认关联字段值匹配
                Map<String,Set> joinedFieldsValueMap=this.meta.getJoinedTableFieldValues().get(table);
                for (Map.Entry<String,Set> e : joinedFieldsValueMap.entrySet()) {
                    Object value=BeanUtil.getFieldValue(valueAfter,e.getKey());
                    if(e.getValue().contains(value)) return true;
                }
            }
            System.out.printf("");
        }
        return false;
    }

    private Object getPkValue(Map<String,String> pks,Entity po) {
        if(po==null || pks.isEmpty() || pks.isEmpty()) return null;
        Object val=null;
        if(pks.size()==1) {
            for (String f : pks.keySet()) {
                val= BeanUtil.getFieldValue(po,f);
            }
        } else {
            Object[] vals=new Object[pks.size()];
            int i=0;
            for (String f : pks.keySet()) {
                vals[i] = BeanUtil.getFieldValue(po,f);
                i++;
            }
            val= StringUtil.join(vals,"-");
        }
        return val;
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
