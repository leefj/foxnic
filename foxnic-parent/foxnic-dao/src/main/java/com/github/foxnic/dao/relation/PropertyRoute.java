package com.github.foxnic.dao.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.sql.entity.EntityUtil;
import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;

public class PropertyRoute<S extends Entity,T extends Entity> {

	public static enum DynamicValue {
		/**
		 * 当前登录账户
		 * */
		LOGIN_USER_ID;
	}
	
    private Class<? extends Entity> sourcePoType;
    private DBTable sourceTable;
    private String property;
    private String label;
    private String detail;


    private Class<T> targetPoType;
    private DBTable targetTable;
    private boolean isMulti=true;

    private Map<String,ConditionExpr> tableConditions=new HashMap<>();


    public PropertyRoute(Class<S> sourcePoType,String property,Class<T> targetPoType,String label,String detail){
        this.sourcePoType=sourcePoType;
        this.property=property;
        this.targetPoType=targetPoType;
        this.label=label;
        this.detail=detail;
        this.sourceTable=EntityUtil.getDBTable(sourcePoType);
        this.targetTable=EntityUtil.getDBTable(targetPoType);
        this.routeTables.add(EntityUtil.getDBTable(this.sourcePoType));
        this.routeFields.put(this.sourceTable.name(),null);
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
    public PropertyRoute<S,T> addCondition(DBTable table, ConditionExpr condition){
        tableConditions.put(table.name(),condition);
        return this;
    }

    /**
     * 增加中间表的条件配置
     * */
    public PropertyRoute<S,T> addConditionEquals(DBField field,Object value) {
    	this.addCondition(field.table(),new ConditionExpr(field+" = ?",value));
    	return this;
    }
    /**
     * 增加中间表的条件配置
     * */
    public PropertyRoute<S,T> addCondition(DBTable table, String condition,Object... ps) {
        this.addCondition(table,new ConditionExpr(condition,ps));
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

	private DBField[] usingProperties;
	
	/**
	 * 指定用于关联的属性清单
	 * */
	public PropertyRoute<S,T> using(DBField... props) {
		this.usingProperties=props;
		return this;
	}
	
 
 
	DBField[] getUsingProperties() {
		return usingProperties;
	}
	
	private List<DBTable> routeTables=new ArrayList<>();
	private Map<String,DBField[]> routeFields=new HashMap<>();
 
	/**
	 * 按顺序指定途径的表 , 源表不需要加入<br>
	 * 逐个指定 Join 的路由
	 * @param  fields 字段清单，如果指定，则需要和join配置中的顺序一致
	 * */
	public PropertyRoute<S,T> addRoute(DBField... fields) {
		this.addRoute(fields[0].table(), fields);
		return this;
	}
	
	/**
	 * 按顺序指定途径的表 , 源表不需要加入<br>
	 * 逐个指定 Join 的路由
	 * @param  table 数据表
	 * @param  fields 字段清单，如果指定，则需要和join配置中的顺序一致
	 * */
	public PropertyRoute<S,T> addRoute(DBTable table,DBField... fields) {
		this.routeTables.add(table);
		for (DBField f : fields) {
			if(!table.name().equalsIgnoreCase(f.table().name())) {
				throw new IllegalArgumentException("字段表与Join表名称不一致,"+f.table().name()+" , "+table);
			}
		}
		this.routeFields.put(table.name(), fields);
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
	public PropertyRoute<S,T> addOrderBy(DBField field, boolean asc, boolean nullsLast) {
		 this.orderByInfos.add(new OrderByInfo(field.table().name(), field.name(), asc, nullsLast));
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

	public DBTable getSourceTable() {
		return sourceTable;
	}

	public DBTable getTargetTable() {
		return targetTable;
	}

	public List<DBTable> getRouteTables() {
		return routeTables;
	}

	public Map<String, DBField[]> getRouteFields() {
		return routeFields;
	}
	
	private Map<String,Map<String,DynamicValue>> dynamicConditions = new HashMap<>();

	/**
	 * 在Join条件中加入动态值
	 * */
	public PropertyRoute<S,T> condition(DBField field, DynamicValue dyValue) {
		Map<String,DynamicValue> map=dynamicConditions.get(field.table().name());
		if(map==null) {
			map=new HashMap<>();
			dynamicConditions.put(field.table().name().toUpperCase(),map);
		}
		map.put(field.name().toUpperCase(), dyValue);
		return this;
	}

	public Map<String,DynamicValue> getDynamicConditions(String table) {
		return dynamicConditions.get(table.toUpperCase());
	}

}