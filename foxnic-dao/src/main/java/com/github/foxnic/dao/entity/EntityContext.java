package com.github.foxnic.dao.entity;

public class EntityContext {
	
	static final String PROXY_PACKAGE="$$proxy$$";
	
	public static <T extends Entity> T create(Class<T> type) { 
		return EntitySourceBuilder.create(type);
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
	 * 是否是一个自我管理的实体
	 * */
	public static boolean isManaged(Object pojo) {
		if(!(pojo instanceof Entity)) return false;
		if(pojo.getClass().getName().endsWith("."+PROXY_PACKAGE+"."+pojo.getClass().getSimpleName())) {
			return true;
		} else {
			return false;
		}
	}
	
}
