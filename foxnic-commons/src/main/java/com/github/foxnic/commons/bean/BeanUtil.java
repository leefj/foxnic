package com.github.foxnic.commons.bean;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.github.foxnic.commons.lang.DataParser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.esotericsoftware.reflectasm.ConstructorAccess;
import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;

/**
 * @author leefangjie
 * */
public class BeanUtil {
 
	private static class ValidWay
	{
		public String name=null;
		public int type=0;
		public boolean sucess=false;
		public MethodAccess methodAccess;
		public Field field;
		public Class<?> requireType;
		public boolean isTargetExists=false;
	}
	
	private static BeanNameUtil NC=new BeanNameUtil();
	
	private static ConcurrentHashMap<Class<?> , ConstructorAccess<?>> CONSTRUCTOR_ACCESS_CACHE=new ConcurrentHashMap<Class<?> , ConstructorAccess<?>>();
	
	private static ConcurrentHashMap<Class<?> , FieldAccess> FIELD_ACCESS_CACHE=new ConcurrentHashMap<Class<?> , FieldAccess>();
	private static ConcurrentHashMap<Class<?> , HashMap<String,Class<?>>> FIELD_TYPE=new ConcurrentHashMap<Class<?> , HashMap<String,Class<?>>>();
	private static ConcurrentHashMap<Class<?> , HashMap<String,Field>> FIELDS=new ConcurrentHashMap<Class<?> , HashMap<String,Field>>();
	private static ConcurrentHashMap<Class<?> , HashMap<String,Field>> BLUR_FIELDS=new ConcurrentHashMap<Class<?> , HashMap<String,Field>>();
	
	private static ConcurrentHashMap<Class<?> , MethodAccess> METHOD_ACCESS_CACHE=new ConcurrentHashMap<Class<?> , MethodAccess>();
	private static ConcurrentHashMap<Class<?> , HashMap<String,Class<?>>> UNIQUE_METHOD_RETURN_TYPE=new ConcurrentHashMap<Class<?> , HashMap<String,Class<?>>>();
	
	private static ConcurrentHashMap<Class<?> , HashMap<String,ValidWay>> VALID_SETTER=new ConcurrentHashMap<Class<?> , HashMap<String,ValidWay>>();
	private static ConcurrentHashMap<Class<?> , HashMap<String,ValidWay>> VALID_GETTER=new ConcurrentHashMap<Class<?> , HashMap<String,ValidWay>>();
	
	
	private static HashMap<String, List<String>> POJO_DATA_FILEDS=new HashMap<String, List<String>>();
	
	/**
	 * 从实体类型获得所有可能的数据库字段
	 * */
	private static List<String> getAllFields(Class<?> type)
	{
		String key=type.getName();
		List<String>  fields=POJO_DATA_FILEDS.get(key);
		if(fields!=null) {
			return fields;
		}
		fields=new ArrayList<String>();
		gatherClassFields(type, fields);
		POJO_DATA_FILEDS.put(key,fields);
		return fields;
	}
	/**
	 * 从实体类型获得所有可能的数据库字段
	 * */
	private  static void  gatherClassFields(Class<?> type,List<String> result)
	{
		Field[] fields=type.getDeclaredFields();
		String name=null;
		for (Field f : fields) {
			if(Modifier.isStatic(f.getModifiers())  || Modifier.isFinal(f.getModifiers())) {
				continue;
			}
			name=f.getName();
			if(result.contains(name)) {
				continue;
			}
			result.add(name);
		}
		if(type.getSuperclass()!=null) {
			gatherClassFields(type.getSuperclass(), result);
		}
	}
	
	private static void initAccessFieldIf(Class<?>  type) {
		
		FieldAccess fa=FIELD_ACCESS_CACHE.get(type);
		if(fa!=null) {
			return;
		}
	 
		fa=FieldAccess.get(type);
		FIELD_ACCESS_CACHE.put(type,fa);
 
	}
	
	private static void initMethodAccessIf(Class<?>  type) {
		
		MethodAccess fa=METHOD_ACCESS_CACHE.get(type);
		if(fa!=null) {
			return;
		}
	 
		fa=MethodAccess.get(type);
		METHOD_ACCESS_CACHE.put(type,fa);
 
	}
	
	private static Field getField(Class<?> type,String fieldName)
	{
		HashMap<String,Field> map=FIELDS.get(type);
		if(map==null)
		{
			map=new HashMap<String,Field>(5);
			Field[] fs=type.getDeclaredFields();
			for (Field f : fs) {
				f.setAccessible(true);
				map.put(f.getName(), f);
			}
			FIELDS.put(type,map);
		}
		Field f= map.get(fieldName);
		if(f==null && type.getSuperclass()!=null)
		{
			f = getField(type.getSuperclass(), fieldName);
		}
		return f;
	}
	
	
	private static Field getBlurField(Class<?> type,String fieldName)
	{
		HashMap<String,Field> map=BLUR_FIELDS.get(type);
		if(map==null)
		{
			map=new HashMap<String,Field>(5);
			Field[] fs=type.getDeclaredFields();
			for (Field f : fs) {
				f.setAccessible(true);
				map.put(f.getName().toUpperCase(), f);
			}
			BLUR_FIELDS.put(type,map);
		}
		Field f= map.get(fieldName.toUpperCase());
		
		if(f==null && type.getSuperclass()!=null)
		{
			f = getBlurField(type.getSuperclass(), fieldName);
		}
		return f;
	}
	
	
	private static Class<?> getFieldType(Class<?> type,String fieldName)
	{
		HashMap<String,Class<?>> fieldTypes=FIELD_TYPE.get(type);
		if(fieldTypes==null)
		{
			fieldTypes=new HashMap<String,Class<?>>(5);
			FIELD_TYPE.put(type,fieldTypes);
		}
		
		if(fieldTypes.containsKey(fieldName))
		{
			Class<?> pType=fieldTypes.get(fieldName);
			if(pType==null && type.getSuperclass()!=null)
			{
				return getFieldType(type.getSuperclass(),fieldName);
			}
			return pType;
		}
		
		Field[] ms= type.getDeclaredFields();
		Field um=null;
		for (Field m : ms) {
			if(m.getName().equals(fieldName))
			{
				if(um==null) {
					um=m;
				} else
				{
					um=null;
					fieldTypes.put(fieldName, null);
					break;
				}
			}
		}
		
		Class<?> returnType=um==null?null:um.getType();
		if(um!=null) {
			um.setAccessible(true);
		}
		fieldTypes.put(fieldName, returnType);
		return returnType;
	}
	
	private static Class<?> getUniqueMethodType(Class<?> type,String methodName,boolean isSetter)
	{
		HashMap<String,Class<?>> methodTypes=UNIQUE_METHOD_RETURN_TYPE.get(type);
		if(methodTypes==null)
		{
			methodTypes=new HashMap<String,Class<?>>(5);
			UNIQUE_METHOD_RETURN_TYPE.put(type,methodTypes);
		}
		
		if(methodTypes.containsKey(methodName))
		{
			Class<?> pType=methodTypes.get(methodName);
			if(pType==null && type.getSuperclass()!=null)
			{
				return getUniqueMethodType(type.getSuperclass(), methodName,isSetter);
			}
			return pType;
		}
		
		Method[] ms= type.getDeclaredMethods();
		Method um=null;
		for (Method m : ms) {
			if(Modifier.isStatic(m.getModifiers())) {
				continue;
			}
			if(m.getName().equals(methodName))
			{
				if(um==null)
				{
					if(isSetter)
					{
						if(m.getParameterCount()==1)
						{
							um=m;
						}
					}
					else
					{
						if(m.getParameterCount()==0)
						{
							um=m;
						}
					}
				}
				else
				{
					if(isSetter)
					{
						if(m.getParameterCount()==1)
						{
							um=null;
							methodTypes.put(methodName, null);
							break;
						}
					}
					else
					{
						if(m.getParameterCount()==0)
						{
							um=null;
							methodTypes.put(methodName, null);
							break;
						}
					}
					
				}
			}
		}
		
		if(um==null)
		{
			methodTypes.put(methodName, null);
			return null;
		}
		
		if(isSetter)
		{
			Class<?>[] pTypes=um.getParameterTypes();
			if(pTypes.length==1)
			{
				methodTypes.put(methodName, pTypes[0]);
				return pTypes[0];
			}
			else
			{
				methodTypes.put(methodName, null);
				return null;
			}
		}
		else
		{
			methodTypes.put(methodName, um.getReturnType());
			return um.getReturnType();
		}

	}
	
	private static FieldAccess getFieldAccess(Class<?>  type)
	{
		initAccessFieldIf(type);
		return FIELD_ACCESS_CACHE.get(type);
	}
	
 
	
	private static MethodAccess getMethodAccess(Class<?>  type)
	{
		initMethodAccessIf(type);
		return METHOD_ACCESS_CACHE.get(type);
	}
 

	/**
	 * 创建一个指定类型的对象
	 * @param <T> 类型
	 * @param type  Type to create 
	 * @return 创建的对象
	 * */
	public static <T> T create(Class<T>  type)
	{
		ConstructorAccess<?> ca=CONSTRUCTOR_ACCESS_CACHE.get(type);
		if(ca==null) {
			ca=ConstructorAccess.get(type);
			CONSTRUCTOR_ACCESS_CACHE.put(type, ca);
		}
		return (T)ca.newInstance();
	}
	
	/**
	 * 把Map的List转成指定类型的对象List
	 * @param <T> 类型
	 * @param list 元素为Map的List
	 * @param type  Type to cast
	 * @return Bean清单
	 * */
	@SuppressWarnings("rawtypes")
	public static <T> List<T> toList(Collection<Map> list,Class<T> type)
	{
		List<T> newlist=new ArrayList<T>();
		for (Map map : list) {
			newlist.add(toJavaObject(map,type));
		}
		for (int i = 0; i < list.size(); i++) {
			
		}
		return newlist;
	}
 
	/**
	 * 把JSONArray转成指定类型的对象列表
	 * @param <T> 类型
	 * @param array  JSONArray
	 * @param type  Type to cast
	 * @return Bean清单
	 * */
	public static <T> List<T> toList(JSONArray array,Class<T> type)
	{
		List<T> list=new ArrayList<T>();
		for (int i = 0; i < array.size(); i++) {
			T object=toJavaObject(array.getJSONObject(i),type);
			list.add(object);
		}
		return list;
	}
 
	/**
	 * 把map中的属性copy到java对象中,默认为silence模式
	 * @param <T> 类型
	 * @param source  数据来源
	 * @param target   目标
	 * @return 复制的对象
	 * */
	public static <T> T copy(Map<String, Object> source, T target) {
 
		for (Map.Entry<String, Object> e : source.entrySet()) {
			setFieldValue(target, e.getKey(), e.getValue());
		}
 
		return target;
	}
	 
	/**
	 * 复制数据
	 * @param <T> 类型
	 * @param source  数据来源
	 * @param target   目标
	 * @param ignorNulls  是否忽略空值，对source中的空值不做拷贝
	 * @return 复制的对象
	 * */
	public static <T> T copy(T source, T target,boolean ignorNulls) {
		Class<?> type=source.getClass();
		List<String> fields=getAllFields(type);
		Object value=null;
		for (String field : fields) {
			value=getFieldValue(source, field);
			if(ignorNulls && value==null) continue;
			setFieldValue(target, field, value);
		}
		return target;
	}
	
	
	/**
	 * 提取Bean清单中某一个属性的值
	 * @param <T> Bean类型
	 * @param <V> 值类型
	 * @param list  bean清单
	 * @param field 要提取的bean属性面层，
	 * @param type  属性值类型
	 * @return 值清单
	 * */
	public static <T,V> List<V> getFieldValueList(Collection<T> list,String field,Class<V> type) {
		 
		List<V> vList=new ArrayList<V>();
		Object tmp=null;
		V value=null;
		for (T bean : list) {
			tmp=getFieldValue(bean, field);
			value=DataParser.parse(type, tmp);
			vList.add(value);
		}
		return vList;
	}
	
	/**
	 * 提取Bean清单中某一个属性的值
	 * 	@param <T> Bean类型
	 * @param <V> 值类型
	 * @param list  bean清单
	 * @param field 要提取的bean属性面层，
	 * @param type  属性值类型
	 * @return 值清单
	 * */
	public static <T,V> Set<V> getFieldValueSet(Collection<T> list,String field,Class<V> type) {
		 
		Set<V> vList=new HashSet<V>();
		Object tmp=null;
		V value=null;
		for (T bean : list) {
			tmp=getFieldValue(bean, field);
			value=DataParser.parse(type, tmp);
			vList.add(value);
		}
		return vList;
	}
	
	/**
	 * 提取Bean清单中某一个属性的值
	 * 	@param <T> Bean类型
	 * @param <V> 值类型
	 * @param list  bean清单
	 * @param field 要提取的bean属性面层，
	 * @param type  属性值类型
	 * @return 值清单
	 * */
	@SuppressWarnings("unchecked")
	public static <T,V> V[] getFieldValueArray(Collection<T> list,String field,Class<V> type) {
		V[] arr=(V[])Array.newInstance(type, list.size());
		Object tmp=null;
		V value=null;
		int i=0;
		for (T bean : list) {
			tmp=getFieldValue(bean, field);
			value=DataParser.parse(type, tmp);
			arr[i]=value;
			i++;
		}
		return arr;
	}
	
	
	 
	
	/**
	 * 设置属性
	 * @param bean java对象
	 * @param field 字段/属性
	 * @param value 值
	 * @return 是否设置成功
	 * */
	public static  boolean setFieldValue(Object bean, String field, Object value) {
		
		Class<?> type=bean.getClass();
		
		HashMap<String,ValidWay> validSetters=VALID_SETTER.get(type);
		if(validSetters==null)
		{
			validSetters=new HashMap<String,ValidWay>(5);
			VALID_SETTER.put(type,validSetters);
		}
 
		ValidWay way=validSetters.get(field);
		boolean setted = false;
		//确定值设置，无需Guess
		if(way!=null)
		{
			if(!way.sucess) {
				return false;
			}
			
			if(way.type==0)
			{
				try {
					way.methodAccess.invoke(bean, way.name, DataParser.parse(way.requireType, value));
					setted=true;
				} catch (Exception e1) {}
			}
			else if(way.type==1)
			{
				try {
					way.field.set(bean, DataParser.parse(way.requireType, value));
					setted=true;
				} catch (Exception e2) {}
			}
			if(setted) {
				return true;
			}
		}
		

		way=new BeanUtil.ValidWay();
		String fieldName = null;
		String setMethodName = null;
		//优先使用set方法设置
		setMethodName = NC.getSimpleSetMethodName(field);
		setted=setValueWithMethod(type,bean,setMethodName,value,way);
		if(setted) {
			validSetters.put(field,way);
			return true;
		}
		
		//优先使用set方法设置
		setMethodName = NC.getSetMethodName(field, false);
		setted=setValueWithMethod(type,bean,setMethodName,value,way);
		if(setted) {
			validSetters.put(field,way);
			return true;
		}
		
		setMethodName = NC.getSetMethodName(field, true);
		setted=setValueWithMethod(type,bean,setMethodName,value,way);
		if(setted) {
			validSetters.put(field,way);
			return true;
		}

		
		fieldName=field;
		setted=setValueWithField(type,bean,fieldName,value,way);
		if(setted) {
			validSetters.put(field,way);
			return true;
		}
		
		//使用属性设置
		fieldName=NC.getPropertyName(field);
		setted=setValueWithField(type,bean,fieldName,value,way);
		if(setted) {
			validSetters.put(field,way);
			return true;
		}
		
		way.sucess=false;
		validSetters.put(field,way);
		
		 
		return false;
		 
	}
	
	/**
	 * 获取属性
	 * @param bean java对象
	 * @param field 字段/属性
	 * @return 属性值
	 * */
	public static  Object getStaticFieldValue(Class clazz, String field) {
		Field f=getField(clazz, field);
		if(f==null) {
			throw new RuntimeException("属性 "+field+" 在"+clazz.getName()+" 中不存在");
		}
		try {
			return f.get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}  
	}
	

	/**
	 * 获取属性
	 * @param bean java对象
	 * @param field 字段/属性
	 * @param type 指定值类型
	 * @return 属性值
	 * */
	public static <T>  T getFieldValue(Object bean, String field,Class<T> type) {
		Object o=getFieldValue(bean,field);
		if(o==null) return null;
		if(type.isAssignableFrom(o.getClass())) return (T)o;
		return DataParser.parse(type, o);
	}
	
	/**
	 * 调用bean方法
	 * @param bean java对象
	 * @param method 方法名
	 * @param args 参数清单
	 * @return 属性值
	 * */
	public static Object invoke(Object bean,String method,Object... args) {
		if(bean==null) {
			throw new IllegalArgumentException("bean is null");
		};
		MethodAccess ma=getMethodAccess(bean.getClass());
		Object ret=ma.invoke(bean, method, args);
		return ret;
	}
	
	/**
	 * 获取属性
	 * @param bean java对象
	 * @param field 字段/属性
	 * @return 属性值
	 * */
	public static  Object getFieldValue(Object bean, String field) {
		
		Class<?> type=bean.getClass();
		
		HashMap<String,ValidWay> validGetters=VALID_GETTER.get(type);
		if(validGetters==null)
		{
			validGetters=new HashMap<String,ValidWay>(5);
			VALID_GETTER.put(type,validGetters);
		}
		Object value=null;
		ValidWay way=validGetters.get(field);
		boolean gotted = false;
		//确定值设置，无需Guess
		if(way!=null)
		{
			if(!way.sucess) {
				return null;
			}
			
			if(way.type==0)
			{
				try {
					value=way.methodAccess.invoke(bean, way.name);
					gotted=true;
				} catch (Exception e1) {}
			}
			else if(way.type==1)
			{
				try {
					value=way.field.get(bean);
					gotted=true;
				} catch (Exception e2) {}
			}
			if(gotted) {
				return value;
			}
		}
		

		way=new BeanUtil.ValidWay();
		String fieldName = null;
		String getMethodName = null;
		
		
		//优先使用get方法设置
		getMethodName = field;
		value =getValueWithMethod(type,bean,getMethodName,way);
		if(way.sucess) {
			validGetters.put(field,way);
			return value;
		}
		
		//优先使用get方法设置
		getMethodName = NC.getSimpleGetMethodName(field);
		value =getValueWithMethod(type,bean,getMethodName,way);
		if(way.sucess) {
			validGetters.put(field,way);
			return value;
		}
		
		//优先使用set方法设置
		getMethodName = NC.getGetMethodName(field, false);
		value=getValueWithMethod(type,bean,getMethodName,way);
		if(way.sucess) {
			validGetters.put(field,way);
			return value;
		}
		
		getMethodName = NC.getGetMethodName(field, true);
		value=getValueWithMethod(type,bean,getMethodName,way);
		if(way.sucess) {
			validGetters.put(field,way);
			return value;
		}

		
		fieldName=field;
		value=getValueWithField(type,bean,fieldName,way);
		if(way.sucess) {
			validGetters.put(field,way);
			return value;
		}
		
		//使用属性设置
		fieldName=NC.getPropertyName(field);
		value=getValueWithField(type,bean,fieldName,way);
		if(way.sucess) {
			validGetters.put(field,way);
			return value;
		}
		
		way.sucess=false;
		validGetters.put(field,way);

		return false;
	
	}
	
	
	/**
	 * 把Map类型转为指定类型的对象
	 * @param <T> Bean类型
	 * @param map Map对象
	 * @param type 转换类型
	 * @return  转换后的对象
	 * */
	public static <T> T toJavaObject(JSONObject json, Class<T> type) {
		json=fixData(json, type);
		try {
			return JSON.parseObject(json.toJSONString(), type);
		} catch (Exception e) {
			try {
				T bean=(T)type.newInstance();
				for (Entry<String,Object> me : json.entrySet()) {
					setFieldValue(bean, me.getKey(), me.getValue());
				}
				return bean;
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		}
	}
	
	/**
	 * 修复一些不是JSON格式，但又符合常规使用习惯的数据
	 * @param 是否继续深入到下一层
	 * */
	private static <T> JSONObject fixData(JSONObject json, Class<T> type) {
		
		Class fieldType=null;
		String clsName=null;
		Field field=null;
		List<String> fieldNames=getAllFields(type);
		for (String fieldName : fieldNames) {
			field=getField(type, fieldName);
			fieldType= field.getType();
			clsName=fieldType.getName();
 
    		String value=json.getString(fieldName);
    		if(fieldType.isArray()) {
    			try {
    				json.put(fieldName,JSONArray.parseArray(value));
				} catch (Exception e) {
					json.put(fieldName,DataParser.parseArray(fieldType, value));
				}
    		}
    		else if(Map.class.isAssignableFrom(fieldType)) {
    			try {
    				json.put(fieldName,JSONObject.parseObject(value));
				} catch (Exception e) {
					json.put(fieldName,DataParser.parseMap(value));
				}
    		}
    		else if(Collection.class.isAssignableFrom(fieldType)) {
    			try {
    				json.put(fieldName,JSONArray.parseArray(value));
				} catch (Exception e) {
					json.put(fieldName,DataParser.parseList(field, value));
				}
    		} else if(DataParser.isSimpleType(fieldType)) {
    			if(Date.class.isAssignableFrom(fieldType)) {
    				json.put(fieldName,DataParser.parseDate(value));
    			} else if(Timestamp.class.isAssignableFrom(fieldType)) {
    				json.put(fieldName,DataParser.parseTimestamp(value));
    			} else {
    				json.put(fieldName,value);
    			}
    		} else {
    			try {
    				json.put(fieldName,JSONObject.parseObject(value));
				} catch (Exception e) {
					json.put(fieldName,DataParser.parseMap(value));
				}
    		}
		}
		return json;
	}
	
	 
	/**
	 * 把Map类型转为指定类型的对象
	 * @param <T> Bean类型
	 * @param map Map对象
	 * @param type 转换类型
	 * @return  转换后的对象
	 * */
	public static <T> T toJavaObject(Map map, Class<T> type) {
		
		T inst = create(type);
		copy(map,inst);
		return inst;
		
	}
	
	private static boolean setValueWithField(Class<?> type, Object inst, String fieldName, Object value,ValidWay setter) {
		setter.isTargetExists=false;
		Field f=getField(type, fieldName);
		if(f==null)
		{
			f=getBlurField(type, fieldName);
		}
		if(f==null) {
			return false;
		}
		if(f!=null) {
			setter.isTargetExists=true;
		}
		Class<?> requireType=f.getType();
		value=DataParser.parse(requireType, value);
		
		if(requireType.isPrimitive() && value==null) {
			value=getPrimitiveDefaultValue(requireType); 
		}
		
		try {
			f.set(inst, value);
			setter.type=1;
			setter.name=fieldName;
			setter.field=f;
			setter.requireType=requireType;
			setter.sucess=true;
			return true;
		} catch (Exception e1) {}
		return false;
	}
	
	private static Object getValueWithField(Class<?> type, Object inst, String fieldName, ValidWay getter) {
		getter.isTargetExists=false;
		Field f=getField(type, fieldName);
		if(f==null)
		{
			f=getBlurField(type, fieldName);
		}
		if(f==null) {
			return null;
		}
		Class<?> requireType=f.getType();
		Object value = null;
		getter.isTargetExists=true;
		try {
			value=f.get(inst);
			getter.type=1;
			getter.name=fieldName;
			getter.field=f;
			getter.requireType=requireType;
			getter.sucess=true;
			return value;
		} catch (Exception e1) {}
		return false;
	}
	
	private static Object getPrimitiveDefaultValue(Class<?> type)
	{
		if(byte.class.equals(type) || short.class.equals(type) || int.class.equals(type) || long.class.equals(type)) {
			return 0;
		}
		else if(float.class.equals(type)) {
			return 0.0f;
		}
		else if(double.class.equals(type)) {
			return 0.0d;
		}
		else if(boolean.class.equals(type)) {
			return false;
		}
		else if(char.class.equals(type)) {
			return '\u0000';
		}
		return null;
	}

	private static  boolean setValueWithMethod(Class<?> type, Object inst, String setMethodName, Object value,ValidWay setter) {
		setter.isTargetExists=false;
		Class requireType=getUniqueMethodType(type, setMethodName,true);
		if(requireType==null)
		{
			if(type.getSuperclass()!=null)
			{
				return setValueWithMethod(type.getSuperclass(), inst, setMethodName, value,setter);
			}
			else
			{
				return false;
			}
		}
		MethodAccess ma = getMethodAccess(type);
		if(ma!=null) {
			setter.isTargetExists=true;
		}
		if(requireType.isPrimitive() && value==null) {
			value=getPrimitiveDefaultValue(requireType); 
		}
		try {
			if(List.class.isAssignableFrom(requireType)) {
				
				Method m=type.getDeclaredMethod(setMethodName, requireType);
				value=DataParser.parseList(m.getParameters()[0],value);
				
			} else {
				value=DataParser.parse(requireType, value);
			}
			ma.invoke(inst, setMethodName, value);
			setter.type=0;
			setter.name=setMethodName;
			setter.methodAccess=ma;
			setter.requireType=requireType;
			setter.sucess=true;
			return true;
		} catch (Exception e) {}
		return false;
	}
	
	
	private static  Object getValueWithMethod(Class<?> type, Object inst, String getMethodName, ValidWay getter) {
		getter.isTargetExists=false;
		Class requireType=getUniqueMethodType(type, getMethodName,false);
		if(requireType==null) {
			if(type.getSuperclass()!=null) {
				return getValueWithMethod(type.getSuperclass(), inst, getMethodName,getter);
			}
			else {
				return null;
			}
		}
		Object value = null;
		MethodAccess ma = getMethodAccess(type);
		getter.isTargetExists=true;
		try {
			value=ma.invoke(inst, getMethodName);
			getter.type=0;
			getter.name=getMethodName;
			getter.methodAccess=ma;
			getter.requireType=requireType;
			getter.sucess=true;
			return value;
		} catch (Exception e) {}
		return null;
	}
	
	/**
	 * 创建分组key
	 * @param bean Bean对象
	 * @param fields 属性
	 * @return 分组key
	 * */
	public static String makeKey(Object bean,String[] fields)
	{
		StringBuilder key=new StringBuilder();
		String subkey=null;
		for (String field : fields) {
			subkey=DataParser.parseString(getFieldValue(bean, field));
			if(key.length()==0) {
				key.append(subkey);
			} else {
				key.append("_"+subkey);
			}
		}
		return key.toString();
	}
 
	/**
	 * 按某个键值把 List 转 map，注意键值是否重复
	 * @param <K> 键类型
	 * @param <T> 值类型
	 * @param list 对象列表
	 * @param field 目标属性
	 * @return 以field指定属性值为key的Bean Map
	 * */
	@SuppressWarnings("unchecked")
	public  static <K,T> Map<K,T> toMap(Collection<T> list,String field)
	{
		HashMap<K, T> map=new HashMap<K, T>(5);
		K key=null;
		for (T bean : list) {
			key=(K)getFieldValue(bean, field);
			map.put(key, bean);
		}
		return map;
	}
	
	/**
	 * 按某个键值把 List 转 map
	 * @param <T> 值类型
	 * @param list Bean清单
	 * @param field 属性列表
	 * @return 以field指定属性值为key的Bean Map
	 * */
	public  static <T> Map<String,T> toMap(Collection<T> list,String... field)
	{
		HashMap<String, T> map=new HashMap<String, T>(5);
		String key=null;
		for (T bean : list) {
			key=makeKey(bean, field);
			map.put(key, bean);
		}
		return map;
	}
	
	
	/**
	 * 按某个键值把 List 转 map
	 * @param <T> 值类型
	 * @param list Bean清单
	 * @param keyField 键属性
	 * @param keyType 键类型
	 * @param valueField 值属性
	 * @param valueType 值类型
	 * @return 以keyField指定属性值为key，以valueField指定属性值为value 的Map对象
	 * */
	public  static <K,V> Map<K,V> toMap(Collection list,String keyField,Class<K> keyType,String valueField,Class<V> valueType)
	{
		HashMap<K,V> map=new HashMap<K,V>(5);
		K key=null;
		V value=null;
		for (Object bean : list) {
			 key=DataParser.parse(keyType,getFieldValue(bean, keyField));
			 value=DataParser.parse(valueType,getFieldValue(bean, valueField));
			 map.put(key, value);
		}
		return map;
	}
	
	
	
	/**
	 * 过滤，保留与value值相等的元素
	 * @param <T> Bean类型
	 * @param list Bean清单
	 * @param field 属性名
	 * @param value 值
	 * @return 过滤结果
	 * */
	public static <T> List<T> filter(Collection<T> list , String field, Object value) {
		return filter(list, field, value,FilterOperator.EQUALS);
	}
	
	/**
	 * 过滤，并指定比较器
	 * @param <T> Bean类型
	 * @param	list Bean清单
	 * @param 	field 属性名
	 * @param 	value 值
 	 * @param  filterOperator  比较器，可以直接从 FilterOperator 定义的常量获得
	 * @return 过滤结果
	 * */
	public static <T> List<T> filter(Collection<T> list , String field, Object value,FilterOperator filterOperator) {
		Object tmp=null;
		List<T> result=new ArrayList<T>();
		for (T t : list) {
			tmp=getFieldValue(t,field);
			if(filterOperator.compare(tmp,value)) {
				result.add(t);
			}
		}
		return result;
	}
	
	/**
	 * 排序
	 * @param <T> Bean类型
	 * @param list Bean清单
	 * @param field 属性
	 * @param ascending  是否升序
 	 * @param  nullslast  null值是否排最后
 	 * @return 	排序结果
	 * */
	public static <T> List<T> sort(Collection<T> list,final String field, final boolean ascending, final boolean nullslast) {
		List<T> nulls=filter(list, field, null);
		List<T> notNulls=filter(list, field, null,FilterOperator.EQUALS.reverse());
		Collections.sort(notNulls, new PropertyComparator(field,ascending));
		if(nullslast) {
			notNulls.addAll(nulls);
		}
		else {
			notNulls.addAll(0, nulls);
		}
		return notNulls;
	}
	
	/**
	 * 把记录集转换成Map形式 如果单个字段，使用原始值作为键；如果是多字段，则用它们的值下划线连接
	 * @param <T> Bean类型
	 * @param <K> 键类型
	 * @param list Bean清单
	 * @param field 属性
	 * @return 分组结果
	 */
	@SuppressWarnings("unchecked")
	public static <T,K> Map<K, List<T>> groupAsMap(Collection<T> list,String field) {
		HashMap<K, List<T>> map = new HashMap<K, List<T>>(5);
		List<T> sublist = null;
		K key=null;
		for (T bean : list) {
			key=(K)getFieldValue(bean, field);
			sublist = map.get(key);
			if (sublist == null) {
				sublist = new ArrayList<T>();
				map.put(key, sublist);
			}
			sublist.add(bean);
		}
		return map;
	}
	
	/**
	 * 把记录集转换成Map形式 如果单个字段，使用原始值作为键；如果是多字段，则用它们的值下划线连接
	 * @param <T> Bean类型
	 * @param list Bean清单
	 * @param field 属性
	 * @return 分组结果
	 */
	public static <T> Map<String, List<T>> groupAsMap(Collection<T> list,String... field) {
		HashMap<String, List<T>> map = new HashMap<String, List<T>>(5);
		List<T> sublist = null;
		String key=null;
		for (T bean : list) {
			key=makeKey(bean, field);
			sublist = map.get(key);
			if (sublist == null) {
				sublist = new ArrayList<T>();
				map.put(key, sublist);
			}
			sublist.add(bean);
		}
		return map;
	}
	
	/**
	 * 清除所有属性值，将所有属性值，置为 null
	 * @param bean 对象
	 * */
	public static void clearValues(Object bean)
	{
		if(bean==null) {
			return;
		}
		List<String> flds=getAllFields(bean.getClass());
		for (String f : flds) {
			setFieldValue(bean, f, null);
		}
	}
	
	/**
	 * 比较两个实体是否具有相同的属性值,返回差异
	 * @param <T> Bean类型
	 * @param o1 对象1
	 * @param o2 对象2
	 * @return 返回差异
	 * */
	public static <T> Map<String,Object[]> compare(T o1,T o2)
	{
		//TODO 待实现
		throw new RuntimeException("待实现");
	}
	
	/**
	 * 把 Java Bean 转成 map，以属性名作为key ,仅 1 层
	 * @param bean  Java Bean
	 * @return Map
	 * */
	public static Map<String,Object> toMap(Object bean)
	{
		if(bean==null) {
			return null;
		}
		Map<String,Object> map=new HashMap<String, Object>(5);
		List<String> fields=BeanUtil.getAllFields(bean.getClass());
		for (String field : fields) {
			map.put(field,getFieldValue(bean, field));
		}
		return map;
	}
	
	public static JSONObject toJSONObject (Object bean) {
		return JSONObject.parseObject(JSON.toJSONString(bean));
	}
	
	
 
 
}
