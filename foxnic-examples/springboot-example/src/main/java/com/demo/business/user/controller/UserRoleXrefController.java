package com.demo.business.user.controller;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import com.github.foxnic.dao.data.SaveMode;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.foxnic.dao.data.PagedList;
import com.demo.framework.SuperController;
import org.springframework.web.bind.annotation.PostMapping;
import com.demo.business.user.domain.UserRoleXrefVO;
import com.demo.business.user.service.IUserRoleXrefService;
import com.github.foxnic.springboot.mvc.Result;
import com.demo.business.user.domain.UserRoleXref;


/**
 * @author 李方捷
 * @since 2021-02-26 04:44:47
*/

@RestController
@RequestMapping("/api/relation")
public class UserRoleXrefController extends SuperController {

	@Autowired
	private IUserRoleXrefService userRoleXrefService;

	
	/**
	 * 按主键获取用户角色关联表
	*/
	@PostMapping("getById")
	public  Result<UserRoleXref> getById(Long id) {
		Result<UserRoleXref> result=new Result<>();
		UserRoleXref userRoleXref=userRoleXrefService.getById(id);
		result.success(true).data(userRoleXref);
		return result;
	}

	
	/**
	 * 查询全部符合条件的用户角色关联表
	*/
	@PostMapping("queryList")
	public  Result<List<UserRoleXref>> queryList(UserRoleXrefVO sample) {
		Result<List<UserRoleXref>> result=new Result<>();
		List<UserRoleXref> list=userRoleXrefService.queryEntities(sample);
		result.success(true).data(list);
		return result;
	}

	
	/**
	 * 分页查询符合条件的用户角色关联表
	*/
	@PostMapping("queryPagedList")
	public  Result<PagedList<UserRoleXref>> queryPagedList(UserRoleXrefVO sample) {
		Result<PagedList<UserRoleXref>> result=new Result<>();
		PagedList<UserRoleXref> list=userRoleXrefService.queryPagedEntities(sample,sample.getPageSize(),sample.getPageIndex());
		result.success(true).data(list);
		return result;
	}

	
	/**
	 * 添加用户角色关联表
	*/
	@PostMapping("insert")
	public  Result<UserRoleXref> insert(UserRoleXrefVO userRoleXrefVO) {
		Result<UserRoleXref> result=new Result<>();
		boolean suc=userRoleXrefService.insertEntity(userRoleXrefVO);
		result.success(suc);
		return result;
	}

	
	/**
	 * 更新用户角色关联表
	*/
	@PostMapping("update")
	public  Result<UserRoleXref> update(UserRoleXrefVO userRoleXrefVO) {
		Result<UserRoleXref> result=new Result<>();
		boolean suc=userRoleXrefService.updateEntity(userRoleXrefVO,SaveMode.NOT_NULL_FIELDS);
		result.success(suc);
		return result;
	}

	
	/**
	 * 按主键删除用户角色关联表
	*/
	@PostMapping("deleteById")
	public  Result deleteById(Long id) {
		Result result=new Result();
		boolean suc=userRoleXrefService.deleteByIdPhysical(id);
		result.success(suc);
		return result;
	}


}