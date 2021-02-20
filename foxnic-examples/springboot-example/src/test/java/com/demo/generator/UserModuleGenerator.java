package com.demo.generator;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.demo.framework.SuperController;
import com.github.foxnic.commons.busi.Result;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.generator.CodeGenerator;
import com.github.foxnic.generator.Pojo;
import com.github.foxnic.generator.Pojo.PojoType;
import com.github.foxnic.generator.ModuleConfig;
 
 

/**
 * 
 */
public class UserModuleGenerator extends BasicGenerator {
 
	private String tablePrefix="usr_";
	private String pkgname="com.demo.business.user";
	
	@Test
	public void generateNone() throws Exception {
		System.out.println();
	}
	
	@Test
	public void generateNews() throws Exception {

		//生成 chihuo_shop 代码
		ModuleConfig cfg=new ModuleConfig();
		//设置模块所在的包
		cfg.setModulePackage(pkgname);
		
		//生成额外的DTO
		Pojo vo1=new Pojo(PojoType.DTO);
		vo1.setName("ActivedUserDTO");
		//不继承自任何父类
		vo1.setSuperClass("");
		//加入属性
		vo1.addProperty("isActive", Boolean.class, "是否激活", "激活时显示内容");
		vo1.addProperty("passed", Boolean.class, "审批通过", "是否已经审批通过");
		vo1.addProperty("action", String.class, "操作类型", "audit:审批 ; cancel:取消");
		vo1.addProperty("amount", BigDecimal.class, "总量", "汇总统计的结果");
		cfg.addPojo(vo1);
		

		generator.build("usr_user",tablePrefix,cfg);
 
	}
	
	
//	@Test
	public void generateRelation() throws Exception {
 
		//生成 chihuo_shop 代码
		ModuleConfig cfg=new ModuleConfig();
		//cfg.setMicroServiceNameConst("SERVICE_ZENTAO");
		//设置模块所在的包
		cfg.setModulePackage("com.github.foxnic.generator.app.relation");
		cfg.setControllerApiPrefix("/api/relation");
		
		generator.build("test_relation","test_",cfg);
 
	}
	
	 

}
