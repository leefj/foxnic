package com.github.foxnic.generator.config;

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
import com.github.foxnic.generator.builder.business.*;
import com.github.foxnic.generator.builder.business.config.ControllerConfig;
import com.github.foxnic.generator.builder.business.config.ServiceConfig;
import com.github.foxnic.generator.builder.business.option.ControllerOptions;
import com.github.foxnic.generator.builder.business.option.ServiceOptions;
import com.github.foxnic.generator.builder.model.*;
import com.github.foxnic.generator.builder.view.*;
import com.github.foxnic.generator.builder.view.config.*;
import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.option.BpmOptions;
import com.github.foxnic.generator.builder.view.option.ViewOptions;
import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;
import com.github.foxnic.sql.treaty.DBTreaty;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ModuleContext {

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
	private MavenProject wrapperProject;

	private PageControllerFile pageControllerFile;

	private ServiceInterfaceFile serviceInterfaceFile;

	private ServiceImplmentFile serviceImplmentFile;

	private BpmEventAdaptorFile bpmEventAdaptorFile;

	private ControllerProxyFile controllerAgentFile;

	private ApiControllerFile apiControllerFile;

	private ListPageHTMLFile listPageHTMLFile;
	private ListPageJSFile listPageJSFile;

	private ExtendJSFile extJSFile;

	private FormPageHTMLFile formPageHTMLFile;
	private FormPageJSFile formPageJSFile;


	private FormWindowConfig formWindowConfig;

	private SearchAreaConfig searchAreaConfig;

	private FormConfig formConfig;

	private ListConfig listConfig;


	private TreeConfig treeConfig;

	private List<FieldInfo> fields;

	/**
	 * UI页面地址前缀
	 * */
	private String viewPrefixURI;

	/**
	 * UI页面地址前缀
	 * */
	private String viewPrefixPath;


	private int apiSort=0;


	private CodePoint codePoint;




	public ModuleContext(GlobalSettings settings,DBTable table,String tablePrefix,String modulePackage) {
		this.table=table;
		this.tablePrefix=tablePrefix;
		this.settings=settings;
		this.modulePackage=modulePackage;
		//
		this.formWindowConfig=new FormWindowConfig();
		this.searchAreaConfig = new SearchAreaConfig();
		this.formConfig = new FormConfig();
		this.listConfig=new ListConfig();
	}

	public PoClassFile getPoClassFile() {
		if(poClassFile==null) {
			poClassFile=new PoClassFile(this,domainProject, this.getPoPackage(), table,tablePrefix);
			poClassFile.setLogicTrue(this.getDAO().getDBTreaty().getTrueValue());
			poClassFile.setLogicFalse(this.getDAO().getDBTreaty().getFalseValue());

//			join 部分无需考虑
//			if(dao.getRelationManager()!=null) {
//				poClassFile.setPropsJoin(dao.getRelationManager().findProperties(poClassFile.getType()));
//			}
		}

		// 如果支持流程，自动加入流程相关的属性
		if(this.getBpmConfig()!=null && !"none".equals(this.getBpmConfig().getIntegrateMode())) {
			Class processInstanceClass=ReflectUtil.forName("org.github.foxnic.web.domain.bpm.ProcessInstance");
			if(processInstanceClass!=null) {
				poClassFile.addListProperty(processInstanceClass,"historicProcessList","历史流程清单","历史流程清单");
				poClassFile.addListProperty(processInstanceClass,"currentProcessList","在批的流程清单","在批的流程清单");
				poClassFile.addSimpleProperty(processInstanceClass,"defaultProcess","默认流程","优先取在批的流程");
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
		voClassFile.addProperty(PojoProperty.simple(String.class, "fuzzyField", "模糊搜索字段", ""));
		voClassFile.addProperty(PojoProperty.simple(String.class, "searchValue", "搜索的值", ""));
		voClassFile.addProperty(PojoProperty.list(String.class, "dirtyFields", "已修改字段", ""));
		voClassFile.addProperty(PojoProperty.simple(String.class, "sortField", "排序字段", ""));
		voClassFile.addProperty(PojoProperty.simple(String.class, "sortType", "排序方式", ""));

		if(tableMeta.getPKColumnCount()==1) {
			DBColumnMeta pk=tableMeta.getPKColumns().get(0);
			PojoProperty prop=PojoProperty.list(pk.getDBDataType().getType(), pk.getColumnVarName()+"s", "主键清单", "用于接收批量主键参数");
			voClassFile.addProperty(prop);
			voClassFile.setIdsPropertyName(prop);
		}

		voClassFile.setLogicTrue(this.getDAO().getDBTreaty().getTrueValue());
		voClassFile.setLogicFalse(this.getDAO().getDBTreaty().getFalseValue());

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


		if(example!=null) {
			example=example.replace('\n',' ');
			example=example.replace('\r',' ');
			example=example.replace("\"","\\\"");
		}


		return example;
	}




	public void setDAO(DAO dao) {
		this.dao = dao;
		this.tableMeta=dao.getTableMeta(this.table.name());
		this.serviceConfig.setTableMeta(tableMeta);
		validateTableMeta();
		this.codePoint = new CodePoint(this.table.name(),dao);
		this.example = this.getDAO().queryRecord("select * from "+this.table.name());

		this.fields=new ArrayList<FieldInfo>();
		for (DBColumnMeta cm : this.tableMeta.getColumns()) {
			FieldInfo f=new FieldInfo(this,cm,this.isDBTreatyFiled(cm,true));
			this.fields.add(f);
		}
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

	public ListPageHTMLFile getListPageHTMLFile() {
		if(listPageHTMLFile==null) {
			listPageHTMLFile=new ListPageHTMLFile(this);
		}
		return listPageHTMLFile;
	}

	public ExtendJSFile getExtJSFile() {
		if(extJSFile==null) {
			extJSFile=new ExtendJSFile(this);
		}
		return extJSFile;
	}

	public ListPageJSFile getListPageJSFile() {
		if(listPageJSFile==null) {
			listPageJSFile=new ListPageJSFile(this);
		}
		return listPageJSFile;
	}


	public FormPageHTMLFile getFormPageHTMLFile() {
		if(formPageHTMLFile==null) {
			formPageHTMLFile=new FormPageHTMLFile(this);
		}
		return formPageHTMLFile;
	}

	public FormPageJSFile getFormPageJSFile() {
		if(formPageJSFile==null) {
			formPageJSFile=new FormPageJSFile(this);
		}
		return formPageJSFile;
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

	public BpmEventAdaptorFile getBpmEventAdaptorFile() {
		if(bpmEventAdaptorFile==null) {
			bpmEventAdaptorFile=new BpmEventAdaptorFile(this,this.serviceProject, modulePackage+".service.bpm", this.getPoClassFile().getSimpleName()+"BpmEventAdaptor");
		}
		return bpmEventAdaptorFile;
	}


	public void buildAll() {
		//生成模型
		PoClassFile poClassFile=this.getPoClassFile();
		PojoMetaClassFile pojoMetaClassFile= this.getPoMetaClassFile();
		poClassFile.setMetaClassFile(pojoMetaClassFile);

		poClassFile.save(true);
		pojoMetaClassFile.save(true);


		VoClassFile voClassFile = this.getVoClassFile();
		PojoMetaClassFile voMetaClassFile = this.getVoMetaClassFile();

		voClassFile.setMetaClassFile(voMetaClassFile);
		voClassFile.save(true);
		voMetaClassFile.save(true);


		for (PojoClassFile pojo : this.getPojos()) {
			PojoMetaClassFile meta=new PojoMetaClassFile(pojo);
			pojo.setMetaClassFile(meta);
			pojo.save(true);
			meta.save(true);
		}

		//服务接口
		this.getServiceInterfaceFile().save();

		//服务实现
		this.getServiceImplmentFile().save();

		if(this.getBpmConfig()!=null && !"none".equals(this.getBpmConfig().getIntegrateMode())) {
			//流程回调实现
			this.getBpmEventAdaptorFile().save();
		}

		//控制器服务代理

		this.getControllerProxyFile().save();


		//接口控制器
		this.getApiControllerFile().save();


		//页面控制器
		this.getPageControllerFile().save();

		//自定义扩展页面
		this.getExtJSFile().save();

		//列表页面
		this.getListPageHTMLFile().save();
		this.getListPageJSFile().save();

		//表单页面
		this.getFormPageHTMLFile().save();
		this.getFormPageJSFile().save();


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


	public String getViewPrefixURI() {
		return viewPrefixURI;
	}

	/**
	 * 设置模块上一级的页面地址前缀<br>
	 * 例如 角色管理的页面位于 /public/pages/system/role 下，则设置为 /pages/system ，后面部分的路径自动生成
	 * */
	public void setViewPrefixURI(String viewPrefixURI) {
		viewPrefixURI=StringUtil.removeFirst(viewPrefixURI, "/");
		this.viewPrefixURI = viewPrefixURI;
	}

	public void setViewPrefixPath(String viewPrefixPath) {
		viewPrefixPath=StringUtil.removeFirst(viewPrefixPath, "/");
		this.viewPrefixPath = viewPrefixPath;
	}

	/**
	 * 模块基础目录路径
	 * */
	public String getUriPrefix4Ui() {
		String prefix=this.getViewPrefixURI();
		prefix=StringUtil.joinUrl(prefix,beanNameUtil.depart(this.getPoClassFile().getSimpleName()));
		return prefix;
	}


	public String getTopic() {
		String topic=tableMeta.getShortTopic();
	 	return topic;
	}

	public ControllerProxyFile getControllerProxyFile() {
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
			 throw new IllegalArgumentException(msNameConst+" 未配置");
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

	public boolean isDBTreatyFiled(DBColumnMeta cm,boolean includeTenantId) {
		return this.isDBTreatyFiled(cm.getColumn(),includeTenantId);
	}



	public boolean isDBTreatyFiled(String f,boolean includeTenantId) {
		DBTreaty dbTreaty=dao.getDBTreaty();
		return dbTreaty.isDBTreatyFiled(f,includeTenantId);
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

	public void setWrapperProject(MavenProject wrapperProject) {
		this.wrapperProject=wrapperProject;
	}

	public MavenProject getWrapperProject() {
		return wrapperProject;
	}

	public String getViewPrefixPath() {
		return viewPrefixPath;
	}

	/**
	 * 树形结构配置项
	 * */
	public TreeConfig tree() {
		return treeConfig;
	}

	/**
	 * 字段配置
	 * */
	private FieldInfo fieldInterenal(String field) {
		for (FieldInfo f : fields) {
			if(f.getColumn().equals(field) || f.getVarName().equals(field)) {
				return f;
			}
		}
		FieldInfo fieldInfo=new FieldInfo(this,field);
		fields.add(fieldInfo);
		return  fieldInfo;
	}
	/**
	 * 指定字段，开始配置字段在界面上的呈现
	 * */
	private FieldInfo fieldInterenal(DBField field) {
		for (FieldInfo f : fields) {
			if(f.getColumn().equalsIgnoreCase(field.name())) {
				return f;
			}
		}
		return null;
	}

	private List<FillByUnit> fillByUnits=null;

	public List<FillByUnit> getFillByUnits() {
		return fillByUnits;
	}

	public void setFillByUnits(List<FillByUnit> fillByUnits) {
		this.fillByUnits = fillByUnits;
	}

	/**
	 * 指定字段，开始配置字段在界面上的呈现
	 * */
	private FieldOptions field(DBField field) {
		 FieldInfo info=this.fieldInterenal(field);
		 if(info==null) return null;
		 return new FieldOptions(this,info);
	}

	/**
	 * 指定字段，开始配置字段在界面上的呈现
	 * */
	private FieldOptions field(String field) {
		FieldInfo info=this.fieldInterenal(field);
		if(info==null) return null;
		return new FieldOptions(this,info);
	}

	public ViewOptions view() {
		return new ViewOptions(this);
	};



	/**
	 * 树形结构配置项
	 * */
	public TreeConfig tree(boolean createNew) {
		if(createNew && treeConfig==null) {
			treeConfig=new TreeConfig();
		}
		return treeConfig;
	}

	public List<FieldInfo> getFields() {
		return fields;
	}

	public List<FieldInfo> getTemplateFields() {
		List<FieldInfo> list=new ArrayList<FieldInfo>();
		for (FieldInfo f : this.fields) {
			list.add(f);
		}
		return list;
	}

	public FormWindowConfig getFormWindowConfig() {
		return formWindowConfig;
	}

	public SearchAreaConfig getSearchAreaConfig() {
		return searchAreaConfig;
	}

	public FormConfig getFormConfig() {
		return formConfig;
	}

	public BpmConfig getBpmConfig() {
		if(this.bpmConfig==null) {
			this.bpmConfig=new BpmConfig(this);
		}
		return bpmConfig;
	}

	public ListConfig getListConfig() {
		return listConfig;
	}

	private Class relationMasterPoType;
	private Class relationSlavePoType;
	private DBField relationMasterIdField;
	private DBField relationSlaveIdField;
	private boolean relationClearWhenEmpty;
	/**
	 * 设置当前表是一个关系表，并指定字段含义 <br/>
	 * 配置后，将在 Service 代码生成关系保存方法的代码
	 * @param masterIdField 指定关系所有者ID字段
	 * @param slaveIdField 指定关系从属对象ID字段
	 * @param clearWhenEmpty  当 slaveIds 元素个数为0时，是否清空关系
	 * */
    public void setRelationField(Class masterPoType,DBField masterIdField, Class slavePoType,DBField slaveIdField,boolean clearWhenEmpty) {
    	if(!masterIdField.table().name().equals(slaveIdField.table().name())) {
    		throw new IllegalArgumentException("主从字段必须在同一个表中");
		}
		this.relationMasterIdField=masterIdField;
		this.relationSlaveIdField=slaveIdField;
		this.relationClearWhenEmpty=clearWhenEmpty;
		this.relationMasterPoType=masterPoType;
		this.relationSlavePoType=slavePoType;
    }

	public DBField getRelationMasterIdField() {
		return relationMasterIdField;
	}

	public DBField getRelationSlaveIdField() {
		return relationSlaveIdField;
	}

	public boolean isRelationClearWhenEmpty() {
		return relationClearWhenEmpty;
	}

	public Class getRelationMasterPoType() {
		return relationMasterPoType;
	}
	public Class getRelationSlavePoType() {
		return relationSlavePoType;
	}

	public ServiceConfig getServiceConfig() {
		return serviceConfig;
	}

	private ServiceConfig serviceConfig=new ServiceConfig();

	private BpmConfig bpmConfig=null;

	public ControllerConfig getControllerConfig() {
		return controllerConfig;
	}

	private ControllerConfig controllerConfig=new ControllerConfig();

	public ServiceOptions service() {
		return  new ServiceOptions(serviceConfig);
	}

	public BpmOptions bpm() {
		if(this.bpmConfig==null) {
			this.bpmConfig=new BpmConfig(this);
		}
		return  new BpmOptions(this,bpmConfig);
	}

	public ControllerOptions controller() {
		return  new ControllerOptions(controllerConfig);
	}

    public FieldInfo getField(Object input) {
		for (FieldInfo field : fields) {
			if(input instanceof String) {
				if(field.getColumn().equals(input)) {
					return field;
				}
			} else if(input instanceof DBField) {
				if(field.getColumn().equals(((DBField)input).name())) {
					return field;
				}
			}
		}
		return null;
    }



}
