package com.github.foxnic.generator;

import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.generator.clazz.ControllerBuilder;
import com.github.foxnic.generator.clazz.PojoBuilder;
import com.github.foxnic.generator.clazz.PoBuilder;
import com.github.foxnic.generator.clazz.ServiceImplBuilder;
import com.github.foxnic.generator.clazz.ServiceInterfaceBuilder;

/**
 *  
 */
public class CodeGenerator {

	private boolean isEnableSwagger=false;
	private boolean isEnableMicroService=false;
	private String author;
	private DAO dao;
	
	private String daoNameConst;
	
	private String superController;
	
	private String controllerResult;
	
	private MavenProject project=null;

	public CodeGenerator(DAO dao) {
		this.dao=dao;
	}


	public CodeGenerator setAuthor(String author) {
		this.author = author;
		return this;
	}
 
	public void build(String tableName, String tablePrefix,ModuleConfig config)
			throws Exception {
 
		//Rcd example=dao.queryRecord("select * from "+tableName);
		
		Context context = new Context(this,config,dao.getDBTreaty(),tableName, tablePrefix, dao.getTableMeta(tableName));

		//构建 PO
		(new PoBuilder(context)).buildAndUpdate();
		//构建 默认VO
		(new PojoBuilder(context,config.getDefaultVO())).buildAndUpdate();
		//构建 自定义VO
		for (Pojo vocfg : config.getPojos()) {
			(new PojoBuilder(context,vocfg)).buildAndUpdate();
		}
		//服务接口
		(new ServiceInterfaceBuilder(context)).buildAndUpdate();
		//服务实现类
		(new ServiceImplBuilder(context)).buildAndUpdate();
		//服务实现类
//		(new AgentBuilder(context)).buildAndUpdate();
		//服务实现类
		(new ControllerBuilder(context)).buildAndUpdate();

	}

	public MavenProject getProject() {
		return project;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}


	public boolean isEnableSwagger() {
		return isEnableSwagger;
	}


	public void setEnableSwagger(boolean isEnableSwagger) {
		this.isEnableSwagger = isEnableSwagger;
	}
	
	


	public String getAuthor() {
		return author;
	}


	public String getDAONameConst() {
		return daoNameConst;
	}


	/**
	 * 设置用于数据库操作的 DAO 常量 , 如  com.github.foxnic.generator.app.config.DBConfigs.PRIMARY_DAO_NAME
	 * */
	public void setDAONameConst(String daoNameConst) {
		this.daoNameConst = daoNameConst;
	}


	public String getSuperController() {
		return superController;
	}

	/**
	 * 设置控制器父类
	 * */
	public void setSuperController(String superController) {
		this.superController = superController;
	}


	public boolean isEnableMicroService() {
		return isEnableMicroService;
	}


	/**
	 * 设置是否使用微服务
	 * */
	public void setEnableMicroService(boolean isEnableMicroService) {
		this.isEnableMicroService = isEnableMicroService;
	}


	public String getControllerResult() {
		return controllerResult;
	}


	/**
	 * 设置 控制器返回的结果类型
	 * */
	public void setControllerResult(String controllerResult) {
		this.controllerResult = controllerResult;
	}


	public DAO getDAO() {
		return dao;
	}

	
	 
 
}
