package com.github.foxnic.dao.spec;

import java.util.List;

import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.data.SaveMode;

public interface SuperService<E> {
	
	/**
	 * 获得 DAO 对象
	 * */
	DAO dao();
	
	
	/**
	 * 获得对应的数据表
	 * */
	default String table() {
		return null;
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
