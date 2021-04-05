package com.github.foxnic.dao.relation;

import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.sql.expr.ConditionExpr;

import java.util.HashMap;
import java.util.Map;

public class PropertyRoute {

    private Class<? extends Entity> poType;
    private String property;



    private Class<? extends Entity> targetPoType;
    private ConditionExpr targetCondition;
    private boolean isMulti=true;

    private Map<String,ConditionExpr> tableConditions=new HashMap<>();


    public PropertyRoute(Class<? extends Entity> poType,String property){
        this.poType=poType;
        this.property=property;
    }

    /**
     * 对应单个实体，生成一个实体类型的属性
     * */
    public PropertyRoute single(Class<? extends Entity> targetPoType, ConditionExpr condition){
        this.targetPoType=targetPoType;
        this.targetCondition=condition;
        this.isMulti=false;
        return this;
    }

    /**
     * 对应多个实体，生成一个 Set 类型的属性
     * */
    public PropertyRoute multi(Class<? extends Entity> targetPoType, ConditionExpr condition){
        this.targetPoType=targetPoType;
        this.targetCondition=condition;
        this.isMulti=true;
        return this;
    }

    /**
     * 对应单个实体，生成一个实体类型的属性
     * */
    public PropertyRoute single(Class<? extends Entity> targetPoType, String condition,Object... ps){
        this.single(targetPoType,new ConditionExpr(condition,ps));
        return this;
    }

    /**
     * 对应单个实体，生成一个实体类型的属性
     * */
    public PropertyRoute single(Class<? extends Entity> targetPoType){
        this.single(targetPoType,null);
        return this;
    }

    /**
     * 对应多个实体，生成一个 Set 类型的属性
     * */
    public PropertyRoute multi(Class<? extends Entity> targetPoType, String condition,Object... ps){
        this.multi(targetPoType,new ConditionExpr(condition,ps));
        return this;
    }

    /**
     * 对应多个实体，生成一个 Set 类型的属性
     * */
    public PropertyRoute multi(Class<? extends Entity> targetPoType){
        this.multi(targetPoType,null);
        return this;
    }

    /**
     * 增加中间表的条件配置
     * */
    public PropertyRoute condition(String table, ConditionExpr condition){
        tableConditions.put(table,condition);
        return this;
    }

    /**
     * 增加中间表的条件配置
     * */
    public PropertyRoute condition(String targetTable, String condition,Object... ps){
        this.condition(targetTable,new ConditionExpr(condition,ps));
        return this;
    }


    public String getProperty() {
        return property;
    }

    public Class<? extends Entity> getTargetPoType() {
        return targetPoType;
    }

    public ConditionExpr getTargetCondition() {
        return targetCondition;
    }

    public Class<? extends Entity> getPoType() {
        return poType;
    }

    public boolean isMulti() {
        return isMulti;
    }

    public Map<String, ConditionExpr> getTableConditions() {
        return tableConditions;
    }
}
