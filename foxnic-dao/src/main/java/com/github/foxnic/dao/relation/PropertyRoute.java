package com.github.foxnic.dao.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.sql.expr.ConditionExpr;

public class PropertyRoute<S extends Entity,T extends Entity> {

    private Class<? extends Entity> poType;
    private String property;
    private String label;
    private String detail;


    private Class<T> targetPoType;
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

    public Class<T> getTargetPoType() {
        return targetPoType;
    }
 
    Class<? extends Entity> getPoType() {
        return poType;
    }

    public boolean isMulti() {
        return isMulti;
    }

    Map<String, ConditionExpr> getTableConditions() {
        return tableConditions;
    }

	public String getLabel() {
		return this.label;
	}
	
	public String getDetail() {
		return this.detail;
	}
	
	public String getSign() {
		String sign=this.poType.getName()+","+this.property+","+label+","+detail+","+targetPoType.getName()+","+isMulti+"|";
		for (String table : tableConditions.keySet()) {
			ConditionExpr ce=tableConditions.get(table);
			sign+=table+"="+(ce==null?"":ce.getSQL());
		}
		sign=MD5Util.encrypt32(sign);
		return sign;
	}

	
	private AfterFunction<S,T> after;
	
	public static interface AfterFunction<S,T> {
		List<T> process(S s, List<T> data);
	}
	
	/**
	 * 设置后处理
	 * */
	public PropertyRoute<S,T> after(AfterFunction<S,T> func) {
		this.after=func;
		return this;
	}

	AfterFunction<S,T> getAfter() {
		return after;
	}

	private String[] usingProperties;
	
	/**
	 * 指定用于关联的属性清单
	 * */
	public PropertyRoute<S,T> using(String... props) {
		this.usingProperties=props;
		return this;
	}

	
	String[] getUsingProperties() {
		return usingProperties;
	}

	
	public static class OrderByInfo {
		
		private String tableName;
		private String field;
		private boolean asc;
		private boolean nullsLast;
		
		public OrderByInfo(String tableName, String field, boolean asc, boolean nullsLast) {
			this.tableName=tableName;
			this.field=field;
			this.asc=asc;
			this.nullsLast=nullsLast;
		}

		public String getTableName() {
			return tableName;
		}

		public String getField() {
			return field;
		}

		public boolean isAsc() {
			return asc;
		}

		public boolean isNullsLast() {
			return nullsLast;
		}
		
		
	}
	
	private List<OrderByInfo> orderByInfos=new ArrayList<>();
 
	/**
	 * 添加排序
	 * */
	public void orderBy(String tableName, String field, boolean asc, boolean nullsLast) {
		 this.orderByInfos.add(new OrderByInfo(tableName, field, asc, nullsLast));
	}

	List<OrderByInfo> getOrderByInfos() {
		return orderByInfos;
	}

	List<ConditionExpr> getTableConditions(String table) {
		List<ConditionExpr> cdrs=new ArrayList<>();
		for (String t : this.tableConditions.keySet()) {
			if(t.equals(table)) {
				cdrs.add(this.tableConditions.get(t));
			}
		}
		return cdrs;
	}

	
	private int fork=-1;
	
	/**
	 * 当关联数量大于 count 使用 fork / join 处理
	 * */
	public PropertyRoute<S,T> fork(int count) {
		if(count<1) {
			throw new IllegalArgumentException("不允许小于1");
		}
		this.fork=count;
		return this;
	}

	int getFork() {
		return fork;
	}
	
}
