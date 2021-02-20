package com.demo.business.user.controller;
import com.github.foxnic.commons.busi.Result;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import com.demo.business.user.service.IUserService;
import com.github.foxnic.dao.data.SaveMode;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import com.demo.framework.SuperController;
import org.springframework.web.bind.annotation.PostMapping;
import com.demo.business.user.domain.dto.UserDTO;
import com.demo.business.user.domain.po.User;


/**
 * @author 李方捷
 * @since 2021-02-20 04:37:54
*/

@RestController
@RequestMapping("/usr/user")
public class UserController extends SuperController {

	@Autowired
	private IUserService userService;

	
	/**
	 * 按主键获取用户表
	*/
	@PostMapping("getById")
	public  Result<User> getById(Long id) {
		Result<User> result=new Result<>();
		User user=userService.getById(id);
		result.success(true).data(user);
		return result;
	}

	
	/**
	 * 查询全部符合条件的用户表
	*/
	@PostMapping("queryList")
	public  Result<List<User>> queryList(UserDTO sample) {
		Result<List<User>> result=new Result<>();
		List<User> list=userService.queryEntities(sample);
		result.success(true).data(list);
		return result;
	}

	
	/**
	 * 分页查询符合条件的用户表
	*/
	@PostMapping("queryPagedList")
	public  Result<List<User>> queryPagedList(UserDTO sample) {
		Result<List<User>> result=new Result<>();
		List<User> list=userService.queryPagedEntities(sample,sample.getPageSize(),sample.getPageIndex());
		result.success(true).data(list);
		return result;
	}

	
	/**
	 * 添加用户表
	*/
	@PostMapping("insert")
	public  Result<User> insert(UserDTO userDTO) {
		Result<User> result=new Result<>();
		boolean suc=userService.insertEntity(userDTO);
		result.success(suc).data(userDTO);
		return result;
	}

	
	/**
	 * 更新用户表
	*/
	@PostMapping("update")
	public  Result<User> update(UserDTO userDTO) {
		Result<User> result=new Result<>();
		boolean suc=userService.updateEntity(userDTO,SaveMode.NOT_NULL_FIELDS);
		result.success(suc).data(userDTO);
		return result;
	}

	
	/**
	 * 按主键删除用户表
	*/
	@PostMapping("deleteById")
	public  Result deleteById(Long id) {
		Result result=new Result();
		boolean suc=userService.deleteByIdPhysical(id);
		result.success(suc);
		return result;
	}


}