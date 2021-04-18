package com.github.foxnic.dao.relation;

import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.sql.entity.EntityUtil;
import com.github.foxnic.sql.expr.ConditionExpr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyRoute<S extends Entity,T extends Entity> {

    private Class<? extends Entity> sourcePoType;
    private String sourceTable;
    private String property;
    private String label;
    private String detail;


    private Class<T> targetPoType;
    private String targetTable;
    private boolean isMulti=true;

    private Map<String,ConditionExpr> tableConditions=new HashMap<>();


    public PropertyRoute(Class<S> sourcePoType,String property,Class<T> targetPoType,String label,String detail){
        this.sourcePoType=sourcePoType;
        this.property=property;
        this.targetPoType=targetPoType;
        this.label=label;
        this.detail=detail;
        this.sourceTable=EntityUtil.getAnnotationTable(sourcePoType);
        this.targetTable=EntityUtil.getAnnotationTable(targetPoType);
        this.routeTables.add(this.sourceTable);
        this.routeFields.put(this.sourceTable,null);
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
 
    Class<? extends Entity> getSourcePoType() {
        return sourcePoType;
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
		String sign=this.sourcePoType.getName()+","+this.property+","+label+","+detail+","+targetPoType.getName()+","+isMulti+"|";
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
	
	private List<String> routeTables=new ArrayList<>();
	private Map<String,String[]> routeFields=new HashMap<>();
	
	/**
	 * 按顺序指定途径的表 , 源表不需要加入<br>
	 * 逐个指定 Join 的路由
	 * @param  cls 实体类，用于获得对应的表名
	 * @param  fields 字段清单，如果指定，则需要和join配置中的顺序一致
	 *
	 * */
	public PropertyRoute<S,T> addRoute(Class<? extends Entity> cls,String... fields) {
		return addRoute(EntityUtil.getAnnotationTable(cls),fields);
	}
	
	/**
	 * 按顺序指定途径的表 , 源表不需要加入<br>
	 * 逐个指定 Join 的路由
	 * @param  table 数据表
	 * @param  fields 字段清单，如果指定，则需要和join配置中的顺序一致
	 * */
	public PropertyRoute<S,T> addRoute(String table,String... fields) {
		this.routeTables.add(table);
		this.routeFields.put(table, fields);
		return this;
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
	 * 添加排序 , 调用多次则添加多个字段的排序
	 * */
	public PropertyRoute<S,T> addOrderBy(String tableName, String field, boolean asc, boolean nullsLast) {
		 this.orderByInfos.add(new OrderByInfo(tableName, field, asc, nullsLast));
		 return this;
	}
	
	/**
	 * 添加排序 , 调用多次则添加多个字段的排序
	 * */
	public PropertyRoute<S,T> addOrderBy(Class<? extends Entity> entityType, String field, boolean asc, boolean nullsLast) {
		 this.orderByInfos.add(new OrderByInfo(EntityUtil.getAnnotationTable(entityType), field, asc, nullsLast)); 
		 return this;
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

	private String groupFor;
	private String[] groupFields=new String[0];
	
	/**
	 * 按指定字段分组汇总，未指定字段时
	 * */
	public PropertyRoute<S,T> groupForCount(String... fields) {
		groupFor="count(1)";
		groupFields=fields;
		return this;
	}

	public String getGroupFor() {
		return groupFor;
	}

	/**
	 * 属性类型
	 * */
	private Class type;
	
	/**
	 * 指定属性类型
	 * */
	public PropertyRoute<S,T> type(Class type) {
		this.type=type;
		return this;
	}

	public Class getType() {
		if(this.type!=null) {
			return type;
		}
		return this.getTargetPoType();
	}

	public String[] getGroupFields() {
		return groupFields;
	}

	private boolean isIgnoreJoin=false;
	
	/**
	 * 忽略，不Join，只是一个属性而已
	 * */
	public void ignoreJoin() {
		isIgnoreJoin=true;
	}
	
	public boolean isIgnoreJoin() {
		return isIgnoreJoin;
	}

	public String getSourceTable() {
		return sourceTable;
	}

	public String getTargetTable() {
		return targetTable;
	}

	public List<String> getRouteTables() {
		return routeTables;
	}

	public Map<String, String[]> getRouteFields() {
		return routeFields;
	}


	
	
 
	
}
