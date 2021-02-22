package com.demo.business.user.domain;

import java.util.Map;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.dao.entity.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;
import javax.persistence.Table;
import java.beans.Transient;

/**
 * @author 李方捷
 * @since 2021-02-22 04:48:49
 * 此文件由工具自动生成，请勿修改。若表结构变动，请使用工具重新生成。
*/

@Table(name = "usr_user_role_xref")
public class UserRoleXref extends Entity {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 主键ID<br>
	 * 主键ID
	*/
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id = null;
	
	/**
	 * 公司ID<br>
	 * 公司ID 
	*/
	private Long companyId = null;
	
	/**
	 * 用户ID<br>
	 * 用户ID 
	*/
	private Long userId = null;
	
	/**
	 * 角色ID<br>
	 * 角色ID

	*/
	private Long roleId = null;
	
	/**
	 * 创建人id<br>
	 * 创建人id
	*/
	private Long createdBy = null;
	
	/**
	 * 修改人id<br>
	 * 修改人id
	*/
	private Long updatedBy = null;
	
	/**
	 * 创建时间<br>
	 * 创建时间

	*/
	private Date createDate = null;
	
	/**
	 * 修改时间<br>
	 * 修改时间

	*/
	private Date updateDate = null;
	
	/**
	 * 数据是否有效<br>
	 * （0：无效 1：有效）
	*/
	private Integer valid = null;
	
	/**
	 * 获得 主键ID<br>
	 * 属性说明 : 主键ID
	 * @return Long , 主键ID
	*/
	public Long getId() {
		return this.id;
	}
	
	/**
	 * 设置 主键ID
	 * @param id 主键ID
	*/
	public void setId(Long id) {
		this.id=id;
	}
	
	/**
	 * 获得 公司ID<br>
	 * 属性说明 : 公司ID 
	 * @return Long , 公司ID
	*/
	public Long getCompanyId() {
		return this.companyId;
	}
	
	/**
	 * 设置 公司ID
	 * @param companyId 公司ID
	*/
	public void setCompanyId(Long companyId) {
		this.companyId=companyId;
	}
	
	/**
	 * 获得 用户ID<br>
	 * 属性说明 : 用户ID 
	 * @return Long , 用户ID
	*/
	public Long getUserId() {
		return this.userId;
	}
	
	/**
	 * 设置 用户ID
	 * @param userId 用户ID
	*/
	public void setUserId(Long userId) {
		this.userId=userId;
	}
	
	/**
	 * 获得 角色ID<br>
	 * 属性说明 : 角色ID

	 * @return Long , 角色ID
	*/
	public Long getRoleId() {
		return this.roleId;
	}
	
	/**
	 * 设置 角色ID
	 * @param roleId 角色ID
	*/
	public void setRoleId(Long roleId) {
		this.roleId=roleId;
	}
	
	/**
	 * 获得 创建人id<br>
	 * 属性说明 : 创建人id
	 * @return Long , 创建人id
	*/
	public Long getCreatedBy() {
		return this.createdBy;
	}
	
	/**
	 * 设置 创建人id
	 * @param createdBy 创建人id
	*/
	public void setCreatedBy(Long createdBy) {
		this.createdBy=createdBy;
	}
	
	/**
	 * 获得 修改人id<br>
	 * 属性说明 : 修改人id
	 * @return Long , 修改人id
	*/
	public Long getUpdatedBy() {
		return this.updatedBy;
	}
	
	/**
	 * 设置 修改人id
	 * @param updatedBy 修改人id
	*/
	public void setUpdatedBy(Long updatedBy) {
		this.updatedBy=updatedBy;
	}
	
	/**
	 * 获得 创建时间<br>
	 * 属性说明 : 创建时间

	 * @return Date , 创建时间
	*/
	public Date getCreateDate() {
		return this.createDate;
	}
	
	/**
	 * 设置 创建时间
	 * @param createDate 创建时间
	*/
	public void setCreateDate(Date createDate) {
		this.createDate=createDate;
	}
	
	/**
	 * 获得 修改时间<br>
	 * 属性说明 : 修改时间

	 * @return Date , 修改时间
	*/
	public Date getUpdateDate() {
		return this.updateDate;
	}
	
	/**
	 * 设置 修改时间
	 * @param updateDate 修改时间
	*/
	public void setUpdateDate(Date updateDate) {
		this.updateDate=updateDate;
	}
	
	/**
	 * 获得 数据是否有效<br>
	 * 属性说明 : （0：无效 1：有效）
	 * @return Integer , 数据是否有效
	*/
	public Integer getValid() {
		return this.valid;
	}
	
	/**
	 * 设置 数据是否有效
	 * @param valid 数据是否有效
	*/
	public void setValid(Integer valid) {
		this.valid=valid;
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
	 * 将 Map 转换成 UserRoleXref
	 * @param userRoleXrefVOMap 包含实体信息的 Map 对象
	 * @return UserRoleXref , 转换好的的 UserRoleXrefVO 对象
	*/
	@Transient
	public static UserRoleXref createFrom(Map<String,Object> userRoleXrefVOMap) {
		if(userRoleXrefVOMap==null) return null;
		UserRoleXref po = EntityContext.create(UserRoleXref.class, userRoleXrefVOMap);
		return po;
	}

	/**
	 * 将 Pojo 转换成 UserRoleXref
	 * @param pojo 包含实体信息的 Pojo 对象
	 * @return UserRoleXref , 转换好的的 UserRoleXrefVO 对象
	*/
	@Transient
	public static UserRoleXref createFrom(Object pojo) {
		if(pojo==null) return null;
		UserRoleXref po = EntityContext.create(UserRoleXref.class,pojo);
		return po;
	}

}