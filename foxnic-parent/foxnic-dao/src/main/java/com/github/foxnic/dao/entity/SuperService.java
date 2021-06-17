package com.github.foxnic.dao.entity;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.github.foxnic.commons.io.StreamUtil;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.excel.ExcelColumn;
import com.github.foxnic.dao.excel.ExcelReader;
import com.github.foxnic.dao.excel.ExcelStructure;
import com.github.foxnic.dao.excel.ExcelWriter;
import com.github.foxnic.dao.sql.SQLBuilder;
import com.github.foxnic.sql.expr.*;
import com.github.foxnic.sql.treaty.DBTreaty;
import org.springframework.transaction.annotation.Transactional;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.entity.EntityUtil;
import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.meta.DBField;

public abstract class SuperService<E> implements ISuperService<E> {
	
	 
	
	/**
	 * 获得 DAO 对象
	 * */
	abstract public DAO dao();
	
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
	/**
	 * 查询全部符合条件的数据
	 *
	 * @param sample 查询条件
	 * @return 查询结果 , News清单
	 */
	public List<E> queryList(E sample,ConditionExpr condition,OrderBy orderBy) {
		//构建查询条件
		ConditionExpr ce = buildQueryCondition(sample);
 
		Expr select=new Expr("select * from "+table());
		select.append(ce.startWithWhere());
		if(condition!=null) {
			if(ce!=null && !ce.isEmpty()) {
				select.append(condition.startWithAnd());
			} else {
				select.append(condition.startWithWhere());
			}
		}
		if(orderBy==null) {
			DBColumnMeta cm=dao().getTableColumnMeta(table(), dao().getDBTreaty().getCreateTimeField());
			if(cm!=null) {
				orderBy=OrderBy.byDescNullsLast(cm.getColumn());
			}
		}
		if(orderBy!=null) {
			select.append(orderBy);
		}
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
	 * @param orderBy 排序
	 * @return 查询结果 , 数据清单
	 */
	@Override
	public PagedList<E> queryPagedList(E sample,int pageSize,int pageIndex) {
		OrderBy orderBy = buildOrderBy(sample);
		return queryPagedList(sample, this.buildQueryCondition(sample), orderBy, pageSize, pageIndex);
	}

	public OrderBy buildOrderBy(E sample) {
		String sortField=BeanUtil.getFieldValue(sample, "sortField",String.class);
		String sortType=BeanUtil.getFieldValue(sample, "sortType",String.class);
		OrderBy orderBy=null;
		if(!StringUtil.isBlank(sortField) && !StringUtil.isBlank(sortType)) {
			DBColumnMeta cm=dao().getTableMeta(this.table()).getColumn(sortField);
			if(cm==null) {
				sortField=BeanNameUtil.instance().depart(sortField);
				cm=dao().getTableMeta(this.table()).getColumn(sortField);
			}
			if(cm!=null) {
				if("asc".equalsIgnoreCase(sortType)) {
					orderBy=OrderBy.byAscNullsLast(sortField);
				}
				else if("desc".equalsIgnoreCase(sortType)) {
					orderBy=OrderBy.byDescNullsLast(sortField);
				}
			}
		}
		return orderBy;
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
		return queryPagedList(sample, this.buildQueryCondition(sample), orderBy, pageSize, pageIndex);
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
		//设置删除标记
		dao().getDBTreaty().updateDeletedFieldIf(sample,false);
		//构建查询条件
		ConditionExpr ce = buildQueryCondition(sample);
 
		DBColumnMeta cm=null;
 
		Expr select=new Expr("select * from "+table());
		select.append(ce.startWithWhere());
		if(condition!=null) {
			select.append(condition.startWithAnd());
		}
		
		if(orderBy==null) {
			cm=dao().getTableColumnMeta(table(), dao().getDBTreaty().getCreateTimeField());
			if(cm!=null) {
				orderBy=OrderBy.byDesc(cm.getColumn());
			}
		}
		
		if(orderBy!=null) {
			select.append(orderBy);
		}
		//执行查询
		return dao().queryPagedEntities((Class<E>)sample.getClass(), pageSize,pageIndex, select);
	}

	/**
	 * 根据实体数构建默认的条件表达式
	 * @param sample 数据样例
	 * @param stringFuzzy 字符串是否使用模糊匹配
	 * @return ConditionExpr 条件表达式
	 * */
	public ConditionExpr buildQueryCondition(E sample,boolean stringFuzzy) {
		return buildQueryCondition(sample, stringFuzzy,null);
	}
	
	/**
	 * 根据实体数构建默认的条件表达式，字符串使用模糊匹配
	 * @param sample 数据样例
	 * @return ConditionExpr 条件表达式
	 * */
	public ConditionExpr buildQueryCondition(E sample) {
		return buildQueryCondition(sample, true,null);
	}
	
	/**
	 * 根据实体数构建默认的条件表达式, 字符串是否使用模糊匹配
	 * @param sample 数据样例
	 * @param tableAliase 数据表别名
	 * 	@return ConditionExpr 条件表达式
	 * */
	public ConditionExpr buildQueryCondition(E sample,String tableAliase) {
		return buildQueryCondition(sample, true, tableAliase);
	}
	
	/**
	 * 根据实体数构建默认的条件表达式
	 * @param sample 数据样例
	 * @param stringFuzzy 字符串是否使用模糊匹配
	 * @param tableAliase 数据表别名
	 * @return ConditionExpr 条件表达式
	 * */
	public ConditionExpr buildQueryCondition(E sample,boolean stringFuzzy,String tableAliase) {
		
		ConditionExpr ce=new ConditionExpr();
		
		
		
		if(!StringUtil.isBlank(tableAliase)) {
			tableAliase=StringUtil.trim(tableAliase, ".");
			tableAliase=tableAliase+".";
		} else {
			tableAliase="";
		}
		
		//加入逻辑删除判断
		String deletedField=dao().getDBTreaty().getDeletedField();
		DBColumnMeta dcm=dao().getTableMeta(this.table()).getColumn(deletedField);
		if(dcm!=null) {
			ce.and(tableAliase+deletedField+"= ?",dao().getDBTreaty().getFalseValue());
		}
		
		Object value=null;
		
		// 设置默认搜索
		String searchField=BeanUtil.getFieldValue(sample, "searchField",String.class);
		String searchValue=BeanUtil.getFieldValue(sample, "searchValue",String.class);
		if(!StringUtil.isBlank(searchField) && !StringUtil.isBlank(searchValue)) {
			BeanUtil.setFieldValue(sample, searchField, searchValue);
		}
		
		
		
		List<DBColumnMeta> cms= dao().getTableMeta(this.table()).getColumns();
		
		// 按属性设置默认搜索
		for (DBColumnMeta cm : cms) {
			value=BeanUtil.getFieldValue(sample, cm.getColumn());
			if(value==null) continue;
			if(cm.getDBDataType()==DBDataType.STRING) {
				if(stringFuzzy) {
					ce.and(tableAliase+cm.getColumn()+" like ?", "%"+value.toString()+"%");
				} else {
					ce.and(tableAliase+cm.getColumn()+" = ?", value.toString());
				}
			} else if(cm.getDBDataType()==DBDataType.BOOL) {
				if(dao().getDBTreaty().isAutoCastLogicField() && DataParser.isBooleanType(value)) {
					Boolean bool=DataParser.parseBoolean(value);
					value=dao().getDBTreatyLogicValue(bool);
				}
				ce.and(tableAliase+cm.getColumn()+" = ?", value);
			}
			else {
				ce.and(tableAliase+cm.getColumn()+" = ?", value);
			}
		}
		return ce;
	}
	
	
	/**
	 * 添加
	 *
	 * @param entity 数据对象
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	public boolean insert(E entity) {
		EntityUtils.setId(entity,this);
		return dao().insertEntity(entity);
	}
	
	/**
	 * 批量插入实体
	 * */
	@Transactional
	public boolean insertList(List<E> list) {
		for (E e : list) {
			insert(e);
		}
		return true;
	}
	
	
	/**
	 * 更新
	 *
	 * @param entity 数据对象
	 * @param mode SaveMode,数据更新的模式
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	public boolean update(E entity , SaveMode mode) {
		return dao().updateEntity(entity, mode);
	}
	
	/**
	 * 批量插入实体
	 * */
	@Transactional
	public boolean updateList(List<E> list,SaveMode mode) {
		for (E e : list) {
			update(e,mode);
		}
		return true;
	}
 
	/**
	 * 保存实体，如果主键值不为 null，则更新，否则插入
	 * */
	public boolean save(E entity , SaveMode mode) {
		
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
		
		if(hasPkValue) {
			return this.update(entity, mode);
		} else {
			return this.insert(entity);
		}

	}
	
	/**
	 * 保存实体，如果主键值不为null，则更新，否则插入
	 * */
	@Transactional
	public boolean saveList(List<E> list , SaveMode mode) {
		for (E e : list) {
			save(e,mode);
		}
		return true;
	}
	
	/**
	 * 检查是否存在
	 * @param entity 被检查的实体数据
	 * @param field DB字段
	 * @param value 字段值
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
	
	
	/**
	 * 按主键批量删除产品标签
	 *
	 * @param id 编号 , 详情 : 编号
	 * @return 删除完成情况
	 */
	public <T> boolean deleteByIdsPhysical(List<T> ids) {
		if(ids==null) throw new IllegalArgumentException("id 列表不允许为 null ");
		DBTableMeta cm=dao().getTableMeta(table());
		if(cm.getPKColumnCount()!=1) {
			throw new IllegalArgumentException("主键数量不符合要求，要求1个主键");
		}
		String idField=cm.getPKColumns().get(0).getColumn();
		In in=new In(idField,ids);
		Integer i=dao().execute("delete from "+table()+" "+in.toConditionExpr().startWithWhere().getListParameterSQL(),in.getListParameters());
		return i!=null && i>0;
	}
	
	/**
	 * 按主键批量删除产品标签
	 *
	 * @param id 编号 , 详情 : 编号
	 * @return 删除完成情况
	 */
	public <T> boolean deleteByIdsLogical(List<T> ids) {
		if(ids==null) throw new IllegalArgumentException("id 列表不允许为 null ");
		DBTableMeta cm=dao().getTableMeta(table());
		if(cm.getPKColumnCount()!=1) {
			throw new IllegalArgumentException("主键数量不符合要求，要求1个主键");
		}
		String idField=cm.getPKColumns().get(0).getColumn();
		In in=new In(idField,ids);
		Object trueValue=dao().getDBTreaty().getTrueValue();
		Expr expr=new Expr("update "+table()+" set "+dao().getDBTreaty().getDeletedField()+" = ? ",trueValue);
		expr.append(in.toConditionExpr().startWithWhere());
		Integer i=dao().execute(expr);
		return i!=null && i>0;
	}
	
 
	public <T> List<T> queryValues(DBField field, Class<T> type, ConditionExpr condition) {
		condition.startWithWhere();
		RcdSet rs=dao().query("select "+field.name() +" from "+field.table().name()+condition.getListParameterSQL(),condition.getListParameters());
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
					if (dao().getDBTreaty().isDBTreatyFiled(cm.getColumn())) continue;
				}
			} else {
				if (dao().getDBTreaty().isDBTreatyFiled(cm.getColumn())) continue;
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

		DBTableMeta tm=this.dao().getTableMeta(this.table);
		//拼接语句
		Expr select=new Expr("select * from "+this.table());
		ConditionExpr condition = this.buildQueryCondition(sample);
		select.append(condition.startWithWhere());
		//查询数据
		RcdSet rs=this.dao().query(select);
		//写入
		ExcelWriter ew=new ExcelWriter();
		ExcelStructure es=buildExcelStructure(true);
		ew.fillSheet(rs, tm.getTopic()+"清单",es);
		ew.setWorkBookName(tm.getTopic()+"清单.xlsx");
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

		ew.fillSheet(rs, tm.getTopic()+"模板", es);
		ew.setWorkBookName(tm.getTopic()+"模板.xlsx");
		return ew;
	}

	/**
	 * 导入 Excel 数据
	 * */
	public String importExcel(InputStream input) {

		ExcelReader er=null;
		try {
			er=new ExcelReader(input);
		} catch (Exception e) {
			return "缺少文件";
		}
		//构建 Excel 结构
		ExcelStructure es=buildExcelStructure(false);
		//装换成记录集
		RcdSet rs=null;
		try {
			rs=er.read(1,es);
		} catch (Exception e) {
			return "Excel 读取失败";
		}

		DBTableMeta tm=dao().getTableMeta(this.table());
		List<DBColumnMeta> pks=tm.getPKColumns();
		DBTreaty  dbTreaty= dao().getDBTreaty();
		//从记录集插入表
		boolean hasPkValue=true;
		for (Rcd r : rs) {
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
				this.dao().execute(update);
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
				this.dao().execute(insert);
			}
		}
		return null;
	}

 
}
