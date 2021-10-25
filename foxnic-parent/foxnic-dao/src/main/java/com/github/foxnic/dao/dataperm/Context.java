package com.github.foxnic.dao.dataperm;

import java.util.HashMap;
import java.util.Map;

public class Context {

    private String mainTable;
    private String mainTableAlias;

    private Map<String,String> aliasMap=new HashMap<>();

    public Context(String mainTable,String mainTableAlias) {
        this.mainTable=mainTable;
        this.mainTableAlias=mainTableAlias;
        this.setAlias(mainTable,mainTableAlias);
    }

    public void setAlias(String table,String alias) {
        aliasMap.put(table.trim().toLowerCase(),alias);
    }

    public String getAlias(String table) {
        return aliasMap.get(table.trim().toLowerCase());
    }

    public String getMainTable() {
        return mainTable;
    }

    public String getMainTableAlias() {
        return mainTableAlias;
    }



}
