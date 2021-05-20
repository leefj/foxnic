package com.github.foxnic.generatorV2.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.generatorV2.builder.business.ApiControllerFile;
import com.github.foxnic.generatorV2.builder.business.CodePoint;
import com.github.foxnic.generatorV2.builder.business.ControllerProxyFile;
import com.github.foxnic.generatorV2.builder.business.PageControllerFile;
import com.github.foxnic.generatorV2.builder.business.ServiceImplmentFile;
import com.github.foxnic.generatorV2.builder.business.ServiceInterfaceFile;
import com.github.foxnic.generatorV2.builder.model.PoClassFile;
import com.github.foxnic.generatorV2.builder.model.PojoClassFile;
import com.github.foxnic.generatorV2.builder.model.PojoMetaClassFile;
import com.github.foxnic.generatorV2.builder.model.PojoProperty;
import com.github.foxnic.generatorV2.builder.model.VoClassFile;
import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.meta.DBTable;
import com.github.foxnic.sql.treaty.DBTreaty;

public class MduCtx {
	
	private static final BeanNameUtil beanNameUtil=new BeanNameUtil();
	
	private DAO dao;
	private DBTableMeta tableMeta;
	private DBTable table;
	private String tablePrefix;
	private GlobalSettings settings;
	private PoClassFile poClassFile;
	private VoClassFile voClassFile;
	
	private String modulePackage;
	private MavenProject domainProject;
	private MavenProject proxyProject;
	private MavenProject serviceProject;
	private MavenProject viewProject;
	
	private PageControllerFile pageControllerFile;
	
	private ServiceInterfaceFile serviceInterfaceFile;
	
	private ServiceImplmentFile serviceImplmentFile;
	
	private ControllerProxyFile controllerAgentFile;
	
	private ApiControllerFile apiControllerFile;
	
	/**
	 * UI页面地址前缀
	 * */
	private String baseUriPrefix4Ui;
	
	
	private int apiSort=0;
	
	
	private CodePoint codePoint;
 
	 
	
	public MduCtx(GlobalSettings settings,DBTable table,String tablePrefix,String modulePackage) {
		this.table=table;
		this.tablePrefix=tablePrefix;
		this.settings=settings;
		this.modulePackage=modulePackage;
	}
	
	public PoClassFile getPoClassFile() {
		if(poClassFile==null) {
			poClassFile=new PoClassFile(this,domainProject, this.getPoPackage(), table,tablePrefix);
			if(dao.getRelationManager()!=null) {
				poClassFile.setPropsJoin(dao.getRelationManager().findProperties(poClassFile.getType()));
			}
		}
		return poClassFile;
	}
	 
	public VoClassFile getVoClassFile() {
		if(voClassFile!=null) return voClassFile;
		voClassFile=new VoClassFile(this.getPoClassFile());
		voClassFile.addProperty(PojoProperty.simple(Integer.class, "pageIndex", "页码", ""));
		voClassFile.addProperty(PojoProperty.simple(Integer.class, "pageSize", "分页大小", ""));
		voClassFile.addProperty(PojoProperty.simple(String.class, "searchField", "搜索字段", ""));
		voClassFile.addProperty(PojoProperty.simple(String.class, "searchValue", "搜索的值", ""));
		if(tableMeta.getPKColumnCount()==1) {
			DBColumnMeta pk=tableMeta.getPKColumns().get(0);
			PojoProperty prop=PojoProperty.list(pk.getDBDataType().getType(), pk.getColumnVarName()+"s", "主键清单", "用于接收批量主键参数");
			voClassFile.addProperty(prop);
			voClassFile.setIdsPropertyName(prop);
		}
		return voClassFile;
	}
	 

	public GlobalSettings getSettings() {
		return settings;
	}

	public MavenProject getDomainProject() {
		return domainProject;
	}

	 

	public String getPoPackage() {
 
		String pkg=null;
		String[] arr=this.getModulePackage().split("\\.");
		String last=arr[arr.length-1];
		arr=ArrayUtil.append(arr, last);
		arr[arr.length-2]="domain";
		pkg = StringUtil.join(arr,".");
		return pkg;
		
	}
	
	public String getProxyPackage() {
		String pkg="";
		String[] arr=this.modulePackage.split("\\.");
		String last=arr[arr.length-1];
		arr=ArrayUtil.append(arr, last);
		arr[arr.length-2]="proxy";
		pkg=StringUtil.join(arr,".");
		return pkg;
		
	}

	public String getModulePackage() {
		return modulePackage;
	}

	 

	private PojoMetaClassFile poMetaClassFile;
	
	public PojoMetaClassFile getPoMetaClassFile() {
		if(poMetaClassFile!=null) return poMetaClassFile;
		poMetaClassFile = new PojoMetaClassFile(poClassFile);
		return poMetaClassFile;
	}
	
	private PojoMetaClassFile voMetaClassFile;
	public PojoMetaClassFile getVoMetaClassFile() {
		if(voMetaClassFile!=null) return voMetaClassFile;
		voMetaClassFile = new PojoMetaClassFile(voClassFile);
		voMetaClassFile.setSuperTypeFile(poMetaClassFile);
		return voMetaClassFile;
	}

  
	private List<PojoClassFile> pojos=new ArrayList<>();
	
	private String daoNameConst;
	
	private String microServiceNameConst;
	
	
	
	/**
	 * 创建 Pojo 默认继承制 Po 类
	 * */
	public PojoClassFile createPojo(String className) {
		return createPojo(className, this.getPoClassFile());
	}
	
	/**
	 * 创建 Pojo , 并指定父类型
	 * */
	public PojoClassFile createPojo(String className,Class superType) {
		PojoClassFile pojo=new PojoClassFile(this, this.getDomainProject(), this.getPoPackage(), className);
		if(superType!=null) {
			pojo.setSuperType(superType);
		} else {
			pojo.setSuperType(null);
			pojo.setSuperTypeFile(null);
		}
		pojos.add(pojo);
		return pojo;
	}
	
	/**
	 * 创建 Pojo , 并指定父类型
	 * */
	public PojoClassFile createPojo(String className,JavaClassFile superFile) {
		PojoClassFile pojo=new PojoClassFile(this, this.getDomainProject(), this.getPoPackage(), className);
		if(superFile!=null) {
			pojo.setSuperTypeFile(superFile);
		} else {
			pojo.setSuperType(null);
			pojo.setSuperTypeFile(null);
		}
		pojos.add(pojo);
		return pojo;
	}

	public List<PojoClassFile> getPojos() {
		return pojos;
	}

	public void setDomainProject(MavenProject domainProject) {
		this.domainProject = domainProject;
	}

	public MavenProject getServiceProject() {
		return serviceProject;
	}

	public void setServiceProject(MavenProject serviceProject) {
		this.serviceProject = serviceProject;
	}

	private Rcd example=null;
	
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
	
	public void setDAO(DAO dao) {
		this.dao = dao;
		this.tableMeta=dao.getTableMeta(this.table.name());
		validateTableMeta();
		this.codePoint = new CodePoint(this.table.name(),dao);
		this.example = this.getDAO().queryRecord("select * from "+this.table.name());
 
	}

	private void validateTableMeta() {
	
		
		
	}

	public PageControllerFile getPageControllerFile() {
		if(pageControllerFile==null) {
			MavenProject project=this.getViewProject();
			if(project==null) {
				project=this.getServiceProject();
			}
			pageControllerFile=new PageControllerFile(this,project, modulePackage+".page", this.getPoClassFile().getSimpleName()+"PageController");
		}
		return pageControllerFile;
	}
	
	public ServiceInterfaceFile getServiceInterfaceFile() {
		if(serviceInterfaceFile==null) {
			serviceInterfaceFile=new ServiceInterfaceFile(this,this.serviceProject, modulePackage+".service", "I"+this.getPoClassFile().getSimpleName()+"Service");
		}
		return serviceInterfaceFile;
	}
	
	public ServiceImplmentFile getServiceImplmentFile() {
		if(serviceImplmentFile==null) {
			serviceImplmentFile=new ServiceImplmentFile(this,this.serviceProject, modulePackage+".service.impl", this.getPoClassFile().getSimpleName()+"ServiceImpl");
		}
		return serviceImplmentFile;
	}
	

	public void buildAll() {
 
		//生成模型
		this.getPoClassFile().save(true);
		this.getPoMetaClassFile().save(true);
		this.getVoClassFile().save(true);
		this.getVoMetaClassFile().save(true);
		
		for (PojoClassFile pojo : this.getPojos()) {
			pojo.save(true);
			PojoMetaClassFile meta=new PojoMetaClassFile(pojo);
			meta.save(true);
		}
		
		//服务接口
		this.getServiceInterfaceFile().save();
		
		//服务实现
		this.getServiceImplmentFile().save();
		
		//控制器服务代理
	
		this.getControllerAgentFile().save();
 
		//接口控制器
		this.getApiControllerFile().save();
		
		
		//页面控制器
		this.getPageControllerFile().save();

		
	}

	public DBTableMeta getTableMeta() {
		return tableMeta;
	}

	public DAO getDAO() {
		return this.dao;
	}

	public void setDAONameConsts(String daoNameConst) {
		this.daoNameConst=daoNameConst;
	}

	public String getDAONameConst() {
		return daoNameConst;
	}

	public String getBaseUriPrefix4Ui() {
		return baseUriPrefix4Ui;
	}

	/**
	 * 设置模块上一级的页面地址前缀<br>
	 * 例如 角色管理的页面位于 /pages/system/role 下，则设置为 /pages/system ，后面部分的路径自动生成
	 * */
	public void setBaseUriPrefix4Ui(String uriPrefix4Ui) {
		uriPrefix4Ui=StringUtil.removeFirst(uriPrefix4Ui, "/");
		this.baseUriPrefix4Ui = uriPrefix4Ui;
	}
 
	/**
	 * 模块基础目录路径
	 * */
	public String getUriPrefix4Ui() {
		String prefix=this.getBaseUriPrefix4Ui();
		prefix=StringUtil.joinUrl(prefix,beanNameUtil.depart(this.getPoClassFile().getSimpleName()));
		return prefix;
	}


	public String getTopic() {
		String topic=tableMeta.getTopic();
		topic=topic.trim();
		if(topic.endsWith("数据表")) {
			topic=topic.substring(0, topic.length()-3);
		}
		if(topic.endsWith("表")) {
			topic=topic.substring(0, topic.length()-1);
		}
		return topic;
	}

	public ControllerProxyFile getControllerAgentFile() {
		if(controllerAgentFile==null) {
			controllerAgentFile=new ControllerProxyFile(this, this.getProxyProject(), getProxyPackage(), this.getPoClassFile().getSimpleName()+"ServiceProxy");
		}
		return controllerAgentFile;
	}

	public MavenProject getProxyProject() {
		return proxyProject;
	}

	public void setProxyProject(MavenProject proxyProject) {
		this.proxyProject = proxyProject;
	}

	public String getMicroServiceNameConst() {
		return microServiceNameConst;
	}

	public void setMicroServiceNameConst(String microServiceNameConst) {
		this.microServiceNameConst = microServiceNameConst;
	}

	/**
	 * 从常量中提取值
	 * */
	public String getMicroServiceNameConstValue() {
		String msNameConst=this.getMicroServiceNameConst();
		if(msNameConst.startsWith("\"") && msNameConst.endsWith("\"")) {
			return StringUtil.trim(msNameConst, "\"");
		}
		 
		String[] tmp=msNameConst.split("\\.");
		String c=tmp[tmp.length-2]+"."+tmp[tmp.length-1];
		String clsName=msNameConst.substring(0,msNameConst.lastIndexOf('.'));
		
		Class cls=ReflectUtil.forName(clsName);
		try {
			Field f=cls.getDeclaredField(tmp[tmp.length-1]);
			return f.get(null).toString();
		} catch (Exception e) {
			 return null;
		} 
	}

	public ApiControllerFile getApiControllerFile() {
		if(apiControllerFile==null) {
			apiControllerFile=new ApiControllerFile(this, this.getServiceProject(), this.modulePackage+".controller", this.getPoClassFile().getSimpleName()+"Controller");
		}
		return apiControllerFile;
	}

	public int getApiSort() {
		return apiSort;
	}

	public void setApiSort(int apiSort) {
		this.apiSort = apiSort;
	}

	public CodePoint getCodePoint() {
		return codePoint;
	}
	
	public boolean isDBTreatyFiled(DBColumnMeta cm) {
		return this.isDBTreatyFiled(cm.getColumn());
	}
	
	private Set<String> dbTreatyFileds= new HashSet<>();
	
	public boolean isDBTreatyFiled(String f) {
		if(dbTreatyFileds.size()==0) {
			DBTreaty dbTreaty=dao.getDBTreaty();
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
 
	
	private Overrides overrides=new Overrides();

 
	public Overrides overrides() {
		return overrides;
	}

	public void setViewProject(MavenProject viewProject) {
		this.viewProject=viewProject;
	}

	public MavenProject getViewProject() {
		return viewProject;
	}
}
