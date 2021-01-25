package com.demo.system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.domain.User;
import com.demo.system.service.UserService;
import com.github.foxnic.commons.busi.Result;
import com.github.foxnic.commons.lang.StringUtil;

@RestController
@RequestMapping("/user")
public class UserController {
 
	@Autowired
	private UserService userService;
	
	
	
	@PostMapping("regist")
	public Result regist(@RequestBody User user) throws Exception {
		return userService.regist(user);
	}
	
	@PostMapping("update")
	public Result update(@RequestBody User user) throws Exception {
		if(StringUtil.isBlank(user.getId())) {
			return Result.FAILURE().message("请输入ID值");
		}
		return userService.update(user);
	}
	
	@PostMapping("select")
	public Result select(@RequestBody User user) throws Exception {
		return userService.select(user);
	}
	
	@PostMapping("delete")
	public Result delete(String id) throws Exception {
		return userService.deleteById(id);
	}
	
	 
	
}
