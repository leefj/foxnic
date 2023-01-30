package com.github.foxnic.dao.entity;

import com.github.foxnic.commons.collection.CollectorUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.excel.DataException;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;
import com.github.foxnic.sql.treaty.DBTreaty;

import java.util.*;

/**
 * 用于构建指定查询所需要的字段
 * */
public class FieldsBuilder {

    private DAO dao;

    private String table;

    private Set<String> fields=new LinkedHashSet<>();

    private Map<String,String> fieldAliasMap=new HashMap<>();

    private DBTableMeta tableMeta = null ;


    public String getTable() {
        return table;
    }

    public DBTableMeta getTableMeta() {
        return tableMeta;
    }

    private FieldsBuilder(DAO dao, String table) {
        this.dao = dao ;
        this.table = table ;
        this.tableMeta=dao.getTableMeta(table);
        if(this.tableMeta==null) {
            throw new DataException("数据表 "+table+" 不存在");
        }
    }


    /**
     * 复制
     * */
    public FieldsBuilder clone() {
        FieldsBuilder fieldsBuilder=new FieldsBuilder(this.dao,this.table);
        fieldsBuilder.tableMeta=this.tableMeta;
        fieldsBuilder.fields.addAll(this.fields);
        fieldsBuilder.fieldAliasMap.putAll(this.fieldAliasMap);
        return fieldsBuilder;
    }


    public static FieldsBuilder build(DAO dao, String table) {
        FieldsBuilder builder=new FieldsBuilder(dao,table);
        return builder;
    }

    public static FieldsBuilder build(DAO dao, DBTable table) {
        return build(dao,table.name());
    }

    /**
     * 包含全部字段
     * */
    public FieldsBuilder addAll() {
        for (DBColumnMeta column : this.tableMeta.getColumns()) {
            fields.add(column.getColumn());
        }
        return this;
    }

    /**
     * 包含全部字段
     * */
    public FieldsBuilder addWithAlias(String field,String alias) {
        fields.add(field);
        fieldAliasMap.put(field,alias);
        return this;
    }

    public FieldsBuilder removeAll() {
        this.fields.clear();
        this.fieldAliasMap.clear();
        return this;
    }

    /**
     * 包含指定字段
     * */
    public FieldsBuilder add(String... field) {
        for (String f : field) {
            if(!this.tableMeta.isColumnExists(f)) {
                throw new DataException("字段 "+table+"."+field+" 不存在");
            }
            fields.add(f);
        }
        return this;
    }

    /**
     * 包含指定字段
     * */
    public FieldsBuilder add(DBField... field) {

        for (DBField f : field) {
            if(!f.table().name().equalsIgnoreCase(this.table)) {
                throw new DataException("数据表 "+table+" 错误，要求 "+table+" 表");
            }
            if(!this.tableMeta.isColumnExists(f.name())) {
                throw new DataException("字段 "+table+"."+field+" 不存在");
            }
            fields.add(f.name());
        }
        return this;
    }

    /**
     * 加入指定前缀的字段
     * */
    public FieldsBuilder addStartsWith(String prefix) {
        for (DBColumnMeta column : this.tableMeta.getColumns()) {
            if(column.getColumn().toLowerCase().startsWith(prefix.toLowerCase())) {
                fields.add(column.getColumn());
            }
        }
        return this;
    }

    /**
     * 加入指定后缀字段
     * */
    public FieldsBuilder addEndsWith(String suffix) {
        for (DBColumnMeta column : this.tableMeta.getColumns()) {
            if(column.getColumn().toLowerCase().endsWith(suffix.toLowerCase())) {
                fields.add(column.getColumn());
            }
        }
        return this;
    }

    /**
     * 加入包含指定字符的字段
     * */
    public FieldsBuilder addContains(String sub) {
        for (DBColumnMeta column : this.tableMeta.getColumns()) {
            if(column.getColumn().toLowerCase().contains(sub.toLowerCase())) {
                fields.add(column.getColumn());
            }
        }
        return this;
    }


    /**
     * 移除指定前缀的字段
     * */
    public FieldsBuilder removeStartsWith(String prefix) {
        Set<String> rms=new LinkedHashSet<>();
        for (String field : fields) {
            if(field.toLowerCase().startsWith(prefix.toLowerCase())) {
                rms.add(field);
            }
        }
        this.fields.removeAll(rms);
        for (String rm : rms) {
            this.fieldAliasMap.remove(rm);
        }
        return this;
    }

    /**
     * 移除指定后缀字段
     * */
    public FieldsBuilder removeEndsWith(String suffix) {
        Set<String> rms=new LinkedHashSet<>();
        for (String field : fields) {
            if(field.toLowerCase().endsWith(suffix.toLowerCase())) {
                rms.add(field);
            }
        }
        this.fields.removeAll(rms);
        for (String rm : rms) {
            this.fieldAliasMap.remove(rm);
        }
        return this;
    }

    /**
     * 移除包含指定字符的字段
     * */
    public FieldsBuilder removeContains(String sub) {
        Set<String> rms=new LinkedHashSet<>();
        for (String field : fields) {
            if(field.toLowerCase().contains(sub.toLowerCase())) {
                rms.add(field);
            }
        }
        this.fields.removeAll(rms);
        for (String rm : rms) {
            this.fieldAliasMap.remove(rm);
        }
        return this;
    }


    /**
     * 移除指定字段
     * */
    public FieldsBuilder remove(String... field) {
        Set<String> rms=new LinkedHashSet<>();
        for (String f : this.fields) {
            for (String s : field) {
                if(f.equalsIgnoreCase(s)) {
                    rms.add(f);
                }
            }
        }
        this.fields.removeAll(rms);
        for (String rm : rms) {
            this.fieldAliasMap.remove(rm);
        }
        return this;
    }

    /**
     * 移除指定字段
     * */
    public FieldsBuilder remove(DBField... field) {
        String[] fields= new String[field.length];
        for (int i = 0; i < field.length; i++) {
            fields[i]=field[i].name();
        }
        for (String rm : fields) {
            this.fieldAliasMap.remove(rm);
        }
        return remove(fields);
    }

    /**
     * 移除规约字段
     * */
    public FieldsBuilder removeDBTreatyFields() {
        DBTreaty dbTreaty=dao.getDBTreaty();
        this.remove(dbTreaty.getCreateTimeField()).remove(dbTreaty.getCreateUserIdField());
        this.remove(dbTreaty.getUpdateTimeField()).remove(dbTreaty.getUpdateUserIdField());
        this.remove(dbTreaty.getDeletedField()).remove(dbTreaty.getDeleteTimeField()).remove(dbTreaty.getDeleteUserIdField());
        this.remove(dbTreaty.getTenantIdField()).remove(dbTreaty.getVersionField());
        return this;
    }

    /**
     * 加入租户字段
     * */
    public FieldsBuilder addTenantIdField() {
        DBTreaty dbTreaty=dao.getDBTreaty();
        if(getTableMeta().isColumnExists(dbTreaty.getTenantIdField())) {
            this.add(dbTreaty.getTenantIdField());
        }
        return this;
    }

    /**
     * 加入规约字段
     * */
    public FieldsBuilder addDBTreatyFields() {
        DBTreaty dbTreaty=dao.getDBTreaty();
        this.add(dbTreaty.getCreateTimeField()).add(dbTreaty.getCreateUserIdField());
        this.add(dbTreaty.getUpdateTimeField()).add(dbTreaty.getUpdateUserIdField());
        this.add(dbTreaty.getDeletedField()).add(dbTreaty.getDeleteTimeField()).add(dbTreaty.getDeleteUserIdField());
        this.add(dbTreaty.getTenantIdField()).add(dbTreaty.getVersionField());
        return this;
    }

    /**
     * 按类型加入字段
     * */
    public FieldsBuilder addByType(DBDataType dataType) {

        for (DBColumnMeta column : this.tableMeta.getColumns()) {
            if(column.getDBDataType()==dataType) {
                this.fields.add(column.getColumn());
            }
        }

        return this;
    }

    /**
     * 按类型移除字段
     * */
    public FieldsBuilder removeByType(DBDataType dataType) {
        Set<String> rms=new LinkedHashSet<>();

        for (String field : fields) {
            DBColumnMeta cm=this.tableMeta.getColumn(field);
            if(cm==null) {
                throw new DataException("字段 "+table+"."+field+" 不存在");
            }
            if(cm.getDBDataType()==dataType) {
                rms.add(field);
            }
        }
        this.fields.removeAll(rms);
        for (String rm : rms) {
            this.fieldAliasMap.remove(rm);
        }
        return this;
    }


    public String getFieldsSQL() {
       return getFieldsSQL(null);
    }

    public String getFieldsSQL(String tableAlias) {
        if(this.fields.isEmpty()) {
            throw new RuntimeException("缺少字段");
        }
        Set<String> flds=new HashSet<>();
        this.fields.forEach((s)->{
            String part=s;
            if(!StringUtil.isBlank(tableAlias)) {
                part=tableAlias+"."+part;
            }
            String fieldAlias=fieldAliasMap.get(s);
            if(!StringUtil.isBlank(fieldAlias)) {
                part=part+" "+fieldAlias;
            }
            flds.add(part);
        });
        return StringUtil.join(flds," , ");
    }

    public List<DBColumnMeta> getColumns() {
        List<DBColumnMeta> columnMetas=new ArrayList<>();
        this.fields.forEach((s)->{
            DBColumnMeta cm=tableMeta.getColumn(s);
            columnMetas.add(cm);
        });
        return columnMetas;
    }

    @Override
    public String toString() {
        return StringUtil.join(this.fields," , ");
    }

    public FieldsBuilder addPrimaryFields() {
        fields.addAll(CollectorUtil.collectList(this.tableMeta.getPKColumns(),DBColumnMeta::getColumn));
        return this;
    }

    public FieldsBuilder removePrimaryFields() {
        List<String> rms=CollectorUtil.collectList(this.tableMeta.getPKColumns(),DBColumnMeta::getColumn);
        fields.removeAll(rms);
        for (String rm : rms) {
            this.fieldAliasMap.remove(rm);
        }
        return this;
    }

}
