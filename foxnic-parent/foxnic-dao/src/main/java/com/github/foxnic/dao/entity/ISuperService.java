package com.github.foxnic.dao.entity;

import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.cache.DoubleCache;
import com.github.foxnic.dao.cache.CacheStrategy;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.excel.ExcelStructure;
import com.github.foxnic.dao.excel.ExcelWriter;
import com.github.foxnic.dao.excel.ValidateResult;
import com.github.foxnic.dao.relation.JoinResult;
import com.github.foxnic.dao.relation.cache.CacheInvalidEventType;
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
	 * 根据实体数构建默认的条件表达式, 不支持 Join 其它表
	 * @param sample 数据样例
	 * @return ConditionExpr 条件表达式
	 * */
	ConditionExpr buildQueryCondition(E sample);

	/**
	 * 根据实体数构建默认的条件表达式, 不支持 Join 其它表
	 * @param sample 数据样例
	 * @param tableAlias 数据表别名
	 * 	@return ConditionExpr 条件表达式
	 * */
	ConditionExpr buildQueryCondition(E sample,String tableAlias);

	/**
	 * 查询实体集合
	 * @param  sample 查询条件，等值查询
	 * @return  实体集合
	 * */
	List<E> queryList(E sample);


	/**
	 * 查询实体集合
	 * @param  sample 查询条件，等值查询
	 * @param  condition 额外的查询条件
	 * @param  orderBy 排序
	 * @return  实体集合
	 * */
	List<E> queryList(E sample,ConditionExpr condition,OrderBy orderBy);

	/**
	 * 查询实体集合
	 * @param  sample 查询条件，等值查询
	 * @param  condition 额外的查询条件
	 * @param  orderBy 排序
	 * @param  dpcode 数据权限代码
	 * @return  实体集合
	 * */
	List<E> queryList(E sample,ConditionExpr condition,OrderBy orderBy,String dpcode);
	/**
	 * 查询实体集合
	 * @param  sample 查询条件，等值查询
	 * @param  orderBy 排序
	 * */
	List<E> queryList(E sample,OrderBy orderBy);

	/**
	 * 查询实体集合
	 * @param  sample 查询条件，等值查询
	 * @param  condition 额外的查询条件
	 * */
	List<E> queryList(E sample,ConditionExpr condition);

	/**
	 * 查询实体集合
	 * @param  condition 条件语句
	 * @param  ps 条件参数
	 * @return  实体集合
	 * */
	List<E> queryList(String condition,Object... ps);

	/**
	 * 查询实体集合
	 * @param  condition 条件语句
	 * @return  实体集合
	 * */
	List<E> queryList(ConditionExpr condition);

	/**
	 * 查询实体集合
	 * @param  condition 条件语句
	 * @param  orderBy 排序
	 * @return  实体集合
	 * */
	List<E> queryList(ConditionExpr condition,OrderBy orderBy);

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
	PagedList<E> queryPagedList(E sample,int pageSize,int pageIndex,String dpcode);

	/**
	 * 分页查询实体集
	 * */
	PagedList<E> queryPagedList(E sample,ConditionExpr condition,int pageSize,int pageIndex,String dpcode);

	/**
	 * 分页查询实体集
	 * */
	PagedList<E> queryPagedList(E sample,ConditionExpr condition,OrderBy orderBy,int pageSize,int pageIndex);

	/**
	 * 分页查询实体集
	 * */
	PagedList<E> queryPagedList(E sample,ConditionExpr condition,OrderBy orderBy,int pageSize,int pageIndex,String dpcode);
	/**
	 * 分页查询实体集
	 * */
	PagedList<E> queryPagedList(E sample,ConditionExpr condition,int pageSize,int pageIndex);

	/**
	 * 分页查询实体集
	 * */
	PagedList<E> queryPagedList(E sample,OrderBy orderBy,int pageSize,int pageIndex);


	/**
	 * 添加，如果语句错误，则抛出异常
	 *
	 * @param entity 数据对象
	 * @return 结果
	 */
	Result insert(E entity);

	/**
	 * 添加，根据 throwsException 参数抛出异常或返回 Result 对象
	 *
	 * @param entity 数据对象
	 * @param throwsException 是否抛出异常，如果不抛出异常，则返回一个失败的 Result 对象
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	Result insert(E entity,boolean throwsException);


	/**
	 * 批量插入实体
	 *
	 * @return*/
	Result insertList(List<E> list);

	/**
	 * 更新，如果执行错误，则抛出异常
	 *
	 * @param entity 数据对象
	 * @param mode SaveMode,数据更新的模式
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	Result update(E entity , SaveMode mode);


	/**
	 * 更新，根据 throwsException 参数抛出异常或返回 Result 对象
	 *
	 * @param entity 数据对象
	 * @param mode SaveMode,数据更新的模式
	 * @param throwsException 是否抛出异常，如果不抛出异常，则返回一个失败的 Result 对象
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	Result update(E entity , SaveMode mode,boolean throwsException);

	/**
	 * 更新所有字段
	 *
	 * @param entity 数据对象
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	Result updateAllFields(E entity);

	/**
	 * 更新非空字段
	 *
	 * @param entity 数据对象
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	Result updateNotNullFields(E entity);

	/**
	 * 更新修改过的字段
	 *
	 * @param entity 数据对象
	 * @return 结果 , 如果失败返回 false，成功返回 true
	 */
	Result updateDirtyFields(E entity);


	/**
	 * 批量更新实体
	 *
	 * @return
	 * */
	Result updateList(List<E> list , SaveMode mode);


	/**
	 * 批量更新实体，所有字段
	 * @param  list       实体列表
	 * @return
	 * */
	Result updateListAllFields(List<E> list);

	/**
	 * 批量更新实体，所有非空字段
	 * @param  list       实体列表
	 * @return
	 * */
	Result updateListNotNullFields(List<E> list);

	/**
	 * 批量更新实体，所有修改过的字段
	 * @param  list       实体列表
	 * @return*/
	Result updateListDirtyFields(List<E> list);

	/**
	 * 保存实体，如果语句错误，则抛出异常
	 * @param  entity 数据实体
	 * @param  mode 保存模式
	 * @return
	 * */
	Result save(E entity , SaveMode mode);

	/**
	 * 保存实体，根据 throwsException 参数抛出异常或返回 Result 对象
	 * @param  entity 数据实体
	 * @param  mode 保存模式
	 * @param throwsException 是否抛出异常，如果不抛出异常，则返回一个失败的 Result 对象
	 * @return
	 * */
	Result save(E entity,SaveMode mode,boolean throwsException);

	/**
	 * 保存实体，保存所有字段
	 * @param  entity 数据实体
	 * @return
	 * */
	Result saveAllFields(E entity);

	/**
	 * 保存实体，保存所有非空字段
	 * @param  entity 数据实体
	 * @return
	 * */
	Result saveNotNullFields(E entity);

	/**
	 * 保存实体，保存修改过字段
	 * @param  entity 数据实体
	 * @return
	 * */
	Result saveDirtyFields(E entity);

	/**
	 * 保存实体
	 *
	 * @return
	 * */
	Result saveList(List<E> list , SaveMode mode);


	/**
	 * 保存实体列表，保存实体中的所有字段
	 *
	 * @return
	 * */
	Result saveListAllFields(List<E> list);

	/**
	 * 保存实体列表，保存实体中的所有非空字段
	 *
	 * @return
	 * */
	Result saveNotNullFields(List<E> list);

	/**
	 * 保存实体列表，保存实体中的所有修改过的字段
	 *
	 * @return
	 * */
	Result saveDirtyFields(List<E> list);

	/**
	 * 检查符合条件的记录是否存在
	 */
	boolean checkExists(ConditionExpr conditionExpr);
	/**
	 * 检查实体存在性 , 判断 主键值不同，但指定字段的值相同的记录是否存在
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
	<T extends Entity> Map<String, JoinResult> join(E po, String... properties);

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
	<T extends Entity> Map<String, JoinResult> join(Collection<E> pos, String... properties);

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
	<T extends Entity> Map<String, JoinResult> join(PagedList<E> pos, String... properties);

	/**
	 * 当前服务是否支持缓存
	 * */
	boolean isSupportCache();

	/**
	 * 获得缓存对象
	 * */
	DoubleCache<String,Object> cache();

	/**
	 * 获得缓存策略
	 * */
	CacheStrategy getCacheStrategy(String name);

	/**
	 * 获得缓存策略
	 * */
	Map<String,CacheStrategy> getCacheStrategies();


	void dispatchJoinCacheInvalidEvent(CacheInvalidEventType eventType, Entity valueBefore, Entity valueAfter);

	void dispatchJoinCacheInvalidEvent(CacheInvalidEventType eventType, List<? extends Entity> valuesBefore, List<? extends Entity> valuesAfter);



}
