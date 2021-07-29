package com.github.foxnic.dao.entity;

import com.github.foxnic.api.transter.Result;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.excel.ExcelStructure;
import com.github.foxnic.dao.excel.ExcelWriter;
import com.github.foxnic.dao.excel.ValidateResult;
import com.github.foxnic.dao.relation.JoinResult;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.expr.OrderBy;
import com.github.foxnic.sql.meta.DBField;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ISuperService<E extends Entity> {
	
	/**
	 * 获得DAO对象
	 * */
	DAO dao();
	/**
	 * 获得对应的数据表
	 * */
	String table();

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
	 *
	 * @return*/
	Result insert(E entity);
	
	
	/**
	 * 批量插入实体
	 *
	 * @return*/
	Result insertList(List<E> list);
	
	/**
	 * 更新实体
	 *
	 * @return*/
	Result update(E entity , SaveMode mode);
	
	
	/**
	 * 更新实体
	 *
	 * @return*/
	Result updateList(List<E> list , SaveMode mode);
	
	/**
	 * 保存实体，如果主键值不为null，则更新，否则插入
	 *
	 * @return*/
	Result save(E entity , SaveMode mode);
	
	/**
	 * 保存实体，如果主键值不为null，则更新，否则插入
	 *
	 * @return*/
	Result saveList(List<E> list , SaveMode mode);
	
	/**
	 * 检查实体存在性
	 * */
	boolean checkExists(E entity,DBField... field);
	
	/**
	 * 物理删除
	 *
	 * @return*/
	<T> Result deleteByIdsPhysical(List<T> ids);
	/**
	 * 逻辑删除
	 *
	 * @return*/
	<T> Result deleteByIdsLogical(List<T> ids);

	/**
	 * 导出 Excel
	 * */
	ExcelWriter exportExcel(E sample);

	/**
	 * 导出用于数据导入的 Excel 模版
	 * */
	ExcelWriter  exportExcelTemplate();

	/**
	 * 构建 Excel 结构
	 * @param  isForExport 是否用于数据导出
	 * @return   ExcelStructure
	 * */
	ExcelStructure buildExcelStructure(boolean isForExport);

	/**
	 * 导入 Excel 数据
	 * @param  input  输入流
	 * @param  sheetIndex sheet 的序号，第一个 sheet 的编号是 0
	 * @return  错误信息，成功时返回 null
	 * */
	List<ValidateResult> importExcel(InputStream input, int sheetIndex, boolean batch);

	/**
	 * join 出单个实体的关联数据
	 * @param po 数据实体
	 * @param targetType  需要关联的实体类型，可多个
	 * @return 返回 join 的结果
	 * */
	Map<String, JoinResult> join(E po, Class... targetType);

	/**
	 * join 出单个实体的关联数据
	 * @param po 数据实体
	 * @param properties  需要关联的属性，可多个
	 * @return 返回 join 的结果
	 * */
	<T extends Entity> Map<String, JoinResult<E, T>> join(E po, String... properties);

	/**
	 * join 出单个实体的关联数据
	 * @param pos 数据实体清单
	 * @param targetType  需要关联的实体类型，可多个
	 * @return 返回 join 的结果
	 * */
	Map<String, JoinResult> join(Collection<E> pos, Class... targetType);

	/**
	 * join 出单个实体的关联数据
	 * @param pos 数据实体清单
	 * @param properties  需要关联的属性，可多个
	 * @return 返回 join 的结果
	 * */
	<T extends Entity> Map<String, JoinResult<E,T>> join(Collection<E> pos, String... properties);

	/**
	 * join 出单个实体的关联数据
	 * @param pos 数据实体清单
	 * @param targetType  需要关联的实体类型，可多个
	 * @return 返回 join 的结果
	 * */
	Map<String, JoinResult> join(PagedList<E> pos, Class... targetType);

	/**
	 * join 出单个实体的关联数据
	 * @param pos 数据实体清单
	 * @param properties  需要关联的属性，可多个
	 * @return 返回 join 的结果
	 * */
	<T extends Entity> Map<String, JoinResult<E,T>> join(PagedList<E> pos, String... properties);
}
