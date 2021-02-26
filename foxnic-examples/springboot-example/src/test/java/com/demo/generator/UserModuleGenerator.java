package com.demo.generator;

import java.math.BigDecimal;

import org.junit.Test;

import com.github.foxnic.generator.ModuleConfig;
import com.github.foxnic.generator.Pojo;
 
 

/**
 * 为以usr_开头的表生成代码
 */
public class UserModuleGenerator extends BasicGenerator {
 
	/**
	 * 表名前缀
	 * */
	private String tablePrefix="usr_";
	
	/**
	 * 默认包名
	 * */
	private String pkgname="com.demo.business.user";
	
 
	@Test
	public void generateNews() throws Exception {

		//指定表名
		String tableName="usr_user";
		
		//创建模块配置对象
		ModuleConfig mducfg=new ModuleConfig();
		//设置包名
		mducfg.setModulePackage(pkgname);
		mducfg.setControllerApiPrefix("/user");
		
		//## 生成额外的DTO
		Pojo activedUserVO=new Pojo();
		activedUserVO.setName("ActivedUserVO");
		//不继承自任何父类
		activedUserVO.setSuperClass("");
		//加入属性
		activedUserVO.addProperty("isActive", Boolean.class, "是否激活", "激活时显示内容");
		activedUserVO.addProperty("passed", Boolean.class, "审批通过", "是否已经审批通过");
		activedUserVO.addProperty("action", String.class, "操作类型", "audit:审批 ; cancel:取消");
		activedUserVO.addProperty("amount", BigDecimal.class, "总量", "汇总统计的结果");
		mducfg.addPojo(activedUserVO);
		
		
		//## 生成一个继承的Pojo 
		Pojo userDTO=new Pojo();
		userDTO.setName("UserDTO");
		//不继承自任何父类
		userDTO.setSuperClass(activedUserVO.getFullName());
		userDTO.addProperty("price", BigDecimal.class, "价格", "单价，精确到小数点后2位");
		mducfg.addPojo(userDTO);
		
		
		
		//##  通过查询语句生成实体（必须有查询数据才能生成）
		Pojo newsByQuery=new Pojo();
		newsByQuery.setName("UserRoleQuery");
		//不继承自任何父类
		newsByQuery.setSuperClass("");
		String sql="SELECT a.*,b.name role_name from usr_user a,usr_role b,usr_user_role_xref c where a.id=c.user_id and b.id=c.role_id";
		newsByQuery.setTemplateSQL(sql);
		//覆盖
		newsByQuery.setProperty("role_name",String.class,"角色","角色名称");
		mducfg.addPojo(newsByQuery);
		
		//## 生成代码
		generator.build(tableName,tablePrefix,mducfg);
 
	}
	
	
	@Test
	public void generateRelation() throws Exception {
 
		//指定表名
		String tableName="usr_user_role_xref";
		ModuleConfig mducfg=new ModuleConfig();
		mducfg.setModulePackage(pkgname);
		mducfg.setControllerApiPrefix("/api/relation");
		
		generator.build(tableName,tablePrefix,mducfg);
 
	}
	
	 

}
