package com.github.foxnic.generator;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.data.QueryMetaData;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;
import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.treaty.DBTreaty;

public class Context {
	
	private DefaultNameConvertor convertor = new DefaultNameConvertor();
	
	private CodeGenerator generator;
	
	private DBTreaty dbTreaty;
	private String daoNameConst;
 
	private String microServiceNameConst;
	private String projectDirName;
	private String tableName;
	private String tablePrefix;
	private String modulePackageName;
	private String apiPrefix;
	private String author;
	private DBTableMeta tableMeta;
	private Rcd example;
	
	//
	private MavenProject domainProject=null;
	private MavenProject serviceProject=null;
	//
	private String poName=null;
	private String poVarName=null;
	private String poPackage=null;
	private String poFullName=null;
	
	//
//	private String pojoName=null;
//	private String pojoVarName=null;
//	private String pojoPackage=null;
//	private String pojoFullName=null;
 

	//
	private String intfName=null;
	private String intfVarName=null;
	private String intfPackage=null;
	private String intfFullName=null;
	
	//
	private String implName=null;
	private String implPackage=null;
	private String implFullName=null;
	
	//
	private String ctrlName=null;
	private String ctrlPackage=null;
	private String ctrlFullName=null;
	
	//
	private String agentName=null;
	private String agentPackage=null;
	private String agentFullName=null;
	
	//
	private String sentinelExceptionHnadlerClassName=null;
	private String superController=null;

	//
	private ModuleConfig config=null;
 
	public Context(CodeGenerator generator,ModuleConfig config,DBTreaty dbTreaty,String tableName,String tablePrefix,DBTableMeta tableMeta) {
		
		this.generator=generator;
		this.config=config;
		this.dbTreaty=dbTreaty;
		this.daoNameConst=this.getFristValue(config.getDAONameConst(),generator.getDAONameConst());
		this.tableName=tableName;
		this.tablePrefix=tablePrefix;
		this.author= this.getFristValue(config.getAuthor(),generator.getAuthor());

		this.tableMeta=tableMeta;
		
		//
		String tmp = tableName.substring(tablePrefix.length());
		this.poName = convertor.getClassName(tmp, 0);
		this.poVarName = convertor.getPropertyName(tmp);
		this.poPackage=config.getModulePackage() + ".domain";
		this.poFullName=this.poPackage+"."+this.poName;
		this.domainProject=this.getFristValue(config.getDomainProject(),config.getProject(),generator.getProject());
		
		//
		DAO dao=generator.getDAO();
		this.config.getDefaultVO().bind(this.poName+"VO",config.getModulePackage()+".domain");
		for (Pojo pojo : config.getPojos()) {
			pojo.bind(null, config.getModulePackage()+".domain");
			if(!StringUtil.isBlank(pojo.getTemplateSQL())) {
				Rcd sample=dao.queryRecord(pojo.getTemplateSQL());
				if(sample==null) {
					throw new IllegalArgumentException("缺少数据");
				}
				QueryMetaData qmd=sample.getOwnerSet().getMetaData();
				for (int i = 0; i < qmd.getColumnCount(); i++) {
					String table=qmd.getTableName(i);
					String field=qmd.getColumnLabel(i);
					String label=field;
					String note=field;
					DBColumnMeta cm=dao.getTableColumnMeta(table, field);
					if(cm!=null) {
						label=cm.getColumn();
						note=cm.getDetail();
					}
					pojo.addProperty(field,  ReflectUtil.forName(qmd.getColumnClassName(i)) , label, note);
				}
			}
		}
		//
//		this.pojoName=this.poName+"DTO";
//		this.pojoVarName=this.poVarName+"DTO";
//		this.voPackage=config.getModulePackage() + ".domain." + modulePackageName+".vo";
//		this.pojoPackage=config.getModulePackage() + ".domain.dto";
//		this.pojoFullName=this.pojoPackage+"."+this.pojoName;
//		this.dtoProject=this.getFristValue(config.getDomainProject(),config.getProject(),generator.getProject());
		//
		this.intfName="I"+this.poName+"Service";
		this.intfVarName=this.poVarName+"Service";
//		this.intfPackage=config.getModulePackage() + "." + modulePackageName+".service";
		this.intfPackage=config.getModulePackage() + ".service";
		this.intfFullName=this.intfPackage+"."+this.intfName;
		this.serviceProject=this.getFristValue(config.getServiceProject(),config.getProject(),generator.getProject());
		//
		this.implName=this.poName+"ServiceImpl";
//		this.implPackage=config.getModulePackage() + "." + modulePackageName+".service.impl";
		this.implPackage=config.getModulePackage() + ".service.impl";
		this.implFullName=this.implPackage+"."+this.implName;
		
		//
		this.ctrlName=this.poName+"Controller";
//		this.ctrlPackage=config.getModulePackage() + "." + modulePackageName+".controller";
		this.ctrlPackage=config.getModulePackage() + ".controller";
		this.ctrlFullName=this.ctrlPackage+"."+this.ctrlName;
		this.superController=this.getFristValue(this.generator.getSuperController());
		//
		this.agentName=this.poName+"ServiceAgent";
//		this.agentPackage=config.getModulePackage() + ".agent.service." + modulePackageName;
		this.agentPackage=config.getModulePackage() + ".agent.service";
		this.agentFullName=this.agentPackage+"."+this.agentName;
		
	}
	
	public String getApiContextPart() {
		return this.tableName.substring(tablePrefix==null?0:(tablePrefix.length()+1));
	}

	public String getPoName() {
		return poName;
	}
	
	/**
	 * 在 mybatis xml 文件中 resultMap 的 Id 
	 * */
	public String getXMLPoResultId() {
		return poName+"Result";
	}
	
	public String getPoPackage() {
		return poPackage;
	}

	public DefaultNameConvertor getConvertor() {
		return convertor;
	}
 
	public String getTableName() {
		return tableName;
	}

	public String getTablePrefix() {
		return tablePrefix;
	}

	public String getModulePackageName() {
		return modulePackageName;
	}

	public DBTableMeta getTableMeta() {
		return tableMeta;
	}

	public String getAuthor() {
		return author;
	}

	public String getPoFullName() {
		return poFullName;
	}
	
	public Pojo getDefaultVO() {
		return config.getDefaultVO();
	}
	

//	public String getDtoName() {
//		return pojoName;
//	}
//
//	public String getDtoPackage() {
//		return pojoPackage;
//	}
//
//	public String getDtoFullName() {
//		return pojoFullName;
//	}

	public String getPoVarName() {
		return poVarName;
	}

//	public String getDtoVarName() {
//		return pojoVarName;
//	}
 
	public String getProjectDirName() {
		return projectDirName;
	}

	public String getIntfName() {
		return intfName;
	}

	public String getIntfVarName() {
		return intfVarName;
	}

	public String getIntfPackage() {
		return intfPackage;
	}

	public String getIntfFullName() {
		return intfFullName;
	}

	public String getImplName() {
		return implName;
	}

	public String getImplPackage() {
		return implPackage;
	}

	public String getImplFullName() {
		return implFullName;
	}

	public String getCtrlName() {
		return ctrlName;
	}

	public String getCtrlPackage() {
		return ctrlPackage;
	}

	public String getCtrlFullName() {
		return ctrlFullName;
	}

	public String getAgentName() {
		return agentName;
	}

	public String getAgentPackage() {
		return agentPackage;
	}

	public String getAgentFullName() {
		return agentFullName;
	}

	public String getApiPrefix() {
		return apiPrefix;
	}

	public String getMicroServiceNameConst() {
		return microServiceNameConst;
	}
	
	public boolean isDBTreatyFiled(DBColumnMeta cm) {
		return this.isDBTreatyFiled(cm.getColumn());
	}
	
	private Set<String> dbTreatyFileds= new HashSet<>();
	
	public boolean isDBTreatyFiled(String f) {
		if(dbTreatyFileds.size()==0) {
			dbTreatyFileds.add(dbTreaty.getCreateTimeField().toUpperCase());
			dbTreatyFileds.add(dbTreaty.getCreateUserIdField().toUpperCase());
			
			dbTreatyFileds.add(dbTreaty.getUpdateTimeField().toUpperCase());
			dbTreatyFileds.add(dbTreaty.getUpdateUserIdField().toUpperCase());
			
			dbTreatyFileds.add(dbTreaty.getDeleteTimeField().toUpperCase());
			dbTreatyFileds.add(dbTreaty.getDeleteUserIdField().toUpperCase());
			dbTreatyFileds.add(dbTreaty.getDeletedField().toUpperCase());
			dbTreatyFileds.add(dbTreaty.getVersionField().toUpperCase());
		}
		return dbTreatyFileds.contains(f.toUpperCase());
	}

	public Rcd getExample() {
		return example;
	}

	
	public String getExampleStringValue(DBColumnMeta cm) {
		if(this.example==null) return null;
		DBDataType ft= cm.getDBDataType();
		String example="";
		if(ft==DBDataType.DATE) {
			Date d=this.example.getDate(cm.getColumn());
			if(d!=null) {
				example=DateUtil.format(d, "yyyy-MM-dd hh:mm:ss");
			}
		} else {
			example=this.example.getString(cm.getColumn());
		}
		return example;
	}
	
	public String makePKConditionSQL(List<DBColumnMeta> pks,String tableAlias) {
		int i=0;
		String condition="where ";
		String andstr="";
		for (DBColumnMeta pk : pks) {
			//}
			String jdbcType=pk.getJDBCDataType();
			if(jdbcType!=null) {
				jdbcType=" , jdbcType="+jdbcType.toUpperCase();
			} else {
				jdbcType="";
			}
			if(i==0) {
				andstr="";
			} else {
				andstr="and ";
			}
			i++;
			condition += andstr +(StringUtil.isBlank(tableAlias)?"":(tableAlias+"."))+pk.getColumn()+" = #{ "+pk.getColumnVarName()+jdbcType+" }";
		}
		return condition;
	}

	public DBTreaty getDBTreaty() {
		return dbTreaty;
	}

	public String getSentinelExceptionHnadlerClassName() {
		return sentinelExceptionHnadlerClassName;
	}

	public void setSentinelExceptionHnadlerClassName(String sentinelExceptionHnadlerClassName) {
		this.sentinelExceptionHnadlerClassName = sentinelExceptionHnadlerClassName;
	}

	public String getSuperController() {
		return superController;
	}
 
	public String getControllerResult() {
		return generator.getControllerResult();
	}

	public String getControllerApiPrefix() {
		return config.getControllerApiPrefix();
	}
	
	
	
	private <T> T getFristValue(T... vals) {
		for (T v : vals) {
			if(v instanceof String) {
				if(!StringUtil.isBlank((String)v)) return v;
			} else {
				if(v!=null) return v;
			}
		}
		return null;
	}

	public MavenProject getDomainProject() {
		return this.domainProject;
	}
	
	public boolean isEnableSwagger() {
		return this.generator.isEnableSwagger();
	}

	public MavenProject getServiceProject() {
		return this.serviceProject;
	}

	public String getDaoNameConst() {
		return daoNameConst;
	}

	public boolean isEnableMicroService() {
		return generator.isEnableMicroService();
	}
	
}
