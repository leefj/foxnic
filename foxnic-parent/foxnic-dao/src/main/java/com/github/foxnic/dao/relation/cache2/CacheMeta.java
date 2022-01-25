package com.github.foxnic.dao.relation.cache2;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;

import java.util.*;

/**
 * 缓存单元
 * */
public class CacheMeta {



    /**
     * 所有者类型
     * */
    private Class<? extends Entity> ownerType;

    private String property;


    /**
     * 所有者ID
     * */
    private Map<String,Object> ownerIds = new LinkedHashMap<>();


    public void setOwnerId(String field,Object value) {
        this.ownerIds.put(field.toLowerCase(),value);
    }

    public static String buildMetaKey(String property,DBTableMeta tm, Entity entity) {
        String key=property+":";
        List<DBColumnMeta> pks=tm.getPKColumns();
        int i=0;
        for (DBColumnMeta pk : pks) {
            key+=pk.getColumn().toLowerCase()+"="+BeanUtil.getFieldValue(entity,pk.getColumn());
            if(i<pks.size()-1) {
                key+=",";
            }
            i++;
        }
        return key;
    }

    public String getMetaKey() {
        String key=this.property+":";
        int i=0;
        for (Map.Entry e : ownerIds.entrySet()) {
            key+= e.getKey()+"="+e.getValue();
            if(i<ownerIds.size()-1) {
                key+=",";
            }
            i++;
        }
        return key;
    }






    /**
     * 数据的缓存建
     * */
    private String valueCacheKey;

    private Map<String, Map<String, String>> joinedTablePks;
    private Map<String, Map<String, String>> joinedTableFields;

    private Map<String, Map<String, Set>> joinedTablePkValues;
    private Map<String, Map<String, Set>> joinedTableFieldValues;


    public CacheMeta(Class<? extends Entity> ownerType, String property, Map<String, Map<String, String>> joinedTablePks, Map<String, Map<String, String>> joinedTableFields) {
        this.ownerType=ownerType;
        this.property=property;
        this.joinedTablePks=joinedTablePks;
        this.joinedTableFields=joinedTableFields;
    }


    //
    public void setValues(Map<String, Map<String, Set>> pkValues, Map<String, Map<String, Set>> fieldValues) {

            this.joinedTablePkValues=pkValues;
            this.joinedTableFieldValues=fieldValues;

//        合并主键数据
//        for (Map.Entry<String,Map<String,String>> table : this.joinedTablePks.entrySet()) {
//
//            Map<String, Set> data = pkValues.get(table.getKey());
//            if(data==null) continue;
//
//            Map<String, Set> dataExists = this.joinedTablePkValues.get(table.getKey());
//            if (dataExists == null) {
//                dataExists = new HashMap<>();
//                this.joinedTablePkValues.put(table.getKey(), dataExists);
//            }
//
//            for (Map.Entry<String, String> field : table.getValue().entrySet()) {
//                Set columnExists=dataExists.get(field.getKey());
//                Set column=data.get(field.getKey());
//                if(column==null) continue;;
//                if(columnExists==null) {
//                    columnExists = new HashSet();
//                    dataExists.put(field.getKey(),columnExists);
//                }
//                columnExists.addAll(column);
//            }
//
//        }

//        合并关联字段数据
//        for (Map.Entry<String,Map<String,String>> table : this.joinedTableFields.entrySet()) {
//
//            Map<String, Set> data = fieldValues.get(table.getKey());
//            if(data==null) continue;
//
//            Map<String, Set> dataExists = this.joinedTableFieldValues.get(table.getKey());
//            if (dataExists == null) {
//                dataExists = new HashMap<>();
//                this.joinedTableFieldValues.put(table.getKey(), dataExists);
//            }
//
//            for (Map.Entry<String, String> field : table.getValue().entrySet()) {
//                Set columnExists=dataExists.get(field.getKey());
//                Set column=data.get(field.getKey());
//                if(column==null) continue;;
//                if(columnExists==null) {
//                    columnExists = new HashSet();
//                    dataExists.put(field.getKey(),columnExists);
//                }
//                columnExists.addAll(column);
//            }
//        }
    }
}
