package com.github.foxnic.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.generator.CodeGenerator.Mode;
import com.github.foxnic.generator.clazz.FormPageHTMLBuilder;
import com.github.foxnic.generator.clazz.FormPageJSBuilder;
import com.github.foxnic.generator.clazz.ListPageHTMLBuilder;
import com.github.foxnic.generator.clazz.ListPageJSBuilder;
import com.github.foxnic.generator.clazz.model.LogicField;
import com.github.foxnic.generatorV2.config.MduCtx;
import com.github.foxnic.generatorV2.config.WriteMode;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;

public class ModuleConfig {
	
	private MduCtx mductx=null;
	
	public static class TreeConfig {
		
		
		
		
		private Object rootId=null;
		private DBField idField;
		private DBField nameField;
		private DBField parentIdField;
		private String dimension;
		
		public DBField getIdField() {
			return idField;
		}
		public void setIdField(DBField idField) {
			this.idField = idField;
		}
		public DBField getParentIdField() {
			return parentIdField;
		}
		public void setParentIdField(DBField parentIdField) {
			this.parentIdField = parentIdField;
		}
		public String getDimension() {
			return dimension;
		}
		public void setDimension(String dimension) {
			this.dimension = dimension;
		}
		public DBField getNameField() {
			return nameField;
		}
		public void setNameField(DBField nameField) {
			this.nameField = nameField;
		}
		public Object getRootId() {
			return rootId;
		}
		public void setRootId(Object rootId) {
			this.rootId = rootId;
		}
	}
	
	
	
	
	
	
	private String modulePackage;
	private String microServiceNameConst;
	 
	private MavenProject project=null;
	private MavenProject domainProject=null;
	private MavenProject serviceProject=null;
	private MavenProject agentProject=null;
	
	
	
	private String author;
	
	private String daoNameConst;
	
	private String controllerApiPrefix;
	
	private String sentinelExceptionHnadlerClassName=null;
	
//	private Pojo defaultVOConfig=null;
	
//	private List<Pojo> voConfigs=new ArrayList<>();
	
	private DBField[] imageIdFields=null;
	
	private DBField[] mulitiLineFields=null;
	
	
	private TreeConfig treeConfig=null;
	
//	private EnumInfo enumInfo;
	
//	public void setEnumInfo(DBField nameField,DBField textField) {
//		enumInfo=new EnumInfo();
//		enumInfo.setDataTable(this.getTableName());
//		enumInfo.setNameField(nameField.name());
//		enumInfo.setTextField(textField.name());
//	}
 
	/**
	 * 生成的接口API sort 从 1000 开始
	 * */
	private static int BASE_API_SORT=1000;
	
	private Integer apiSort=0;
	private String uiPathPrefix;
	
	//指定表名
	private DBTable table=null;
 
	private String tablePrefix="prd_";
	
	public ModuleConfig(MduCtx mdu, DBTable table,String tablePrefix) {
		
		mductx=mdu;
		
		this.table=table;
		this.tablePrefix=tablePrefix;
		
//		defaultVOConfig=new Pojo();
//		//默认继承自PO
//		defaultVOConfig.setSuperClass(null);
//		//
//		defaultVOConfig.setDoc("继承自PO的默认VO类，主要用于接口传参");
//		//设置属性
//		defaultVOConfig.addProperty("pageIndex", Integer.class, "页码", "");
//		defaultVOConfig.addProperty("pageSize", Integer.class, "分页大小", "");
//		//设置搜索属性
//		defaultVOConfig.addProperty("searchField", String.class, "搜索的字段", "");
//		defaultVOConfig.addProperty("searchValue", String.class, "搜索的值", "");
// 
		
	}
	
	public String getModulePackage() {
		return modulePackage;
	}
	
	public String getPoPackage(Mode mode) {
		String pkg=null;
		if(mode==Mode.ONE_PROJECT) {
			pkg = this.getModulePackage() + ".domain";
		} else if(mode==Mode.MULTI_PROJECT) {
			String[] arr=this.getModulePackage().split("\\.");
			String last=arr[arr.length-1];
			arr=ArrayUtil.append(arr, last);
			arr[arr.length-2]="domain";
			pkg = StringUtil.join(arr,".");
		}
		return pkg;
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

//	public Pojo getDefaultVO() {
//		return defaultVOConfig;
//	}

//	public void addPojo(Pojo vocfg) {
//		voConfigs.add(vocfg);
// 
//	}

//	public List<Pojo> getPojos() {
//		return voConfigs;
//	}
//	
	
	
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
	
	
 
	public String getUIPathPrefix() {
		return uiPathPrefix;
	}

	public void setUIPathPrefix(String uiPathPrefix) {
		this.uiPathPrefix = uiPathPrefix;
	}

	public String getTableName() {
		return table.name();
	}

//	public void setTableName(String tableName) {
//		this.tableName = tableName;
//	}

	public String getTablePrefix() {
		return tablePrefix;
	}

	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	public TreeConfig getTreeConfig() {
		return treeConfig;
	}

	public void setTreeConfig(TreeConfig treeConfig) {
		this.treeConfig = treeConfig;
	}

	public DBField[] getImageIdFields() {
		return imageIdFields;
	}
	
	public void setImageIdFields(DBField...  imageIdFields) {
		this.imageIdFields = imageIdFields;
	}

	 
	
	private List<LogicField> logicFields=new  ArrayList<>();

	public LogicField addLogicField(DBField field) {
		LogicField lf=new LogicField(field);
		logicFields.add(lf);
		return lf;
	}

	public List<LogicField> getLogicFields() {
		return logicFields;
	}

	public void setMultiLineField(DBField... fields) {
		this.mulitiLineFields=fields;
	}

	public DBField[] getMulitiLineFields() {
		return mulitiLineFields;
	}

//	public EnumInfo getEnumInfo() {
//		if(enumInfo==null) return null;
//		enumInfo.setDataTable(this.getTableName());
//		return enumInfo;
//	}

	public DBTable getTable() {
		return this.table;
	}

	public MduCtx getMductx() {
		return mductx;
	}

	public void setMductx(MduCtx mductx) {
		this.mductx = mductx;
	}
	
	
	
 
 
}
