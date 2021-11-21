package com.github.foxnic.dao.relation;

import com.github.foxnic.sql.expr.Expr;

import java.util.Map;

public class BuildingResult {

    private boolean isForJoin=true;
    /**
     * 缓存模式
     * */
    private RelationSolver.JoinCacheType cacheType;

    /**
     * 已经缓存的记录
     * */
    private Map<Object, Object> cachedTargetPoRecords;

    /**
     * cacheMode 为 RelationSolver.JoinCacheMode.SIMPLE_PRIMARY_KEY 时 po 表的主键字段
     * */
    private String targetTableSimplePrimaryField;

    /**
     * 最终构建的语句
     * */
    private Expr expr;

    private String[] groupFields;

    /**
     * 最后一个 Join
     * */
    private  Join lastJoin;

    private String[] groupJoinFields;

    private String[] catalogFields;

    /**
     * 数据表别名Map
     * */
    private Map<String,String> tableAlias;

    private  RelationCacheSolver cacheSolver;

    public RelationCacheSolver getCacheSolver() {
        return cacheSolver;
    }

    public void setCacheSolver(RelationCacheSolver cacheSolver) {
        this.cacheSolver = cacheSolver;
    }

    public RelationSolver.JoinCacheType getCacheType() {
        return cacheType;
    }

    public void setCacheType(RelationSolver.JoinCacheType cacheType) {
        this.cacheType = cacheType;
    }



    public Map<Object, Object> getCachedTargetPoRecords() {
        return cachedTargetPoRecords;
    }

    public void setCachedTargetPoRecords(Map<Object, Object> cachedTargetPoRecords) {
        this.cachedTargetPoRecords = cachedTargetPoRecords;
    }

    public String getTargetTableSimplePrimaryField() {
        return targetTableSimplePrimaryField;
    }

    public void setTargetTableSimplePrimaryField(String targetTableSimplePrimaryField) {
        this.targetTableSimplePrimaryField = targetTableSimplePrimaryField;
    }

    public Expr getExpr() {
        return expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    public String[] getGroupFields() {
        return groupFields;
    }

    public void setGroupFields(String[] groupFields) {
        this.groupFields = groupFields;
    }

    public Join getLastJoin() {
        return lastJoin;
    }

    public void setLastJoin(Join lastJoin) {
        this.lastJoin = lastJoin;
    }

    public String[] getGroupJoinFields() {
        return groupJoinFields;
    }

    public void setGroupJoinFields(String[] groupJoinFields) {
        this.groupJoinFields = groupJoinFields;
    }

    public String[] getCatalogFields() {
        return catalogFields;
    }

    public void setCatalogFields(String[] catalogFields) {
        this.catalogFields = catalogFields;
    }

    public Map<String, String> getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(Map<String, String> tableAlias) {
        this.tableAlias = tableAlias;
    }

    public boolean isForJoin() {
        return isForJoin;
    }

    public void setForJoin(boolean forJoin) {
        isForJoin = forJoin;
    }







}
