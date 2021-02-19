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
	
	private String author;
	
	private String daoNameConst;
	
	private String controllerApiPrefix;
	
	private DtoConfig defaultVOConfig=null;
	
	private List<DtoConfig> voConfigs=new ArrayList<>();
	
	public ModuleConfig() {
		defaultVOConfig=new DtoConfig();
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

	public DtoConfig getDefaultVOConfig() {
		return defaultVOConfig;
	}

	public void addVoConfig(DtoConfig vocfg) {
		voConfigs.add(vocfg);
		
	}

	public List<DtoConfig> getVOConfigs() {
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
 
}
