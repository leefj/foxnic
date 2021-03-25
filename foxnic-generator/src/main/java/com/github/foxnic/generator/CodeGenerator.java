package com.github.foxnic.generator;

import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.generator.clazz.AgentBuilder;
import com.github.foxnic.generator.clazz.ControllerBuilder;
import com.github.foxnic.generator.clazz.PoBuilder;
import com.github.foxnic.generator.clazz.PoMetaBuilder;
import com.github.foxnic.generator.clazz.PojoBuilder;
import com.github.foxnic.generator.clazz.PojoMetaBuilder;
import com.github.foxnic.generator.clazz.ServiceImplBuilder;
import com.github.foxnic.generator.clazz.ServiceInterfaceBuilder;
import com.github.foxnic.generator.feature.plugin.ControllerMethodAnnotiationPlugin;

/**
 *  
 */
public class CodeGenerator {
	
	public static enum Mode {
		/**
		 * 所有的代码生成到一个项目
		 * */
		ONE_PROJECT,
		/**
		 * 代码生成多个不同的项目
		 * */
		MULTI_PROJECT;
	}

	private boolean isEnableSwagger=false;
	private boolean isEnableMicroService=false;
	private String author;
	private DAO dao;
	
	private String daoNameConst;
	
	private String superController;
	
	private String controllerResult;
	
	private MavenProject project=null;
	
	private MavenProject domainProject=null;
	private MavenProject serviceProject=null;
	private MavenProject agentProject=null;
	
	private Mode mode = Mode.ONE_PROJECT;
	
	private String sentinelExceptionHandlerClassName=null;
	
	private String feignConfigClassName=null;
	
	private String microServiceNameConst=null;
	
 	 

	public CodeGenerator(DAO dao) {
		this.dao=dao;
	}


	public CodeGenerator setAuthor(String author) {
		this.author = author;
		return this;
	}
	
	private ControllerMethodAnnotiationPlugin controllerMethodAnnotiationPlugin;
 
	public void addCodeBeforeControllerMethod(ControllerMethodAnnotiationPlugin plugin) {
		controllerMethodAnnotiationPlugin=plugin;
	}
	
 
	public void build(String tableName, String tablePrefix,ModuleConfig config)
			throws Exception {
		
		CodePoint codePoint = new CodePoint(tableName,dao);
 
		Rcd example=dao.queryRecord("select * from "+tableName);
		
		DBTableMeta tm =  dao.getTableMeta(tableName);
		if(tm.getPKColumnCount()==0) {
			throw new IllegalArgumentException("表 "+tableName+" 缺少主键");
		}
		
		Context context = new Context(codePoint,this,config,dao.getDBTreaty(),tableName, tablePrefix, tm,example);

		//构建 PO
		(new PoBuilder(context)).buildAndUpdate();
		//构建 POMeta
		(new PoMetaBuilder(context)).buildAndUpdate();
		//构建 默认VO
		(new PojoBuilder(context,config.getDefaultVO())).buildAndUpdate();
		config.getDefaultVO().setSuperClass(context.getPoName());
		(new PojoMetaBuilder(context,config.getDefaultVO())).buildAndUpdate(); 
		//构建 自定义Pojo
		for (Pojo vocfg : config.getPojos()) {
			(new PojoBuilder(context,vocfg)).buildAndUpdate();
			(new PojoMetaBuilder(context,vocfg)).buildAndUpdate(); 
		}
		//服务接口
		(new ServiceInterfaceBuilder(context)).buildAndUpdate();
		//服务实现类
		(new ServiceImplBuilder(context)).buildAndUpdate();
		//接口代理现类
		if(this.isEnableMicroService) {
			(new AgentBuilder(context)).buildAndUpdate();
		}
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
	
	
	public MavenProject getDomainProject() { 
		return domainProject;
	}

	public void setDomanProject(MavenProject domainProject) {
		this.domainProject = domainProject;
	}

	public MavenProject getServiceProject() {
		return serviceProject;
	}

	public void setServiceProject(MavenProject serviceProject) {
		this.serviceProject = serviceProject;
	}


	public Mode getMode() {
		return mode;
	}
 
	
	public void setMode(Mode mode) {
		this.mode = mode;
	}


	public MavenProject getAgentProject() {
		return agentProject;
	}


	public void setAgentProject(MavenProject agentProject) {
		this.agentProject = agentProject;
	}


	public String getSentinelExceptionHandlerClassName() {
		return sentinelExceptionHandlerClassName;
	}


	public void setSentinelExceptionHandlerClassName(String sentinelExceptionHnadlerClassName) {
		this.sentinelExceptionHandlerClassName = sentinelExceptionHnadlerClassName;
	}


	public String getFeignConfigClassName() {
		return feignConfigClassName;
	}


	public void setFeignConfigClassName(String feignConfigClassName) {
		this.feignConfigClassName = feignConfigClassName;
	}


	public String getMicroServiceNameConst() {
		return microServiceNameConst;
	}


	public void setMicroServiceNameConst(String microServiceNamesCont) {
		this.microServiceNameConst = microServiceNamesCont;
	}


	public ControllerMethodAnnotiationPlugin getControllerMethodAnnotiationPlugin() {
		return controllerMethodAnnotiationPlugin;
	}
 
 
}
