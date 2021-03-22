package com.github.foxnic.dao.entity;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.spec.DAO;

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
	default String table() {
		Type[] t=this.getClass().getGenericInterfaces();
		Class intf=this.getClass().getInterfaces()[0];
		intf=intf.getInterfaces()[0];
		intf.getGenericSuperclass();
		 //intf.getTypeParameters()
		//ParameterizedType pt=((ParameterizedType)
		//Object xx=(Class<E>) pt.getActualTypeArguments()[0];
//		ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getSuperclass().getGenericSuperclass();
//		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
//		for(Type actualTypeArgument: actualTypeArguments) {
//		    System.out.println(actualTypeArgument);
//		}
 
		if(t.length>0) {
			Class type=(Class)t[0];
			String table=com.github.foxnic.sql.entity.EntityUtil.getAnnotationTable(type);
			return table;
		} else {
			return null;
		}
	}
	
	
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
		Object logincDeleteValue=BeanUtil.getFieldValue(sample, dao().getDBTreaty().getDeletedField());
		if(logincDeleteValue==null) {
			if(dao().getDBTreaty().isAutoCastLogicField()) {
				BeanUtil.setFieldValue(sample, dao().getDBTreaty().getDeletedField(),false);
			} else {
				BeanUtil.setFieldValue(sample, dao().getDBTreaty().getDeletedField(),dao().getDBTreaty().getFalseValue());
			}
		}
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
	
	
	
	
	
	
 
}
