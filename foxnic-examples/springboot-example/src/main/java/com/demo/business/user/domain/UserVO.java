package com.demo.business.user.domain;

import java.util.Map;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.dao.entity.Entity;
import com.demo.business.user.domain.User;
import java.beans.Transient;

/**
 * @author 李方捷
 * @since 2021-02-22 04:48:52
*/

public class UserVO extends User {

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
	 * 将自己转换成User
	 * @return User , 转换好的 User 对象
	*/
	@Transient
	public User toPO() {
		return EntityContext.create(User.class, this);
	}

	/**
	 * 将自己转换成指定类型的PO
	 * @param poType  PO类型
	 * @return User , 转换好的 User 对象
	*/
	@Transient
	public <T extends Entity> T toPO(Class<T> poType) {
		return EntityContext.create(poType, this);
	}

	/**
	 * 将自己转换成任意指定类型
	 * @param pojoType  Pojo类型
	 * @return User , 转换好的 PoJo 对象
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
	 * 将 User 转换成 UserVO
	 * @param pojo 任意 Pojo 对象
	 * @return UserVO , 转换好的的 UserVO 对象
	*/
	@Transient
	public static UserVO createFrom(Object pojo) {
		if(pojo==null) return null;
		UserVO vo=new UserVO();
		EntityContext.copyProperties(vo, pojo);
		return vo;
	}

	/**
	 * 将 Map 转换成 UserVO
	 * @param userVOMap 包含实体信息的 Map 对象
	 * @return UserVO , 转换好的的 UserVO 对象
	*/
	@Transient
	public static UserVO createFrom(Map<String,Object> userVOMap) {
		if(userVOMap==null) return null;
		UserVO vo=new UserVO();
		EntityContext.copyProperties(vo, userVOMap);
		return vo;
	}
}