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
 * @since 2021-02-26 04:44:50
 * 此文件由工具自动生成，请勿修改。若表结构变动，请使用工具重新生成。
*/

@Table(name = "usr_user")
public class User extends Entity {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 主键ID<br>
	 * 主键ID
	*/
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id = null;
	
	/**
	 * 用户姓名<br>
	 * 用户姓名
	*/
	private String username = null;
	
	/**
	 * 登录账号<br>
	 * 登录账号
	*/
	private String account = null;
	
	/**
	 * 密码<br>
	 * 密码
	*/
	private String password = null;
	
	/**
	 * 盐值<br>
	 * 盐值
	*/
	private String salt = null;
	
	/**
	 * 用户头像<br>
	 * 用户头像
	*/
	private String profileImg = null;
	
	/**
	 * 用户类型（0：商城用户<br>
	 * 1：商城后台系统管理员  2：智运司机端用户  9：国烨机器人 ）
	*/
	private Integer userType = null;
	
	/**
	 * 性别<br>
	 * ( 0：女  1：男)
	*/
	private Integer sex = null;
	
	/**
	 * 出生日期<br>
	 * 出生日期
	*/
	private Date birthday = null;
	
	/**
	 * 邮箱<br>
	 * 邮箱
	*/
	private String email = null;
	
	/**
	 * 手机号<br>
	 * 手机号
	*/
	private String phone = null;
	
	/**
	 * 详细地址<br>
	 * 详细地址
	*/
	private String address = null;
	
	/**
	 * （0：普通会员<br>
	 * 1：黄金会员  2：铂金会员 3：钻石会员）
	*/
	private Integer membershipGrade = null;
	
	/**
	 * score<br>
	 * score
	*/
	private Integer score = null;
	
	/**
	 * 备注<br>
	 * 备注
	*/
	private String remark = null;
	
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
	 * 设备号(可能多<br>
	 * 逗号隔开)
	*/
	private String clientIds = null;
	
	/**
	 * 业务范围<br>
	 * 业务范围
	*/
	private String businessScope = null;
	
	/**
	 * 用户来源<br>
	 * 用户来源
	*/
	private String source = null;
	
	/**
	 * 公司名称<br>
	 * 公司名称
	*/
	private String affiliatedCompany = null;
	
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
	 * 获得 用户姓名<br>
	 * 属性说明 : 用户姓名
	 * @return String , 用户姓名
	*/
	public String getUsername() {
		return this.username;
	}
	
	/**
	 * 设置 用户姓名
	 * @param username 用户姓名
	*/
	public void setUsername(String username) {
		this.username=username;
	}
	
	/**
	 * 获得 登录账号<br>
	 * 属性说明 : 登录账号
	 * @return String , 登录账号
	*/
	public String getAccount() {
		return this.account;
	}
	
	/**
	 * 设置 登录账号
	 * @param account 登录账号
	*/
	public void setAccount(String account) {
		this.account=account;
	}
	
	/**
	 * 获得 密码<br>
	 * 属性说明 : 密码
	 * @return String , 密码
	*/
	public String getPassword() {
		return this.password;
	}
	
	/**
	 * 设置 密码
	 * @param password 密码
	*/
	public void setPassword(String password) {
		this.password=password;
	}
	
	/**
	 * 获得 盐值<br>
	 * 属性说明 : 盐值
	 * @return String , 盐值
	*/
	public String getSalt() {
		return this.salt;
	}
	
	/**
	 * 设置 盐值
	 * @param salt 盐值
	*/
	public void setSalt(String salt) {
		this.salt=salt;
	}
	
	/**
	 * 获得 用户头像<br>
	 * 属性说明 : 用户头像
	 * @return String , 用户头像
	*/
	public String getProfileImg() {
		return this.profileImg;
	}
	
	/**
	 * 设置 用户头像
	 * @param profileImg 用户头像
	*/
	public void setProfileImg(String profileImg) {
		this.profileImg=profileImg;
	}
	
	/**
	 * 获得 用户类型（0：商城用户<br>
	 * 属性说明 : 1：商城后台系统管理员  2：智运司机端用户  9：国烨机器人 ）
	 * @return Integer , 用户类型（0：商城用户
	*/
	public Integer getUserType() {
		return this.userType;
	}
	
	/**
	 * 设置 用户类型（0：商城用户
	 * @param userType 用户类型（0：商城用户
	*/
	public void setUserType(Integer userType) {
		this.userType=userType;
	}
	
	/**
	 * 获得 性别<br>
	 * 属性说明 : ( 0：女  1：男)
	 * @return Integer , 性别
	*/
	public Integer getSex() {
		return this.sex;
	}
	
	/**
	 * 设置 性别
	 * @param sex 性别
	*/
	public void setSex(Integer sex) {
		this.sex=sex;
	}
	
	/**
	 * 获得 出生日期<br>
	 * 属性说明 : 出生日期
	 * @return Date , 出生日期
	*/
	public Date getBirthday() {
		return this.birthday;
	}
	
	/**
	 * 设置 出生日期
	 * @param birthday 出生日期
	*/
	public void setBirthday(Date birthday) {
		this.birthday=birthday;
	}
	
	/**
	 * 获得 邮箱<br>
	 * 属性说明 : 邮箱
	 * @return String , 邮箱
	*/
	public String getEmail() {
		return this.email;
	}
	
	/**
	 * 设置 邮箱
	 * @param email 邮箱
	*/
	public void setEmail(String email) {
		this.email=email;
	}
	
	/**
	 * 获得 手机号<br>
	 * 属性说明 : 手机号
	 * @return String , 手机号
	*/
	public String getPhone() {
		return this.phone;
	}
	
	/**
	 * 设置 手机号
	 * @param phone 手机号
	*/
	public void setPhone(String phone) {
		this.phone=phone;
	}
	
	/**
	 * 获得 详细地址<br>
	 * 属性说明 : 详细地址
	 * @return String , 详细地址
	*/
	public String getAddress() {
		return this.address;
	}
	
	/**
	 * 设置 详细地址
	 * @param address 详细地址
	*/
	public void setAddress(String address) {
		this.address=address;
	}
	
	/**
	 * 获得 （0：普通会员<br>
	 * 属性说明 : 1：黄金会员  2：铂金会员 3：钻石会员）
	 * @return Integer , （0：普通会员
	*/
	public Integer getMembershipGrade() {
		return this.membershipGrade;
	}
	
	/**
	 * 设置 （0：普通会员
	 * @param membershipGrade （0：普通会员
	*/
	public void setMembershipGrade(Integer membershipGrade) {
		this.membershipGrade=membershipGrade;
	}
	
	/**
	 * 获得 score<br>
	 * 属性说明 : score
	 * @return Integer , score
	*/
	public Integer getScore() {
		return this.score;
	}
	
	/**
	 * 设置 score
	 * @param score score
	*/
	public void setScore(Integer score) {
		this.score=score;
	}
	
	/**
	 * 获得 备注<br>
	 * 属性说明 : 备注
	 * @return String , 备注
	*/
	public String getRemark() {
		return this.remark;
	}
	
	/**
	 * 设置 备注
	 * @param remark 备注
	*/
	public void setRemark(String remark) {
		this.remark=remark;
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
	 * 获得 设备号(可能多<br>
	 * 属性说明 : 逗号隔开)
	 * @return String , 设备号(可能多
	*/
	public String getClientIds() {
		return this.clientIds;
	}
	
	/**
	 * 设置 设备号(可能多
	 * @param clientIds 设备号(可能多
	*/
	public void setClientIds(String clientIds) {
		this.clientIds=clientIds;
	}
	
	/**
	 * 获得 业务范围<br>
	 * 属性说明 : 业务范围
	 * @return String , 业务范围
	*/
	public String getBusinessScope() {
		return this.businessScope;
	}
	
	/**
	 * 设置 业务范围
	 * @param businessScope 业务范围
	*/
	public void setBusinessScope(String businessScope) {
		this.businessScope=businessScope;
	}
	
	/**
	 * 获得 用户来源<br>
	 * 属性说明 : 用户来源
	 * @return String , 用户来源
	*/
	public String getSource() {
		return this.source;
	}
	
	/**
	 * 设置 用户来源
	 * @param source 用户来源
	*/
	public void setSource(String source) {
		this.source=source;
	}
	
	/**
	 * 获得 公司名称<br>
	 * 属性说明 : 公司名称
	 * @return String , 公司名称
	*/
	public String getAffiliatedCompany() {
		return this.affiliatedCompany;
	}
	
	/**
	 * 设置 公司名称
	 * @param affiliatedCompany 公司名称
	*/
	public void setAffiliatedCompany(String affiliatedCompany) {
		this.affiliatedCompany=affiliatedCompany;
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
	 * 将 Map 转换成 User
	 * @param userVOMap 包含实体信息的 Map 对象
	 * @return User , 转换好的的 UserVO 对象
	*/
	@Transient
	public static User createFrom(Map<String,Object> userVOMap) {
		if(userVOMap==null) return null;
		User po = EntityContext.create(User.class, userVOMap);
		return po;
	}

	/**
	 * 将 Pojo 转换成 User
	 * @param pojo 包含实体信息的 Pojo 对象
	 * @return User , 转换好的的 UserVO 对象
	*/
	@Transient
	public static User createFrom(Object pojo) {
		if(pojo==null) return null;
		User po = EntityContext.create(User.class,pojo);
		return po;
	}

}