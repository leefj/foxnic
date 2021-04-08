package com.github.foxnic.dao.entity;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.entity.EntityUtil;
import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.In;
import com.github.foxnic.sql.expr.Where;
import com.github.foxnic.sql.meta.DBDataType;

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
	/**
	 * 获得对应的数据表
	 * */
	/**
	 * 数据表
	 * */
	public String table() {
		if(table!=null) return table;
		ParameterizedType type=(ParameterizedType)this.getClass().getGenericSuperclass();
		Type[] types=type.getActualTypeArguments();
		Class poType=(Class)types[0];
		table=EntityUtil.getAnnotationTable(poType);
		return table;
		
	}
	
	
	/**
	 * 查询全部符合条件的数据
	 *
	 * @param sample 查询条件
	 * @return 查询结果 , News清单
	 */
	public List<E> queryEntities(E sample) {
		//构建查询条件
		ConditionExpr ce = buildQueryCondition(sample);
		return dao().queryEntities((Class<E>)sample.getClass(),ce);
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
	 * 分页查询符合条件的数据
	 *
	 * @param sample 查询条件
	 * @return 查询结果 , News清单
	 */
	public PagedList<E> queryPagedEntities(E sample,int pageSize,int pageIndex) {
		//设置删除标记
		dao().getDBTreaty().updateDeletedFieldIf(sample,false);
		//构建查询条件
		ConditionExpr ce = buildQueryCondition(sample);
		//执行查询
		return dao().queryPagedEntities((Class<E>)sample.getClass(), pageSize, pageIndex, ce);
	}

	
	protected ConditionExpr buildQueryCondition(E sample) {
		
		Object value=null;
		
		// 设置默认搜索
		String searchField=BeanUtil.getFieldValue(sample, "searchField",String.class);
		String searchValue=BeanUtil.getFieldValue(sample, "searchValue",String.class);
		if(!StringUtil.isBlank(searchField) && !StringUtil.isBlank(searchValue)) {
			BeanUtil.setFieldValue(sample, searchField, searchValue);
		}
		
		
		ConditionExpr ce=new ConditionExpr();
		List<DBColumnMeta> cms= dao().getTableMeta(this.table()).getColumns();
		
		// 按属性设置默认搜索
		for (DBColumnMeta cm : cms) {
			value=BeanUtil.getFieldValue(sample, cm.getColumn());
			if(value==null) continue;
			if(cm.getDBDataType()==DBDataType.STRING) {
				ce.and(cm.getColumn()+" like ?", "%"+value.toString()+"%");
			} else {
				ce.and(cm.getColumn()+" = ?", value);
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
	public boolean insertEntity(E entity) {
		//EntityContext.setId(entity,this);
		return dao().insertEntity(entity);
	}
	
	/**
	 * 更新
	 *
	 * @param entity 数据对象
	 * @param mode SaveMode,数据更新的模式
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	public boolean updateEntity(E entity , SaveMode mode) {
		return dao().updateEntity(entity, mode);
	}
	
	/**
	 * 检查是否存在
	 * @param entity 被检查的实体数据
	 * @param field DB字段
	 * @param value 字段值
	 * */
	public boolean checkExists(E entity,String field) {
		String table=this.table();
		Object value =BeanUtil.getFieldValue(entity, field);
		Where ce=new Where(field+" = ?",value);
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
		Integer i=dao().execute("delete from "+table()+in.toConditionExpr().startWithWhere().getListParameterSQL(),in.getListParameters());
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
	
 
}
