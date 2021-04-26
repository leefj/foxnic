package com.github.foxnic.generatorV2.config;

import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.generator.CodeGenerator.Mode;
import com.github.foxnic.generatorV2.builder.business.PageControllerFile;
import com.github.foxnic.generatorV2.builder.business.ServiceInterfaceFile;
import com.github.foxnic.generatorV2.builder.model.PoClassFile;
import com.github.foxnic.generatorV2.builder.model.PojoClassFile;
import com.github.foxnic.generatorV2.builder.model.PojoMetaClassFile;
import com.github.foxnic.generatorV2.builder.model.PojoProperty;
import com.github.foxnic.generatorV2.builder.model.VoClassFile;
import com.github.foxnic.sql.meta.DBTable;

public class MduCtx {
	
	private DAO dao;
	private DBTableMeta tableMeta;
	private DBTable table;
	private String tablePrefix;
	private GlobalSettings settings;
	private PoClassFile poClassFile;
	private VoClassFile voClassFile;
	
	private String modulePackage;
	private MavenProject domainProject;
	private MavenProject serviceProject;
	
	private PageControllerFile pageControllerFile;
	
	private ServiceInterfaceFile serviceInterfaceFile;
	

	public MduCtx(GlobalSettings settings,DBTable table,String tablePrefix,String modulePackage) {
		this.table=table;
		this.tablePrefix=tablePrefix;
		this.settings=settings;
		this.modulePackage=modulePackage;
	}
	
	public PoClassFile getPoClassFile() {
		if(poClassFile==null) {
			poClassFile=new PoClassFile(this,domainProject, this.getPoPackage(this.settings.getGeneratorMode()), table,tablePrefix);
			poClassFile.setPropsJoin(dao.getRelationManager().findProperties(poClassFile.getType()));
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
		return voClassFile;
	}
	 

	public GlobalSettings getSettings() {
		return settings;
	}

	public MavenProject getDomainProject() {
		return domainProject;
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
		PojoClassFile pojo=new PojoClassFile(this, this.getDomainProject(), this.getPoPackage(this.getSettings().getGeneratorMode()), className);
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
		PojoClassFile pojo=new PojoClassFile(this, this.getDomainProject(), this.getPoPackage(this.getSettings().getGeneratorMode()), className);
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

	public void setDAO(DAO dao) {
		this.dao = dao;
		this.tableMeta=dao.getTableMeta(this.table.name());
	}

	public PageControllerFile getPageControllerFile() {
		if(pageControllerFile==null) {
			pageControllerFile=new PageControllerFile(this,this.serviceProject, modulePackage+".page", this.getPoClassFile().getSimpleName()+"PageController");
		}
		return pageControllerFile;
	}
	
	public ServiceInterfaceFile getServiceInterfaceFile() {
		if(serviceInterfaceFile==null) {
			serviceInterfaceFile=new ServiceInterfaceFile(this,this.serviceProject, modulePackage+".service", "I"+this.getPoClassFile().getSimpleName()+"Service");
		}
		return serviceInterfaceFile;
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
		
		//页面控制器
//		this.getPageControllerFile().save();
		
		
		
		
	}

	public DBTableMeta getTableMeta() {
		return tableMeta;
	}

	public DAO getDAO() {
		return this.dao;
	}

	

 
	
	
}
