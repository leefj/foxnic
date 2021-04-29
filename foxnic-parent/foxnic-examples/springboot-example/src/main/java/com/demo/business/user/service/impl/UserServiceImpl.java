package com.demo.business.user.service.impl;
import com.demo.business.user.service.IUserService;
import com.demo.framework.configs.DBConfigs;
import com.github.foxnic.dao.spec.DAO;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.demo.business.user.domain.User;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 * @author 李方捷
 * @since 2021-02-26 04:44:50
*/

@Service
public class UserServiceImpl implements IUserService {
	
	@Resource(name=DBConfigs.PRIMARY_DAO)
	private DAO dao=null;
	
	/**
	 * 获得 DAO 对象
	 * */
	public DAO dao() { return dao; }
	
	/**
	 * 按主键获取用户表
	 *
	 * @param id 主键ID
	 * @return 查询结果 , User对象
	 */
	public User getById(Long id) {
		User sample = new User();
		if(id==null) throw new IllegalArgumentException("id 不允许为 null 。");
		sample.setId(id);
		return dao.queryEntity(sample);
	}
	
	/**
	 * 按主键删除用户表
	 *
	 * @param id 主键ID , 详情 : 主键ID
	 * @return 查询结果 , User对象
	 */
	public boolean deleteByIdPhysical(Long id) {
		User user = new User();
		if(id==null) throw new IllegalArgumentException("id 不允许为 null 。");
		user.setId(id);
		return dao.deleteEntity(user);
	}

}