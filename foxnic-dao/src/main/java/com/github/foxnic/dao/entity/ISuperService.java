package com.github.foxnic.dao.entity;

import java.util.List;

import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.expr.OrderBy;

public interface ISuperService<E> {
	
	/**
	 * 获得DAO对象
	 * */
	DAO dao();
	/**
	 * 获得对应的数据表
	 * */
	String table();
	
	/**
	 * 查询实体集合
	 * */
	List<E> queryList(E sample);
	
	
	/**
	 * 查询实体集合
	 * */
	List<E> queryList(E sample,ConditionExpr condition,OrderBy orderBy);
	
	/**
	 * 查询实体集合
	 * */
	List<E> queryList(E sample,OrderBy orderBy);
	
	/**
	 * 查询实体集合
	 * */
	List<E> queryList(E sample,ConditionExpr condition);
	
	/**
	 * 查询单个实体
	 * */
	E queryEntity(E sample);
	
	/**
	 * 分页查询实体集
	 * */
	PagedList<E> queryPagedList(E sample,int pageSize,int pageIndex);
	
	/**
	 * 分页查询实体集
	 * */
	PagedList<E> queryPagedList(E sample,ConditionExpr condition,OrderBy orderBy,int pageSize,int pageIndex);
	
	/**
	 * 分页查询实体集
	 * */
	PagedList<E> queryPagedList(E sample,ConditionExpr condition,int pageSize,int pageIndex);
	
	/**
	 * 分页查询实体集
	 * */
	PagedList<E> queryPagedList(E sample,OrderBy orderBy,int pageSize,int pageIndex);
 
	
	/**
	 * 插入实体
	 * */
	boolean insert(E entity);
	
	
	/**
	 * 批量插入实体
	 * */
	boolean insertList(List<E> entity);
	
	/**
	 * 更新实体
	 * */
	boolean update(E entity , SaveMode mode);
	
	
	/**
	 * 更新实体
	 * */
	boolean updateList(List<E> entity , SaveMode mode);
	
	/**
	 * 保存实体，如果主键值不为null，则更新，否则插入
	 * */
	boolean save(E entity , SaveMode mode);
	
	/**
	 * 保存实体，如果主键值不为null，则更新，否则插入
	 * */
	boolean saveList(List<E> entity , SaveMode mode);
	
	/**
	 * 检查实体存在性
	 * */
	boolean checkExists(E entity,String... field);
	
	/**
	 * 物理删除
	 * */
	<T> boolean deleteByIdsPhysical(List<T> ids);
	/**
	 * 逻辑删除
	 * */
	<T> boolean deleteByIdsLogical(List<T> ids);
	
	
}
