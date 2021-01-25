package com.demo.system.service;

import com.demo.domain.User;
import com.github.foxnic.commons.busi.Result;

public interface UserService {
	
	Result regist(User user);
	
	Result update(User user);
	
	Result select(User user);
	
	Result deleteById(String id);

}
