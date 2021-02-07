package com.github.foxnic.dao.entity;

public class EntityContext {
	
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
	
}
