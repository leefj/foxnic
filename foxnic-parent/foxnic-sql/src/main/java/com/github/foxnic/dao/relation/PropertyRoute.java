package com.github.foxnic.dao.relation;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.sql.data.ExprRcd;
import com.github.foxnic.sql.entity.EntityUtil;
import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;

import java.lang.reflect.Field;
import java.util.*;

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
    private boolean isList=true;


    private boolean distinct=false;
	private DBField[] fields=null;



	public PropertyRoute(Class<S> sourcePoType, String property, Class<T> targetPoType, String label, String detail){

		try {
			Field field=sourcePoType.getDeclaredField(property);
			if(field.getType().equals(List.class)) {
				this.isList=true;
				if(targetPoType==null) {
					targetPoType = ReflectUtil.getListComponentType(field);
				}
			} else {
				this.isList=false;
				if(targetPoType==null) {
					targetPoType = (Class<T>) field.getType();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

    	this.sourcePoType=sourcePoType;
        this.property=property;
        this.targetPoType=targetPoType;
        this.label=label;
        this.detail=detail;
        this.sourceTable=EntityUtil.getDBTable(sourcePoType);
        this.targetTable=EntityUtil.getDBTable(targetPoType);

    }

	private Map<Integer,Map<String,DynamicValue>> dyConditions = new HashMap<>();

    /**
     * 增加 join 表的查询条件,必须跟随在 join、leftJoin、rightJoin 方法后面
     * */
    public PropertyRoute<S,T> condition(ConditionExpr condition) {
    	Join join=joins.get(joins.size()-1);
		if(join==null || join.getTargetTable()==null) {
			throw new RuntimeException("请在 join 方法后调用");
		}
		join.getTargetPoint().addCondition(condition);
        return this;
    }

    /**
	 * 指定中间表要带出的其它字段
	 * */
	public PropertyRoute<S,T> select(DBField field,String alias) {
		Join join=joins.get(joins.size()-1);
		if(join==null || join.getTargetTable()==null) {
			throw new RuntimeException("请在 join 方法后调用");
		}
		join.getTargetPoint().addSelectFields(field,alias);
		return this;
	}

    /**
     * 增加 join 表的查询条件,必须跟随在 join、leftJoin、rightJoin 方法后面
     * */
    public PropertyRoute<S,T> conditionEquals(DBField field,Object value) {
    	Join join=joins.get(joins.size()-1);
    	if(join==null || join.getTargetTable()==null) {
    		throw new RuntimeException("请在 join 方法后调用");
		}
    	if(!join.getTargetTable().equalsIgnoreCase(field.table().name())) {
			throw new RuntimeException(field.table().name()+"."+field.name()+" 表名与 join 方法中字段的表名不一致");
		}
    	this.condition(new ConditionExpr(field.name()+" = ?",value));
    	return this;
    }
    /**
     * 增加 join 表的查询条件,必须跟随在 join、leftJoin、rightJoin 方法后面
     * */
    public PropertyRoute<S,T> condition(String condition,Object... ps) {
        this.condition(new ConditionExpr(condition,ps));
        return this;
    }

//	public List<ConditionExpr> getConditions(Join join) {
//    	return conditions.get(joins.indexOf(join));
//	}



	/**
	 * 在Join条件中加入动态值
	 * */
	public PropertyRoute<S,T> condition(DBField field, DynamicValue dyValue) {
		Map<String,DynamicValue> map= dyConditions.get(joins.size()-1);
		if(map==null) {
			map=new HashMap<>();
			dyConditions.put(joins.size()-1,map);
		}
		map.put(field.name().toUpperCase(), dyValue);
		return this;
	}

	public Map<String,DynamicValue> getDynamicConditions(Join join) {
		Map<String,DynamicValue> map= dyConditions.get(joins.indexOf(join));
		if(map==null) map = new HashMap<>();
		return map;
	}


    public String getProperty() {
        return property;
    }

    public Class<T> getTargetPoType() {
        return targetPoType;
    }

	public Class<? extends Entity> getSourcePoType() {
        return sourcePoType;
    }

    public boolean isList() {
        return isList;
    }


	public String getLabel() {
		return this.label;
	}

	public String getDetail() {
		return this.detail;
	}

	private AfterFunction<S,T> after;

	public static interface AfterFunction<S,T> {
		List<T> process(S s, List<T> data,Map<Object, ExprRcd> m);
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

	DBField[] getUsingProperties() {
		return joins.get(0).getSourceFields();
	}

	public List<Join> getJoins() {
		List<Join> js=new ArrayList<>(this.joins);
		Collections.reverse(js);
		return js;
	}

	private  List<Join> joins=new ArrayList<>();


	public DBTable validateSameTable(DBField... fields) {
		DBField prev=null;
		for (DBField field : fields) {
			if(prev!=null) {
				if(!prev.table().name().equalsIgnoreCase(field.table().name())) {
					throw new RuntimeException(prev.table().name()+"."+prev.name()+" 与 "+field.table().name()+"."+field.name()+" 表名不一致");
				}
			}
			prev=field;
		}
		return prev.table();
	}
	/**
	 * 指定字段进行 Join，后面跟随 join 方法
	 * */
	public PropertyRoute<S, T> using(DBField... fields) {
		Join prev = null;
		//校验并获得传入字段的数据表
		DBTable table = validateSameTable(fields);
		//如果是第一个 using
		if (joins.isEmpty()) {
			if (!this.getSourceTable().name().equalsIgnoreCase(table.name())) {
				throw new IllegalArgumentException("第一个 using 指定的字段必须属于 "+ this.getSourceTable().name());
			}
		} else {
			//如果不是第一个 using ，则获取前一个 join
			prev=joins.get(joins.size()-1);
		}
		//
		Join join = new Join(fields);
		joins.add(join);
		//
		if(prev!=null) {
			for (ConditionExpr condition : prev.getTargetPoint().getConditions()) {
				join.getSourcePoint().addCondition(condition);
			}
		}
		return this;
	}

	/**
	 * 与 using 配合使用，指定 join 的表字段，跟随在 using 方法后面<br/>
	 * 如果有其他额外条件，后面可跟随 condition 方法
	 * */
	public PropertyRoute<S,T> join(DBField... fields) {
		return join(JoinType.JOIN,fields);
	}

	/**
	 * 与 using 配合使用，指定 join 的表字段，跟随在 using 方法后面, <br/>
	 * 如果有其他额外条件，后面可跟随 condition 方法
	 * */
	public PropertyRoute<S,T> leftJoin(DBField... fields) {
		return join(JoinType.LEFT_JOIN,fields);
	}

	/**
	 * 与 using 配合使用，指定 join 的表字段，跟随在 using 方法后面<br/>
	 * 如果有其他额外条件，后面可跟随 condition 方法
	 * */
	public PropertyRoute<S,T> rightJoin(DBField... fields) {
		return join(JoinType.RIGHT_JOIN,fields);
	}

	private PropertyRoute<S,T> join(JoinType joinType, DBField... fields) {
		Join join=joins.get(joins.size()-1);
		join.setJoinType(joinType);
		join.target(fields);
		return this;
	}


//	private List<DBTable> routeTables=new ArrayList<>();
//	private Map<String,DBField[]> routeFields=new HashMap<>();

//	/**
//	 * 按顺序指定途径的表 , 源表不需要加入<br>
//	 * 逐个指定 Join 的路由
//	 * @param  fields 字段清单，如果指定，则需要和join配置中的顺序一致
//	 * */
//	public PropertyRoute<S,T> addRoute(DBField... fields) {
//		this.addRoute(fields[0].table(), fields);
//		return this;
//	}

//	/**
//	 * 按顺序指定途径的表 , 源表不需要加入<br>
//	 * 逐个指定 Join 的路由
//	 * @param  table 数据表
//	 * @param  fields 字段清单，如果指定，则需要和join配置中的顺序一致
//	 * */
//	public PropertyRoute<S,T> addRoute(DBTable table,DBField... fields) {
//		this.routeTables.add(table);
//		for (DBField f : fields) {
//			if(!table.name().equalsIgnoreCase(f.table().name())) {
//				throw new IllegalArgumentException("字段表与Join表名称不一致,"+f.table().name()+" , "+table);
//			}
//		}
//		this.routeFields.put(table.name(), fields);
//		return this;
//	}


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
	 */
	public PropertyRoute<S, T> addOrderBy(DBField field, boolean asc, boolean nullsLast) {
		if(!field.table().name().equalsIgnoreCase(this.getTargetTable().name())) {
			throw new IllegalArgumentException("只允许 "+this.getTargetTable().name()+" 表字段用于排序");
		}
		this.orderByInfos.add(new OrderByInfo(field.table().name(), field.name(), asc, nullsLast));
		return this;
	}


	List<OrderByInfo> getOrderByInfos() {
		return orderByInfos;
	}

//	List<ConditionExpr> getTableConditions(String table) {
//		List<ConditionExpr> cdrs=new ArrayList<>();
//		for (String t : this.tableConditions.keySet()) {
//			if(t.equals(table)) {
//				cdrs.add(this.tableConditions.get(t));
//			}
//		}
//		return cdrs;
//	}


	private int fork=128;

	/**
	 * 当关联数量大于 count 使用 fork / join 处理 <br/>
	 * 默认值 128
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
	public PropertyRoute<S,T> groupForCount(DBField... fields) {
		groupFor="count(1)";
		groupFields=new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			groupFields[i]=fields[i].name();
		}
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

//	public List<DBTable> getRouteTables() {
//		return routeTables;
//	}

//	public Map<String, DBField[]> getRouteFields() {
//		return routeFields;
//	}

	/**
	 * @param tab 查询字段所在的表名
	 * */
	public static  <S extends Entity,T extends Entity> PropertyRoute merge(List<PropertyRoute> routes,String tab) {
		PropertyRoute prop=null;

		PropertyRoute first=routes.get(0);

		if(routes.size()==1) {
			prop=new PropertyRoute(first.sourcePoType,first.property,first.targetPoType,first.label,first.detail);
		} else {
			PropertyRoute last=routes.get(routes.size()-1);
			prop=new PropertyRoute(first.sourcePoType,first.property,last.targetPoType,first.label,first.detail);
		}

		boolean isList=false;
		for (PropertyRoute route : routes) {
			prop.joins.addAll(route.joins);
			if(route.isList()) {
				isList=true;
			}
		}

		if(prop.joins.size()>1 && !StringUtil.isBlank(tab)) {
			int end = -1;
			for (int i = prop.joins.size() - 1; i >= 0; i--) {
				Join join = (Join) prop.joins.get(i);
				if (join.getTargetTable().equalsIgnoreCase(tab)) {
					end = i;
					break;
				}
			}

			//这里其实不判断也可以，后期看情况拿掉
			if (end == -1) {
				throw new RuntimeException("合并异常,可能是未指定正确的查询字段");
			}

			//移除不必要Join进去的表
			while (prop.joins.size()>(end+1)) {
				prop.joins.remove(prop.joins.size()-1);
			}
		}

		prop.isList=isList;

		return prop;
	}

	/**
	 * 是否 distinct 查询
	 * */
	public PropertyRoute<S,T> distinct() {
		this.distinct=true;
		return this;
	}

	public boolean isDistinct() {
		return distinct;
	}


	/**
	 * 指定需要查询的字段
	 * */
	public PropertyRoute<S,T> fields(DBField... fields) {

		for (DBField field : fields) {
			if(field==null) throw new IllegalArgumentException("不允许指定 null");
			if(!this.getTargetTable().name().equals(field.table().name())) {
				throw new IllegalArgumentException("数据表不一致，要求 "+this.getTargetTable().name()+" , 传入 "+field.table().name());
			}
		}
		this.fields=fields;
		return this;
	}

	public DBField[] getFields() {
		return fields;
	}
}
