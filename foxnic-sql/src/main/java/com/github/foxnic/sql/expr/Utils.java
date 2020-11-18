package com.github.foxnic.sql.expr;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Clob;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.exception.DBIdentityException;

/**
 * @author fangjieli
 * */
public class Utils
{
  
	/**
	 * 简单防SQL注入
	 * @param sql 语句
	 * @param   dialect 方言
	 * @return 改写后的语句
	 * */
	private static String castSQLInjection(String sql,SQLDialect dialect)
	{
		return dialect.getDialectProcessor().castCharInjection(sql);
	}
	
	private static String[] INVALID_IDENTITY_CHARS= {" ","　","+","-","/","\\","\n","\t","\r",",",";","*","=","?",":"};
	
	public static void  validateDBIdentity(String identity)
	{
		if(identity==null) return;
		identity=identity.trim();
		for (String c: INVALID_IDENTITY_CHARS) {
			if(identity.contains(c)) {
				throw new DBIdentityException(identity);
			}
		}
	}
 
	/**
	 * 转日期格式
	 * @param date 日期时间
	 * @param   dialect 方言
	 * @return 改写后的语句
	 * */
	private static String castDateValue(Date date,SQLDialect dialect)
	{
		return dialect.getDialectProcessor().getToDateTimeSQL(date);
	}

	/**
	 * 转换值
	 * @param val val
	 * @param   dialect 方言
	 * @return 抓换后的值
	 * */
	public static String castValue(Object val,SQLDialect dialect)
	{
		if (val == null)
		{
			return " null ";
		}
		else if (DataParser.isNumberType(val))
		{
			return  val.toString();
		} else if (DataParser.isDateTimeType(val))
		{
			Date date=DataParser.parseDate(val);
			return castDateValue(date,dialect) ;
		} else
		{
			return "'" + castSQLInjection(val.toString(),dialect) + "'";
		}
	}
 
	public static List<Object> toList(Object[] arr)
	{
		return Arrays.asList(arr);
	}
	
	
	private static final String TYPE_ORACLE_TIMESTAMP="oracle.sql.TIMESTAMP";
	private static final String TYPE_ORACLE_CLOB="oracle.sql.CLOB";
	
	public static Object parseParmeterValue(Object p) {
		if(p==null) return null;
		if(p instanceof Enum)
		{
			Enum e=(Enum) p;
			p=e.name();
		}
		else if(TYPE_ORACLE_TIMESTAMP.equals(p.getClass().getName()))
		{
			p=DataParser.parseDate(p);
		}
		else if(p instanceof LocalDateTime)
		{
			p=DataParser.parseDate(p);
		}
		else if(p instanceof Clob)
		{
			p=DataParser.parseString(p);
		} else if(isClobProxy(p))
		{
			p=DataParser.parseString(p);
		}
		else if(TYPE_ORACLE_CLOB.equals(p.getClass().getName()))
		{
			p=DataParser.parseString(p);
		}
		return p;
	}
	
	private static Class clobProxyClass=null;
	
	private static boolean isClobProxy(Object p) {
		if(p==null) return false;
		if(clobProxyClass==null) {
			clobProxyClass=ReflectUtil.forName("com.alibaba.druid.proxy.jdbc.ClobProxy",true);
		}
		if(clobProxyClass==null) return false;
		return ReflectUtil.isSubType(clobProxyClass, p.getClass());
	}
	
	
	private static  HashMap<String,Map<String,String>> PROPERTY_CACHE=new HashMap<String,Map<String,String>>();

//	/**
//	 * 通过实体类型获得bean属性与数据库字段之间的对照关系
//	 * @param clazz Bean类型
//	 * @return 对应关系
//	 * */
//	public static Map<String,String> getDBFields(Class clazz)
//	{	
// 
//		Map<String,String> fs=null;
//		
//		fs=PROPERTY_CACHE.get(clazz.getName());
//		if(fs!=null) {
//			return fs;
//		}
//		
//		fs=new HashMap<>();
//		Field[] fields=clazz.getDeclaredFields();
//		Method m=null;
//		for (Field field : fields) {
//			if(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) ) {
//				continue;
//			}
//			//查看属性本身的JPA注解
//			ColumnDesc column=field.getAnnotation(ColumnDesc.class);
//			//查看对应Getter的JPA注解
//			if(column==null)
//			{
//				m=getGetter(field);
//				if(m!=null) {
//					column=m.getAnnotation(ColumnDesc.class);
//				}
//				
//			}
//			//查看对应Setter的JPA注解
//			if(column==null)
//			{
//				m=getSetter(field);
//				if(m!=null) {
//					column=m.getAnnotation(ColumnDesc.class);
//				}
//			}
//			//通过属性名称直接转换
//			if(column==null)
//			{
//				fs.put(field.getName(), getDBFieldName(field));
//			}
//			else
//			{
//				fs.put(field.getName(),column.name());
//			}
//		}
//		PROPERTY_CACHE.put(clazz.getName(),Collections.unmodifiableMap(fs));
//		return fs;
//	}
	
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
 

}
