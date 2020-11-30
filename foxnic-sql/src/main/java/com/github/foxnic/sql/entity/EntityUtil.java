package com.github.foxnic.sql.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.cache.LocalCache;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.sql.entity.annotations.ColumnDesc;

 

public class EntityUtil {

	private static  HashMap<String,Map<String,String>> PROPERTY_CACHE=new HashMap<String,Map<String,String>>();

	/**
	 * 通过实体类型获得bean属性与数据库字段之间的对照关系
	 * @param clazz Bean类型
	 * @return 对应关系
	 * */
	public static Map<String,String> getDBFields(Class clazz)
	{	
 
		Map<String,String> fs=null;
		
		fs=PROPERTY_CACHE.get(clazz.getName());
		if(fs!=null) {
			return fs;
		}
		
		fs=new HashMap<>();
		Field[] fields=clazz.getDeclaredFields();
		Method m=null;
		for (Field field : fields) {
			if(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) ) {
				continue;
			}
			//查看属性本身的JPA注解
			ColumnDesc column=field.getAnnotation(ColumnDesc.class);
			//查看对应Getter的JPA注解
			if(column==null)
			{
				m=getGetter(field);
				if(m!=null) {
					column=m.getAnnotation(ColumnDesc.class);
				}
				
			}
			//查看对应Setter的JPA注解
			if(column==null)
			{
				m=getSetter(field);
				if(m!=null) {
					column=m.getAnnotation(ColumnDesc.class);
				}
			}
			//通过属性名称直接转换
			if(column==null)
			{
				fs.put(field.getName(), getDBFieldName(field));
			}
			else
			{
				fs.put(field.getName(),column.name());
			}
		}
		PROPERTY_CACHE.put(clazz.getName(),Collections.unmodifiableMap(fs));
		return fs;
	}
	
	/**
	 * 通过属性名称直接转换
	 * @param fld 字段
	 * @return 返回值
	 * */
	private static String getDBFieldName(Field fld)
	{
		String name=fld.getName();
		String field="";
		int j=0;
		int z=0;
		for (int i = 0; i < name.length(); i++) {
			char c=name.charAt(i);
			if(Character.isUpperCase(c))
			{
				field+=(j==0?"":"_")+name.substring(z, i);
				z=i;j++;
			}
		}
		
		if(j==0) {
			field=name;
		} else {
			field+="_"+name.substring(z, name.length());
		}
		
		return field.toLowerCase();
		
	}
	
	private static  Method getGetter(Field field)
	{
		String[] names=getGetterNames(field);
		for (int i = 0; i < names.length; i++) {
			String name=names[i];
			try {
				Method m=field.getDeclaringClass().getDeclaredMethod(name);
				if(m!=null) {
					return m;
				}
			} catch (Exception e) {}
		}
		return null;
	}
	
	
	private static  Method getSetter(Field field)
	{
		String[] names=getSetterNames(field);
		for (int i = 0; i < names.length; i++) {
			String name=names[i];
			try {
				Method m=field.getDeclaringClass().getDeclaredMethod(name,field.getType());
				if(m!=null) {
					return m;
				}
			} catch (Exception e) {}
		}
		return null;
	}
	
	private static  String[] getGetterNames(Field field)
	{
		String name=field.getName();
		return new String[] {"get"+name.substring(0,1).toUpperCase()+name.substring(1),"is"+name.substring(0,1).toUpperCase()+name.substring(1),name};
	}
	
	private static  String[] getSetterNames(Field field)
	{
		String name=field.getName();
		return new String[] {"set"+name.substring(0,1).toUpperCase()+name.substring(1),name,"is"+name.substring(0,1).toUpperCase()+name.substring(1),"set"+name.substring(2)};
	}
	
	
	
	private static Class MYBATIS_TABLE_NAME_ANNOTATION_TYPE=Object.class;
	private static Class JPA_TABLE_NAME_ANNOTATION_TYPE=Object.class;
	
	private static LocalCache<Class<?>, String> TABLE_CACHE=new LocalCache<Class<?>, String>(); 
	 
	/**
	 * 获得注解的表名
	 * */
	public static String getAnnotationTable(Class<?> type) {
		
		String tableName=TABLE_CACHE.get(type);
		if(tableName!=null) {
			return tableName;
		}
		
		//MyBatis Plus
		if(MYBATIS_TABLE_NAME_ANNOTATION_TYPE!=null && Object.class.equals(MYBATIS_TABLE_NAME_ANNOTATION_TYPE)) {
			MYBATIS_TABLE_NAME_ANNOTATION_TYPE=ReflectUtil.forName("com.baomidou.mybatisplus.annotation.TableName");
		}
		
		if(tableName==null && MYBATIS_TABLE_NAME_ANNOTATION_TYPE!=null) {
			Object ann=type.getAnnotation(MYBATIS_TABLE_NAME_ANNOTATION_TYPE);
			if(ann!=null) {
				tableName=(String)BeanUtil.getFieldValue(ann, "value");
			}
		}
		
		//JPA
		if(JPA_TABLE_NAME_ANNOTATION_TYPE!=null && Object.class.equals(JPA_TABLE_NAME_ANNOTATION_TYPE)) {
			JPA_TABLE_NAME_ANNOTATION_TYPE=ReflectUtil.forName("javax.persistence.Table");
		}
		
		if(tableName==null && JPA_TABLE_NAME_ANNOTATION_TYPE!=null) {
			Object ann=type.getAnnotation(JPA_TABLE_NAME_ANNOTATION_TYPE);
			if(ann!=null) {
				tableName=(String)BeanUtil.getFieldValue(ann, "name");
			}
		}
		
		
		
		if(!StringUtil.isBlank(tableName)) {
			TABLE_CACHE.put(type, tableName);
		}
		
		return tableName;
	}
	
	
	
}
