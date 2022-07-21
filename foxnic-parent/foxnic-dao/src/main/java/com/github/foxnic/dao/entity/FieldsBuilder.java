package com.github.foxnic.dao.entity;

import com.github.foxnic.commons.collection.CollectorUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;
import com.github.foxnic.sql.treaty.DBTreaty;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 用于构建指定查询所需要的字段
 * */
public class FieldsBuilder {

    private DAO dao;

    private String table;

    private Set<String> fields=new LinkedHashSet<>();

    private FieldsBuilder(DAO dao,String table) {
        this.dao = dao ;
        this.table = table ;
    }


    public FieldsBuilder build(DAO dao,String table) {
        FieldsBuilder builder=new FieldsBuilder(dao,table);
        return builder;
    }

    public FieldsBuilder build(DAO dao,DBTableMeta table) {
        return build(dao,table.getTableName());
    }

    /**
     * 包含全部字段
     * */
    public FieldsBuilder addAll() {
        DBTableMeta tm=dao.getTableMeta(this.table);
        for (DBColumnMeta column : tm.getColumns()) {
            fields.add(column.getColumn());
        }
        return this;
    }

    /**
     * 包含指定字段
     * */
    public FieldsBuilder add(String... field) {
        for (String f : field) {
            fields.add(f);
        }
        return this;
    }

    /**
     * 包含指定字段
     * */
    public FieldsBuilder add(DBField... field) {
        for (DBField f : field) {
            fields.add(f.name());
        }
        return this;
    }

    /**
     * 加入指定前缀的字段
     * */
    public FieldsBuilder addStartsWith(String prefix) {
        DBTableMeta tm=dao.getTableMeta(this.table);
        for (DBColumnMeta column : tm.getColumns()) {
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
        DBTableMeta tm=dao.getTableMeta(this.table);
        for (DBColumnMeta column : tm.getColumns()) {
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
        DBTableMeta tm=dao.getTableMeta(this.table);
        for (DBColumnMeta column : tm.getColumns()) {
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
        fields.removeAll(rms);
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
        fields.removeAll(rms);
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
        fields.removeAll(rms);
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
        fields.removeAll(rms);
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
        this.remove(dbTreaty.getTenantIdField());
        return this;
    }




    public String getFieldsSQL() {
        if(this.fields.isEmpty()) {
            throw new RuntimeException("缺少字段");
        }
        return "";
    }

    public String getFieldsSQL(String tableAlias) {
        if(this.fields.isEmpty()) {
            throw new RuntimeException("缺少字段");
        }
        return "";
    }

    @Override
    public String toString() {
        return StringUtil.join(this.fields," , ");
    }

    private static enum MatchType {
        exact,starts,ends,contains;
    }

    private static class RuleItem {

        public RuleItem(MatchType matchType,String field) {
            this.matchType=matchType;
            this.field=field;
        }

        private MatchType matchType;
        private String field;
    }


}
