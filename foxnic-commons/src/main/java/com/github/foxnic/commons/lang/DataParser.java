package com.github.foxnic.commons.lang;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.cache.LocalCache;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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
	 * @param arg 任意类型值
	 * @return  是否为 Boolean 类型
	 * */
	public static boolean isBooleanType(Object value)
	{
		if(value==null) return false;
		return isBooleanType(value.getClass());
	}
	
	/**
	 * 是否为 Boolean 类型
	 * @param arg 任意类型值
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
	 * @param value 任意类型值
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
	 * @param value 任意类型值
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
	 * @param cls 类型
	 * @return  是否为简单类型
	 * */
	public static boolean isSimpleType(Class type)
	{
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

	public static Object[] parseArray(Class arrayType, Object value) {
		
		if(value==null) return null;
		if(value.getClass().isArray()) return (Object[])value;
		if(!(value instanceof CharSequence)) return (Object[])value; 
		String str=(String)value;
		
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
		
		as=ArrayUtil.castArrayType(new Object[] {value}, arrayType.getComponentType());
		
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
			return parseDate(value);
		}else if(value!=null && value.getClass().getName().equals("oracle.sql.TIMESTAMP")) {
			return parseTimestamp(value);
		}
		else if(cls.equals(Character.class)) {
			return parseChar(value);
		} else {
			return value;
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

	
 
}
