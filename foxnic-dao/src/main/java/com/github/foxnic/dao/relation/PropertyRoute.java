package com.github.foxnic.dao.relation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.sql.expr.ConditionExpr;

public class PropertyRoute<S extends Entity,T extends Entity> {

    private Class<? extends Entity> poType;
    private String property;
    private String label;
    private String detail;


    private Class<? extends Entity> targetPoType;
    private ConditionExpr targetCondition;
    private boolean isMulti=true;

    private Map<String,ConditionExpr> tableConditions=new HashMap<>();


    public PropertyRoute(Class<S> poType,String property,Class<T> targetPoType,String label,String detail){
        this.poType=poType;
        this.property=property;
        this.targetPoType=targetPoType;
        this.label=label;
        this.detail=detail;
    }

    /**
     * 对应单个实体，生成一个实体类型的属性
     * */
    public PropertyRoute<S,T> single(){
        this.isMulti=false;
        return this;
    }

    /**
     * 对应多个实体，生成一个 Set 类型的属性
     * */
    public PropertyRoute<S,T> multi(){
        this.isMulti=true;
        return this;
    }
 
 
    /**
     * 增加中间表的条件配置
     * */
    public PropertyRoute<S,T> condition(String table, ConditionExpr condition){
        tableConditions.put(table,condition);
        return this;
    }

    /**
     * 增加中间表的条件配置
     * */
    public PropertyRoute<S,T> condition(String targetTable, String condition,Object... ps) {
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

	public String getLabel() {
		return this.label;
	}
	
	public String getDetail() {
		return this.detail;
	}
	
	public String getSign() {
		String cd=targetCondition==null?"":targetCondition.getSQL();
		String sign=this.poType.getName()+","+this.property+","+label+","+detail+","+targetPoType.getName()+","+cd+","+isMulti+"|";
		for (String table : tableConditions.keySet()) {
			ConditionExpr ce=tableConditions.get(table);
			sign+=table+"="+(ce==null?"":ce.getSQL());
		}
		sign=MD5Util.encrypt32(sign);
		return sign;
	}

	
	private AfterFunction<T> after;
	
	public static interface AfterFunction<T> {
		Set<T> process(Set<T> data);
	}
	
	/**
	 * 设置后处理
	 * */
	public PropertyRoute<S,T> after(AfterFunction<T> func) {
		this.after=func;
		return this;
	}

	public AfterFunction<T> getAfter() {
		return after;
	}
	
}
