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
import java.util.Map.Entry;

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
		if(p instanceof Enum) {
			Enum e=(Enum) p;
			p=e.name();
		} else if(TYPE_ORACLE_TIMESTAMP.equals(p.getClass().getName())) {
			p=DataParser.parseDate(p);
		} else if(p instanceof LocalDateTime) {
			p=DataParser.parseDate(p);
		} else if(p instanceof Clob) {
			p=DataParser.parseString(p);
		} else if(isClobProxy(p)) {
			p=DataParser.parseString(p);
		} else if(TYPE_ORACLE_CLOB.equals(p.getClass().getName())) {
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
 
	/**
	 * 对参数进行过滤和处理
	 * @param ps 参数
	 * @return 处理后的参数
	 * */
	public static Object[] filterParameter(Object[] ps)
	{
		Object p=null;
		for (int i = 0; i < ps.length; i++) {
			p=ps[i];
			if(p==null) continue;
			ps[i]=Utils.parseParmeterValue(p);
		}
		return ps;
	}
	
	

	/**
	 * 对参数进行过滤和处理
	 * @param ps 参数
	 * @return 处理后的参数
	 * */
	public static Map<String,Object> filterParameter(Map<String,Object> ps)
	{
		Map<String,Object> map=new HashMap<String, Object>();
		map.putAll(ps);
		Object p=null;
		for (Entry<String,Object> kv : ps.entrySet()) {
			p=kv.getValue();
			if(p==null) continue;
			p=Utils.parseParmeterValue(p);
			map.put(kv.getKey(),p);
		}
		return map;
	}
 

}
