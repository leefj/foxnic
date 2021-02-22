package com.demo.business.user.domain;

import java.util.Map;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.dao.entity.Entity;
import java.sql.Timestamp;
import com.demo.business.user.domain.User;
import java.beans.Transient;

/**
 * @author 李方捷
 * @since 2021-02-22 04:48:52
*/

public class UserRoleQuery {

	private static final long serialVersionUID = 1L;
	
	/**
	 * id：id
	*/
	private Long id = null;
	
	/**
	 * username：username
	*/
	private String username = null;
	
	/**
	 * account：account
	*/
	private String account = null;
	
	/**
	 * password：password
	*/
	private String password = null;
	
	/**
	 * salt：salt
	*/
	private String salt = null;
	
	/**
	 * profile_img：profile_img
	*/
	private String profileImg = null;
	
	/**
	 * user_type：user_type
	*/
	private Integer userType = null;
	
	/**
	 * sex：sex
	*/
	private Integer sex = null;
	
	/**
	 * birthday：birthday
	*/
	private Timestamp birthday = null;
	
	/**
	 * email：email
	*/
	private String email = null;
	
	/**
	 * phone：phone
	*/
	private String phone = null;
	
	/**
	 * address：address
	*/
	private String address = null;
	
	/**
	 * membership_grade：membership_grade
	*/
	private Integer membershipGrade = null;
	
	/**
	 * score：score
	*/
	private Integer score = null;
	
	/**
	 * remark：remark
	*/
	private String remark = null;
	
	/**
	 * created_by：created_by
	*/
	private Long createdBy = null;
	
	/**
	 * updated_by：updated_by
	*/
	private Long updatedBy = null;
	
	/**
	 * create_date：create_date
	*/
	private Timestamp createDate = null;
	
	/**
	 * update_date：update_date
	*/
	private Timestamp updateDate = null;
	
	/**
	 * valid：valid
	*/
	private Integer valid = null;
	
	/**
	 * client_ids：client_ids
	*/
	private String clientIds = null;
	
	/**
	 * business_scope：business_scope
	*/
	private String businessScope = null;
	
	/**
	 * source：source
	*/
	private String source = null;
	
	/**
	 * affiliated_company：affiliated_company
	*/
	private String affiliatedCompany = null;
	
	/**
	 * role_name：role_name
	*/
	private String roleName = null;
	
	/**
	 * 获得 id：id
	 * @return id
	*/
	public Long getId() {
		return this.id;
	}
	
	/**
	 * 获得 username：username
	 * @return username
	*/
	public String getUsername() {
		return this.username;
	}
	
	/**
	 * 获得 account：account
	 * @return account
	*/
	public String getAccount() {
		return this.account;
	}
	
	/**
	 * 获得 password：password
	 * @return password
	*/
	public String getPassword() {
		return this.password;
	}
	
	/**
	 * 获得 salt：salt
	 * @return salt
	*/
	public String getSalt() {
		return this.salt;
	}
	
	/**
	 * 获得 profile_img：profile_img
	 * @return profile_img
	*/
	public String getProfileImg() {
		return this.profileImg;
	}
	
	/**
	 * 获得 user_type：user_type
	 * @return user_type
	*/
	public Integer getUserType() {
		return this.userType;
	}
	
	/**
	 * 获得 sex：sex
	 * @return sex
	*/
	public Integer getSex() {
		return this.sex;
	}
	
	/**
	 * 获得 birthday：birthday
	 * @return birthday
	*/
	public Timestamp getBirthday() {
		return this.birthday;
	}
	
	/**
	 * 获得 email：email
	 * @return email
	*/
	public String getEmail() {
		return this.email;
	}
	
	/**
	 * 获得 phone：phone
	 * @return phone
	*/
	public String getPhone() {
		return this.phone;
	}
	
	/**
	 * 获得 address：address
	 * @return address
	*/
	public String getAddress() {
		return this.address;
	}
	
	/**
	 * 获得 membership_grade：membership_grade
	 * @return membership_grade
	*/
	public Integer getMembershipGrade() {
		return this.membershipGrade;
	}
	
	/**
	 * 获得 score：score
	 * @return score
	*/
	public Integer getScore() {
		return this.score;
	}
	
	/**
	 * 获得 remark：remark
	 * @return remark
	*/
	public String getRemark() {
		return this.remark;
	}
	
	/**
	 * 获得 created_by：created_by
	 * @return created_by
	*/
	public Long getCreatedBy() {
		return this.createdBy;
	}
	
	/**
	 * 获得 updated_by：updated_by
	 * @return updated_by
	*/
	public Long getUpdatedBy() {
		return this.updatedBy;
	}
	
	/**
	 * 获得 create_date：create_date
	 * @return create_date
	*/
	public Timestamp getCreateDate() {
		return this.createDate;
	}
	
	/**
	 * 获得 update_date：update_date
	 * @return update_date
	*/
	public Timestamp getUpdateDate() {
		return this.updateDate;
	}
	
	/**
	 * 获得 valid：valid
	 * @return valid
	*/
	public Integer getValid() {
		return this.valid;
	}
	
	/**
	 * 获得 client_ids：client_ids
	 * @return client_ids
	*/
	public String getClientIds() {
		return this.clientIds;
	}
	
	/**
	 * 获得 business_scope：business_scope
	 * @return business_scope
	*/
	public String getBusinessScope() {
		return this.businessScope;
	}
	
	/**
	 * 获得 source：source
	 * @return source
	*/
	public String getSource() {
		return this.source;
	}
	
	/**
	 * 获得 affiliated_company：affiliated_company
	 * @return affiliated_company
	*/
	public String getAffiliatedCompany() {
		return this.affiliatedCompany;
	}
	
	/**
	 * 获得 role_name：role_name
	 * @return role_name
	*/
	public String getRoleName() {
		return this.roleName;
	}
	
	/**
	 * 设置 id：id
	 * @param id id
	*/
	public void setId(Long id) {
		this.id=id;
	}
	
	/**
	 * 设置 username：username
	 * @param username username
	*/
	public void setUsername(String username) {
		this.username=username;
	}
	
	/**
	 * 设置 account：account
	 * @param account account
	*/
	public void setAccount(String account) {
		this.account=account;
	}
	
	/**
	 * 设置 password：password
	 * @param password password
	*/
	public void setPassword(String password) {
		this.password=password;
	}
	
	/**
	 * 设置 salt：salt
	 * @param salt salt
	*/
	public void setSalt(String salt) {
		this.salt=salt;
	}
	
	/**
	 * 设置 profile_img：profile_img
	 * @param profileImg profile_img
	*/
	public void setProfileImg(String profileImg) {
		this.profileImg=profileImg;
	}
	
	/**
	 * 设置 user_type：user_type
	 * @param userType user_type
	*/
	public void setUserType(Integer userType) {
		this.userType=userType;
	}
	
	/**
	 * 设置 sex：sex
	 * @param sex sex
	*/
	public void setSex(Integer sex) {
		this.sex=sex;
	}
	
	/**
	 * 设置 birthday：birthday
	 * @param birthday birthday
	*/
	public void setBirthday(Timestamp birthday) {
		this.birthday=birthday;
	}
	
	/**
	 * 设置 email：email
	 * @param email email
	*/
	public void setEmail(String email) {
		this.email=email;
	}
	
	/**
	 * 设置 phone：phone
	 * @param phone phone
	*/
	public void setPhone(String phone) {
		this.phone=phone;
	}
	
	/**
	 * 设置 address：address
	 * @param address address
	*/
	public void setAddress(String address) {
		this.address=address;
	}
	
	/**
	 * 设置 membership_grade：membership_grade
	 * @param membershipGrade membership_grade
	*/
	public void setMembershipGrade(Integer membershipGrade) {
		this.membershipGrade=membershipGrade;
	}
	
	/**
	 * 设置 score：score
	 * @param score score
	*/
	public void setScore(Integer score) {
		this.score=score;
	}
	
	/**
	 * 设置 remark：remark
	 * @param remark remark
	*/
	public void setRemark(String remark) {
		this.remark=remark;
	}
	
	/**
	 * 设置 created_by：created_by
	 * @param createdBy created_by
	*/
	public void setCreatedBy(Long createdBy) {
		this.createdBy=createdBy;
	}
	
	/**
	 * 设置 updated_by：updated_by
	 * @param updatedBy updated_by
	*/
	public void setUpdatedBy(Long updatedBy) {
		this.updatedBy=updatedBy;
	}
	
	/**
	 * 设置 create_date：create_date
	 * @param createDate create_date
	*/
	public void setCreateDate(Timestamp createDate) {
		this.createDate=createDate;
	}
	
	/**
	 * 设置 update_date：update_date
	 * @param updateDate update_date
	*/
	public void setUpdateDate(Timestamp updateDate) {
		this.updateDate=updateDate;
	}
	
	/**
	 * 设置 valid：valid
	 * @param valid valid
	*/
	public void setValid(Integer valid) {
		this.valid=valid;
	}
	
	/**
	 * 设置 client_ids：client_ids
	 * @param clientIds client_ids
	*/
	public void setClientIds(String clientIds) {
		this.clientIds=clientIds;
	}
	
	/**
	 * 设置 business_scope：business_scope
	 * @param businessScope business_scope
	*/
	public void setBusinessScope(String businessScope) {
		this.businessScope=businessScope;
	}
	
	/**
	 * 设置 source：source
	 * @param source source
	*/
	public void setSource(String source) {
		this.source=source;
	}
	
	/**
	 * 设置 affiliated_company：affiliated_company
	 * @param affiliatedCompany affiliated_company
	*/
	public void setAffiliatedCompany(String affiliatedCompany) {
		this.affiliatedCompany=affiliatedCompany;
	}
	
	/**
	 * 设置 role_name：role_name
	 * @param roleName role_name
	*/
	public void setRoleName(String roleName) {
		this.roleName=roleName;
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
	 * 将 User 转换成 UserRoleQuery
	 * @param pojo 任意 Pojo 对象
	 * @return UserRoleQuery , 转换好的的 UserRoleQuery 对象
	*/
	@Transient
	public static UserRoleQuery createFrom(Object pojo) {
		if(pojo==null) return null;
		UserRoleQuery vo=new UserRoleQuery();
		EntityContext.copyProperties(vo, pojo);
		return vo;
	}

	/**
	 * 将 Map 转换成 UserRoleQuery
	 * @param userRoleQueryMap 包含实体信息的 Map 对象
	 * @return UserRoleQuery , 转换好的的 UserRoleQuery 对象
	*/
	@Transient
	public static UserRoleQuery createFrom(Map<String,Object> userRoleQueryMap) {
		if(userRoleQueryMap==null) return null;
		UserRoleQuery vo=new UserRoleQuery();
		EntityContext.copyProperties(vo, userRoleQueryMap);
		return vo;
	}
}