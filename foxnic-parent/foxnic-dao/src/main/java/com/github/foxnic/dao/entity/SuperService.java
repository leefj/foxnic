package com.github.foxnic.dao.entity;

import com.github.foxnic.api.error.CommonError;
import com.github.foxnic.api.error.ErrorDesc;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.cache.DoubleCache;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.excel.ExcelReader;
import com.github.foxnic.dao.excel.ExcelStructure;
import com.github.foxnic.dao.excel.ExcelWriter;
import com.github.foxnic.dao.excel.ValidateResult;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.relation.JoinResult;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.dao.sql.SQLBuilder;
import com.github.foxnic.sql.entity.EntityUtil;
import com.github.foxnic.sql.expr.*;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.treaty.DBTreaty;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public abstract class SuperService<E extends Entity> implements ISuperService<E> {

	/**
	 * 数据表默认别名
	 * */
	public  static  final String TABLE_ALAIS="t";

	/**
	 * 获得 DAO 对象
	 * */
	abstract public DAO dao();

	private  DBTableMeta dbTableMeta;
	private  List<DBColumnMeta> pkColumns;
	/**
	 * 获得数据表对应的 DBTableMeta
	 * */
	public DBTableMeta getDBTableMeta() {
		if(dbTableMeta!=null) return dbTableMeta;
		dbTableMeta=dao().getTableMeta(this.table());
		return dbTableMeta;
	}

	/**
	 * 获得数据表对应的 主键列
	 * */
	public List<DBColumnMeta> getPKColumns() {
		if(pkColumns!=null) return pkColumns;
		pkColumns=dbTableMeta.getPKColumns();
		return pkColumns;
	}

	private boolean enableCache=false;

	private Map<String, CacheStrategy> cacheStrategies =new HashMap<>();

	public void registCacheStrategy(String name,boolean isAccurate,boolean cacheEmptyResult,String... conditionProperty) {
		if(cacheStrategies.containsKey(name)) {
			throw new IllegalArgumentException("缓存策略 "+name+" 重复定义");
		}
		cacheStrategies.put(name,new CacheStrategy(name,isAccurate,cacheEmptyResult,conditionProperty));
	}

	public CacheStrategy getCacheStrategy(String methodName) {
		return cacheStrategies.get(methodName);
	}
	private DoubleCache<String,Object> cache=null;

	/**
	 * 获得二级缓存(本地缓存+远程缓存)，如果远程缓存不可用，就使用本地缓存
	 * */
	public DoubleCache<String,Object> cache() {
		if(this.cache!=null) return this.cache;
		this.cache=(DoubleCache<String,Object>)dao().getDataCacheManager().defineEntityCache(this.getClass(),1024,-1);
		return cache;
	}

	/**
	 * 使匹配到的精准缓存失效
	 * */
	public void invalidateAccurateCache(E entity){
		if(entity==null) return;
		if(this.cache()==null) return;
		String key=null;
		for (CacheStrategy cacheStrategy : cacheStrategies.values()) {
			if(!cacheStrategy.isAccurate()) continue;
			key=cacheStrategy.makeKey(entity);
			this.cache().remove(key);
			this.cache().removeKeyStarts(key);
		}
	}

	public void invalidateAccurateCache(List<E> entity){
		for (E e : entity) {
			this.invalidateAccurateCache(e);
		}
	}

	/**
	 * 生成ID，覆盖方法实现
	 * */
	public Object generateId(Field field) { return null; };


	private String table=null;

	private Class<? extends E> poType;

	/**
	 * 数据表
	 * */
	public String table() {
		init();
		return table;
	}

	/**
	 * PO 类型
	 * */
	public Class<? extends E> getPoType() {
		init();
		return poType;
	}

	private void init() {
		if(table!=null) return;
		ParameterizedType type=(ParameterizedType)this.getClass().getGenericSuperclass();
		Type[] types=type.getActualTypeArguments();
		poType=(Class)types[0];
		table=EntityUtil.getAnnotationTable(poType);
	}

	public List<E> queryList(E sample) {
		OrderBy orderBy = buildOrderBy(sample);
		return queryList(sample,null,orderBy);
	}

	public List<E> queryList(E sample,OrderBy orderBy) {
		return queryList(sample,null,orderBy);
	}



	public List<E> queryList(E sample,ConditionExpr condition) {
		OrderBy orderBy = buildOrderBy(sample);
		return queryList(sample,condition,orderBy);
	}

	public List<E> queryList(String condition,Object... ps) {
		return queryList(new ConditionExpr(condition,ps),null);
	}

	public List<E> queryList(ConditionExpr condition) {
		return queryList(condition,null);
	}

	public List<E> queryList(ConditionExpr condition,OrderBy orderBy) {
		Expr expr=new Expr("select * from "+this.table()+" "+TABLE_ALAIS);
		if(condition!=null) {
			condition.startWithWhere();
			expr.append(condition);
		}
		//
		ConditionExpr conditionExpr=buildDBTreatyCondition(TABLE_ALAIS);
		expr.append(conditionExpr);
		if(orderBy!=null) {
			expr.append(orderBy);
		}
		return (List<E>)dao().queryEntities(this.poType,expr);
	}
	/**
	 * 查询全部符合条件的数据
	 *
	 * @param sample 查询条件
	 * @return 查询结果 , News清单
	 */
	public List<E> queryList(E sample,ConditionExpr condition,OrderBy orderBy) {

		Expr select=this.buildQuerySQL(sample,TABLE_ALAIS,condition,orderBy);
//		String tableAlias="t";
//		//构建查询条件
//		ConditionExpr ce = buildQueryCondition(sample,tableAlias);
//
//		Expr select=new Expr("select * from "+table()+" "+tableAlias);
//		select.append(ce.startWithWhere());
//		if(condition!=null) {
//			if(ce!=null && !ce.isEmpty()) {
//				select.append(condition.startWithAnd());
//			} else {
//				select.append(condition.startWithWhere());
//			}
//		}
//		if(orderBy==null) {
//			DBColumnMeta cm=dao().getTableColumnMeta(table(), dao().getDBTreaty().getCreateTimeField());
//			if(cm!=null) {
//				orderBy=OrderBy.byDescNullsLast(cm.getColumn());
//			}
//		}
//		if(orderBy!=null) {
//			select.append(orderBy);
//		}
		return dao().queryEntities((Class<E>)sample.getClass(),select);
	}

	/**
	 * 查询符合条件的数据,并返回第一个，如果没有则返回 null
	 *
	 * @param sample 查询条件
	 * @return 查询结果 , News清单
	 */
	public E queryEntity(E sample) {
		//设置删除标记
		dao().getDBTreaty().updateDeletedFieldIf(sample,false);
		DBTableMeta tm=getDBTableMeta();
		if(tm.isColumnExists(dao().getDBTreaty().getTenantIdField())) {
			BeanUtil.setFieldValue(sample,dao().getDBTreaty().getTenantIdField(),dao().getDBTreaty().getActivedTenantId());
		}
		List<E> list=dao().queryEntities(sample);
		if(list.size()==0) return null;
		return list.get(0);
	}

	/**
	 * 查询符合条件的数据,并返回第一个，如果没有则返回 null
	 *
	 * @param condition 查询条件
	 * @return 查询结果 , News清单
	 */
	public E queryEntity(ConditionExpr condition) {
		if(condition!=null) {
			DBTableMeta tm=this.getDBTableMeta();
			if(tm.isColumnExists(this.dao().getDBTreaty().getDeletedField())) {
				condition.and(this.dao().getDBTreaty().getDeletedField()+" = ?",this.dao().getDBTreaty().getFalseValue());
			}
			if(tm.isColumnExists(this.dao().getDBTreaty().getTenantIdField())) {
				condition.and(this.dao().getDBTreaty().getTenantIdField()+" = ?",this.dao().getDBTreaty().getActivedTenantId());
			}
		}
		List list=dao().queryEntities(getPoType(), condition);
		if(list==null || list.isEmpty()) return null;
		return (E)list.get(0);
	}

	/**
	 * 查询符合条件的数据,并返回第一个，如果没有则返回 null
	 *
	 * @param condition 查询条件
	 * @param ps 参数列表
	 * @return 查询结果 , News清单
	 */
	public E queryEntity(String condition,Object... ps) {
		return this.queryEntity(new ConditionExpr(condition,ps));
	}


	/**
	 * 分页查询符合条件的数据
	 *
	 * @param sample 查询条件
	 * @param condition 额外的查询条件
	 * @return 查询结果 , 数据清单
	 */
	@Override
	public PagedList<E> queryPagedList(E sample,ConditionExpr condition,int pageSize,int pageIndex) {
		OrderBy orderBy = buildOrderBy(sample);
		return queryPagedList(sample, condition, orderBy, pageSize, pageIndex);
	}

	/**
	 * 分页查询符合条件的数据
	 *
	 * @param sample 查询条件
	 * @param pageSize 分页大小
	 * @param pageIndex 页码
	 * @return 查询结果 , 数据清单
	 */
	@Override
	public PagedList<E> queryPagedList(E sample,int pageSize,int pageIndex) {
		OrderBy orderBy = buildOrderBy(sample);
		return queryPagedList(sample, null, orderBy, pageSize, pageIndex);
	}

	/**
	 * 根据实体数构建默认的条件表达式，不支持 Join 其它表
	 * @param sample 数据样例
	 * @return ConditionExpr 条件表达式
	 * */
	public ConditionExpr buildQueryCondition(E sample){
		QuerySQLBuilder builder=new QuerySQLBuilder(this);
		return builder.buildLocalCondition(sample,TABLE_ALAIS);
	}

	/**
	 * 根据实体数构建默认的条件表达式, 不支持 Join 其它表
	 * @param sample 数据样例
	 * @param tableAlias 数据表别名
	 * 	@return ConditionExpr 条件表达式
	 * */
	public ConditionExpr buildQueryCondition(E sample,String tableAlias) {
		QuerySQLBuilder builder=new QuerySQLBuilder(this);
		return builder.buildLocalCondition(sample,tableAlias);
	}


	public Expr buildQuerySQL(E sample, String tabAlias,ConditionExpr conditionExpr,OrderBy orderBy) {
		QuerySQLBuilder builder=new QuerySQLBuilder(this);
		return builder.build(sample,tabAlias,conditionExpr,orderBy);
	}

	public ConditionExpr buildDBTreatyCondition(String tableAlias) {
		QuerySQLBuilder builder=new QuerySQLBuilder(this);
		return builder.buildDBTreatyCondition(tableAlias);
	}

	public OrderBy buildOrderBy(E sample) {
		QuerySQLBuilder builder=new QuerySQLBuilder(this);
		return builder.buildOrderBy(sample,TABLE_ALAIS);
	}

	public OrderBy buildOrderBy(E sample,String tabAlias) {
		QuerySQLBuilder builder=new QuerySQLBuilder(this);
		return builder.buildOrderBy(sample,tabAlias);
	}

	/**
	 * 分页查询符合条件的数据
	 *
	 * @param sample 查询条件
	 * @param orderBy 排序
	 * @return 查询结果 , 数据清单
	 */
	@Override
	public PagedList<E> queryPagedList(E sample,OrderBy orderBy,int pageSize,int pageIndex) {
		return queryPagedList(sample, null, orderBy, pageSize, pageIndex);
	}


	/**
	 * 分页查询符合条件的数据
	 *
	 * @param sample 查询条件
	 * @param condition 额外的查询条件
	 * @param orderBy 排序
	 * @return 查询结果 , 数据清单
	 */
	@Override
	public PagedList<E> queryPagedList(E sample,ConditionExpr condition,OrderBy orderBy,int pageSize,int pageIndex) {

		if(orderBy==null) {
			DBColumnMeta cm=dao().getTableColumnMeta(table(), dao().getDBTreaty().getCreateTimeField());
			if(cm!=null) {
				orderBy=OrderBy.byDesc(TABLE_ALAIS+"."+cm.getColumn());
			}
		}

		Expr select=buildQuerySQL(sample,TABLE_ALAIS,condition,orderBy);

//		String tableAlais="t";
//		//设置删除标记
//		dao().getDBTreaty().updateDeletedFieldIf(sample,false);
//		//构建查询条件
//		ConditionExpr ce = buildQueryCondition(sample,tableAlais);
//
//		DBColumnMeta cm=null;
//
//		Expr select=new Expr("select * from "+table()+" "+tableAlais);
//		select.append(ce.startWithWhere());
//		if(condition!=null) {
//			select.append(condition.startWithAnd());
//		}
//
//		if(orderBy==null) {
//			cm=dao().getTableColumnMeta(table(), dao().getDBTreaty().getCreateTimeField());
//			if(cm!=null) {
//				orderBy=OrderBy.byDesc(cm.getColumn());
//			}
//		}
//
//		if(orderBy!=null) {
//			select.append(orderBy);
//		}
		//执行查询
		return dao().queryPagedEntities((Class<E>)sample.getClass(), pageSize,pageIndex, select);
	}

//	/**
//	 * 根据实体数构建默认的条件表达式
//	 * @param sample 数据样例
//	 * @return ConditionExpr 条件表达式
//	 * */
//	public ConditionExpr buildQueryCondition(E sample) {
//		return buildQueryCondition(sample,"t");
//	}

//	/**
//	 * 根据实体数构建默认的条件表达式
//	 * @param sample 数据样例
//	 * @param tableAliase 数据表别名
//	 * @return ConditionExpr 条件表达式
//	 * */
//	public ConditionExpr buildQueryCondition(E sample,String tableAliase) {
//		// 设置默认搜索
//		String fuzzyField=BeanUtil.getFieldValue(sample, "fuzzyField",String.class);
//		Set<String> fuzzyFields=null;
//		if(!StringUtil.isBlank(fuzzyField)) {
//			String[] arr=fuzzyField.split(",");
//			fuzzyFields=new HashSet<>();
//			for (String s : arr) {
//				try {
//					fuzzyFields.add(BeanNameUtil.instance().depart(s).toLowerCase());
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		String searchField=BeanUtil.getFieldValue(sample, "searchField",String.class);
//		String searchValue=BeanUtil.getFieldValue(sample, "searchValue",String.class);
//		if(searchValue!=null) searchValue=searchValue.trim();
//		//复合查询模式
//		if("$composite".equals(searchField)) {
//			return buildFuzzyFieldsQueryCondition(sample,searchValue,fuzzyFields,tableAliase);
//		} else {
//			return buildSimpleQueryCondition(sample, tableAliase, searchField, searchValue, fuzzyFields);
//		}
//	}

//	private ConditionExpr buildSimpleQueryCondition(E sample,String tableAliase,String searchField,String searchValue,Set<String> fuzzyFields) {
//
//		ConditionExpr ce=new ConditionExpr();
//
//		if(!StringUtil.isBlank(tableAliase)) {
//			tableAliase=StringUtil.trim(tableAliase, ".");
//			tableAliase=tableAliase+".";
//		} else {
//			tableAliase="";
//		}
//
//		//加入逻辑删除判断
//		String deletedField=dao().getDBTreaty().getDeletedField();
//		DBColumnMeta dcm=dao().getTableMeta(this.table()).getColumn(deletedField);
//		Object deletedValue=BeanUtil.getFieldValue(sample,deletedField);
//		if(dcm!=null && deletedValue==null) {
//			ce.and(tableAliase+deletedField+"= ?",dao().getDBTreaty().getFalseValue());
//		}
//		String tenantField=dao().getDBTreaty().getTenantIdField();
//		DBColumnMeta tenantColumn=dao().getTableMeta(this.table()).getColumn(tenantField);
//		if(tenantColumn!=null) {
//			ce.and(tableAliase+tenantField+"= ?",dao().getDBTreaty().getActivedTenantId());
//		}
//
//
//
//		Object value=null;
//
//
//		String[] searchFields=null;
//		if(!StringUtil.isBlank(searchField)) {
//			searchFields=searchField.split(",");
//		}
//
//		if(searchFields!=null && searchFields.length==1) {
//			String field = searchFields[0];
//			if (!StringUtil.isBlank(field) && !StringUtil.isBlank(searchValue)) {
//				BeanUtil.setFieldValue(sample, field, searchValue);
//			}
//		}
//
//		DBTableMeta tm=dao().getTableMeta(this.table());
//		List<DBColumnMeta> cms= tm.getColumns();
//
//		// 按属性设置默认搜索
//		for (DBColumnMeta cm : cms) {
//			value=BeanUtil.getFieldValue(sample, cm.getColumn());
//			if(value==null) continue;
//
//			if(cm.getDBDataType()==DBDataType.STRING
//					|| cm.getDBDataType()==DBDataType.CLOB) {
//				if(fuzzyFields!=null && fuzzyFields.contains(cm.getColumn().toLowerCase())) {
//					ConditionExpr ors=buildFuzzyConditionExpr(cm.getColumn(),value.toString(),tableAliase);
//					if(ors!=null && !ors.isEmpty()) {
//						ce.and(ors);
//					}
//				} else {
//					ce.and(tableAliase+cm.getColumn()+" = ?", value.toString());
//				}
//			} else if(cm.getDBDataType()==DBDataType.BOOL) {
//				if(dao().getDBTreaty().isAutoCastLogicField() && DataParser.isBooleanType(value)) {
//					Boolean bool=DataParser.parseBoolean(value);
//					value=dao().getDBTreatyLogicValue(bool);
//				}
//				ce.and(tableAliase+cm.getColumn()+" = ?", value);
//			}
//			else {
//				ce.and(tableAliase+cm.getColumn()+" = ?", value);
//			}
//		}
//
//		if(searchFields!=null && searchFields.length>1) {
//			ConditionExpr ors=new ConditionExpr();
//			for (String field : searchFields) {
//				if (!StringUtil.isBlank(field) && !StringUtil.isBlank(searchValue)) {
//					DBColumnMeta cm=tm.getColumn(field);
//					if(cm==null) {
//						field=BeanNameUtil.instance().depart(field);
//						cm=tm.getColumn(field);
//					}
//					if(cm!=null) {
//						if(cm.getDBDataType()==DBDataType.STRING || cm.getDBDataType()==DBDataType.CLOB ) {
//							if(fuzzyFields!=null && fuzzyFields.contains(cm.getColumn().toLowerCase())) {
//								ors.or(tableAliase + cm.getColumn() + " like ?", "%" + searchValue + "%");
//							} else {
//								ce.and(tableAliase+cm.getColumn()+" = ?", searchValue.toString());
//							}
//						}
//					}
//				}
//			}
//			if(!ors.isEmpty()){
//				ce.and(ors);
//			}
//		}
//
//		return ce;
//
//	}

//	private ConditionExpr buildFuzzyConditionExpr(String filed, String value,String prefix) {
//		if(StringUtil.isBlank(value)) return null;
//		value=value.trim();
//		value=value.replace("\t"," ");
//		value=value.replace("\r"," ");
//		value=value.replace("\n"," ");
//		String[] vs=value.split(" ");
//		ConditionExpr ors=new ConditionExpr();
//		for (String v : vs) {
//			ors.orLike(prefix+filed,v);
//		}
//		ors.startWithSpace();
//		return ors;
//	}

//	private ConditionExpr buildFuzzyFieldsQueryCondition(E sample,String searchValue, Set<String> fuzzyFields, String tableAliase){
//		//设置表名前缀
//		String prefix="";
//		if(!StringUtil.isBlank(tableAliase)) prefix=tableAliase+".";
//		//
//		DBTableMeta tm=dao().getTableMeta(this.table());
//		ConditionExpr conditionExpr=new ConditionExpr();
//
//		CompositeParameter compositeParameter=new CompositeParameter(searchValue,BeanUtil.toMap(sample));
//
//		String tab=null;
//		for (CompositeItem item : compositeParameter) {
//			//优先使用明确指定的查询字段
//			String field=item.getField();
//			//如未明确指定，则使用key作为查询字段
//			if(StringUtil.isBlank(field)) {
//				field=item.getKey();
//			}
//			tab=null;
//			if(field.contains(".")) {
//				String[] tmp=field.split("\\.");
//				tab=tmp[0];
//				field=tmp[1];
//			}
//			field=BeanNameUtil.instance().depart(field);
//			//获得字段Meta
//			DBColumnMeta cm = null;
//			if(tab==null || tab.equalsIgnoreCase(this.table)) {
//				cm=tm.getColumn(field);
//			}
//
//			field=prefix+field;
//			//如果字段在当前表不存在，则使用已关联的外部表查询
//			if(cm==null) {
//					conditionExpr = makeJoinedTableCondition(conditionExpr,item,tableAliase);
//			}
//			//如果字段已经存在，则使用当前表字段查询
//			else {
//				conditionExpr = makeCurrentTableCondition(conditionExpr,field,cm,item,fuzzyFields,prefix);
//			}
//		}
//
//		//加入逻辑删除条件
//		DBColumnMeta delColumn=tm.getColumn(dao().getDBTreaty().getDeletedField());
//		if(delColumn!=null) {
//			conditionExpr.and(prefix+delColumn.getColumn()+"=?",dao().getDBTreaty().getFalseValue());
//		}
//		//加入租户条件
//		DBColumnMeta tenantColumn=tm.getColumn(dao().getDBTreaty().getTenantIdField());
//		if(tenantColumn!=null) {
//			conditionExpr.and(prefix+tenantColumn.getColumn()+"=?",dao().getDBTreaty().getActivedTenantId());
//		}
//		return  conditionExpr;
//	}

//	private ConditionExpr makeCurrentTableCondition(ConditionExpr conditionExpr,String field,DBColumnMeta cm,CompositeItem item, Set<String> fuzzyFields,String prefix){
//
//		Boolean fuzzy=item.getFuzzy();
//		if(fuzzy==null) fuzzy=false;
//
//		Object fieldValue=item.getValue();
//		Object beginValue=item.getBegin();
//		Object endValue=item.getEnd();
//		String valuePrefix=item.getValuePrefix();
//		if(valuePrefix==null) valuePrefix="";
//		String valueSuffix=item.getValueSuffix();
//		if(valueSuffix==null) valueSuffix="";
//
//		//1.单值匹配
//		if (fieldValue != null && beginValue == null && endValue == null) {
//			if ((fieldValue instanceof List)) {
//				if (fuzzy || (fuzzyFields != null && fuzzyFields.contains(cm.getColumn().toLowerCase()))) {
//					List<String> list = (List) fieldValue;
//					ConditionExpr listOr = new ConditionExpr();
//					for (String itm : list) {
//						ConditionExpr ors = buildFuzzyConditionExpr(cm.getColumn(), valuePrefix + itm.toString() + valueSuffix, prefix);
//						if (ors != null && !ors.isEmpty()) {
//							listOr.or(ors);
//						}
//					}
//					conditionExpr.and(listOr);
//				} else {
//					if (!((List) fieldValue).isEmpty()) {
//						In in = new In(field, (List) fieldValue);
//						conditionExpr.and(in);
//					}
//				}
//			} else {
//				if (cm.getDBDataType() == DBDataType.STRING
//						|| cm.getDBDataType() == DBDataType.CLOB) {
//					if (!StringUtil.isBlank(fieldValue)) {
//						if (fuzzy || (fuzzyFields != null && fuzzyFields.contains(cm.getColumn().toLowerCase()))) {
//							ConditionExpr ors = buildFuzzyConditionExpr(cm.getColumn(), valuePrefix + fieldValue.toString() + valueSuffix, prefix);
//							if (ors != null && !ors.isEmpty()) {
//								conditionExpr.and(ors);
//							}
//						} else {
//							conditionExpr.andEquals(field, fieldValue);
//						}
//					}
//				} else {
//					fieldValue = DataParser.parse(cm.getDBDataType().getType(), fieldValue);
//					conditionExpr.andEquals(field, fieldValue);
//				}
//			}
//		}
//		//2.范围匹配
//		else if (fieldValue == null && (beginValue != null || endValue != null)) {
//
//			if (cm.getDBDataType() == DBDataType.DATE) {
//				Date beginDate = DataParser.parseDate(beginValue);
//				Date endDate = DataParser.parseDate(endValue);
//				//必要时交换位置
//				if (beginDate != null && endDate != null && beginDate.getTime() > endDate.getTime()) {
//					Date tmp = beginDate;
//					beginDate = endDate;
//					endDate = tmp;
//				}
//				//
//				conditionExpr.andIf(field + " >= ?", beginDate);
//				conditionExpr.andIf(field + " <= ?", endDate);
//			} else if (cm.getDBDataType() == DBDataType.TIMESTAME) {
//				Timestamp beginDate = DataParser.parseTimestamp(beginValue);
//				Timestamp endDate = DataParser.parseTimestamp(endValue);
//				//必要时交换位置
//				if (beginDate != null && endDate != null && beginDate.getTime() > endDate.getTime()) {
//					Timestamp tmp = beginDate;
//					beginDate = endDate;
//					endDate = tmp;
//				}
//				//
//				conditionExpr.andIf(field + " >= ?", beginDate);
//				conditionExpr.andIf(field + " <= ?", endDate);
//			} else if (cm.getDBDataType() == DBDataType.INTEGER
//					|| cm.getDBDataType() == DBDataType.LONG
//					|| cm.getDBDataType() == DBDataType.DOUBLE
//					|| cm.getDBDataType() == DBDataType.DECIMAL
//					|| cm.getDBDataType() == DBDataType.BIGINT
//					|| cm.getDBDataType() == DBDataType.FLOAT) {
//				BigDecimal begin = DataParser.parseBigDecimal(beginValue);
//				BigDecimal end = DataParser.parseBigDecimal(endValue);
//				//必要时交换位置
//				if (begin != null && end != null && begin.compareTo(end) == 1) {
//					BigDecimal tmp = begin;
//					begin = end;
//					end = tmp;
//				}
//				//
//				conditionExpr.andIf(field + " >= ?", begin);
//				conditionExpr.andIf(field + " <= ?", end);
//			}
//		}
//		return conditionExpr;
//	}

//	private ConditionExpr makeJoinedTableCondition(ConditionExpr conditionExpr, CompositeItem item, String tableAliase){
//
//		Boolean fuzzy=item.getFuzzy();
//		if(fuzzy==null) fuzzy=false;
//
//		Object fieldValue=item.getValue();
//		Object fillBy = item.getFillBy();
//		List<String> fillByArr = new ArrayList<>();
//		if (fillBy != null && fillBy instanceof List) {
//			List arr = (List<String>) item.getFillBy();
//			fillByArr.addAll(arr);
//		} else {
//			fillByArr.add((String) fillBy);
//		}
//		String configedField = item.getField();
//		//如果字段不存在，那么说明是扩展外部，进行 Join 查询条件
//		if ((StringUtil.isBlank(configedField) && fillByArr.size() > 1) || (!StringUtil.isBlank(configedField) && fillByArr.size() > 0)) {
//			if (!StringUtil.isBlank(fillBy) && fieldValue != null) {
////				if (StringUtil.isBlank(configedField)) {
//				String configedFieldInFillBy = fillByArr.remove(fillByArr.size() - 1);
//				if (StringUtil.isBlank(configedField)) {
//					configedField=configedFieldInFillBy;
//				}
////				}
//				Expr exists = null;
//				//针对不同类型
//				if ((fieldValue instanceof List) && !((List) fieldValue).isEmpty()) {
//					exists = buildExists(tableAliase, fillByArr, configedField, fieldValue, fuzzy);
//				} else if ((fieldValue instanceof String) && !StringUtil.isBlank(fieldValue.toString())) {
//					exists = buildExists(tableAliase, fillByArr, configedField, fieldValue, fuzzy);
//				}
//				//
//				if (exists != null) {
//					conditionExpr.and(exists);
//				}
//			}
//		}
//		return conditionExpr;
//	}

//	private <S extends Entity,T extends Entity> Expr buildExists(String tableAliase,List<String> fillBys, String field,Object value,boolean fuzzy) {
//		if(value==null) return null;
//
//		String tab=null;
//		if(field.contains(".")) {
//			String[] tmp=field.split("\\.");
//			tab=tmp[0];
//			field=tmp[1];
//		}
//
//		Class poType=(Class)this.getPoType();
//		List<PropertyRoute> routes=new ArrayList<>();
//		for (String fillBy : fillBys) {
//			PropertyRoute<S, T> route=dao().getRelationManager().findProperties(poType,fillBy);
//			if(route==null) {
//				throw new RuntimeException("关联关系未配置");
//			}
//			poType=route.getTargetPoType();
//			routes.add(route);
//		}
//		//路由合并
//		PropertyRoute<S, T> route=PropertyRoute.merge(routes,tab);
//
//		RelationSolver relationSolver=dao().getRelationSolver();
//		JoinResult jr=new JoinResult();
//		Class<T> targetType=route.getTargetPoType();
//
//		Map<String,Object> result=relationSolver.buildJoinStatement(jr,poType,null,route,targetType,false);
//		Expr expr=(Expr)result.get("expr");
//
//		Map<String,String> alias=(Map<String,String>)result.get("tableAlias");
//
//		Join firstJoin=route.getJoins().get(0);
//		Join lastJoin=route.getJoins().get(route.getJoins().size()-1);
//		DBField[] sourceFields=lastJoin.getSourceFields();
//		DBField[] targetFields=lastJoin.getTargetFields();
//		String joinTableAlias=alias.get(lastJoin.getTargetTable());
//		String targetTableAlias=alias.get(firstJoin.getTargetTable());
//
//		//判断字段有效性
//		Where where = null;
//
//		//检测字段，并调整字段的真实名称
//		DBTableMeta tm = dao().getTableMeta(firstJoin.getTargetTable());
//		DBColumnMeta cm = tm.getColumn(field);
//		if (cm == null) {
//			field=BeanNameUtil.instance().depart(field);
//			cm = tm.getColumn(field);
//		}
//		if (cm == null) {
//			throw new IllegalArgumentException("字段 " + firstJoin.getTargetTable() + "." + field + "不存在");
//		}
//
//		//设置关联条件
//		where=new Where();
//		for (int i = 0; i < sourceFields.length; i++) {
//			where.and(tableAliase+"."+sourceFields[i].name()+" = "+joinTableAlias+"."+targetFields[i].name());
//		}
//
//		//如果是模糊搜索
//		if(fuzzy) {
//			if(value instanceof  List) {
//				List<String> list = (List) value;
//				ConditionExpr listOr = new ConditionExpr();
//				for (String itm : list) {
//					ConditionExpr ors = buildFuzzyConditionExpr(field, itm.toString(), targetTableAlias + ".");
//					if (ors != null && !ors.isEmpty()) {
//						listOr.or(ors);
//					}
//				}
//				where.and(listOr);
//			} else {
//				where.andLike(targetTableAlias+"."+field,value.toString());
//			}
//		} else {
//			if(value instanceof String) {
//				value=((String)value).split(",");
//				In in = new In(targetTableAlias+"."+field, (String[]) value);
//				where.and(in);
//			}
//			else if (!((List) value).isEmpty()) {
//				In in = new In(targetTableAlias+"."+field, (List) value);
//				where.and(in);
//			}
//		}
//
//		//追加条件
//		expr.append(where);
//
//		//装配 exists 语句
//		String sql="exists( "+expr.getListParameterSQL()+" )";
//		int a=sql.toLowerCase().indexOf("select ");
//		int b=sql.toLowerCase().indexOf(" from");
//		sql=sql.substring(0,a+7)+" 1 "+sql.substring(b);
//		Expr exists=new Expr(sql,expr.getListParameters());
//		return exists;
//
//	}




	/**
	 * 添加
	 *
	 * @param entity 数据对象
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	public Result insert(E entity) {
		try {
			EntityUtils.setId(entity,this);
			boolean suc=dao().insertEntity(entity);
			if(suc) {
				return ErrorDesc.success();
			} else {
				return ErrorDesc.failure();
			}
		} catch (DuplicateKeyException e) {
			return ErrorDesc.failure(CommonError.DATA_REPETITION);
		} catch (BadSqlGrammarException e) {
			return ErrorDesc.failure().message("SQL语法错误，请确认表字段中是否使用了关键字");
		} catch (DataIntegrityViolationException e) {
//			List<DBColumnMeta> cms=dao().getTableMeta(this.table()).getColumns();
//			List<String> columns=new ArrayList<>();
//			for (DBColumnMeta cm : cms) {
//				if(dao().getDBTreaty().isDBTreatyFiled(cm.getColumn(),true)) continue;
//				if(!cm.isNullable()) {
//					Object value=BeanUtil.getFieldValue(entity,cm.getColumn());
//					if(value==null) {
//						columns.add(cm.getLabel()+"("+cm.getColumn()+")");
//					}
//				}
//			}
			String msg=e.getCause().getMessage();
			if(msg.indexOf(":")!=-1) {
				msg=msg.split(":")[1];
			}
			return ErrorDesc.failure().message("数据插入失败: "+msg);
		}
		catch (Exception e) {
			Result r=ErrorDesc.failure();
			r.extra().setException(e);
			return r;
		}
	}

	/**
	 * 批量插入实体
	 *
	 * @return*/
	public Result insertList(List<E> list) {
	 	boolean suc=this.dao().insertEntities(list);
	 	if(suc) {
			return ErrorDesc.success();
		} else {
			return ErrorDesc.failure().message("批量插入失败");
		}
	}

	/**
	 * 更新所有字段
	 *
	 * @param entity 数据对象
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	public Result updateAllFields(E entity) {
	 	return  this.update(entity,SaveMode.ALL_FIELDS);
	}

	/**
	 * 更新非空字段
	 *
	 * @param entity 数据对象
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	public Result updateNotNullFields(E entity) {
		return  this.update(entity,SaveMode.NOT_NULL_FIELDS);
	}

	/**
	 * 更新修改过的字段
	 *
	 * @param entity 数据对象
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	public Result updateDirtyFields(E entity) {
		return  this.update(entity,SaveMode.DIRTY_FIELDS);
	}


	/**
	 * 更新
	 *
	 * @param entity 数据对象
	 * @param mode SaveMode,数据更新的模式
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	public Result update(E entity , SaveMode mode) {
		try {
			boolean suc=dao().updateEntity(entity, mode);
			if(suc) {
				return ErrorDesc.success();
			} else {
				return ErrorDesc.failure();
			}
		} catch (DuplicateKeyException e) {
			return ErrorDesc.failure(CommonError.DATA_REPETITION);
		}
		catch (Exception e) {
			Result r=ErrorDesc.failure();
			r.extra().setException(e);
			return r;
		}
	}

	/**
	 * 批量更新实体，所有字段
	 * @param  list       实体列表
	 * @return*/
	public Result updateListAllFields(List<E> list) {
		return updateList(list,SaveMode.ALL_FIELDS);
	}

	/**
	 * 批量更新实体，所有非空字段
	 * @param  list       实体列表
	 * @return*/
	public Result updateListNotNullFields(List<E> list) {
		return updateList(list,SaveMode.NOT_NULL_FIELDS);
	}

	/**
	 * 批量更新实体，所有修改过的字段
	 * @param  list       实体列表
	 * @return*/
	public Result updateListDirtyFields(List<E> list) {
		return updateList(list,SaveMode.DIRTY_FIELDS);
	}


	/**
	 * 批量更新实体
	 * @param  list       实体列表
	 * @param  mode  保存模式
	 * @return*/
	@Transactional
	public Result updateList(List<E> list, SaveMode mode) {
		Result result=null;
	 	for (E e : list) {
			result=update(e,mode);
			if(result.failure()) {
				return result;
			}
		}
		return ErrorDesc.success();
	}

	/**
	 * 保存实体，保存所有字段
	 * @param  entity 数据实体
	 * @return
	 * */
	public Result saveAllFields(E entity) {
	 	return this.save(entity,SaveMode.ALL_FIELDS);
	}

	/**
	 * 保存实体，保存所有非空字段
	 * @param  entity 数据实体
	 * @return
	 * */
	public Result saveNotNullFields(E entity) {
		return this.save(entity,SaveMode.NOT_NULL_FIELDS);
	}

	/**
	 * 保存实体，保存修改过字段
	 * @param  entity 数据实体
	 * @return
	 * */
	public Result saveDirtyFields(E entity) {
		return this.save(entity,SaveMode.DIRTY_FIELDS);
	}

	/**
	 * 保存实体
	 * @param  entity 数据实体
	 * @param  mode 保存模式
	 * @return
	 * */
	public Result save(E entity,SaveMode mode) {

		boolean hasPkValue=true;
		List<DBColumnMeta> pks=this.dao().getTableMeta(this.table()).getPKColumns();
		if(pks.size()==0) {
			throw new IllegalArgumentException("数据表 "+this.table+" 缺少主键");
		}
		for (DBColumnMeta pk : pks) {
			if(BeanUtil.getFieldValue(entity,pk.getColumn())==null) {
				hasPkValue=false;
				break;
			}
		}

		//指定主键记录也有可能不存在
		if(hasPkValue) {
			Result r=this.update(entity, mode);
			if(!r.success()) {
				return this.insert(entity);
			} else {
				return r;
			}
		} else {
			return this.insert(entity);
		}

	}

	/**
	 * 保存实体列表，保存实体中的所有字段
	 *
	 * @return
	 * */
	public Result saveListAllFields(List<E> list) {
	 	return  this.saveList(list,SaveMode.ALL_FIELDS);
	}

	/**
	 * 保存实体列表，保存实体中的所有非空字段
	 *
	 * @return
	 * */
	public Result saveNotNullFields(List<E> list) {
		return  this.saveList(list,SaveMode.NOT_NULL_FIELDS);
	}

	/**
	 * 保存实体列表，保存实体中的所有修改过的字段
	 *
	 * @return
	 * */
	public Result saveDirtyFields(List<E> list) {
		return  this.saveList(list,SaveMode.DIRTY_FIELDS);
	}

	/**
	 * 保存实体列表
	 *
	 * @return
	 * */
	@Transactional
	public Result saveList(List<E> list , SaveMode mode) {
		Result result = null;
	 	for (E e : list) {
			result=save(e,mode);
			if(result.failure()) return result;
		}
		return ErrorDesc.success();
	}

	/**
	 * 检查是否存在
	 * @param entity 被检查的实体数据
	 * @param field DB字段
	 * @return  是否存在
	 * */
	public boolean checkExists(E entity,DBField... field) {
		String table=this.table();
		Object value = null;
		Where ce = new Where();
		for (DBField f : field) {
			value =BeanUtil.getFieldValue(entity, f.name());
			ce.and(f+" = ?",value);
		}

		//添加主键
		List<DBColumnMeta> pks=dao().getTableMeta(table).getPKColumns();
		for (DBColumnMeta pk : pks) {
			ce.andIf(pk.getColumn()+" != ?", BeanUtil.getFieldValue(entity, pk.getColumn()));
		}
		//加入删除标记的判断
		DBColumnMeta delcol=dao().getTableMeta(table).getColumn(dao().getDBTreaty().getDeletedField());
		if(delcol!=null) {
			ce.and(delcol.getColumn()+" =?",dao().getDBTreaty().getFalseValue());
		}
		//查询
		Integer o=dao().queryInteger("select 1 from "+table+" "+ce.getListParameterSQL(),ce.getListParameters());
		return o!=null && o==1;
	}

	protected <T> String validateIds(List<T> ids) {
		if(ids==null) throw new IllegalArgumentException("id 列表不允许为 null ");
		DBTableMeta cm=dao().getTableMeta(table());
		if(cm.getPKColumnCount()!=1) {
			throw new IllegalArgumentException("主键数量不符合要求，要求1个主键");
		}
		String idField=cm.getPKColumns().get(0).getColumn();
		return idField;
	}

	/**
	 * 按主键批量删除产品标签
	 *
	 * @param ids 编号 , 详情 : 编号
	 * @return 删除完成情况
	 */
	public <T> Result deleteByIdsPhysical(List<T> ids) {
		if(ids.isEmpty()) {
			return ErrorDesc.failure().message("至少指定一个ID");
		}
		String idField=validateIds(ids);
		In in=new In(idField,ids);
		Delete delete=new Delete(this.table());
		delete.where().and(in);
		Integer i=dao().execute(delete);
		boolean suc= i!=null && i>0;
		if(suc) return ErrorDesc.success();
		else return ErrorDesc.failure();
	}

	/**
	 * 按主键批量删除产品标签
	 *
	 * @param ids 编号 , 详情 : 编号
	 * @return 删除完成情况
	 */
	public <T> Result deleteByIdsLogical(List<T> ids) {
		if(ids.isEmpty()) {
			return ErrorDesc.failure().message("至少指定一个ID");
		}
		String idField=validateIds(ids);
		In in=new In(idField,ids);
		Object trueValue=dao().getDBTreaty().getTrueValue();
		Expr expr=new Expr("update "+table()+" set "+dao().getDBTreaty().getDeletedField()+" = ? ",trueValue);
		expr.append(in.toConditionExpr().startWithWhere());
		Integer i=dao().execute(expr);
		boolean suc= i!=null && i>0;
		if(suc) return ErrorDesc.success();
		else return ErrorDesc.failure();
	}


	public <T> List<T> queryValues(DBField field, Class<T> type, ConditionExpr condition) {

		Expr expr=new Expr("select "+field.name() +" from "+field.table().name()+" "+TABLE_ALAIS);

		Where where=new Where();
		ConditionExpr dbTreatyCondition=this.buildDBTreatyCondition(TABLE_ALAIS);
		where.and(dbTreatyCondition);
		if(condition!=null) {
			where.and(condition);
		}

		expr.append(where);

		RcdSet rs=dao().query(expr);
		return rs.getValueList(field.name(), type);
	}

	public <T> List<T> queryValues(DBField field, Class<T> type, String condition,Object... ps) {
		return queryValues(field, type, new ConditionExpr(condition, ps));
	}


	/**
	 * 构建 Excel 结构
	 * @param  isForExport 是否用于数据导出
	 * @return   ExcelStructure
	 * */
	public ExcelStructure buildExcelStructure(boolean isForExport) {
		ExcelStructure es=new ExcelStructure();
		es.setDataColumnBegin(0);
		es.setDataRowBegin(2);
		DBTableMeta tm=dao().getTableMeta(this.table());
		List<DBColumnMeta> pks=tm.getPKColumns();
		List<DBColumnMeta> cms=tm.getColumns();
		int index=0;
		String charIndex="";
		for (DBColumnMeta pk:pks) {
			charIndex=ExcelStructure.toExcel26(index);
			es.addColumn(charIndex,pk.getColumn(),pk.getLabel());
			index++;
		}

		for (DBColumnMeta cm:cms) {
			if(cm.isPK()) continue;
			//排除策略字段
			if(isForExport) {
				//如果不是创建时间
				if(!dao().getDBTreaty().getCreateTimeField().equalsIgnoreCase(cm.getColumn())) {
					if (dao().getDBTreaty().isDBTreatyFiled(cm.getColumn(),true)) continue;
				}
			} else {
				if (dao().getDBTreaty().isDBTreatyFiled(cm.getColumn(),true)) continue;
			}
			charIndex=ExcelStructure.toExcel26(index);
			es.addColumn(charIndex,cm.getColumn(),cm.getLabel());
			index++;
		}

		return es;
	}

	/**
	 * 按条件导出 Excel 数据
	 * */
	public ExcelWriter  exportExcel(E sample) {

		DBTableMeta tm=this.dao().getTableMeta(this.table());
//		//拼接语句
//		Expr select=new Expr("select * from "+this.table()+" t ");
//		ConditionExpr condition = this.buildQueryCondition(sample);
//		select.append(condition.startWithWhere());

		String tableAlais="t";
		OrderBy orderBy=this.buildOrderBy(sample);
		if(orderBy==null) {
			DBColumnMeta cm=dao().getTableColumnMeta(table(), dao().getDBTreaty().getCreateTimeField());
			if(cm!=null) {
				orderBy=OrderBy.byDesc(tableAlais+"."+cm.getColumn());
			}
		}

		Expr select=buildQuerySQL(sample,tableAlais,null,orderBy);

		//查询数据
		RcdSet rs=this.dao().query(select);
		//写入
		ExcelWriter ew=new ExcelWriter();
		ExcelStructure es=buildExcelStructure(true);
		//ExcelStructure es1=ExcelStructure.parse(rs,true);
		Sheet sheet=ew.fillSheet(rs, tm.getShortTopic()+"清单",es);
		ew.setWorkBookName(tm.getShortTopic()+"清单-"+ DateUtil.format(new Date(),"yyyyMMdd-HHmmss") +".xlsx");
		Logger.info("导出 "+this.table()+" 数据 "+rs.size() +" 行");
		return ew;
	}

	/**
	 * 导出用于数据导入的 Excel 模版
	 * */
	public ExcelWriter  exportExcelTemplate() {

		DBTableMeta tm=this.dao().getTableMeta(this.table);
		//拼接语句
		Expr select = new Expr("select * from " + this.table());
		//查询数据
		RcdSet rs = this.dao().queryPage(select, 1, 1);
		if(rs.size()==1) {
			Rcd r=rs.getRcd(0);
			//若涉及敏感数据，请自行覆盖此方法，并对数据进行调整
		}
		//写入
		ExcelWriter ew = new ExcelWriter();
		ExcelStructure es = buildExcelStructure(false);

		ew.fillSheet(rs, tm.getShortTopic()+"模板", es);
		ew.setWorkBookName(tm.getShortTopic()+"模板.xlsx");
		return ew;
	}

	/**
	 * 导入 Excel 数据
	 * */
	@Transactional
	public List<ValidateResult> importExcel(InputStream input, int sheetIndex,boolean batch) {

		List<ValidateResult> errors=new ArrayList<>();

		ExcelReader er=null;
		try {
			er=new ExcelReader(input);
		} catch (Exception e) {
			errors.add(new ValidateResult(null,-1,"缺少文件"));
			return errors;
		}
		//构建 Excel 结构
		ExcelStructure es=buildExcelStructure(false);
		//装换成记录集
		RcdSet rs=null;
		try {
			rs=er.read(sheetIndex,es);
		} catch (Exception e) {
			Logger.error("Excel 导入错误",e);
			errors.add(new ValidateResult(null,-1,"Excel 读取失败"));
			return errors;
		}

		DBTableMeta tm=dao().getTableMeta(this.table());
		List<DBColumnMeta> pks=tm.getPKColumns();
		DBTreaty  dbTreaty= dao().getDBTreaty();
		//从记录集插入表
		boolean hasPkValue=true;
		List<SQL> sqls=new ArrayList<>();
		for (Rcd r : rs) {

			//可在此处校验数据

			//判定是否填写主键
			hasPkValue=true;
			for (DBColumnMeta pk:pks) {
				if(r.getValue(pk.getColumn())==null || StringUtil.isBlank(pk.getColumn())) {
					hasPkValue=false;
					break;
				}
			}
			if(hasPkValue) {
				Update update=SQLBuilder.buildUpdate(r,SaveMode.ALL_FIELDS,this.table,this.dao());
				//设置创建时间
				if(tm.getColumn(dbTreaty.getUpdateTimeField())!=null) {
					update.set(dbTreaty.getUpdateTimeField(),new Date());
				}
				if(tm.getColumn(dbTreaty.getUpdateUserIdField())!=null) {
					update.set(dbTreaty.getUpdateUserIdField(), dbTreaty.getLoginUserId());
				}
				if(batch) {
					sqls.add(update);
				} else {
					this.dao().execute(update);
				}
			} else {
				Insert insert = SQLBuilder.buildInsert(r,this.table(),this.dao(), true);
				//设置创建时间
				if(tm.getColumn(dbTreaty.getCreateTimeField())!=null) {
					insert.set(dbTreaty.getCreateTimeField(),new Date());
				}
				if(tm.getColumn(dbTreaty.getCreateUserIdField())!=null) {
					insert.set(dbTreaty.getCreateUserIdField(), dbTreaty.getLoginUserId());
				}
				if(tm.getColumn(dbTreaty.getDeletedField())!=null) {
					insert.set(dbTreaty.getDeletedField(), dbTreaty.getFalseValue());
				}
				if(batch) {
					sqls.add(insert);
				} else {
					this.dao().execute(insert);
				}

			}
		}

		if(batch) {
			try {
				dao().batchExecute(sqls);
			} catch (Exception e) {
				throw  e;
			}
		}

		return errors;



	}

	/**
	 * 按主键查询，并返回 Map
	 * */
	protected Map<Object,E> getByIdsMap(List ids) {
		Map<Object,E> map=new HashMap<>();
		if(ids==null || ids.isEmpty()) {
			return map;
		}
		DBTableMeta tm=dao().getTableMeta(table());
		DBColumnMeta pk=tm.getPKColumns().get(0);
		DBColumnMeta deletedField=tm.getColumn(dao().getDBTreaty().getDeletedField());
		Select select=new Select();
		select.from(table()).where().andIn(pk.getColumn(),ids);
		if(deletedField!=null) {
			select.where().andEquals(dao().getDBTreaty().getDeletedField(),dao().getDBTreaty().getFalseValue());
		}
		List<E> list=(List<E>)dao().queryEntities(this.getPoType(),select);
		for (E e : list) {
			map.put(BeanUtil.getFieldValue(e,pk.getColumn()),e);
		}
		return map;
	}

	public Map<String, JoinResult> join(E po, Class... targetType){
		return dao().join(po,targetType);
	}

	public <T extends Entity> Map<String, JoinResult> join(E po, String... properties) {
		return dao().join(po,properties);
	}

	public Map<String, JoinResult> join(Collection<E> pos, Class... targetType){
		return dao().join(pos,targetType);
	}

	public <T extends Entity> Map<String, JoinResult> join(Collection<E> pos, String... properties){
		return dao().join(pos,properties);
	}

	public Map<String, JoinResult> join(PagedList<E> pos, Class... targetType){
		return dao().join(pos,targetType);
	}

	public <T extends Entity> Map<String, JoinResult> join(PagedList<E> pos, String... properties){
		return dao().join(pos,properties);
	}

	/**
	 * 清除原有关系，保存新关系;如果 slaveIds 为 null 则不执行
	 * @param masterIdField 指定关系所有者ID字段
	 * @param masterId 指定关系所有者ID值
	 * @param slaveIdField 指定关系从属对象ID字段
	 * @param slaveIds 指定关系从属对象ID值清单
	 * @param clearWhenEmpty  当 slaveIds 元素个数为0时，是否清空关系
	 * */
	@Transactional
	public void saveRelation(DBField masterIdField, Object masterId,DBField slaveIdField,List slaveIds,boolean clearWhenEmpty) {
		if(slaveIds==null) return;
		if(slaveIds.isEmpty() && !clearWhenEmpty) return;
		this.dao().execute("delete from "+this.table()+" where "+masterIdField.name()+" = ?",masterId);

		List<SQL> sqls=new ArrayList<>();
		DBTableMeta tm=this.dao().getTableMeta(this.table());
		for (Object slaveId : slaveIds) {
			Insert insert=new Insert(this.table());
			insert.set(masterIdField,masterId);
			insert.set(slaveIdField,slaveId);
			DBColumnMeta cm=tm.getColumn(dao().getDBTreaty().getDeletedField());
			if(cm!=null) {
				insert.set(cm.getColumn(),dao().getDBTreaty().getFalseValue());
			}
			cm=tm.getColumn(dao().getDBTreaty().getCreateTimeField());
			if(cm!=null) {
				insert.set(cm.getColumn(),new Date());
			}
			cm=tm.getColumn(dao().getDBTreaty().getCreateUserIdField());
			if(cm!=null) {
				insert.set(cm.getColumn(),this.dao().getDBTreaty().getLoginUserId());
			}
			if(tm.getPKColumnCount()==1) {
				cm=tm.getPKColumns().get(0);
				if(!cm.getColumn().equalsIgnoreCase(masterIdField.name()) && !cm.getColumn().equalsIgnoreCase(slaveIdField.name())) {
					insert.set(cm.getColumn(),this.generateId(null));
				}
			}

			sqls.add(insert);
		}

		if(!sqls.isEmpty()) {
			this.dao().batchExecute(sqls);
		}
	}



}
