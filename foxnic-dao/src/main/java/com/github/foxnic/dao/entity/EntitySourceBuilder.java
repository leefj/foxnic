package com.github.foxnic.dao.entity;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.code.JavaCompileUtil;

public class EntitySourceBuilder<T extends Entity> {
	
	public  <E extends Entity> E create(Class<T>  entityType) {
		return null;
	}
	
	private Class entityType;
	
	public EntitySourceBuilder(Class<T>  entityType) {
		this.entityType=entityType;
	}
	
	public String compiledClassName;
	
	/**
	 * 构建代理类代码
	 * */
	private String makeClass () {
		Package pkg=entityType.getPackage();
		String cpkgName=pkg.getName()+".$$proxy$$";
		compiledClassName=cpkgName+"."+entityType.getSimpleName();
		CodeBuilder code=new CodeBuilder();
		code.ln("package "+cpkgName+";");
		code.ln("public class "+entityType.getSimpleName()+" extends "+entityType.getName()+"{");
		code.ln("}");
		return code.toString();
	}

	private String getCompiledClassName() {
		return compiledClassName;
	}
	
//	LocalCache<K, V>
 
	public T create() {
		JavaCompileUtil.compile(this.makeClass(), this.getCompiledClassName());
		T e=null;
		try {
			e = (T)Class.forName(this.getCompiledClassName()).newInstance();
		} catch (Exception err) {
			throw new RuntimeException(err);
		}  
		return e;
	}
	
}
