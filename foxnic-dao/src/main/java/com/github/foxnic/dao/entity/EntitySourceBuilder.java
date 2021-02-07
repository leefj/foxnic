package com.github.foxnic.dao.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.github.foxnic.commons.cache.LocalCache;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.code.JavaCompileUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;

public class EntitySourceBuilder<T extends Entity> {
	
	private static LocalCache<Class, Class> TYPE_CACHE=new LocalCache<>();
	
	public static <E extends Entity> E create(Class<E>  entityType) {
		Class<E>  proxyType=TYPE_CACHE.get(entityType);
		if(proxyType==null) {
			EntitySourceBuilder<E> esb=new EntitySourceBuilder<>(entityType);
			JavaCompileUtil.compile(esb.makeClass(), esb.getCompiledClassName());
			proxyType=ReflectUtil.forName(esb.getCompiledClassName());
			TYPE_CACHE.put(entityType,proxyType);
		}
		try {
			return (E)proxyType.newInstance();
		} catch (Exception err) {
			throw new RuntimeException(err);
		}
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
		
		Field[] fields=ReflectUtil.getFields(entityType);
		for (Field f : fields) {
			String setter=f.getName();
			if(setter.length()==1) {
				setter="set"+setter.toUpperCase();
			} else {
				setter="set"+setter.substring(0,1).toUpperCase()+setter.substring(1);
			}
			Method m=ReflectUtil.getMethod(entityType,setter,f.getType());
			if(m!=null && Modifier.isPublic(m.getModifiers())) {
				buildSetter(code,setter,f,m);
			}
		}
		
		
		
		code.ln("}");
		return code.toString();
	}

	private void buildSetter(CodeBuilder code, String setter, Field f, Method m) {
		code.ln(1,"public void "+setter+"("+f.getType().getName()+" "+f.getName()+" ) {");
		code.ln(2,"super."+setter+"("+f.getName()+");");
		//code.ln(2,"System.out.println(\""+f.getName()+"=\"+"+f.getName()+");");
		code.ln(1,"}");
	}

	private String getCompiledClassName() {
		return compiledClassName;
	}
	

 
	 
	
}
