package com.github.foxnic.dao.entity;

import com.github.foxnic.api.transter.Result;

import java.util.List;

public interface ISimpleIdService<E extends Entity,P> extends  ISuperService<E> {

	/**
	 * 按主键删除对象
	 *
	 * @param id ID
	 * @return 删除是否成功
	 */
	Result deleteByIdPhysical(P id);

	/**
	 * 按主键删除对象
	 *
	 * @param id ID
	 * @return 删除是否成功
	 */
	default Result deleteByIdLogical(P id) {
		throw new RuntimeException("待实现，请按需实现逻辑删除");
	}

	/**
	 * 按主键获取对象
	 *
	 * @param id ID
	 * @return User 数据对象
	 */
	E getById(P id);

	/**
	 * 检查实体中的数据字段是否已经存在
	 * @param ids  主键清单
	 * @return 实体集
	 * */
	List<E> getByIds(List<P> ids);


}
