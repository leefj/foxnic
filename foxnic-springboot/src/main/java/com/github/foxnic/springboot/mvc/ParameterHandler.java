package com.github.foxnic.springboot.mvc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.springboot.api.validator.ParameterValidateManager;

import io.swagger.annotations.ApiImplicitParam;

@Component
public class ParameterHandler {
	
//	private static Set<Class> SIMPLE_TYPES=new HashSet<>();
//	static {
//		SIMPLE_TYPES.addAll(Arrays.asList(Byte.class,byte.class,Short.class,short.class,Integer.class,int.class,Long.class,long.class));
//		SIMPLE_TYPES.addAll(Arrays.asList(Boolean.class,boolean.class));
//		SIMPLE_TYPES.addAll(Arrays.asList(Float.class,float.class,Double.class,double.class));
//		SIMPLE_TYPES.addAll(Arrays.asList(BigInteger.class,BigDecimal.class));
//		SIMPLE_TYPES.addAll(Arrays.asList(java.sql.Date.class,java.util.Date.class,java.sql.Timestamp.class,java.sql.Time.class));
//		SIMPLE_TYPES.addAll(Arrays.asList(java.time.LocalDate.class,java.time.LocalDateTime.class,java.time.LocalTime.class));
//		SIMPLE_TYPES.addAll(Arrays.asList(String.class,StringBuffer.class,StringBuffer.class));
//	}
	
	
	

	@Autowired
	private ParameterValidateManager parameterValidateManager;

	private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

	/**
	 * 处理的入口
	 * */
	public Object[] process(Method method, RequestParameter requestParameter, Object[] args) {

		Parameter[] params = method.getParameters();
		String[] paramNames = parameterNameDiscoverer.getParameterNames(method);

		Parameter param = null;
		String paramName = null;

		//逐个处理方法参数
		for (int i = 0; i < args.length; i++) {
			param = params[i];
			paramName = paramNames[i];
			ApiImplicitParam ap = parameterValidateManager.getApiImplicitParam(method, paramName);
			Object requestValue = getRequestParameterValue(paramName, ap, requestParameter);
			args[i] = processMethodParameter(requestParameter, method,param, args[i], requestValue);
		}
		return args;
	}

	/**
	 * 处理单个方法参数
	 * */
	private Object processMethodParameter(RequestParameter requestParameter, Method method,Parameter param, Object value, Object requestValue) {
		
		if(this.isSimpleType(param)) {
			return processSimpleParameter(param,requestValue,value);
		} else if(param.getType().isArray()) {
			return processArrayParameter(param,requestValue,value);
		} else if(ReflectUtil.isSubType(List.class, param.getType())) {
			return processListParameter(param,requestValue,value);
		} else  if(ReflectUtil.isSubType(Map.class, param.getType())) {
			return processMapParameter(param,requestValue,value);
		} else  if(ReflectUtil.isSubType(Set.class, param.getType())) {
			return processSetParameter(param,requestValue,value);
		} else {
			return processPojoParameter(requestParameter, method,param, value);
		}
		
	}

	private Object processPListParameter(Parameter param, Object requestValue, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 处理Pojo类型的参数
	 * */
	private Object processPojoParameter(RequestParameter requestParameter,Method method, Parameter param, Object value) {
		//如果是实体类型，则创建代理对象
		Object pojo=value;
		boolean isEntity=isEntity(param);
		boolean isManaged=EntityContext.isManaged(value);
		if(isEntity && !isManaged) {
			pojo = EntityContext.create((Class<Entity>) value.getClass());
		} else {
			pojo = value;
		}
 
		Map<String,String> header=requestParameter.getHeader();
	 
		//处理从 header 读取
		for (Map.Entry<String, String> e : header.entrySet()) {
			ApiImplicitParam ap = parameterValidateManager.getApiImplicitParam(method, e.getKey());
			if(ap!=null && "header".equals(ap.type())) {
				Object pv=getPojoPropertyValue(value, e.getKey());
				//如果属性没有值，则尝试设置值
				if(pv==null) {
					setPojoProperty(pojo,e.getKey(),e.getValue());
				}
			}
		}
		
		//处理从 body 或从请求参数读取
		for (Map.Entry<String, Object> e : requestParameter.entrySet()) {
			Object pv=getPojoPropertyValue(value, e.getKey());
			if(pv==null) {
				setPojoProperty(pojo,e.getKey(),e.getValue());
			} else {
				if(isEntity) {
					BeanUtil.setFieldValue(pojo, e.getKey(), pv);
				}
			}
		}
 
		if(isEntity) {
			((Entity) value).clearModifies();
		}
		
		return pojo;
	}
	
	/**
	 * 设置 Pojo 属性
	 * */
	private void setPojoProperty(Object pojo, String prop, Object value) {
		Field f = ReflectUtil.getField(pojo.getClass(), prop);
		if(f==null) return;
		if(this.isSimpleType(f)) {
			BeanUtil.setFieldValue(pojo, prop, value);
		} else if(f.getType().isArray()) {
			//TODO 待实现
			throw new IllegalArgumentException("待实现 : "+f.getType().getName());
		} else if(ReflectUtil.isSubType(List.class, f.getType())) {
			value = parseList(f,value);
			BeanUtil.setFieldValue(pojo, prop, value);
		} else  if(ReflectUtil.isSubType(Map.class, f.getType())) {
			//TODO 待实现
			throw new IllegalArgumentException("待实现 : "+f.getType().getName());
		} else  if(ReflectUtil.isSubType(Set.class, f.getType())) {
			//TODO 待实现
			throw new IllegalArgumentException("待实现 : "+f.getType().getName());
		} else {
			throw new IllegalArgumentException("类型不支持 : "+f.getType().getName());
		}
	}

	/**
	 * 转换成列表
	 * */
	private Object parseList(Field f,Object value) {
		
		Class cType=ReflectUtil.getListComponentType(f);
		
		List list = DataParser.parseList(f, value);
		 
		if (list == null && value != null) {
			if (value instanceof String) {
				if (!StringUtil.isBlank((String) value)) {
					list = Arrays.asList(value);
				}
			} else {
				list = Arrays.asList(value);
			}
		}
		
		List castedList=null;
		if(f.getType().equals(List.class)) {
			castedList=new ArrayList<>();
		}
		list = buildList(cType, list,castedList);
		return list;
	}

	
	private List buildList(Class cType, List list,List castedList) {
		if(cType!=null && !cType.equals(Object.class)  && list!=null && list instanceof JSONArray) {
			 
			for (int k = 0; k < list.size(); k++) {
				JSONObject item=((JSONArray)list).getJSONObject(k);
				Object e=EntityContext.create(cType, item);
				castedList.add(e);
			}
			list=castedList;
		}
		return list;
	}

	private Object getPojoPropertyValue(Object pojo,String prop) {
		if(pojo==null) return null;
		return BeanUtil.getFieldValue(pojo, prop);
	}
	
	
	private Object processSetParameter(Parameter param, Object requestValue, Object value) {
		throw new IllegalArgumentException("待实现 : processSetParameter");
	}

	private Object processMapParameter(Parameter param, Object requestValue, Object value) {
		throw new IllegalArgumentException("待实现 : processMapParameter");
	}

	
	private Object processListParameter(Parameter param, Object requestValue, Object value) {
		
		final Type type = param.getParameterizedType();
		String typename = type.getTypeName();
		int i = typename.indexOf("<");
		int j = typename.indexOf(">", i);
		typename = typename.substring(i + 1, j);
		Class cType = ReflectUtil.forName(typename);
		List list = null;
		if(requestValue instanceof List) {
			list = buildList(cType, (List)requestValue,(List)value);
		}
		return list;
		
	}

	private Object processArrayParameter(Parameter param, Object requestValue, Object value) {
		throw new IllegalArgumentException("待实现 : processArrayParameter");
	}

	private Object processSimpleParameter(Parameter param, Object requestValue, Object value) {
		if(value == null) {
			value = DataParser.parse(param.getType(), requestValue);
		}
		return value;
	}

	public boolean isSimpleType(Parameter param) {
		return BeanUtils.isSimpleValueType(param.getType());
//		return SIMPLE_TYPES.contains(param.getType());
	}
	
	public boolean isSimpleType(Field field) {
		return BeanUtils.isSimpleValueType(field.getType());
//		return SIMPLE_TYPES.contains(field.getType());
	}
	
	private boolean isEntity(Parameter param) {
		return ReflectUtil.isSubType(Entity.class, param.getType());
	}

	private Object getRequestParameterValue(String paramName, ApiImplicitParam ap, RequestParameter requestParameter) {
		if (ap == null || !"header".equals(ap.paramType())) {
			return requestParameter.get(paramName);
		} else {
			return requestParameter.getHeader().get(paramName);
		}
	}

}
