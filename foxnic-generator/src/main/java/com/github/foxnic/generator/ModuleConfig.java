package com.github.foxnic.generator;

import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;

public class ModuleConfig {
	
	private String modulePackage;
	private String microServiceNameConst;
	 
	private MavenProject project=null;
	private MavenProject domainProject=null;
	private MavenProject serviceProject=null;
	private MavenProject agentProject=null;
	
	private boolean override=true;
	
	private String author;
	
	private String daoNameConst;
	
	private String controllerApiPrefix;
	
	private String sentinelExceptionHnadlerClassName=null;
	
	private Pojo defaultVOConfig=null;
	
	private List<Pojo> voConfigs=new ArrayList<>();
	
	
	private boolean forceOverrideController=true;
	
	
	/**
	 * 生成的接口API sort 从 1000 开始
	 * */
	private static int BASE_API_SORT=1000;
	
	private Integer apiSort=0;
	
	public ModuleConfig() {
		defaultVOConfig=new Pojo();
		//默认继承自PO
		defaultVOConfig.setSuperClass(null);
		//设置属性
		defaultVOConfig.addProperty("pageIndex", Integer.class, "页码", "");
		defaultVOConfig.addProperty("pageSize", Integer.class, "分页大小", "");
 
		
	}
	
	public String getModulePackage() {
		return modulePackage;
	}
	
	/**
	 * 设置包的完整路径，如 com.github.foxnic.generator.app.news
	 * */
	public void setModulePackage(String modulePackage) {
		this.modulePackage = StringUtil.trim(modulePackage, ".") ;
	}
	public String getMicroServiceNameConst() {
		return microServiceNameConst;
	}
	public void setMicroServiceNameConst(String microServiceNameConst) {
		this.microServiceNameConst = microServiceNameConst;
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

	public MavenProject getProject() {
		return project;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Pojo getDefaultVO() {
		return defaultVOConfig;
	}

	public void addPojo(Pojo vocfg) {
		voConfigs.add(vocfg);
		
	}

	public List<Pojo> getPojos() {
		return voConfigs;
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

	public String getControllerApiPrefix() {
		return controllerApiPrefix;
	}

	public void setControllerApiPrefix(String controllerApiPrefix) {
		this.controllerApiPrefix = controllerApiPrefix;
	}

	public MavenProject getAgentProject() {
		return agentProject;
	}

	public void setAgentProject(MavenProject agentProject) {
		this.agentProject = agentProject;
	}

	public String getSentinelExceptionHnadlerClassName() {
		return sentinelExceptionHnadlerClassName;
	}

	public void setSentinelExceptionHnadlerClassName(String sentinelExceptionHnadlerClassName) {
		this.sentinelExceptionHnadlerClassName = sentinelExceptionHnadlerClassName;
	}

	public Integer getApiSort() {
		return BASE_API_SORT+apiSort;
	}

	public void setApiSort(Integer apiSort) {
		this.apiSort = apiSort;
	}

	public boolean isOverride() {
		return override;
	}

	/**
	 * 是否重新生成 Controller 和 Agent 方法，默认 true , 当设置为 false 时，会生成一个新的 .code 文件
	 * */
	public void setOverride(boolean override) {
		this.override = override;
	}

	public boolean isForceOverrideController() {
		return forceOverrideController;
	}

	public void setForceOverrideController(boolean forceOverrideController) {
		this.forceOverrideController = forceOverrideController;
	}
 
}
