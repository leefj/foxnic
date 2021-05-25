package com.github.foxnic.generator;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.relation.PropertyRoute;
import com.github.foxnic.dao.relation.RelationManager;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.generatorV2.builder.business.CodePoint;
import com.github.foxnic.generatorV2.builder.model.PojoClassFile;
import com.github.foxnic.generatorV2.builder.model.PojoMetaClassFile;
import com.github.foxnic.generatorV2.builder.view.field.config.LogicFieldConfig;
import com.github.foxnic.generatorV2.config.MduCtx;
import com.github.foxnic.generatorV2.config.TreeConfig;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;
import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;
import com.github.foxnic.sql.treaty.DBTreaty;

public class Context {
	
	private DefaultNameConvertor convertor = new DefaultNameConvertor();
	
	private CodeGenerator generator;
	
	private DBTreaty dbTreaty;
	private String daoNameConst;
 
	 
	private String projectDirName;
	private String tableName;
	private String tablePrefix;
	private String modulePackageName;
//	private String author;
	private DBTableMeta tableMeta;
	private Rcd example;
	
	//
	private MavenProject domainProject=null;
	private MavenProject serviceProject=null;
	private MavenProject agentProject=null;
	//
	private String poName=null;
	private String poVarName=null;
	private String poPackage=null;
	private String poFullName=null;
	//
	private String poMetaName=null;
	private String poMetaFullName=null;
	private String poMetaPackage=null;

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
	private String pageCtrlName=null;
	private String pageCtrlPackage=null;
	private String pageCtrlFullName=null;
	
	//
	private String agentName=null;
	private String agentPackage=null;
	private String agentFullName=null;
	
	//
	private String sentinelExceptionHandlerClassName=null;
	private String superController=null;

	//
	private ModuleConfig module=null;
	
	private String beanNameMainPart;
	
	private Rcd sample;
	private CodePoint codePoint;
	
	
	private MduCtx mductx;
 
	public Context(MduCtx mductx,CodePoint codePoint,CodeGenerator generator,ModuleConfig module,DBTreaty dbTreaty,String tableName,String tablePrefix,DBTableMeta tableMeta,Rcd example) {
		this.mductx=mductx;
		this.codePoint=codePoint;
		this.generator=generator;
		this.module=module;
		this.dbTreaty=dbTreaty;
		this.daoNameConst=this.getFirstValue(module.getDAONameConst(),generator.getDAONameConst());
		this.tableName=tableName;
		this.beanNameMainPart=convertor.getClassName(tableName, 0);
		this.tablePrefix=tablePrefix;
//		this.author= this.getFirstValue(module.getAuthor(),generator.getAuthor());

		this.tableMeta=tableMeta;
		this.example=example;
		
		//
		String tmp = tableName.substring(tablePrefix.length());
		this.poName = convertor.getClassName(tmp, 0);
		this.poVarName = convertor.getPropertyName(tmp);
//		if(generator.getMode()==Mode.ONE_PROJECT) {
//			this.poPackage=module.getModulePackage() + ".domain";
//		} else if(generator.getMode()==Mode.MULTI_PROJECT) {
			String[] arr=module.getModulePackage().split("\\.");
			String last=arr[arr.length-1];
			arr=ArrayUtil.append(arr, last);
			arr[arr.length-2]="domain";
			this.poPackage=StringUtil.join(arr,".");
//		}
		this.poFullName=this.poPackage+"."+this.poName;
		this.domainProject=this.getFirstValue(module.getDomainProject(),module.getProject(),generator.getDomainProject() ,generator.getProject());
		
		//
		this.poMetaName=this.poName+"Meta";
		this.poMetaPackage=this.poPackage+".meta";
		this.poMetaFullName=poMetaPackage+"."+poMetaName;
		
		//
		DAO dao=generator.getDAO();
//		this.module.getDefaultVO().bind(this.poName+"VO",this.poPackage);
//		for (Pojo pojo : module.getPojos()) {
//			if(generator.getMode()==Mode.ONE_PROJECT) {
//				pojo.bind(null, module.getModulePackage()+".domain");
//			} else if(generator.getMode()==Mode.MULTI_PROJECT) {
//				String[] arr=module.getModulePackage().split("\\.");
//				String last=arr[arr.length-1];
//				arr=ArrayUtil.append(arr, last);
//				arr[arr.length-2]="domain";
//				last=StringUtil.join(arr,".");
//				pojo.bind(null, last);
//			}
//			if(!StringUtil.isBlank(pojo.getTemplateSQL())) {
//				Rcd sample=dao.queryRecord(pojo.getTemplateSQL());
//				if(sample==null) {
//					throw new IllegalArgumentException("缺少数据");
//				}
//				QueryMetaData qmd=sample.getOwnerSet().getMetaData();
//				for (int i = 0; i < qmd.getColumnCount(); i++) {
//					String table=qmd.getTableName(i);
//					String field=qmd.getColumnLabel(i);
//					String label=field;
//					String note=field;
//					DBColumnMeta cm=dao.getTableColumnMeta(table, field);
//					if(cm!=null) {
//						label=cm.getColumn();
//						note=cm.getDetail();
//					}
//					pojo.addProperty(field,  ReflectUtil.forName(qmd.getColumnClassName(i)) , label, note);
//				}
//			}
//		}
 
		//
		this.intfName="I"+this.poName+"Service";
		this.intfVarName=this.poVarName+"Service";
		this.intfPackage=module.getModulePackage() + ".service";
		this.intfFullName=this.intfPackage+"."+this.intfName;
		this.serviceProject=this.getFirstValue(module.getServiceProject(),module.getProject(),generator.getServiceProject(),generator.getProject());
		//
		this.implName=this.poName+"ServiceImpl";
		this.implPackage=module.getModulePackage() + ".service.impl";
		this.implFullName=this.implPackage+"."+this.implName;
		
		//
		this.ctrlName=this.poName+"Controller";
		this.ctrlPackage=module.getModulePackage() + ".controller";
		this.ctrlFullName=this.ctrlPackage+"."+this.ctrlName;
		this.superController=this.getFirstValue(this.generator.getSuperController());
		
		
		this.pageCtrlName=this.poName+"PageController";
		this.pageCtrlPackage=module.getModulePackage() + ".page";
		this.pageCtrlFullName=this.pageCtrlPackage+"."+this.pageCtrlName;
		
		//
		this.agentName=this.poName+"ServiceAgent";
//		if(generator.getMode()==Mode.ONE_PROJECT) {
//			this.agentPackage=module.getModulePackage() + ".agent.service";
//		}  else if(generator.getMode()==Mode.MULTI_PROJECT) {
			arr=module.getModulePackage().split("\\.");
			last=arr[arr.length-1];
			arr=ArrayUtil.append(arr, last);
			arr[arr.length-2]="agent.service";
			this.agentPackage=StringUtil.join(arr,".");
//		}
		this.agentFullName=this.agentPackage+"."+this.agentName;
		this.agentProject=this.getFirstValue(module.getAgentProject(),module.getProject(),generator.getProxyProject(),generator.getProject());
		
		//
		this.sentinelExceptionHandlerClassName=getFirstValue(module.getSentinelExceptionHnadlerClassName(),generator.getSentinelExceptionHandlerClassName());
	}
	
	public String getApiContextPart() {
//		String s= this.tableName.substring(tablePrefix==null?0:(tablePrefix.length()));
		return tableName.replace('_', '-');
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
		return "XXXXXX";
	}

	public String getPoFullName() {
		return poFullName;
	}
	
//	public Pojo getDefaultVO() {
//		return module.getDefaultVO();
//	}
	

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
		if(cm.getColumn().equalsIgnoreCase("password") || cm.getColumn().equalsIgnoreCase("passwd")) return "******";
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

	public String getSentinelExceptionHandlerClassName() {
		return sentinelExceptionHandlerClassName;
	}

	public void setSentinelExceptionHandlerClassName(String sentinelExceptionHnadlerClassName) {
		this.sentinelExceptionHandlerClassName = sentinelExceptionHnadlerClassName;
	}

	public String getSuperController() {
		return superController;
	}
 
	public String getControllerResult() {
		return generator.getControllerResult();
	}

//	public String getControllerApiPrefix() {
//		if(!StringUtil.isBlank(module.getControllerApiPrefix())) {
//			return module.getControllerApiPrefix();
//		}
//		Class cls=ReflectUtil.forName(this.getMicroServiceNamesClassName());
//		try {
//			Field f=cls.getDeclaredField(this.getMicroServicePropertyConst());
//			return f.get(null).toString();
//		} catch (Exception e) {
//			 return null;
//		} 
//	}
	
	
	
	private <T> T getFirstValue(T... vals) {
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

	public MavenProject getAgentProject() {
		return agentProject;
	}
	
	public String getFeignConfigClassName() {
		return generator.getFeignConfigClassName();
	}
	
	
	
//	public String getMicroServiceNamesClassName() {
//		String str=generator.getMicroServiceNameConst();
//		String microServiceNameClass=str.substring(0,str.lastIndexOf('.'));
//		return microServiceNameClass;
//	}
//	
//	public String getMicroServicePropertyConst() {
//		String str=generator.getMicroServiceNameConst();
//		String microServiceNameConst=str.substring(str.lastIndexOf('.')+1);
//		return microServiceNameConst;
//	}
	
//	public List<Pojo.Property> getDefaultVOProperties() {
//		return module.getDefaultVO().getProperties();
//	}
	
	public String getTopic() {
		String t=tableMeta.getTopic();
		t=StringUtil.removeLast(t, "数据表");
		t=StringUtil.removeLast(t, "表");
		return t;
	}
	
	public Integer getApiSort() {
		return this.module.getApiSort();
	}

	public String getPoMetaFullName() {
		return poMetaFullName;
	}

	public String getPoMetaName() {
		return poMetaName;
	}

	public String getPoMetaPackage() {
		return poMetaPackage;
	}

	public String getBeanNameMainPart() {
		return beanNameMainPart;
	}

	public void setBeanNameMainPart(String beanNameMainPart) {
		this.beanNameMainPart = beanNameMainPart;
	}
	
//	public ControllerMethodAnnotiationPlugin getControllerMethodAnnotiationPlugin() {
//		return this.generator.getControllerMethodAnnotiationPlugin();
//	}
	
//	public boolean isForceOverrideController() {
//		return this.module.isForceOverrideController();
//	}

	public CodePoint getCodePoint() {
		return this.codePoint;
	}

	public String getPageCtrlName() {
		return pageCtrlName;
	}

	public String getPageCtrlPackage() {
		return pageCtrlPackage;
	}

	public String getPageCtrlFullName() {
		return pageCtrlFullName;
	}

//	public PageControllerMethodAnnotiationPlugin getPageControllerMethodAnnotiationPlugin() {
//		return this.generator.getPageControllerMethodAnnotiationPlugin();
//	}

	public String getListHTMLTemplate() {
		return this.generator.getListHTMLTemplate();
	}

	public String getListJSTemplate() {
		return this.generator.getListJSTemplate();
	}

	public String getUIPathPrefix() {
		return this.module.getUIPathPrefix();
	}

	public String getFormHTMLTemplate() {
		return this.generator.getFormHTMLTemplate();
	}

	public String getFormJSTemplate() {
		return this.generator.getFormJSTemplate();
	}

	public String getUIModuleFolderName() {
		return (new BeanNameUtil()).depart(this.getPoName()).toLowerCase();
	}

//	public WriteMode getWriteMode(Class builderCls) {
//		return this.module.getOverrides().getWriteMode(builderCls);
//	}

	public TreeConfig getTreeConfig() {
		return this.module.getTreeConfig();
	}

	public boolean isImageIdField(DBColumnMeta cm) {
		DBField[] imgIdFlds=this.module.getImageIdFields();
		if(imgIdFlds==null) return false;
		for (DBField f : imgIdFlds) {
			if( f.name().equalsIgnoreCase(cm.getColumn()) || f.name().equalsIgnoreCase(cm.getColumnVarName()) ) {
				return true;
			}
		}
		return false;
	}

	public List<PropertyRoute> getJoinProperties() {
		Class poCls=ReflectUtil.forName(this.getPoFullName());
		if(poCls==null) return null;
		RelationManager rm=this.generator.getDAO().getRelationManager();
		return rm.findProperties(poCls);
				
	}
	
	
	public boolean isLogicField(DBColumnMeta cm) {
		return getLogicField(cm)!=null;
	}

	/**
	 * 查找逻辑配置
	 * */
	public LogicFieldConfig getLogicField(DBColumnMeta cm) {
		List<LogicFieldConfig> logicFields=this.module.getLogicFields();
		if(logicFields==null) return null;
		for (LogicFieldConfig f : logicFields) {
			if( f.getField().name().equalsIgnoreCase(cm.getColumn()) || f.getField().name().equalsIgnoreCase(cm.getColumnVarName()) ) {
				return f;
			}
		}
		return null;
	}

	public boolean isMulitiLineField(DBColumnMeta cm) {
		DBField[] fields=this.module.getMulitiLineFields();
		if(fields==null) return false;
		for (DBField f : fields) {
			if( f.name().equalsIgnoreCase(cm.getColumn()) || f.name().equalsIgnoreCase(cm.getColumnVarName()) ) {
				return true;
			}
		}
		return false;
	}

	public String getEnumName() {
		// TODO Auto-generated method stub
		return this.getPoName()+"Enum";
	}

	public RcdSet query(String dataTable) {
		return generator.getDAO().query("select * from "+dataTable);
	}

	public String getPoEnumFullName() {
		return poMetaPackage+"."+getEnumName();
	}

	public String getConstsPackage() {
		return generator.getConstsPackage();
	}

	public DBTable getModuleTable() {
		return this.module.getTable();
	}

	public MduCtx getMductx() {
		return mductx;
	}

	public PojoClassFile getDefaultVO() {
		 
		return this.mductx.getVoClassFile();
	}
	
	public PojoMetaClassFile getDefaultVOMeta() {
		return this.mductx.getVoMetaClassFile();
	}
	
	
	
}
