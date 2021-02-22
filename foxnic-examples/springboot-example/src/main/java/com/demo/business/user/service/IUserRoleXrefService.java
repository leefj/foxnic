package com.demo.business.user.service;
import com.github.foxnic.dao.entity.SuperService;
import com.demo.business.user.domain.UserRoleXref;
/**
 * <p>
 * 用户角色关联表 服务接口
 * </p>
 * @author 李方捷
 * @since 2021-02-22 04:48:49
*/

public interface IUserRoleXrefService extends SuperService<UserRoleXref> {
	
	/**
	 * 按主键获取用户角色关联表
	 *
	 * @param id 主键ID
	 * @return 查询结果 , UserRoleXref对象
	 */
	UserRoleXref getById(Long id);
	
	/**
	 * 按主键删除用户角色关联表
	 *
	 * @param id 主键ID , 详情 : 主键ID
	 * @return 查询结果 , UserRoleXref对象
	 */
	boolean deleteByIdPhysical(Long id);

}