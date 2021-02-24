package com.github.foxnic.dao.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;

public class EntityContext {
	
	private static final BeanNameUtil NC = new BeanNameUtil();	
	
	static final String PROXY_PACKAGE="$$proxy$$";

	private static HashMap<String, List<String>> ENTITY_DATA_FILEDS = new HashMap<String, List<String>>();
	
	private static ConcurrentHashMap<String, List<Field>> IDS = new ConcurrentHashMap<String, List<Field>>();
	
	
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
	 * 判断是否已经是代理类型
	 * */
	public static boolean isProxyType(Class type) {
		if(type.getName().endsWith("."+PROXY_PACKAGE+"."+type.getSimpleName())) {
			return true;
		} else {
			return false;
		}
	}
	
	

	/**
	 * 从实体类型获得所有可能的数据库字段
	 * 
	 * @param type  POJO类型
	 * @param table 数据表
	 * @return POJO对象的所有字段
	 */
	public static List<String> getEntityFields(Class<?> type, DAO dao,String table) {
 
		String key = type.getName() + "@" + table;
		List<String> fields = ENTITY_DATA_FILEDS.get(key);
		if (fields != null)
			return fields;

		DBTableMeta tm = dao.getTableMeta(table);
		if (tm == null) {
			if (!dao.isTableExists(table)) {
				throw new IllegalArgumentException("数据表[" + table + "]不存在");
			}
		}

		//搜集所有可能的字段清单
		ArrayList<String> result = new ArrayList<String>();
		gatherPossibleFields(type, result);

		//进一步判定是否是数据库中的字段
		fields = new ArrayList<String>();
		for (String fn : result) {
			if (tm.isColumnExists(fn)) {
				fields.add(fn);
			} else {
				fn = NC.depart(fn);
				if (tm.isColumnExists(fn)) {
					fields.add(fn);
				}
			}
		}
		ENTITY_DATA_FILEDS.put(key, fields);
		return fields;
	}
	
 
	/**
	 * 从实体类型获得所有可能的数据库字段
	 */
	private static void gatherPossibleFields(Class<?> type, List<String> result) {
		Field[] fields = type.getDeclaredFields();
		String name = null;
		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers()) || Modifier.isFinal(f.getModifiers()))
				continue;
			name = f.getName();
			if (result.contains(name))
				continue;
			result.add(name);
			if (name.startsWith("is") || name.startsWith("if")) {
				name = name.substring(2);
				result.add(name);
			}
		}
		if (type.getSuperclass() != null) {
			gatherPossibleFields(type.getSuperclass(), result);
		}
	}
	
	
	/**
	 * 从实体类型获得所有可能的数据库字段
	 */
	private static  List<String> getAllFields(Class<?> type) {
		List<String>  all=new ArrayList<String>();
		while(true) {
			Field[] fields = type.getDeclaredFields();
			String name = null;
			for (Field f : fields) {
				if (Modifier.isStatic(f.getModifiers()) || Modifier.isFinal(f.getModifiers()))
					continue;
				name = f.getName();
				if (all.contains(name))
					continue;
				all.add(name);
			}
			//
			type=type.getSuperclass();
			if(type==null) break;
		}
		return all;
	}

 
	
	public static int setId(Object entity, SuperService service) {
		Class type=entity.getClass();
		List<Field> all=IDS.get(entity.getClass().getName());  
		if(all==null) { 
			all=new ArrayList<Field>();
			while(true) {
				Field[] fields = type.getDeclaredFields();
				String name = null;
				for (Field f : fields) {
					if (Modifier.isStatic(f.getModifiers()) || Modifier.isFinal(f.getModifiers()))
						continue;
					name = f.getName();
					if (all.contains(name))
						continue;
					Id id=f.getAnnotation(Id.class);
					if(id!=null) {
						all.add(f);
					}
				}
				//
				type=type.getSuperclass();
				if(type==null) break;
			}
			IDS.put(entity.getClass().getName(), all);
		}
		int hasAI=0;
		//处理主键
		for (Field f : all) {
			GeneratedValue gval=f.getAnnotation(GeneratedValue.class);
			if(gval==null) {
				Object val=BeanUtil.getFieldValue(entity, f.getName());
				if(val==null) {
					val=service.generateId(f);
					BeanUtil.setFieldValue(entity, f.getName(),f);
				}
			} else {
				if(gval.strategy()==GenerationType.IDENTITY) {
					//不处理
					hasAI++;
				} else {
					throw new IllegalArgumentException("not support "+gval.strategy().name());
				}
			}
		}
		return hasAI;
	}

	public static String convertProxyName(String dataType) {
		if(dataType==null) return null; 
		Class type=ReflectUtil.forName(dataType, true);
		if(type==null) return dataType;
		if(!isProxyType(type)) return dataType;
		return type.getSuperclass().getName();
	}

}
