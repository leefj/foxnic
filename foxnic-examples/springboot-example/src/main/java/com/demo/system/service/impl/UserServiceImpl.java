package com.demo.system.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.demo.domain.User;
import com.demo.system.service.UserService;
import com.github.foxnic.commons.busi.Result;
import com.github.foxnic.commons.busi.id.IDGenerator;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.spec.DAO;

@Service
public class UserServiceImpl implements UserService {
	
	@Autowired
	private DAO dao; 
	
	public Result regist(User user) {
		user.setId(IDGenerator.getSnowflakeId()+"");
		boolean suc=dao.insertEntity(user);
		if(suc) {
			return Result.SUCCESS().data(user);
		} else {
			return Result.FAILURE();
		}
	}

	@Override
	public Result update(User user) {
		boolean suc=dao.updateEntity(user, false);
		if(suc) {
			return Result.SUCCESS().data(user);
		} else {
			return Result.FAILURE();
		}
	}

	@Override
	public Result select(User user) {

		RcdSet rs=dao.query("select * from sys_user where nick_name like ?","%"+user.getNickName()+"%");
		return Result.SUCCESS().data(rs.getRcd(0));
		
	}

	@Override
	public Result deleteById(String id) {
		User user=new User();
		user.setId(id);
		dao.deleteEntity(user);
		//或
		//dao.delete("sys_user", "id=?",id).execute();
		//方法有很多，各种方法都可以
		return Result.SUCCESS();
	}
	
}
