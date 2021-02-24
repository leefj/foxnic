package com.demo.business.user.domain;

import java.util.Map;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.dao.entity.Entity;
import java.math.BigDecimal;
import com.demo.business.user.domain.User;
import java.beans.Transient;

/**
 * @author 李方捷
 * @since 2021-02-24 08:53:03
*/

public class UserDTO extends ActivedUserVO {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 价格：单价，精确到小数点后2位
	*/
	private BigDecimal price = null;
	
	/**
	 * 获得 价格：单价，精确到小数点后2位
	 * @return 价格
	*/
	public BigDecimal getPrice() {
		return this.price;
	}
	
	/**
	 * 设置 价格：单价，精确到小数点后2位
	 * @param price 价格
	*/
	public void setPrice(BigDecimal price) {
		this.price=price;
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
	 * 将 User 转换成 UserDTO
	 * @param pojo 任意 Pojo 对象
	 * @return UserDTO , 转换好的的 UserDTO 对象
	*/
	@Transient
	public static UserDTO createFrom(Object pojo) {
		if(pojo==null) return null;
		UserDTO vo=new UserDTO();
		EntityContext.copyProperties(vo, pojo);
		return vo;
	}

	/**
	 * 将 Map 转换成 UserDTO
	 * @param userDTOMap 包含实体信息的 Map 对象
	 * @return UserDTO , 转换好的的 UserDTO 对象
	*/
	@Transient
	public static UserDTO createFrom(Map<String,Object> userDTOMap) {
		if(userDTOMap==null) return null;
		UserDTO vo=new UserDTO();
		EntityContext.copyProperties(vo, userDTOMap);
		return vo;
	}
}