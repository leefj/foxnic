package com.demo.business.user.service.impl;
import com.demo.configs.DBConfigs;
import com.github.foxnic.dao.spec.DAO;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.demo.business.user.service.IUserRoleXrefService;
import com.demo.business.user.domain.UserRoleXref;

/**
 * <p>
 * 用户角色关联表 服务实现类
 * </p>
 * @author 李方捷
 * @since 2021-02-22 04:48:49
*/

@Service
public class UserRoleXrefServiceImpl implements IUserRoleXrefService {
	
	@Resource(name=DBConfigs.PRIMARY_DAO)
	private DAO dao=null;
	
	/**
	 * 获得 DAO 对象
	 * */
	public DAO dao() { return dao; }
	
	/**
	 * 按主键获取用户角色关联表
	 *
	 * @param id 主键ID
	 * @return 查询结果 , UserRoleXref对象
	 */
	public UserRoleXref getById(Long id) {
		UserRoleXref sample = new UserRoleXref();
		sample.setId(id);
		return dao.queryEntity(sample);
	}
	
	/**
	 * 按主键删除用户角色关联表
	 *
	 * @param id 主键ID , 详情 : 主键ID
	 * @return 查询结果 , UserRoleXref对象
	 */
	public boolean deleteByIdPhysical(Long id) {
		UserRoleXref userRoleXref = new UserRoleXref();
		userRoleXref.setId(id);
		return dao.deleteEntity(userRoleXref);
	}

}