package com.demo.business.user.service;
import com.github.foxnic.dao.entity.SuperService;
import com.demo.business.user.domain.User;
/**
 * <p>
 * 用户表 服务接口
 * </p>
 * @author 李方捷
 * @since 2021-02-26 04:44:50
*/

public interface IUserService extends SuperService<User> {
	
	/**
	 * 按主键获取用户表
	 *
	 * @param id 主键ID
	 * @return 查询结果 , User对象
	 */
	User getById(Long id);
	
	/**
	 * 按主键删除用户表
	 *
	 * @param id 主键ID , 详情 : 主键ID
	 * @return 查询结果 , User对象
	 */
	boolean deleteByIdPhysical(Long id);

}