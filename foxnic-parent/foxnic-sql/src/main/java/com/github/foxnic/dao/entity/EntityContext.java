package com.github.foxnic.dao.entity;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;

import java.util.Map;

public class EntityContext {
	
	static final String PROXY_CLASS_NAME="$$proxy$$";
	
 
	/**
	 * 创建一个空对象
	 * */
	public static <T extends Entity> T create(Class<T> type) { 
		return EntitySourceBuilder.create(type);
	}
	
	/**
	 * 创建一个对象，并填充数据
	 * */
	public static <T extends Entity> T create(Class<T> type,Map<String,Object> data) {
		T t=create(type);
		copyProperties(t, data);
		t.clearModifies();
		return t;
	}
	
	/**
	 * 创建一个对象，并填充数据
	 * */
	public static <T extends Entity> T create(Class<T> type,Object pojo) {
		T t=create(type);
		copyProperties(t, pojo);
		t.clearModifies();
		return t;
	}
	
	public static <T> T copyProperties(T target,Object source) {
		if(source==null) {
			new IllegalArgumentException("pojo is require");
		}
		//针对复杂对象，后续进一步扩展
		BeanUtil.copy(source, target, false);
		return target;
	}
	
	
	public static <T> T copyProperties(T entity,Map<String,Object> data) {
		if(data==null) {
			new IllegalArgumentException("data is require");
		}
		//针对复杂对象，后续进一步扩展
		BeanUtil.copy(data, entity);
		return entity;
	}
	
	public static <T extends Entity>  Class<T> getProxyType(Class<T> type) {
		return EntitySourceBuilder.getProxyType(type);
	}

	public static void clearModifies(Object entity) {
		if(entity instanceof Entity) {
			((Entity)entity).clearModifies();
		}
	}

	/**
	 * 是否是一个代理实体
	 * */
	public static boolean isManaged(Object pojo) {
		if(!(pojo instanceof Entity)) return false;
		return isProxyType(pojo.getClass());
	}
	
	/**
	 * 是否实体类型
	 * */
	public static boolean isEntityType(Class type) {
		return ReflectUtil.isSubType(Entity.class, type);
	}
	
	/**
	 * 判断是否已经是代理类型
	 * */
	public static boolean isProxyType(Class type) {
		if(type==null) return false;
		if(PROXY_CLASS_NAME.equals(type.getSimpleName()) && Entity.class.isAssignableFrom(type) ) {
			return true;
		} else {
			return false;
		}
	}
	
	

	

 
	
	

	public static String convertProxyName(String dataType) {
		if(dataType==null) return null; 
		Class type=ReflectUtil.forName(dataType, true);
		if(type==null) return dataType;
		if(!isProxyType(type)) return dataType;
		return type.getSuperclass().getName();
	}

}
