package com.github.foxnic.dao.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.cache.LocalCache;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;

public class EntityUtil {
	
	
	private static BeanNameUtil NC = new BeanNameUtil();
	private static HashMap<String, List<String>> ENTITY_DATA_FILEDS = new HashMap<String, List<String>>();
	
	
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

		ArrayList<String> result = new ArrayList<String>();
		gatherPossibleFields(type, result);

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
	
}
