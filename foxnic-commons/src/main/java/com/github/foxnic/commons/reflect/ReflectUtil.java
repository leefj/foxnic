package com.github.foxnic.commons.reflect;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fangjieli
 * */
public class ReflectUtil {
	
	private static Map<String, Class> CLASS_CACHE=new ConcurrentHashMap<>();
	private static Map<String, Method> METHOD_CACHE=new ConcurrentHashMap<>();
 	
	
	/**
	 * forName,使用缓存
	 * @param className 类名
	 * @return 返回Class
	 * */
	public static Class forName(String className) {
		return forName(className, true);
	}
	
	/**
	 * forName
	 * @param className 类名
	 * @param useCache 是否缓存
	 * @return 返回Class
	 * */
	public static Class forName(String className,boolean useCache)
	{
		Class cls=null;
		if(useCache)
		{
			cls=CLASS_CACHE.get(className);
		}
		if(cls!=null) {
			return cls;
		}
		 
		try {
			cls=Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
		
		CLASS_CACHE.put(className, cls);
		 
		return cls;
	}
	
	
	/**
	 * @param  signature  exapmle  <br>public org.tity.ends.transfer.Result org.tity.backends.console.webpage.controller.WebPathController.listAll(org.tity.backends.transfer.Parameter)
	 * @param useCache 是否使用缓存
	 * @return  Method
	 * */
	
	public static Method getMethod(String signature,boolean useCache)
	{
		Method m=null;
		if(useCache)
		{
			m=METHOD_CACHE.get(signature);
		}
		if(m!=null) {
			return m;
		}
 
		int a=signature.indexOf(" ")+1;
		a=signature.indexOf(" ",a)+1;
		String clsName=signature.substring(a,signature.indexOf("("));
		String methodName=clsName.substring(clsName.lastIndexOf(".")+1);
		clsName=clsName.substring(0, clsName.lastIndexOf("."));
		Class cls=forName(clsName, useCache);
		String[] paramsTypeNames=signature.substring(signature.indexOf("(")+1,signature.indexOf(")")).split(",");
		Class[] types=new Class[paramsTypeNames.length];
		for (int i = 0; i < types.length; i++) {
			types[i]=forName(paramsTypeNames[i], useCache);
		}
		
		try {
			m=cls.getDeclaredMethod(methodName, types);
		} catch (Exception e) {
			m=null;
		}
 
		METHOD_CACHE.put(methodName, m);
		
		return m;
	}
	
	/**
	 * 获得枚举值中的所有常量清单
	 * @param className 枚举类名
	 * @param useCache 是否缓存
	 * @return 返回Class
	 * @throws  Exception Exception
	 * */
	public static Enum[] getAllEnumValues(String className,boolean useCache) throws Exception {
		Class<Enum> clz = (Class<Enum>) forName(className,useCache);
        Enum[] objects = clz.getEnumConstants();
        return objects;
	}
	
	/**
	 * 检查两个类型(类、接口)是否是继承或实现关系
	 *
	 * @param superType the super type
	 * @param subType the sub type
	 * @return true, if is sub type
	 */
	public static boolean isSubType(Class<?> superType,Class<?> subType)
	{
		if(superType==null || subType==null) return false;
		return superType.isAssignableFrom(subType);
	}
 
}
