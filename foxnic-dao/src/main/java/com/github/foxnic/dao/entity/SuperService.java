package com.github.foxnic.dao.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.entity.EntityUtil;
import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.expr.Where;

public interface SuperService<E> {
	
	/**
	 * 获得 DAO 对象
	 * */
	DAO dao();
	
	/**
	 * 生成ID
	 * */
	default Object generateId(Field field) { return null; };
	
	
	/**
	 * 获得对应的数据表
	 * */
	String table();
	
	
	/**
	 * 查询全部符合条件的数据
	 *
	 * @param sample 查询条件
	 * @return 查询结果 , News清单
	 */
	default List<E> queryEntities(E sample) {
		return dao().queryEntities(sample);
	}
	
	/**
	 * 查询符合条件的数据,并返回第一个，如果没有则返回 null
	 *
	 * @param sample 查询条件
	 * @return 查询结果 , News清单
	 */
	default E queryEntity(E sample) {
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
	default PagedList<E> queryPagedEntities(E sample,int pageSize,int pageIndex) {
		//设置删除标记
		dao().getDBTreaty().updateDeletedFieldIf(sample,false);
		return dao().queryPagedEntities(sample, pageSize, pageIndex);
	}
	
	
	/**
	 * 添加
	 *
	 * @param entity 数据对象
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	default boolean insertEntity(E entity) {
		EntityContext.setId(entity,this);
		return dao().insertEntity(entity);
	}
	
	/**
	 * 更新
	 *
	 * @param entity 数据对象
	 * @param mode SaveMode,数据更新的模式
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	default boolean updateEntity(E entity , SaveMode mode) {
		return dao().updateEntity(entity, mode);
	}
	
	/**
	 * 检查是否存在
	 * @param entity 被检查的实体数据
	 * @param field DB字段
	 * @param value 字段值
	 * */
	default boolean checkExists(E entity,String field) {
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
	
	
	
	
 
}
