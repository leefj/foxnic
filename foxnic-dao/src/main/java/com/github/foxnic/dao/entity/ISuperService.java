package com.github.foxnic.dao.entity;

import java.util.List;

import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.expr.OrderBy;
import com.github.foxnic.sql.meta.DBField;

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
	 * 根据实体数构建默认的条件表达式
	 * @param sample 数据样例
	 * @param stringFuzzy 字符串是否使用模糊匹配
	 * @return ConditionExpr 条件表达式
	 * */
	ConditionExpr buildQueryCondition(E sample,boolean stringFuzzy);
	
	/**
	 * 根据实体数构建默认的条件表达式，字符串使用模糊匹配
	 * @param sample 数据样例
	 * @return ConditionExpr 条件表达式
	 * */
	ConditionExpr buildQueryCondition(E sample);
	
	/**
	 * 根据实体数构建默认的条件表达式, 字符串是否使用模糊匹配
	 * @param sample 数据样例
	 * @param tableAliase 数据表别名
	 * 	@return ConditionExpr 条件表达式
	 * */
	ConditionExpr buildQueryCondition(E sample,String tableAliase);
	
	/**
	 * 根据实体数构建默认的条件表达式
	 * @param sample 数据样例
	 * @param stringFuzzy 字符串是否使用模糊匹配
	 * @param tableAliase 数据表别名
	 * @return ConditionExpr 条件表达式
	 * */
	ConditionExpr buildQueryCondition(E sample,boolean stringFuzzy,String tableAliase);
	
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
	boolean insertList(List<E> list);
	
	/**
	 * 更新实体
	 * */
	boolean update(E entity , SaveMode mode);
	
	
	/**
	 * 更新实体
	 * */
	boolean updateList(List<E> list , SaveMode mode);
	
	/**
	 * 保存实体，如果主键值不为null，则更新，否则插入
	 * */
	boolean save(E entity , SaveMode mode);
	
	/**
	 * 保存实体，如果主键值不为null，则更新，否则插入
	 * */
	boolean saveList(List<E> list , SaveMode mode);
	
	/**
	 * 检查实体存在性
	 * */
	boolean checkExists(E entity,DBField... field);
	
	/**
	 * 物理删除
	 * */
	<T> boolean deleteByIdsPhysical(List<T> ids);
	/**
	 * 逻辑删除
	 * */
	<T> boolean deleteByIdsLogical(List<T> ids);
 
}
