package com.demo.business.user.domain.dto;

import java.util.Map;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.dao.entity.Entity;
import com.demo.business.user.domain.po.User;
import java.math.BigDecimal;
import java.beans.Transient;

/**
 * @author 李方捷
 * @since 2021-02-20 04:37:54
*/

public class ActivedUserDTO {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 是否激活：激活时显示内容
	*/
	private Boolean isActive = null;
	
	/**
	 * 审批通过：是否已经审批通过
	*/
	private Boolean passed = null;
	
	/**
	 * 操作类型：audit:审批 ; cancel:取消
	*/
	private String action = null;
	
	/**
	 * 总量：汇总统计的结果
	*/
	private BigDecimal amount = null;
	
	/**
	 * 获得 是否激活：激活时显示内容
	 * @return 是否激活
	*/
	public Boolean isActive() {
		return this.isActive;
	}
	
	/**
	 * 获得 审批通过：是否已经审批通过
	 * @return 审批通过
	*/
	public Boolean isPassed() {
		return this.passed;
	}
	
	/**
	 * 获得 操作类型：audit:审批 ; cancel:取消
	 * @return 操作类型
	*/
	public String getAction() {
		return this.action;
	}
	
	/**
	 * 获得 总量：汇总统计的结果
	 * @return 总量
	*/
	public BigDecimal getAmount() {
		return this.amount;
	}
	
	/**
	 * 设置 是否激活：激活时显示内容
	 * @param isActive 是否激活
	*/
	public void setActive(Boolean isActive) {
		this.isActive=isActive;
	}
	
	/**
	 * 设置 审批通过：是否已经审批通过
	 * @param passed 审批通过
	*/
	public void setPassed(Boolean passed) {
		this.passed=passed;
	}
	
	/**
	 * 设置 操作类型：audit:审批 ; cancel:取消
	 * @param action 操作类型
	*/
	public void setAction(String action) {
		this.action=action;
	}
	
	/**
	 * 设置 总量：汇总统计的结果
	 * @param amount 总量
	*/
	public void setAmount(BigDecimal amount) {
		this.amount=amount;
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
	public <T> T toAny(Class<T> pojoType) {
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
	 * 将 User 转换成 ActivedUserDTO
	 * @param pojo 任意 Pojo 对象
	 * @return ActivedUserDTO , 转换好的的 ActivedUserDTO 对象
	*/
	@Transient
	public static ActivedUserDTO createFrom(Object pojo) {
		if(pojo==null) return null;
		ActivedUserDTO vo=new ActivedUserDTO();
		EntityContext.copyProperties(vo, pojo);
		return vo;
	}

	/**
	 * 将 Map 转换成 ActivedUserDTO
	 * @param userDTOMap 包含实体信息的 Map 对象
	 * @return ActivedUserDTO , 转换好的的 ActivedUserDTO 对象
	*/
	@Transient
	public static ActivedUserDTO createFrom(Map<String,Object> userDTOMap) {
		if(userDTOMap==null) return null;
		ActivedUserDTO vo=new ActivedUserDTO();
		EntityContext.copyProperties(vo, userDTOMap);
		return vo;
	}
}