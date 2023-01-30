package com.github.foxnic.dao.entity;

import com.github.foxnic.api.error.ErrorDesc;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.cache.DoubleCache;
import com.github.foxnic.commons.collection.CollectorUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.cache.CacheStrategy;
import com.github.foxnic.dao.cache.DataCacheManager;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.excel.*;
import com.github.foxnic.dao.excel.wrapper.SheetWrapper;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBIndexMeta;
import com.github.foxnic.dao.meta.DBMetaData;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.relation.Join;
import com.github.foxnic.dao.relation.JoinResult;
import com.github.foxnic.dao.relation.PropertyRoute;
import com.github.foxnic.dao.relation.RelationSolver;
import com.github.foxnic.dao.relation.cache.CacheInvalidEventType;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.dao.sql.SQLBuilder;
import com.github.foxnic.sql.entity.EntityUtil;
import com.github.foxnic.sql.exception.DBMetaException;
import com.github.foxnic.sql.expr.*;
import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.treaty.DBTreaty;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

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

	public FieldsBuilder createFieldsBuilder() {
		return FieldsBuilder.build(this.dao(),this.table()).addAll();
	}

//	private boolean enableCache=false;

//	private Map<String, CacheStrategy> cacheStrategies =new HashMap<>();

//	public void registCacheStrategy(String name,boolean isAccurate,boolean cacheEmptyResult,String... conditionProperty) {
//		dao().getDataCacheManager().registStrategy(this.getPoType(),isAccurate,cacheEmptyResult,conditionProperty);
//	}

	public CacheStrategy getCacheStrategy(String name) {
		return this.getCacheStrategies().get(name);
	}

	public Map<String,CacheStrategy> getCacheStrategies() {
		return dao().getDataCacheManager().getStrategies(this.getPoType());
	}

	private DoubleCache<String,Object> cache=null;

	/**
	 * 获得二级缓存(本地缓存+远程缓存)，如果远程缓存不可用，就使用本地缓存
	 * */
	public DoubleCache<String,Object> cache() {
		if(this.cache!=null) return this.cache;
		this.cache=(DoubleCache<String,Object>)dao().getDataCacheManager().getEntityCache(this.getPoType());
		return cache;
	}


	public void dispatchJoinCacheInvalidEvent(CacheInvalidEventType eventType, Entity valueBefore, Entity valueAfter) {
		this.dao().getDataCacheManager().dispatchJoinCacheInvalidEvent(eventType,dao().getDataCacheManager(),this.table(),valueBefore,valueAfter);
	}

	public void dispatchJoinCacheInvalidEvent(CacheInvalidEventType eventType, List<? extends Entity> valuesBefore, List<? extends Entity> valuesAfter) {
		//CacheInvalidEvent<E> event = new CacheInvalidEvent(eventType,this.table(),valueBefore,valueAfter);
		this.dao().getDataCacheManager().dispatchJoinCacheInvalidEvent(eventType,this.table(),valuesBefore,valuesAfter);
	}



	/**
	 * 生成ID，覆盖方法实现
	 * */
	public Object generateId(Field field) { return null; };


	private String table=null;

	private String nameField=null;

	private Class<? extends E> poType;

	/**
	 * 数据表
	 * */
	public String table() {
		init();
		return table;
	}

	public String nameField() {
		init();
		return nameField;
	}

	public String tableAlias() {
		init();
		return TABLE_ALAIS;
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
		Class clazz=this.getClass();
		Object superclass = null;
		while (true) {
			superclass=clazz.getGenericSuperclass();
			if(superclass instanceof ParameterizedType) {
				break;
			} else {
				clazz=clazz.getSuperclass();
				if(clazz==null) {
					break;
				}
			}
		}

		ParameterizedType type=(ParameterizedType)superclass;
		Type[] types=type.getActualTypeArguments();
		poType=(Class)types[0];
		table=EntityUtil.getAnnotationTable(poType);
		nameField=this.guessReferNameColumn(table);
	}

	public List<E> queryList(E sample) {
		OrderBy orderBy = buildOrderBy(sample);
		return queryList(sample,null,null,orderBy);
	}

	public List<E> queryList(E sample, FieldsBuilder fieldsBuilder) {
		OrderBy orderBy = buildOrderBy(sample);
		return queryList(sample,fieldsBuilder,null,orderBy);
	}

	public List<E> queryList(E sample,OrderBy orderBy) {
		return queryList(sample,null,null,orderBy);
	}

	public List<E> queryList(E sample, FieldsBuilder fieldsBuilder, OrderBy orderBy) {
		return queryList(sample,fieldsBuilder,null,orderBy);
	}



	public List<E> queryList(E sample,ConditionExpr condition) {
		OrderBy orderBy = buildOrderBy(sample);
		return queryList(sample,condition,orderBy);
	}

	public List<E> queryList(E sample, FieldsBuilder fieldsBuilder, ConditionExpr condition) {
		OrderBy orderBy = buildOrderBy(sample);
		return queryList(sample,fieldsBuilder,condition,orderBy);
	}

	public List<E> queryList(String condition,Object... ps) {
		return queryList(new ConditionExpr(condition,ps),null);
	}

	public List<E> queryList(ConditionExpr condition) {
		return queryList(condition,null);
	}

	public List<E> queryList(FieldsBuilder fields,ConditionExpr condition) {
		return queryList(fields,condition,null);
	}

	public List<E> queryList(ConditionExpr condition,OrderBy orderBy) {
		return queryList(this.createFieldsBuilder(),condition,orderBy);
	}

	public List<E> queryList(FieldsBuilder fields,ConditionExpr condition,OrderBy orderBy) {

		Expr expr=new Expr("select "+fields.getFieldsSQL()+" from "+this.table()+" "+TABLE_ALAIS);
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
		return dao().queryEntities((Class<E>)this.getPoType(),select);
	}



	/**
	 * 查询全部符合条件的数据
	 *
	 * @param sample 查询条件
	 * @return 查询结果 , News清单
	 */
	public List<E> queryList(E sample, FieldsBuilder fieldsBuilder, ConditionExpr condition, OrderBy orderBy) {
		Expr select=this.buildQuerySQL(sample,fieldsBuilder,TABLE_ALAIS,condition,orderBy);
		return dao().queryEntities((Class<E>)getPoType(),select);
	}

	/**
	 * 查询全部符合条件的数据
	 *
	 * @param sample 查询条件
	 * @return 查询结果 , News清单
	 */
	public List<E> queryList(E sample,ConditionExpr condition,OrderBy orderBy,String dpcode) {
		Expr select=this.buildQuerySQL(sample,TABLE_ALAIS,condition,orderBy,dpcode);
		return dao().queryEntities((Class<E>)getPoType(),select);
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

	public  <T> T queryField(String id, DBField field, Class<T> valueType) {
		DBTableMeta tm=this.getDBTableMeta();
		if(tm.getPKColumnCount()!=1) {
			throw new DataException("表 "+this.table()+" 主键数量要求 1 , 实际 "+tm.getPKColumnCount());
		}
		ConditionExpr condition=new ConditionExpr(tm.getPKColumns().get(0).getColumn()+" = ?",id);
		if(condition!=null) {
			if(tm.isColumnExists(this.dao().getDBTreaty().getDeletedField())) {
				condition.and(this.dao().getDBTreaty().getDeletedField()+" = ?",this.dao().getDBTreaty().getFalseValue());
			}
			if(tm.isColumnExists(this.dao().getDBTreaty().getTenantIdField())) {
				condition.and(this.dao().getDBTreaty().getTenantIdField()+" = ?",this.dao().getDBTreaty().getActivedTenantId());
			}
		}
		Expr select = new Expr("select "+field.name()+" from "+this.table());
		select.append(condition.startWithWhere());
		Object value=dao().queryObject(select);
		value= DataParser.parse(valueType,value);
		return (T)value;
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
	 * @param condition 额外的查询条件
	 * @return 查询结果 , 数据清单
	 */
	@Override
	public PagedList<E> queryPagedList(E sample, FieldsBuilder fieldsBuilder, ConditionExpr condition, int pageSize, int pageIndex) {
		OrderBy orderBy = buildOrderBy(sample);
		return queryPagedList(sample,fieldsBuilder, condition, orderBy, pageSize, pageIndex);
	}

	@Override
	public PagedList<E> queryPagedList(E sample,ConditionExpr condition,int pageSize,int pageIndex,String dpcode) {
		OrderBy orderBy = buildOrderBy(sample);
		return queryPagedList(sample, condition, orderBy, pageSize, pageIndex,dpcode);
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
		return queryPagedList(sample, null,null, orderBy, pageSize, pageIndex);
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
	public PagedList<E> queryPagedList(E sample, FieldsBuilder fieldsBuilder, int pageSize, int pageIndex) {
		OrderBy orderBy = buildOrderBy(sample);
		return queryPagedList(sample, fieldsBuilder,null, orderBy, pageSize, pageIndex);
	}

	@Override
	public PagedList<E> queryPagedList(E sample,int pageSize,int pageIndex,String dpcode) {
		OrderBy orderBy = buildOrderBy(sample);
		return queryPagedList(sample, null, orderBy, pageSize, pageIndex,dpcode);
	}

	@Override
	public PagedList<E> queryPagedList(E sample, FieldsBuilder fieldsBuilder, int pageSize, int pageIndex, String dpcode) {
		OrderBy orderBy = buildOrderBy(sample);
		return queryPagedList(sample, fieldsBuilder,null, orderBy, pageSize, pageIndex,dpcode);
	}








	/**
	 * 根据实体数构建默认的条件表达式，不支持 Join 其它表
	 * @param sample 数据样例
	 * @return ConditionExpr 条件表达式
	 * */
	public ConditionExpr buildQueryCondition(E sample){
		QuerySQLBuilder builder=new QuerySQLBuilder(this);
		return builder.buildLocalCondition(sample,TABLE_ALAIS,null);
	}

	/**
	 * 根据实体数构建默认的条件表达式, 不支持 Join 其它表
	 * @param sample 数据样例
	 * @param tableAlias 数据表别名
	 * 	@return ConditionExpr 条件表达式
	 * */
	public ConditionExpr buildQueryCondition(E sample,String tableAlias) {
		QuerySQLBuilder builder=new QuerySQLBuilder(this);
		return builder.buildLocalCondition(sample,tableAlias,null);
	}


	public Expr buildQuerySQL(E sample, String tabAlias,ConditionExpr conditionExpr,OrderBy orderBy) {
		QuerySQLBuilder builder=new QuerySQLBuilder(this);
		return builder.buildSelect(sample,tabAlias,conditionExpr,orderBy);
	}

	public Expr buildQuerySQL(E sample, FieldsBuilder fieldsBuilder, String tabAlias, ConditionExpr conditionExpr, OrderBy orderBy) {
		QuerySQLBuilder builder=new QuerySQLBuilder(this);
		return builder.buildSelect(sample,fieldsBuilder,tabAlias,conditionExpr,orderBy);
	}

	public Expr buildQuerySQL(E sample, String tabAlias,ConditionExpr conditionExpr,OrderBy orderBy,String dpcode) {
		QuerySQLBuilder builder=new QuerySQLBuilder(this);
		return builder.buildSelect(sample,tabAlias,conditionExpr,orderBy,dpcode);
	}

	public Expr buildQuerySQL(E sample, FieldsBuilder fieldsBuilder, String tabAlias, ConditionExpr conditionExpr, OrderBy orderBy, String dpcode) {
		QuerySQLBuilder builder=new QuerySQLBuilder(this);
		return builder.buildSelect(sample,fieldsBuilder,tabAlias,conditionExpr,orderBy,dpcode);
	}

	public ConditionExpr buildDBTreatyCondition(String tableAlias) {
		QuerySQLBuilder builder=new QuerySQLBuilder(this);
		return builder.buildDBTreatyCondition(this.table(),tableAlias);
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
		return queryPagedList(sample, null,null, orderBy, pageSize, pageIndex);
	}

	/**
	 * 分页查询符合条件的数据
	 *
	 * @param sample 查询条件
	 * @param orderBy 排序
	 * @return 查询结果 , 数据清单
	 */
	@Override
	public PagedList<E> queryPagedList(E sample, FieldsBuilder fieldsBuilder, OrderBy orderBy, int pageSize, int pageIndex) {
		return queryPagedList(sample, fieldsBuilder, null, orderBy, pageSize, pageIndex);
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
		return queryPagedList(sample, condition, orderBy, pageSize, pageIndex,null);
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
	public PagedList<E> queryPagedList(E sample, FieldsBuilder fieldsBuilder, ConditionExpr condition, OrderBy orderBy, int pageSize, int pageIndex) {
		return queryPagedList(sample, fieldsBuilder,condition, orderBy, pageSize, pageIndex,null);
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
	public PagedList<E> queryPagedList(E sample,ConditionExpr condition,OrderBy orderBy,int pageSize,int pageIndex,String dpcode) {
		return queryPagedList(sample,null,condition,orderBy,pageSize,pageIndex,dpcode);
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
	public PagedList<E> queryPagedList(E sample, FieldsBuilder fieldsBuilder, ConditionExpr condition, OrderBy orderBy, int pageSize, int pageIndex, String dpcode) {

		String sortField=BeanUtil.getFieldValue(sample, "sortField",String.class);

		if(StringUtil.isBlank(sortField) && orderBy==null) {
			DBTableMeta tm=dao().getTableMeta(table());
			DBColumnMeta cm=dao().getTableColumnMeta(table(), dao().getDBTreaty().getCreateTimeField());
			if(cm!=null) {
				orderBy=OrderBy.byDesc(TABLE_ALAIS+"."+cm.getColumn());
			}

			if(tm.getPKColumnCount()>0) {
				cm =  tm.getPKColumns().get(0);
				orderBy = OrderBy.byDesc(TABLE_ALAIS + "." + cm.getColumn());
			}
		}

		Expr select = null;
		if(StringUtil.isBlank(dpcode)) {
			select=buildQuerySQL(sample,fieldsBuilder,TABLE_ALAIS,condition,orderBy);
		} else {
			select = buildQuerySQL(sample, fieldsBuilder,TABLE_ALAIS, condition, orderBy, dpcode);
		}

		//执行查询
		return dao().queryPagedEntities((Class<E>)this.getPoType(), pageSize,pageIndex, select);
	}


	/**
	 * 添加，如果语句错误，则抛出异常
	 *
	 * @param entity 数据对象
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	public Result insert(E entity) {
		return insert(entity,true);
	}

	/**
	 * 当前服务是否支持缓存
	 * */
	public boolean isSupportCache() {
		return  this.dao().getDataCacheManager().isSupportAccurateCache(this.getPoType());
	}

	/**
	 * 添加，根据 throwsException 参数抛出异常或返回 Result 对象
	 *
	 * @param entity 数据对象
	 * @param throwsException 是否抛出异常，如果不抛出异常，则返回一个失败的 Result 对象
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	public Result insert(E entity,boolean throwsException) {
		try {
			EntityUtils.setId(entity,this);
			boolean suc=dao().insertEntity(entity);
			if(suc) {
				// 如果成功，返回主键值
				DBTableMeta tm = this.getDBTableMeta();
				List<DBColumnMeta> pks = tm.getPKColumns();
				Map<String,Object> pkValues=new HashMap<>();
				if(pks!=null && !pks.isEmpty()) {
					for (DBColumnMeta pk : pks) {
						pkValues.put(pk.getColumnVarName(),BeanUtil.getFieldValue(entity,pk.getColumn()));
					}
				}
				return ErrorDesc.success().data(pkValues);
			} else {
				return ErrorDesc.failure();
			}
		} catch (DuplicateKeyException e) {
			if(handleDuplicateKey(entity,e,1,null)) {
				return ErrorDesc.success();
			}
			Logger.exception("数据插入异常",e);
			if(throwsException) throw  e;
			return ExceptionMessageUtil.getResult(e,this);
		} catch (BadSqlGrammarException e) {
			if(throwsException) throw  e;
			return ErrorDesc.failure().message("SQL语法错误，请确认表字段中是否使用了关键字");
		} catch (DataIntegrityViolationException e) {
			Logger.exception("数据插入异常",e);
			if(throwsException) throw  e;
			String msg=e.getCause().getMessage();
			if(msg.indexOf(":")!=-1) {
				msg=msg.split(":")[1];
			}
			return ErrorDesc.failure().message("数据插入失败: "+msg);
		}
		catch (Exception e) {
			Logger.exception("数据插入异常",e);
			if(throwsException) throw  e;
			Result r=ErrorDesc.failure();
			r.extra().setException(e);
			return r;
		}
	}

	/**
	 * 处理主键重复异常
	 * @param  sourceFn  来自与哪个方法的调用，insert:1,update:2
	 * @return  是否处理成功
	 * */
	private boolean handleDuplicateKey(E entity,DuplicateKeyException e,int sourceFn,SaveMode mode) {
		DBIndexMeta indexMeta=this.getUniqueIndex(e);
		if(indexMeta==null) return false;
		if(indexMeta.getFields()==null || indexMeta.getFields().length==0) {
			throw new IllegalArgumentException("无法识别索引字段");
		}
		ConditionExpr conditionExpr=new ConditionExpr();
		String[] fields= indexMeta.getFields();
		for (String field : fields) {
			conditionExpr.and(field+" = ?",BeanUtil.getFieldValue(entity,field));
		}
		Expr select =new Expr("select * from "+this.table()+" "+conditionExpr.startWithWhere().getListParameterSQL(),conditionExpr.getListParameters());
		Rcd r=dao().queryRecord(select);
		if(r==null) {
			if(sourceFn==1) {
				this.dao().insertEntity(entity);
				return true;
			} else if(sourceFn==2) {
				this.dao().updateEntity(entity,mode);
				return true;
			} else {
				return false;
			}
		} else {
			DBTableMeta tm=this.getDBTableMeta();
			DBColumnMeta deletedField=tm.getColumn(dao().getDBTreaty().getDeletedField());
			if(deletedField!=null) {
				Object deleted=r.getValue(deletedField.getColumn());
				//判断是否已经被逻辑删除
				if(dao().getDBTreaty().getTrueValue().equals(deleted)) {
					DBColumnMeta m=tm.getColumn(fields[0]);
					Object newValue=null;
					if(m.getDBDataType()== DBDataType.STRING) {
						int tag=1;
						while (true) {
							newValue = r.getString(fields[0]) + ":d" + tag;
							Expr update=new Expr("update " + this.table() + " set " + fields[0] + " = ?  " ,  newValue);
							update=update.append(conditionExpr.startWithWhere());
							try {
								int i  = dao().execute(update);
								if(i==1) {
									boolean suc = false;
									if(sourceFn==1) {
										Result ir=this.insert(entity,true);
										suc=ir.success();
									} else if(sourceFn==2) {
										suc=this.dao().updateEntity(entity,mode);
									}
									if(suc) {
										return suc;
									}
								}
							} catch (Exception exception) {
								tag++;
							}
							if(tag>=128) {
								return false;
							}
						}
					} else {
						//其它数据类型，暂不考虑
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	/**
	 * 在异常中获得唯一索引
	 * */
	public DBIndexMeta getUniqueIndex(DuplicateKeyException exception) {
		String msg = exception.getMessage();
		String key = "' for key '";
		int a = msg.lastIndexOf(key);
		if (a == -1) return null;
		int b = msg.indexOf("'", a + key.length());
		key = msg.substring(a + key.length(), b);
		String[] tmp=key.split("\\.");
		key=tmp[tmp.length-1];
		DBTableMeta tm = this.getDBTableMeta();
		DBIndexMeta index = tm.getIndex(key);
		if (index == null) {
			DBMetaData.buildIndex(this.dao(), this.table(), tm);
			index = tm.getIndex(key);
		}
		return index;
	}

	/**
	 * 批量插入实体，异常时抛出异常
	 *
	 * @return*/
	public Result insertList(List<E> list) {
		for (E e : list) {
			if (e == null) continue;
			EntityUtils.setId(e, this);
		}
		boolean suc = this.dao().insertEntities(list);
		if (suc) {
			return ErrorDesc.success();
		} else {
			return ErrorDesc.failure().message("批量插入失败");
		}
	}

	public boolean deleteEntity(E entity,boolean logical) {

		DBTableMeta tm=dao().getTableMeta(this.table());
		List<DBColumnMeta> pkcols=tm.getPKColumns();
		if(pkcols==null || pkcols.isEmpty()) {
			throw new IllegalArgumentException(this.table() +" 缺少主键");
		}

		 if(logical) {
			 DBColumnMeta delcol=dao().getTableColumnMeta(this.table(),this.dao().getDBTreaty().getDeletedField());
			 if(delcol==null) {
				throw new IllegalArgumentException("逻辑删字段 "+this.dao().getDBTreaty().getDeletedField()+" 不存在");
			 }
			 BeanUtil.setFieldValue(entity,delcol.getColumnVarName(),this.dao().getDBTreaty().getTrueValue());
			 return dao().updateEntity(entity,SaveMode.DIRTY_FIELDS);
		 } else {
			 return dao().deleteEntity(entity, table);
		 }
	}
	public int deleteList(List<E> list,boolean logical) {
		 if(list==null || list.isEmpty()) return 0;
		 DBTableMeta tm=dao().getTableMeta(this.table());
		 List<DBColumnMeta> pkcols=tm.getPKColumns();
		 if(pkcols==null || pkcols.isEmpty()) {
			 throw new IllegalArgumentException(this.table() +" 缺少主键");
		 }
		In in = null;
		 String[] pks=CollectorUtil.collectArray(pkcols,DBColumnMeta::getColumn,String.class);
		 if(pks.length==1) {
			 List<Object> values=BeanUtil.getFieldValueList(list,pks[0],Object.class);
			 in=new In(pks[0],values);
		 } else {
			 List<Object[]> values=new ArrayList<>();
			 for (E e : list) {
				 Object[] item=new Object[pks.length];
				 for (int i = 0; i < pks.length; i++) {
					 item[i]=BeanUtil.getFieldValue(e,pks[i]);
				 }
			 }
			 in=new In(pks,values);
		 }

		 Expr expr=null;

		 if(logical) {
			expr=new Expr("update "+table+" set "+dao().getDBTreaty().getDeletedField()+" = ? ",dao().getDBTreaty().getTrueValue());
			expr.append(in.toConditionExpr().startWithWhere());
		 } else {
			 expr=new Expr("delete from "+table);
			 expr.append(in.toConditionExpr().startWithWhere());
		 }
		return  dao().execute(expr);

	}

	/**
	 * 更新所有字段，如果执行错误，则抛出异常
	 *
	 * @param entity 数据对象
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	public Result updateAllFields(E entity) {
	 	return  this.update(entity,SaveMode.ALL_FIELDS);
	}

	/**
	 * 更新非空字段，如果执行错误，则抛出异常
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
	 * 更新，如果执行错误，则抛出异常
	 *
	 * @param entity 数据对象
	 * @param mode SaveMode,数据更新的模式
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	public Result update(E entity , SaveMode mode) {
		return update(entity,mode,true);
	}


	/**
	 * 更新，根据 throwsException 参数抛出异常或返回 Result 对象
	 *
	 * @param entity 数据对象
	 * @param mode SaveMode,数据更新的模式
	 * @param throwsException 是否抛出异常，如果不抛出异常，则返回一个失败的 Result 对象
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	public Result update(E entity , SaveMode mode,boolean throwsException) {
		try {
			boolean suc=dao().updateEntity(entity, mode);
			if(suc) {
				return ErrorDesc.success();
			} else {
				return ErrorDesc.failure();
			}
		} catch (DuplicateKeyException e) {
			if(handleDuplicateKey(entity,e,2,mode)) {
				return ErrorDesc.success();
			}
			Logger.exception("数据插入异常",e);
			if(throwsException) throw  e;
			return ExceptionMessageUtil.getResult(e,this);
		} catch (BadSqlGrammarException e) {
			Logger.exception("数据插入异常",e);
			if(throwsException) throw  e;
			return ErrorDesc.failure().message("SQL语法错误，请确认表字段中是否使用了关键字");
		} catch (DataIntegrityViolationException e) {
			Logger.exception("数据插入异常",e);
			if(throwsException) throw  e;
			String msg=e.getCause().getMessage();
			if(msg.indexOf(":")!=-1) {
				msg=msg.split(":")[1];
			}
			return ErrorDesc.failure().message("数据插入失败: "+msg);
		}
		catch (Exception e) {
			Logger.exception("数据插入异常",e);
			if(throwsException) throw  e;
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
	 * 批量更新实体，如果执行错误，则抛出异常
	 * @param  list       实体列表
	 * @param  mode  保存模式
	 * @return*/
	@Transactional
	public Result updateList(List<E> list, SaveMode mode) {
		Result result=null;
	 	for (E e : list) {
			if(e!=null) {
				result = update(e, mode);
				if (result.failure()) {
					return result;
				}
			}
		}
		return ErrorDesc.success();
	}

	/**
	 * 保存实体，保存所有字段，如果执行错误，则抛出异常
	 * @param  entity 数据实体
	 * @return
	 * */
	public Result saveAllFields(E entity) {
	 	return this.save(entity,SaveMode.ALL_FIELDS);
	}

	/**
	 * 保存实体，保存所有非空字段，如果执行错误，则抛出异常
	 * @param  entity 数据实体
	 * @return
	 * */
	public Result saveNotNullFields(E entity) {
		return this.save(entity,SaveMode.NOT_NULL_FIELDS);
	}

	/**
	 * 保存实体，保存修改过字段，如果执行错误，则抛出异常
	 * @param  entity 数据实体
	 * @return
	 * */
	public Result saveDirtyFields(E entity) {
		return this.save(entity,SaveMode.DIRTY_FIELDS);
	}

	/**
	 * 保存实体，如果语句错误，则抛出异常
	 * @param  entity 数据实体
	 * @param  mode 保存模式
	 * @return
	 * */
	public Result save(E entity,SaveMode mode) {
		return save(entity,mode,true);
	}
	/**
	 * 保存实体，根据 throwsException 参数抛出异常或返回 Result 对象
	 * @param  entity 数据实体
	 * @param  mode 保存模式
	 * @param throwsException 是否抛出异常，如果不抛出异常，则返回一个失败的 Result 对象
	 * @return
	 * */
	public Result save(E entity,SaveMode mode,boolean throwsException) {

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
			Result r=this.update(entity, mode,throwsException);
			if(!r.success()) {
				return this.insert(entity,throwsException);
			} else {
				return r;
			}
		} else {
			return this.insert(entity);
		}

	}

	/**
	 * 保存实体列表，保存实体中的所有字段，如果执行错误，则抛出异常
	 *
	 * @return
	 * */
	public Result saveListAllFields(List<E> list) {
	 	return  this.saveList(list,SaveMode.ALL_FIELDS);
	}

	/**
	 * 保存实体列表，保存实体中的所有非空字段，如果执行错误，则抛出异常
	 *
	 * @return
	 * */
	public Result saveNotNullFields(List<E> list) {
		return  this.saveList(list,SaveMode.NOT_NULL_FIELDS);
	}

	/**
	 * 保存实体列表，保存实体中的所有修改过的字段，如果执行错误，则抛出异常
	 *
	 * @return
	 * */
	public Result saveDirtyFields(List<E> list) {
		return  this.saveList(list,SaveMode.DIRTY_FIELDS);
	}

	/**
	 * 保存实体列表，如果执行错误，则抛出异常
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
	 * 检查符合条件的记录是否存在
	 */
	public boolean checkExists(ConditionExpr conditionExpr) {
		//加入删除标记的判断
		DBColumnMeta delcol=this.getDBTableMeta().getColumn(dao().getDBTreaty().getDeletedField());
		if(delcol!=null) {
			conditionExpr.and(delcol.getColumn()+" =?",dao().getDBTreaty().getFalseValue());
		}
		// 加入租户过滤
		DBColumnMeta tenantIdField=this.getDBTableMeta().getColumn(dao().getDBTreaty().getTenantIdField());
		if(tenantIdField!=null) {
			conditionExpr.and(tenantIdField.getColumn()+" =?",dao().getDBTreaty().getActivedTenantId());
		}
		Rcd r=this.dao().queryRecord("select 1 from "+this.table() +" "+ conditionExpr.startWithWhere().getListParameterSQL(),conditionExpr.getListParameters());
		return  r!=null;
	}

	/**
	 * 检查是否存在: 判断 主键值不同，但指定字段的值相同的记录是否存在
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
		// 加入租户过滤
		DBColumnMeta tenantIdField=dao().getTableMeta(table).getColumn(dao().getDBTreaty().getTenantIdField());
		if(tenantIdField!=null) {
			ce.and(tenantIdField.getColumn()+" =?",dao().getDBTreaty().getActivedTenantId());
		}

		//查询
		Integer o=dao().queryInteger("select 1 from "+table+" "+ce.getListParameterSQL(),ce.getListParameters());
		return o!=null && o==1;
	}


	/**
	 * 判断ids是否被指定的多个字段锁引用
	 * */
	public <T> Map<T, ReferCause> hasRefers(List<T> ids, DBField... fields){
		if(fields==null || fields.length==0) {
			throw new IllegalArgumentException("缺少字段");
		}
		Map<T, ReferCause> result = null;
		for (DBField field : fields) {
			// 此处可以考虑更高的性能，把已经是 true 的 id 排除掉，不进行下一个字段的校验
			Map<T, ReferCause> map = this.hasRefers(field,ids);
			if(result==null){
				result=map;
			} else {
				for (Map.Entry<T, ReferCause> e : map.entrySet()) {
					if(result.get(e.getKey()).hasRefer()==true) {
						continue;
					} else {
						result.put(e.getKey(), e.getValue());
					}
				}
			}
		}
		return result;
	}

	/**
	 * 检查是否被外部表引用
	 * */
	public <T> Map<T, ReferCause> hasRefers(DBField field, List<T> ids) {
		return hasRefers(field.table().name(),field.name(),ids);
	}

	/**
	 * 检查是否被外部表引用
	 * */
	public <T> Map<T, ReferCause> hasRefers(DBField field, List<T> ids,String finalTable,String finalNameField) {
		return hasRefers(field.table().name(),field.name(),ids,finalTable,finalNameField);
	}

	/**
	 * 检查是否被外部表引用
	 * */
	public <T> Map<T, ReferCause> hasRefers(DBField field, List<T> ids,String finalTable) {
		return hasRefers(field.table().name(),field.name(),ids,finalTable,guessReferNameColumn(finalTable));
	}

	public <T> Map<T, ReferCause> hasRefers(String targetTable, String targetField, List<T> ids) {
		return hasRefers(targetTable,targetField,ids,null,null);
	}

	public <T> Map<T, ReferCause> hasRefers(String targetTable, String targetField, List<T> ids,String finalTable) {
		return hasRefers(targetTable,targetField,ids,finalTable,guessReferNameColumn(finalTable));
	}

	 /**
	  * 检查是否被外部表引用
	  * */
	public <T> Map<T, ReferCause> hasRefers(String targetTable, String targetField, List<T> ids,String finalTable,String finalNameField) {
		Map<T, ReferCause> map=new HashMap<>();
		if(ids==null || ids.isEmpty()) return map;

		DBColumnMeta col=this.dao().getTableColumnMeta(targetTable,targetField);
		if(col==null) {
			throw new DBMetaException("字段 "+targetTable+"."+targetField+" 未定义");
		}


		In in=new In(targetField,ids);
		ConditionExpr conditionExpr=new ConditionExpr();
		conditionExpr.and(in);
		//加入删除标记的判断
		DBColumnMeta delcol=dao().getTableMeta(targetTable).getColumn(dao().getDBTreaty().getDeletedField());
		if(delcol!=null) {
			conditionExpr.and(delcol.getColumn()+" =?",dao().getDBTreaty().getFalseValue());
		}
		// 加入租户过滤
		DBColumnMeta tenantIdField=dao().getTableMeta(targetTable).getColumn(dao().getDBTreaty().getTenantIdField());
		if(tenantIdField!=null && dao().getDBTreaty().getActivedTenantId()!=null) {
			conditionExpr.and(tenantIdField.getColumn()+" =?",dao().getDBTreaty().getActivedTenantId());
		}
		// 性能问题可后期优化
		Expr select=new Expr("select "+targetField+",count("+targetField+") c from "+targetTable);
		select=select.append(conditionExpr.startWithWhere());
		select=select.append("group by "+targetField);
		RcdSet rs=dao().query(select);
		Map<T, Boolean> booleanMap= (Map<T, Boolean>)rs.getValueMap(targetField,col.getDBDataType().getType(),"c",Boolean.class);

		// 补充0值ID
		List<T> zeroIds=new ArrayList<>();
		for (T id : ids) {
			if(!booleanMap.containsKey(id)) zeroIds.add(id);
		}
		for (T zeroId : zeroIds) {
			booleanMap.put(zeroId,false);
		}
		DBTableMeta tm=this.dao().getTableMeta(targetTable);
		DBColumnMeta cm=tm.getColumn(targetField);
		List<T> hasReferIds=new ArrayList<>();
		for (Map.Entry<T, Boolean> e : booleanMap.entrySet()) {
			if(e.getValue()) {
				map.put(e.getKey(),new ReferCause(true,"已被"+tm.getShortTopic()+"的"+cm.getLabel()+"("+targetTable+"."+targetField+")使用",targetTable,targetField));
				hasReferIds.add(e.getKey());
			} else {
				map.put(e.getKey(),ReferCause.noRefers());
			}
		}

		// 求取引用对象的名称
		if(!hasReferIds.isEmpty() && !StringUtil.isBlank(finalTable) && !StringUtil.isBlank(finalNameField)) {
			tm=this.dao().getTableMeta(finalTable);
			Map<T,List<ReferCause.Names>> names=queryReferSubjectNames(targetTable,targetField,hasReferIds,finalTable,finalNameField);
			for (Map.Entry<T, ReferCause> e : map.entrySet()) {
				ReferCause cause=e.getValue();
				List<ReferCause.Names> namesList=names.get(e.getKey());
				if(cause==null || namesList==null) continue;
				Set<String> items=CollectorUtil.collectSet(namesList,ReferCause.Names::getMasterName);
				List<String> itemList=new ArrayList<>();

				for (String item : items) {
					itemList.add(item);
					if(itemList.size()>=5) break;
				}
				String itemStr=StringUtil.join(itemList,"</span> , <span class='dialog-quote'>");
				String msg="<span class='dialog-quote'>"+namesList.get(0).getLocalName()+"</span> 已经被 <span class='dialog-quote'>"+itemStr+"</span> ";
				if(items.size()>5) {
					msg+=" 等"+items.size()+"个";
				}
				msg+=tm.getShortTopic()+"引用。";
				map.put(e.getKey(),new ReferCause(true,msg,targetTable,targetField));
			}
		}
		return map;
	}

	private  <T> Map<T,List<ReferCause.Names>> queryReferSubjectNames(String targetTable,String targetField, List<T> ids,String finalTable,String finalNameField) {


		// 查找正向关系
		Set<PropertyRoute> matchedProps=new HashSet<>();
		List<PropertyRoute> props=this.dao().getRelationManager().findPropertiesBySlaveTable(finalTable);
		for (PropertyRoute prop : props) {
			System.out.println(prop);
			List<Join> joins=prop.getJoins();
			for (Join join : joins) {
				if(join.getSlaveTable().equalsIgnoreCase(targetTable) || join.getMasterTable().equalsIgnoreCase(targetTable)) {
					matchedProps.add(prop);
				}
			}
		}

		// 查找反向关系
		Set<PropertyRoute> matchedPropsR=new HashSet<>();
		props = this.dao().getRelationManager().findPropertiesByMasterTable(finalTable);
		for (PropertyRoute prop : props) {
			System.out.println(prop);
			List<Join> joins = prop.getJoins();
			for (Join join : joins) {
				if (join.getSlaveTable().equalsIgnoreCase(targetTable) || join.getMasterTable().equalsIgnoreCase(targetTable)) {
					matchedPropsR.add(prop);
				}
			}
		}

		if(matchedProps.isEmpty() && matchedPropsR.isEmpty()) {
			throw new DBMetaException(this.table() +" 与 "+finalTable+" 之间未配置关联关系。");
		}

		// 搜集与查询关联对象
		RelationSolver relationSolver=new RelationSolver(this.dao());
		List<ReferCause.Names> namesList=new ArrayList<>();
		for (PropertyRoute prop : matchedProps) {
			SQL sql=relationSolver.buildReferStatement(prop,false,ids,finalNameField,this.nameField());
			RcdSet rs=dao().query(sql);
			namesList.addAll(rs.toEntityList(ReferCause.Names.class));
		}
		for (PropertyRoute prop : matchedPropsR) {
			SQL sql=relationSolver.buildReferStatement(prop,true,ids,finalNameField,this.nameField());
			RcdSet rs=dao().query(sql);
			namesList.addAll(rs.toEntityList(ReferCause.Names.class));
		}

		Map<T,List<ReferCause.Names>> map=(Map<T,List<ReferCause.Names>>)CollectorUtil.groupBy(namesList,ReferCause.Names::getLocalId);

		return map;

	}

	private static final String[] REFER_NAME_FIELDS={"name","title","label","text"};
	private String guessReferNameColumn(String tableName) {
		DBColumnMeta nameColumn=null;
		for (String field : REFER_NAME_FIELDS) {
			nameColumn=this.dao().getTableColumnMeta(tableName,field);
			if(nameColumn!=null) {
				return nameColumn.getColumn();
			}
		}

		DBTableMeta tm=this.dao().getTableMeta(tableName);
		for (String field : REFER_NAME_FIELDS) {
			for (int i = 0; i < tm.getColumns().size(); i++) {
				nameColumn = tm.getColumns().get(i);
				if(nameColumn.getColumn().toLowerCase().endsWith(field)) {
					return nameColumn.getColumn();
				}
			}
		}
		return null;
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
		List<Entity> entities = null;
		if(this.isSupportCache()) {
			entities = (List<Entity>) this.queryList(in.toConditionExpr());
		}
		Delete delete=new Delete(this.table());
		delete.where().and(in);
		Integer i=dao().execute(delete);
		boolean suc= i!=null && i>0;
		if(suc){
			if(entities!=null){
				this.dispatchJoinCacheInvalidEvent(CacheInvalidEventType.DELETE,entities,null);
			}
			return ErrorDesc.success();
		}
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
		List<Entity> entities = null;
		if(this.isSupportCache()) {
			entities = (List<Entity>) this.queryList(in.toConditionExpr());
		}
		Object trueValue=dao().getDBTreaty().getTrueValue();
		Expr expr=new Expr("update "+table()+" set "+dao().getDBTreaty().getDeletedField()+" = ? ",trueValue);
		expr.append(in.toConditionExpr().startWithWhere());
		Integer i=dao().execute(expr);
		boolean suc= i!=null && i>0;
		if(suc) {
			if(entities!=null){
				this.dispatchJoinCacheInvalidEvent(CacheInvalidEventType.DELETE,entities,null);
			}
			return ErrorDesc.success();
		}
		else return ErrorDesc.failure();
	}

	public <T> List<T> queryValues(DBField field, Class<T> type, ConditionExpr condition) {
		return queryValues(field,type,condition,null);
	}

	public <T> List<T> queryValues(DBField field, Class<T> type, ConditionExpr condition,OrderBy orderBy) {

		Expr expr=new Expr("select "+field.name() +" from "+field.table().name()+" "+TABLE_ALAIS);

		Where where=new Where();
		ConditionExpr dbTreatyCondition=this.buildDBTreatyCondition(TABLE_ALAIS);
		where.and(dbTreatyCondition);
		if(condition!=null) {
			where.and(condition);
		}

		expr.append(where);

		if(orderBy!=null) {
			expr.append(orderBy);
		}

		RcdSet rs=dao().query(expr);
		return rs.getValueList(field.name(), type);
	}

	public <T> List<T> queryValues(DBField field, Class<T> type, String condition,Object... ps) {
		return queryValues(field, type, new ConditionExpr(condition, ps),null);
	}


	/**
	 * 构建 Excel 结构
	 * @param  isForExport 是否用于数据导出
	 * @return   ExcelStructure
	 * */
	public ExcelStructure buildExcelStructure(boolean isForExport) {
		ExcelStructure es=new ExcelStructure();
		es.setDataRowBegin(2);
		DBTableMeta tm=dao().getTableMeta(this.table());
		List<DBColumnMeta> pks=tm.getPKColumns();
		List<DBColumnMeta> cms=tm.getColumns();
		int index=0;
		String charIndex="";
		for (DBColumnMeta pk:pks) {
			charIndex= ExcelUtil.toExcel26(index);
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
			charIndex=ExcelUtil.toExcel26(index);
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

//		if(tm!=null) {
//			try {
//				Thread.sleep(5000);
//			} catch (Exception e){}
//			throw new RuntimeException("XXX");
//		}

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
		SheetWrapper sheet=ew.fillSheet(rs, tm.getShortTopic()+"清单",es);
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
				Object pkValue=r.getValue(pk.getColumn());
				if(StringUtil.isBlank(pkValue)) {
					hasPkValue=false;
					break;
				}
			}
			if(hasPkValue) {
				Update update=SQLBuilder.buildUpdate(r,SaveMode.ALL_FIELDS,this.table,this.dao());
				//设置更新时间、更新人
				if(tm.getColumn(dbTreaty.getUpdateTimeField())!=null) {
					update.set(dbTreaty.getUpdateTimeField(),new Date());
				}
				if(tm.getColumn(dbTreaty.getUpdateUserIdField())!=null && dbTreaty.getLoginUserId()!=null) {
					update.set(dbTreaty.getUpdateUserIdField(), dbTreaty.getLoginUserId());
				}
				if(batch) {
					sqls.add(update);
				} else {
					this.dao().execute(update);
				}
			} else {
				Insert insert = SQLBuilder.buildInsert(r,this.table(),this.dao(), true);
				//设置创建时间、创建人
				if(tm.getColumn(dbTreaty.getCreateTimeField())!=null) {
					insert.set(dbTreaty.getCreateTimeField(),new Date());
				}
				if(tm.getColumn(dbTreaty.getCreateUserIdField())!=null && dbTreaty.getLoginUserId()!=null) {
					insert.set(dbTreaty.getCreateUserIdField(), dbTreaty.getLoginUserId());
				}
				// 租户
				if(tm.getColumn(dbTreaty.getTenantIdField())!=null && dbTreaty.getActivedTenantId()!=null) {
					insert.set(dbTreaty.getTenantIdField(), dbTreaty.getActivedTenantId());
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
	 * 按主键查询
	 * */
	public E queryEntityById(E sample) {
		 return this.dao().queryEntity(sample,true);
	}

	/**
	 * 按唯一键查询，并返回 Map
	 * */
	public <T> Map<T,E> queryMapByUKeys(DBField ukeyField,List<T> ukValues, Function<E, T> mapKey) {
		return queryMapByUKeys(ukeyField.name(),ukValues,mapKey);
	}
	/**
	 * 按唯一键查询，并返回 Map
	 * */
	public <T> Map<T,E> queryMapByUKeys(String ukeyField,List<T> ukValues, Function<E, T> mapKey) {
		if(ukValues==null || ukValues.isEmpty()) {
			return new HashMap<>();
		}
		List<E> list = this.queryListByUKeys(ukeyField,ukValues);
		return CollectorUtil.collectMap(list,mapKey,(e)->{return e;});
	}

	public <T> E queryListByUKey(String ukeyField,T ukValue) {
		List<E> list=queryListByUKeys(ukeyField,Arrays.asList(ukValue));
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
	}
	public <T> E queryListByUKey(DBField ukeyField,T ukValue) {
		return queryListByUKey(ukeyField.name(),ukValue);
	}

	public <T> List<E> queryListByUKeys(DBField ukeyField, List<T> ukValues) {
		return queryListByUKeys(ukeyField.name(),ukValues);
	}

	public <T> List<E> queryListByUKeys(String ukeyField, List<T> ukValues) {
		List<E> list = new ArrayList<>();
		if(ukValues==null || ukValues.isEmpty()) {
			return list;
		}
		DBTableMeta tm=dao().getTableMeta(table());
		DBColumnMeta ukey=tm.getColumn(ukeyField);
		if(ukey==null) throw new IllegalArgumentException("字段 "+ukeyField+" 不是 "+this.table()+" 的字段");
		DBColumnMeta deletedField=tm.getColumn(dao().getDBTreaty().getDeletedField());
		Select select=new Select();
		select.from(table()).where().andIn(ukey.getColumn(),ukValues);
		if(deletedField!=null) {
			select.where().andEquals(dao().getDBTreaty().getDeletedField(),dao().getDBTreaty().getFalseValue());
		}
		DBColumnMeta tenantIdField=tm.getColumn(dao().getDBTreaty().getTenantIdField());
		Object tenantId=dao().getDBTreaty().getActivedTenantId();
		if(tenantIdField!=null && tenantId!=null) {
			select.where().and(tenantIdField.getColumn()+" = ?",tenantId);
		}
		list=(List<E>)dao().queryEntities(this.getPoType(),select);
		return list;
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
	public void saveRelation(Class masterPoType,DBField masterIdField, Object masterId,Class salvePoType,DBField slaveIdField,List slaveIds,boolean clearWhenEmpty) {
		if(slaveIds==null) return;
		if(slaveIds.isEmpty() && !clearWhenEmpty) return;


		DataCacheManager dcm=dao().getDataCacheManager();
		//Entity master=null;
		String slavePoTable = null ;
		List<Entity> valuesBefore=null;
		List<Entity> valuesAfter=null;
		if(dcm.isSupportAccurateCache(salvePoType)) {
			//master=(Entity)dao().queryEntity(this.getPoType(), "select * from " + slaveIdField.table().name() + " where " + masterIdField.name() + " = ?", masterId);
			slavePoTable=EntityUtil.getDBTable(salvePoType).name();
			DBTableMeta tm=dao().getTableMeta(slavePoTable);
			if(tm!=null && tm.getPKColumnCount()==1) {
				String pkField=tm.getPKColumns().get(0).getColumn();
				if(slaveIds!=null && slaveIds.size()>0) {
					In in = new In(pkField, slaveIds);
					valuesAfter = dao().queryEntities(salvePoType, "select * from " + slavePoTable +" "+ in.toConditionExpr().startWithWhere().getListParameterSQL(),in.getListParameters());
				}
				valuesBefore = dao().queryEntities(salvePoType, "select * from " + slavePoTable +" where "+ masterIdField.name()+" = ? ",masterId);
			}
		}

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

		if(valuesBefore!=null && valuesAfter!=null) {
			// 此处需要再细致考虑
			dcm.dispatchJoinCacheInvalidEvent(CacheInvalidEventType.UPDATE,slavePoTable,valuesBefore,valuesAfter);
		}

	}



}
