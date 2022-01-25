package com.github.foxnic.dao.relation.cache2;

import com.github.foxnic.dao.entity.Entity;

import java.beans.Transient;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 缓存单元
 * */
public class CacheMeta implements Serializable {


    private String property;
    /**
     * 所有者类型
     * */
    private Class<? extends Entity> masterType;

    /**
     * 所有者ID
     * */
    private Map<String,Object> masterIds = new LinkedHashMap<>();

    /**
     * 数据的缓存建
     * */
    private String valueCacheKey;

    private Map<String, Map<String, String>> joinedTablePks;
    private Map<String, Map<String, String>> joinedTableFields;

    private Map<String, Map<String, Set>> joinedTablePkValues;
    private Map<String, Map<String, Set>> joinedTableFieldValues;


    public CacheMeta() { }
    //
    public CacheMeta(Class<? extends Entity> ownerType, String property, Map<String, Map<String, String>> joinedTablePks, Map<String, Map<String, String>> joinedTableFields) {
        this.masterType =ownerType;
        this.property=property;
        this.joinedTablePks=joinedTablePks;
        this.joinedTableFields=joinedTableFields;
    }


    public Class<? extends Entity> getMasterType() {
        return masterType;
    }


    public Map<String, Object> getMasterIds() {
        return masterIds;
    }

    public void setOwnerId(String field, Object value) {
        this.masterIds.put(field.toLowerCase(),value);
    }


    @Transient
    public String getMetaKey() {
        String key=this.property+":";
        int i=0;
        for (Map.Entry e : masterIds.entrySet()) {
            key+= e.getKey()+"="+e.getValue();
            if(i< masterIds.size()-1) {
                key+=",";
            }
            i++;
        }
        return key;
    }

    public Map<String, Map<String, Set>> getJoinedTableFieldValues() {
        return joinedTableFieldValues;
    }

    public Map<String, Map<String, Set>> getJoinedTablePkValues() {
        return joinedTablePkValues;
    }

    public Map<String, Map<String, String>> getJoinedTableFields() {
        return joinedTableFields;
    }

    public Map<String, Map<String, String>> getJoinedTablePks() {
        return joinedTablePks;
    }


    //
    public void setValues(Map<String, Map<String, Set>> pkValues, Map<String, Map<String, Set>> fieldValues) {

            this.joinedTablePkValues=pkValues;
            this.joinedTableFieldValues=fieldValues;

    }

    public String getValueCacheKey() {
        return valueCacheKey;
    }

    public void setValueCacheKey(String valueCacheKey) {
        this.valueCacheKey = valueCacheKey;
    }

}
