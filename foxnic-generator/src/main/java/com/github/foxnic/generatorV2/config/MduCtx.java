package com.github.foxnic.generatorV2.config;

import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.generator.CodeGenerator.Mode;
import com.github.foxnic.generatorV2.builder.PoClassFile;
import com.github.foxnic.generatorV2.builder.PojoClassFile;
import com.github.foxnic.generatorV2.builder.PojoMetaClassFile;
import com.github.foxnic.generatorV2.builder.PojoProperty;
import com.github.foxnic.generatorV2.builder.VoClassFile;
import com.github.foxnic.sql.meta.DBTable;

public class MduCtx {
	
	private GlobalSettings settings;
	private PoClassFile poClassFile;
	private VoClassFile voClassFile;
	
	private String modulePackage;
	private MavenProject domainProject;

	public MduCtx(GlobalSettings settings,DBTable table,String tablePrefix,MavenProject domainProject,String modulePackage) {
		this.settings=settings;
		this.domainProject=domainProject;
		this.modulePackage=modulePackage;
		poClassFile=new PoClassFile(this,domainProject, this.getPoPackage(this.settings.getGeneratorMode()), table,tablePrefix);
	}
	
	public PoClassFile getPoClassFile(DAO dao) {
		poClassFile.setPropsJoin(dao.getRelationManager().findProperties(poClassFile.getType()));
		return poClassFile;
	}
	 
	public VoClassFile getVoClassFile() {
		if(voClassFile!=null) return voClassFile;
		voClassFile=new VoClassFile(poClassFile);
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

	public PoClassFile getPoClassFile() {
		return this.poClassFile;
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

 
	
	
}
