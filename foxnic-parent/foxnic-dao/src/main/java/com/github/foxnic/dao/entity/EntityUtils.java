package com.github.foxnic.dao.entity;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class EntityUtils {

	private static final BeanNameUtil NC = new BeanNameUtil();

	private static HashMap<String, List<String>> ENTITY_DATA_FILEDS = new HashMap<String, List<String>>();

	private static ConcurrentHashMap<String, List<Field>> IDS = new ConcurrentHashMap<String, List<Field>>();

	/**
	 * 设置ID值，如果ID为null 或 blank 就设置一个生成的值
	 * */
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
				if(val==null || StringUtil.isBlank(val)) {
					val=service.generateId(f);
					BeanUtil.setFieldValue(entity, f.getName(),val);
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

}
