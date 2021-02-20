package com.demo.generator;

import java.io.File;

import com.alibaba.druid.pool.DruidDataSource;
import com.demo.framework.SuperController;
import com.github.foxnic.commons.busi.Result;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.commons.property.YMLProperties;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.dao.spec.DAOBuilder;
import com.github.foxnic.generator.CodeGenerator;
import com.github.foxnic.sql.treaty.DBTreaty;

public class BasicGenerator {
 
	protected MavenProject project;
	protected YMLProperties properties;
	protected DAO dao;
	protected CodeGenerator generator = null;
	
	public BasicGenerator() {
		//项目描述对象
		project=new MavenProject(this.getClass());
		//配置文件
		File yml=FileUtil.resolveByPath(project.getMainResourceDir(), "application.yml");
		properties=new YMLProperties(yml);
		try {
			initDAO();
		} catch (Exception e) {
			e.printStackTrace();
		}
		initGenerator();
	}
	
	
	private void initGenerator() {
		 
		//创建代码生成器
		generator=new CodeGenerator(this.dao);
		//设置代码生成的默认位置
		generator.setProject(project);
		//
		generator.setAuthor("李方捷");
		//
		generator.setDAONameConst("com.demo.configs.DBConfigs.PRIMARY_DAO");
		//
		generator.setSuperController(SuperController.class.getName());
		generator.setControllerResult(Result.class.getName());
		// 不使用 Swagger
		generator.setEnableSwagger(false);
		// 不生成微服务组件
		generator.setEnableMicroService(false);
	}

	private void initDAO() throws Exception {
		
		// 读取数据库配置
		String prefix="spring.datasource.druid.";
		String driver=properties.getProperty(prefix+"driver-class-name").stringValue();
		String url=properties.getProperty(prefix+"url").stringValue();
		String username=properties.getProperty(prefix+"username").stringValue();
		String password=properties.getProperty(prefix+"password").stringValue();
		
		// 创建数据源
		DruidDataSource ds = new DruidDataSource();
		ds.setUrl(url);
		ds.setDriverClassName(driver);
		ds.setUsername(username);
		ds.setPassword(password);
		dao = (new DAOBuilder()).datasource(ds).build();
		
		// 设置数据库规约
		DBTreaty dbTreaty = new DBTreaty();
		dbTreaty.setAutoCastLogicField(true);
		dbTreaty.setFalseValue(0);
		dbTreaty.setTrueValue(1);
		dbTreaty.setVersionField("version");
		dao.setDBTreaty(dbTreaty);
		

	}
	
}
