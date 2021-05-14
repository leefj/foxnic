package com.github.foxnic.commons.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.bean.BeanUtil;

import sun.reflect.Reflection;

/**
 * @author fangjieli
 * */
public class ReflectUtil {
	
	private static Map<String, Class> CLASS_CACHE=new ConcurrentHashMap<>();
	private static Map<String, Method> METHOD_CACHE=new ConcurrentHashMap<>();
	private static Map<String, Field> FIELD_CACHE=new ConcurrentHashMap<>();

	private static BeanNameUtil beanNameUtil=new BeanNameUtil();
	
	/**
	 * forName,使用缓存
	 * @param className 类名
	 * @return 返回Class
	 * */
	public static Class forName(String className) {
		return forName(className, true);
	}
	
	/**
	 * 获得指定类型的类加载器，如果类型不存在则返回 SystemClassLoader 
	 * @param className 类名
	 * @return  返回 ClassLoader
	 * */
	public static ClassLoader getClassLoader(String className) {
		Class cls=forName(className);
		if(cls!=null) return cls.getClassLoader();
		return ClassLoader.getSystemClassLoader();
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
		} catch (ClassNotFoundException e1) {
			try {
				cls = Class.forName(className,false, Thread.currentThread().getContextClassLoader());
			} catch (ClassNotFoundException e2) {
				return null;
			}
			
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

	/**
	 * 获得当前类以及所有父级类的属性
	 *
	 * @param type 类型
	 * @return Field[], 字段清单
	 */
	public static Field[] getFields(Class type) {
		HashMap<String,Field> fields=new HashMap<>();
		while(true) {
			Field[] fs=type.getDeclaredFields();
			for (Field f : fs) {
				if(fields.containsKey(f.getName())) continue;
				fields.put(f.getName(), f);
			}
			type=type.getSuperclass();
			if(type==null) break;
		}
		return fields.values().toArray(new Field[0]);
	}

	/**
	 * 获得当前类以及所有父级类的属性
	 *
	 * @param type 类型
	 * @return Field[], 字段清单
	 */
	public static Field getField(Class type,String fieldName) {
		String key=type.getName()+"_"+fieldName;
		Field f=FIELD_CACHE.get(key);
		if(f!=null) return f;
		String propertyName=null;

			while(true) {
				try {
					f=type.getDeclaredField(fieldName);
				} catch (NoSuchFieldException e) {}
				if(f==null) {
					propertyName=beanNameUtil.getPropertyName(fieldName);
					try {
						f=type.getDeclaredField(propertyName);
					} catch (NoSuchFieldException e) {}
				}
				if(f!=null){
					FIELD_CACHE.put(key,f);
					break;
				}
				type=type.getSuperclass();
				if(type==null) break;
			}




		return f;
	}


	public static Method getMethod(Class type, String methodName, Class... paramType) {
		Method m=null;
		while(true) {
			try {
				m=type.getDeclaredMethod(methodName, paramType);
				if(m!=null) {
					return m;
				}
			} catch (Exception e) {
				if(e instanceof NoSuchMethodException) {
					
				} else {
					throw new RuntimeException(e);
				}
			}
			type=type.getSuperclass();
			if(type==null) break;
		}
		return m;
	}

	
	public static Class getListComponentType(Field f) {
		if(!isSubType(List.class, f.getType())) {
			throw new IllegalArgumentException(f.getType().getName()+" 不是一个 List 类型");
		}
		String sign=BeanUtil.getFieldValue(f, "signature",String.class);
		int i=sign.indexOf('<');
		int j=sign.indexOf('>',i);
		sign=sign.substring(i+2, j-1);
		sign=sign.replace('/', '.');
		return ReflectUtil.forName(sign);
	}
	
	
	
 
}
