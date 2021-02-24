package com.demo.business.user.domain;

import java.util.Map;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.dao.entity.Entity;
import com.demo.business.user.domain.UserRoleXref;
import java.beans.Transient;

/**
 * @author 李方捷
 * @since 2021-02-24 08:52:59
*/

public class UserRoleXrefVO extends UserRoleXref {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 页码
	*/
	private Integer pageIndex = null;
	
	/**
	 * 分页大小
	*/
	private Integer pageSize = null;
	
	/**
	 * 获得 页码
	 * @return 页码
	*/
	public Integer getPageIndex() {
		return this.pageIndex;
	}
	
	/**
	 * 获得 分页大小
	 * @return 分页大小
	*/
	public Integer getPageSize() {
		return this.pageSize;
	}
	
	/**
	 * 设置 页码
	 * @param pageIndex 页码
	*/
	public void setPageIndex(Integer pageIndex) {
		this.pageIndex=pageIndex;
	}
	
	/**
	 * 设置 分页大小
	 * @param pageSize 分页大小
	*/
	public void setPageSize(Integer pageSize) {
		this.pageSize=pageSize;
	}

	/**
	 * 将自己转换成UserRoleXref
	 * @return UserRoleXref , 转换好的 UserRoleXref 对象
	*/
	@Transient
	public UserRoleXref toPO() {
		return EntityContext.create(UserRoleXref.class, this);
	}

	/**
	 * 将自己转换成指定类型的PO
	 * @param poType  PO类型
	 * @return UserRoleXref , 转换好的 UserRoleXref 对象
	*/
	@Transient
	public <T extends Entity> T toPO(Class<T> poType) {
		return EntityContext.create(poType, this);
	}

	/**
	 * 将自己转换成任意指定类型
	 * @param pojoType  Pojo类型
	 * @return UserRoleXref , 转换好的 PoJo 对象
	*/
	@Transient
	public <T> T toPojo(Class<T> pojoType) {
		if(Entity.class.isAssignableFrom(pojoType)) {
			return (T)this.toPO((Class<Entity>)pojoType);
		}
		try {
			T pojo=pojoType.newInstance();
			EntityContext.copyProperties(pojo, this);
			return pojo;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将 UserRoleXref 转换成 UserRoleXrefVO
	 * @param pojo 任意 Pojo 对象
	 * @return UserRoleXrefVO , 转换好的的 UserRoleXrefVO 对象
	*/
	@Transient
	public static UserRoleXrefVO createFrom(Object pojo) {
		if(pojo==null) return null;
		UserRoleXrefVO vo=new UserRoleXrefVO();
		EntityContext.copyProperties(vo, pojo);
		return vo;
	}

	/**
	 * 将 Map 转换成 UserRoleXrefVO
	 * @param userRoleXrefVOMap 包含实体信息的 Map 对象
	 * @return UserRoleXrefVO , 转换好的的 UserRoleXrefVO 对象
	*/
	@Transient
	public static UserRoleXrefVO createFrom(Map<String,Object> userRoleXrefVOMap) {
		if(userRoleXrefVOMap==null) return null;
		UserRoleXrefVO vo=new UserRoleXrefVO();
		EntityContext.copyProperties(vo, userRoleXrefVOMap);
		return vo;
	}
}