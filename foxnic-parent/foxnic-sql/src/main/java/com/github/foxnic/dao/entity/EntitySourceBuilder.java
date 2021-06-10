package com.github.foxnic.dao.entity;

import com.github.foxnic.commons.cache.LocalCache;
import com.github.foxnic.commons.compiler.GroovyCompiler;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;

public class EntitySourceBuilder<T extends Entity> {
	
	private static LocalCache<Class, Class> TYPE_CACHE=new LocalCache<>();
	
	
	private static GroovyCompiler compiler;
	/**
	 * 获取对应的类型
	 * */
	public static <E extends Entity>  Class<E> getProxyType(Class<E> type) {
 
		Class<E>  proxyType=TYPE_CACHE.get(type);
//		proxyType=null;
		if(proxyType==null) {
			
			String[] parts=type.getName().split("\\.");
			parts[parts.length-1]="meta."+parts[parts.length-1]+"Meta";
			String name=StringUtil.join(parts,".");
			proxyType=ReflectUtil.forName(name+"$"+EntityContext.PROXY_CLASS_NAME);
			
			//在某些代码未生成的情况下返回本身
			if(proxyType==null) {
				return type;
			}
//			EntitySourceBuilder<E> esb=new EntitySourceBuilder<>(type);
//			if(compiler==null) {
//				compiler = new GroovyCompiler();
//			}
//			String source=esb.makeClass();
//			System.out.println(source);
//			proxyType=(Class<E>)compiler.compile(source);
			TYPE_CACHE.put(type,proxyType);
		}
		return proxyType;
	}
	
	/**
	 * 创建对象
	 * */
	public static <E extends Entity> E create(Class<E>  entityType) {
		Class<E>  proxyType=getProxyType(entityType);
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
	
//	/**
//	 * 构建代理类代码
//	 * */
//	private String makeClass () {
//		Package pkg=entityType.getPackage();
//		String cpkgName=pkg.getName()+"."+EntityContext.PROXY_PACKAGE;
//		compiledClassName=cpkgName+"."+entityType.getSimpleName();
//		CodeBuilder code=new CodeBuilder();
//		code.ln("package "+cpkgName+";");
//		code.ln("public class "+entityType.getSimpleName()+" extends "+entityType.getName()+"{");
//		
//		Field[] fields=ReflectUtil.getFields(entityType);
//		for (Field f : fields) {
//			String setter=f.getName();
//			String getter=f.getName();
//			if(setter.length()==1) {
//				setter="set"+setter.toUpperCase();
//				getter="get"+getter.toUpperCase();
//			} else {
//				setter="set"+setter.substring(0,1).toUpperCase()+setter.substring(1);
//				getter="get"+getter.substring(0,1).toUpperCase()+getter.substring(1);
//			}
//			
//			//获得get方法
//			Method getterMethod=ReflectUtil.getMethod(entityType,getter);
//			if(getterMethod==null) {
//				getter=f.getName();
//				if(setter.length()==1) {
//					getter="is"+getter.toUpperCase();
//				} else {
//					getter="is"+getter.substring(0,1).toUpperCase()+getter.substring(1);
//				}
//				getterMethod=ReflectUtil.getMethod(entityType,getter);		
//			}
//			
//			//获得set方法
//			Method setterMethod=ReflectUtil.getMethod(entityType,setter,f.getType());
//			if(setterMethod!=null && Modifier.isPublic(setterMethod.getModifiers())) {
//				buildSetter(code,f,setter,setterMethod,getter,getterMethod);
//			}
//		}
//		
//		
//		
//		code.ln("}");
//		//System.out.println(code);
//		return code.toString();
//	}
//
//	private void buildSetter(CodeBuilder code,  Field f, String setter,Method setterMethod , String getter,Method getterMethod) {
//		Class returnType=setterMethod.getReturnType();
//		String returnTypeName="void";
//		boolean isReturnVoid=true;
//		if(!returnType.getName().equals(returnTypeName)) {
//			returnTypeName=this.entityType.getSimpleName();
//			isReturnVoid=false;
//		}
//		
////		String ger="";
////		if(ReflectUtil.isSubType(List.class, returnType)) {
////			System.out.println();
////		}
//		
//		code.ln(1,"public "+returnTypeName+" "+setter+"("+f.getType().getName()+" "+f.getName()+" ) {");
//		code.ln(2,"super.change(\""+f.getName()+"\",super."+getter+"(),"+f.getName()+");");
//		code.ln(2,"super."+setter+"("+f.getName()+");");
//		if(!isReturnVoid) {
//			code.ln(2,"return this;");
//		}
//		code.ln(1,"}");
//	}

	private String getCompiledClassName() {
		return compiledClassName;
	}
	

 
	 
	
}
