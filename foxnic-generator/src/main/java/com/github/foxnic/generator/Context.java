package com.github.foxnic.generator;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;
import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.treaty.DBTreaty;

public class Context {
	
	private DefaultNameConvertor convertor = new DefaultNameConvertor();
	
	
	private DBTreaty dbTreaty;
	
	/**
	 * 基础包
	 * */
	private  String basePackage = null;
	
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
	private String poName=null;
	private String poVarName=null;
	private String poPackage=null;
	private String poFullName=null;
	//
	private String voName=null;
	private String voVarName=null;
	private String voPackage=null;
	private String voFullName=null;
	
	//
	private String mapperName=null;
	private String mapperVarName=null;
	private String mapperPackage=null;
	private String mapperFullName=null;
	
	//
	private String rawMapperName=null;
	private String rawMapperVarName=null;
	private String rawMapperPackage=null;
	private String rawMapperFullName=null;
	
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
	private String baseControllerClassName=null;
	private String resultClassName=null;
	//
	private Config config=null;
 
	public Context(CodeGenerator generator,Config config,DBTreaty dbTreaty,String tableName,String tablePrefix,DBTableMeta tableMeta) {
		
		this.config=config;
//		this.basePackage=basePackage;
		this.dbTreaty=dbTreaty;
//		this.microServiceNameConst=microServiceNameConst;
//		this.projectDirName=projectDirName;
		this.tableName=tableName;
		this.tablePrefix=tablePrefix;
		this.author=author;
//		this.modulePackageName=modulePackageName;
//		this.apiPrefix="/"+this.modulePackageName.replace('.', '/')+"/";
		this.tableMeta=tableMeta;
//		this.example=example;
		//
		String tmp = tableName.substring(tablePrefix.length()+1);
		this.poName = convertor.getClassName(tmp, 0);
		this.poVarName = convertor.getPropertyName(tmp);
		this.poPackage=basePackage + ".domain." + modulePackageName+".po";
		this.poFullName=this.poPackage+"."+this.poName;
		//
		this.voName=this.poName+"VO";
		this.voVarName=this.poVarName+"VO";
		this.voPackage=basePackage + ".domain." + modulePackageName+".vo";
		this.voFullName=this.voPackage+"."+this.voName;
		
		//
		this.mapperName=this.poName+"Mapper";
		this.mapperVarName=this.poVarName+"Mapper";
		this.mapperPackage=basePackage + "." + modulePackageName+".mapper";
		this.mapperFullName=this.mapperPackage+"."+this.mapperName;
		
		//
		this.rawMapperName=this.poName+"RawMapper";
		this.rawMapperVarName=this.poVarName+"RawMapper";
		this.rawMapperPackage=basePackage + "." + modulePackageName+".mapper.raw";
		this.rawMapperFullName=this.mapperPackage+".raw."+this.rawMapperName;
		
		
		//
		this.intfName="I"+this.poName+"Service";
		this.intfVarName=this.poVarName+"Service";
		this.intfPackage=basePackage + "." + modulePackageName+".service";
		this.intfFullName=this.intfPackage+"."+this.intfName;
		
		//
		this.implName=this.poName+"ServiceImpl";
		this.implPackage=basePackage + "." + modulePackageName+".service.impl";
		this.implFullName=this.implPackage+"."+this.implName;
		
		//
		this.ctrlName=this.poName+"Controller";
		this.ctrlPackage=basePackage + "." + modulePackageName+".controller";
		this.ctrlFullName=this.ctrlPackage+"."+this.ctrlName;
		
		//
		this.agentName=this.poName+"ServiceAgent";
		this.agentPackage=basePackage + ".agent.service." + modulePackageName;
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

	public String getBasePackage() {
		return basePackage;
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

	public String getVoName() {
		return voName;
	}

	public String getVoPackage() {
		return voPackage;
	}

	public String getVoFullName() {
		return voFullName;
	}

	public String getPoVarName() {
		return poVarName;
	}

	public String getVoVarName() {
		return voVarName;
	}

	public String getMapperName() {
		return mapperName;
	}

	public String getMapperVarName() {
		return mapperVarName;
	}

	public String getMapperPackage() {
		return mapperPackage;
	}

	public String getMapperFullName() {
		return mapperFullName;
	}

	public String getRawMapperName() {
		return rawMapperName;
	}

	public String getRawMapperVarName() {
		return rawMapperVarName;
	}

	public String getRawMapperPackage() {
		return rawMapperPackage;
	}

	public String getRawMapperFullName() {
		return rawMapperFullName;
	}

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

	public String getBaseControllerClassName() {
		return baseControllerClassName;
	}

	public void setBaseControllerClassName(String baseControllerClassName) {
		this.baseControllerClassName = baseControllerClassName;
	}

	public String getResultClassName() {
		return resultClassName;
	}

	public void setResultClassName(String resultClassName) {
		this.resultClassName = resultClassName;
	}
	
	
	
	private String getFristValue(String... vals)
	{
		for (String v : vals) {
			if(!StringUtil.isBlank(v)) return v;
		}
		return null;
	}
	
}
