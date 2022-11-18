package com.github.foxnic.springboot.mvc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.collection.CollectorUtil;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.springboot.web.WebContext;
import io.swagger.annotations.ApiImplicitParam;
import org.springframework.beans.BeanUtils;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import java.lang.reflect.*;
import java.util.*;

@Component
public class ParameterHandler {


	private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

	/**
	 * 处理的入口
	 * */
	public Object[] process(Method method, RequestParameter requestParameter, Object[] args) {

		Parameter[] params = method.getParameters();
		String[] paramNames = parameterNameDiscoverer.getParameterNames(method);

		Parameter param = null;
		String paramName = null;
		boolean single=args.length==1;
		//逐个处理方法参数
		for (int i = 0; i < args.length; i++) {
			param = params[i];
			paramName = paramNames[i];
			ApiImplicitParam ap = WebContext.get().getApiImplicitParam(method, paramName);
			Object requestValue = getRequestParameterValue(paramName, ap, requestParameter);
			args[i] = processMethodParameter(requestParameter, method,param, args[i], requestValue,single);

			//Feign
			if(args[i]==null && method.getParameterCount()==1 && !StringUtil.isBlank(requestParameter.getRequestBody())) {
				args[i] = requestParameter.getRequestBody();
			}
			//标记脏
			if(args[i] instanceof  Entity) {
				Entity entity=(Entity)args[i];
				List<String> dirtys=BeanUtil.getFieldValue(entity,"dirtyFields",List.class);
				if(dirtys!=null && !dirtys.isEmpty()) {
					entity.flagDirty(dirtys.toArray(new String[0]));
				}
				//System.out.println();
			}
		}
		return args;
	}

	/**
	 * 处理单个方法参数
	 * */
	private Object processMethodParameter(RequestParameter requestParameter, Method method,Parameter param, Object value, Object requestValue,boolean single) {

		if(this.isSimpleType(param)) {
			return processSimpleParameter(param,requestValue,value);
		} else if(param.getType().isArray()) {
			return processArrayParameter(param,requestValue,value);
		} else if(ReflectUtil.isSubType(JSONArray.class, param.getType())) {
			return processJSONArrayParameter(requestParameter,param,requestValue,value);
		} else if(ReflectUtil.isSubType(List.class, param.getType())) {
			return processListParameter(requestParameter,param,requestValue,value);
		} else  if(ReflectUtil.isSubType(JSONObject.class, param.getType())) {
			return processJSONObjectParameter(param,requestValue,value);
		} else  if(ReflectUtil.isSubType(Map.class, param.getType())) {
			return processMapParameter(requestParameter,param,requestValue,value);
		} else  if(ReflectUtil.isSubType(Set.class, param.getType())) {
			return processSetParameter(param,requestValue,value);
		} else {
			return processPojoParameter(requestParameter, method,param, value,single);
		}

	}

	private Object processPListParameter(Parameter param, Object requestValue, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 处理Pojo类型的参数
	 * */
	private Object processPojoParameter(RequestParameter requestParameter,Method method, Parameter param, Object value,boolean single) {


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
			ApiImplicitParam ap = WebContext.get().getApiImplicitParam(method, e.getKey());
			if(ap!=null && "header".equals(ap.type())) {
				Object pv=getPojoPropertyValue(value, e.getKey());
				//如果属性没有值，则尝试设置值
				if(pv==null) {
					setPojoProperty(pojo,e.getKey(),e.getValue());
				}
			}
		}

		Map<String,Object> data= new HashMap<>();
		data.putAll(requestParameter);
		if(!single) {
			// 用独立的参数做一次覆盖
			Map<String,Object> namedData=(Map<String,Object>)requestParameter.get(param.getName());
			if(namedData!=null) {
				data.putAll(namedData);
			}
		}
		if(data==null) data=new HashMap<>();

		List<Field> fields=BeanUtil.getAllFields(pojo.getClass());
		Map<String,Field> fieldMap= CollectorUtil.collectMap(fields,Field::getName,(f)->{return f;});
		Field field = null;
		//处理从 body 或从请求参数读取
		for (Map.Entry<String, Object> e : data.entrySet()) {
			field = fieldMap.get(e.getKey());
			if(field==null) {
				continue;
			}
			if(field.getType().isArray()) {
				throw new IllegalArgumentException("not support type");
			} else if(ReflectUtil.isSubType(JSONArray.class,field.getType())) {
				fillPojoJSONArrayProperty(pojo,field,e.getValue());
			} else if(ReflectUtil.isSubType(List.class,field.getType())) {
				fillPojoListProperty(pojo,field,e.getValue());
			} else if(ReflectUtil.isSubType(JSONObject.class,field.getType())) {
				fillPojoJSONObjectProperty(pojo,field,e.getValue());
			} else if(ReflectUtil.isSubType(Map.class,field.getType())) {
				fillPojoMapProperty(pojo,field,e.getValue());
			} else if(ReflectUtil.isSubType(Set.class,field.getType())) {
				throw new IllegalArgumentException("not support type");
			} else if(ReflectUtil.isSubType(Enum.class,field.getType())) {
				fillPojoEnumProperty(pojo,field,e.getValue());
			}else if (DataParser.isSimpleType(field.getType())){

				Object pv=getPojoPropertyValue(value, e.getKey());
				if(pv==null) {
					setPojoProperty(pojo,e.getKey(),e.getValue());
				} else {
					if(isEntity) {
						BeanUtil.setFieldValue(pojo, e.getKey(), pv);
					}
				}

			} else {
				if(e.getValue() instanceof Map) {
					fillPojoBeanProperty(pojo,field,e.getValue());
				} else {
					throw new IllegalArgumentException("not support type");
				}
			}


		}

		if(isEntity) {
			((Entity) value).clearModifies();
		}

		return pojo;
	}

	private void fillBeanProperties(Object bean, Map<String,Object> map) {

		boolean isEntity=EntityContext.isEntityType(bean.getClass());
		boolean isManaged=EntityContext.isManaged(bean);

		List<Field> fields=BeanUtil.getAllFields(bean.getClass());
		Map<String,Field> fieldMap= CollectorUtil.collectMap(fields,Field::getName,(f)->{return f;});
		Field field = null;
		for (Map.Entry<String, Object> e : map.entrySet()) {
			field=fieldMap.get(e.getKey());
			if(field==null) {
				continue;
			}
			if(field.getType().isArray()) {
				throw new IllegalArgumentException("not support type");
			} else if(ReflectUtil.isSubType(List.class,field.getType())) {
				fillPojoListProperty(bean,field,e.getValue());
			} else if(ReflectUtil.isSubType(Map.class,field.getType())) {
				fillPojoMapProperty(bean,field,e.getValue());
			} else if(ReflectUtil.isSubType(Set.class,field.getType())) {
				throw new IllegalArgumentException("not support type");
			} else if (DataParser.isSimpleType(field.getType())){

				Object pv=getPojoPropertyValue(bean, e.getKey());
				if(pv==null) {
					setPojoProperty(bean,e.getKey(),e.getValue());
				} else {
					if(isEntity) {
						BeanUtil.setFieldValue(bean, e.getKey(), pv);
					}
				}
			} else if (ReflectUtil.isSubType(Enum.class,field.getType())){
				fillPojoEnumProperty(bean,field,e.getValue());
			}else {
				if(e.getValue() instanceof Map) {
					fillPojoBeanProperty(bean,field,e.getValue());
				} else {

					throw new IllegalArgumentException("not support type");
				}
			}

		}

	}

	private void fillPojoEnumProperty(Object pojo, Field field, Object value) {

		Object enumValue = null;
		if (value instanceof String) {
			if (ReflectUtil.isSubType(CodeTextEnum.class, field.getType())) {
				enumValue = DataParser.parseCodeTextEnum(value.toString(), (Class<? extends CodeTextEnum>) field.getType());
			} else {
				enumValue = DataParser.parseEnum(value, (Class<? extends Enum>) field.getType());
			}
		} else if (value instanceof Enum) {
			enumValue = value;
		} else {
			throw new IllegalArgumentException("not support");
		}

		BeanUtil.setFieldValue(pojo, field.getName(), enumValue);

	}

	private void fillPojoBeanProperty(Object pojo, Field field, Object value) {

		if(value==null) {
			BeanUtil.setFieldValue(pojo,field.getName(),value);
			return;
		}

		JSONObject json= null;
		if(value instanceof JSONObject) {
			json=(JSONObject) value;
		} else if(value instanceof String) {
			try {
				json = JSONObject.parseObject((String) value);
			} catch (Exception e) {
				throw new IllegalArgumentException("not support");
			}
		} else {
			throw new IllegalArgumentException("not support");
		}

		if(json==null) {
			throw new IllegalArgumentException("无法转换为 JSONObject");
		}


		Class beanType=field.getType();
		boolean isEntity=EntityContext.isEntityType(beanType);
		boolean isProxyType=EntityContext.isProxyType(beanType);

		Object bean = null;
		if(isEntity && !isProxyType) {
			bean=EntityContext.create(beanType);
		} else {
			try {
				Constructor constructor = BeanUtils.findPrimaryConstructor(beanType);
				if(constructor==null) {
					if (beanType.getConstructors().length == 1) {
						constructor = beanType.getConstructors()[0];
					} else {
						constructor = beanType.getDeclaredConstructor();
					}
				}

				if (constructor==null || constructor.getParameterCount() != 0) {
					throw new IllegalArgumentException("构造函数不符合要求");
				}

				bean=constructor.newInstance();

			} catch (Exception e) {
				Logger.exception(e);
			}
		}

		if(bean!=null) {
			fillBeanProperties(bean,json);
		}

		//setPojoProperty(pojo,field.getName(),json);
		BeanUtil.setFieldValue(pojo,field.getName(),bean);

	}

	private void fillPojoJSONArrayProperty(Object pojo, Field field, Object value) {
		if(value==null) return;
		if(value instanceof JSONArray) {
			BeanUtil.setFieldValue(pojo, field.getName(),value);
		} else if(value instanceof String) {
			value = JSONArray.parseArray(value.toString());
			BeanUtil.setFieldValue(pojo, field.getName(),value);
		}
	}

	private void fillPojoJSONObjectProperty(Object pojo, Field field, Object value) {
		if(value==null) return;
		if(value instanceof JSONObject) {
			BeanUtil.setFieldValue(pojo, field.getName(),value);
		} else if(value instanceof String) {
			value = JSONObject.parseObject(value.toString());
			BeanUtil.setFieldValue(pojo, field.getName(),value);
		}
	}
	private void fillPojoMapProperty(Object pojo, Field field, Object value) {
		if(value==null) {
			BeanUtil.setFieldValue(pojo, field.getName(),null);
			return;
		}

		ParameterizedType parameterizedType=(ParameterizedType)field.getGenericType();
		Class keyType = (Class) parameterizedType.getActualTypeArguments()[0];
		Class valueType = (Class) parameterizedType.getActualTypeArguments()[1];
		boolean isEntity = EntityContext.isEntityType(valueType);
		boolean isProxyType = EntityContext.isProxyType(valueType);

		if (isEntity && !isProxyType) {
			Map map = null;
			JSONObject jsonObject = null;
			if (value instanceof JSONObject) {
				jsonObject = (JSONObject) value;
			} else if (value instanceof String) {
				jsonObject = JSONObject.parseObject((String) value);
			} else {
				throw new IllegalArgumentException("not support");
			}
			if (jsonObject == null) {
				throw new IllegalArgumentException("not support");
			}
			map = new HashMap();
			Object bean = null;
			for (String key : jsonObject.keySet()) {
				bean = EntityContext.create(valueType);
				fillBeanProperties(bean, jsonObject.getJSONObject(key));
				map.put(key, bean);
			}
			BeanUtil.setFieldValue(pojo, field.getName(),map);
		} else {
			if(value instanceof  Map) {
				BeanUtil.setFieldValue(pojo, field.getName(),value);
				return;
			}
		}
	}

	private void fillPojoListProperty(Object pojo, Field field, Object value) {

		if(value ==null ) {
			BeanUtil.setFieldValue(pojo,field.getName(),value);
			return;
		}
		JSONArray array = null;
		if(value instanceof JSONArray) {
			array=(JSONArray) value;
		} else if(value instanceof String) {
			String str=(String)value;
			str=str.trim();
			if (str.startsWith("[") && str.equals("]")) {
				try {
					array = JSONArray.parseArray(str);
				} catch (Exception e) {
					throw new IllegalArgumentException("格式错误");
				}
			} else {
				String[] arr=str.split(",");
				array=new JSONArray();
				array.addAll(Arrays.asList(arr));
			}



		} else {
			throw new IllegalArgumentException("not support");
		}

		if(array==null) {
			throw new IllegalArgumentException("无法转换为 JSONArray");
		}


		ParameterizedType parameterizedType=(ParameterizedType)field.getGenericType();
		Class cmpType=(Class)parameterizedType.getActualTypeArguments()[0];
		boolean isEntity=EntityContext.isEntityType(cmpType);
		boolean isProxyType=EntityContext.isProxyType(cmpType);


		List list=new ArrayList();
		if(isEntity && !isProxyType) {
			Object bean;
			for (int i = 0; i < array.size(); i++) {
				bean=EntityContext.create(cmpType);
				fillBeanProperties(bean,array.getJSONObject(i));
				list.add(bean);
			}
		} else {
			try {
				Constructor constructor = BeanUtils.findPrimaryConstructor(cmpType);
				if(constructor==null) {
					if (cmpType.getConstructors().length == 1) {
						constructor = cmpType.getConstructors()[0];
					} else {
						constructor = cmpType.getDeclaredConstructor();
					}
				}

				if (constructor==null || constructor.getParameterCount() != 0) {
					throw new IllegalArgumentException("构造函数不符合要求");
				}

				Object bean;
				for (int i = 0; i < array.size(); i++) {
					if(array.get(i) instanceof JSONObject) {
						bean=constructor.newInstance();
						fillBeanProperties(bean, array.getJSONObject(i));
						list.add(bean);
					} else {
						list.add(DataParser.parse(cmpType,array.get(i)));
					}

				}

			} catch (Exception e) {
				Logger.exception(e);
			}
		}

		//
//		setPojoProperty(pojo,field.getName(),list);
		BeanUtil.setFieldValue(pojo,field.getName(),list);

	}

	/**
	 * 设置 Pojo 属性
	 * */
	private void setPojoProperty(Object pojo, String prop, Object value) {
		Field f = ReflectUtil.getField(pojo.getClass(), prop);
		if(f==null) return;
		if(value==null) {
			BeanUtil.setFieldValue(pojo, prop, null);
			return;
		}

		if(this.isSimpleType(f)) {
			value=DataParser.parse(f.getType(),value);
			BeanUtil.setFieldValue(pojo, prop, value);
		} else if(f.getType().isArray()) {
			//TODO 待实现
			throw new IllegalArgumentException("待实现 : "+f.getType().getName());
		} else if(ReflectUtil.isSubType(List.class, f.getType())) {
			value = parseList(f,value);
			BeanUtil.setFieldValue(pojo, prop, value);
		} else  if(ReflectUtil.isSubType(Map.class, f.getType())) {
			if(value!=null && (value instanceof Map)) {
				BeanUtil.setFieldValue(pojo, prop, value);
			} else {
				throw new IllegalArgumentException("待实现 : " + f.getType().getName());
			}
		} else  if(ReflectUtil.isSubType(Set.class, f.getType())) {
			//TODO 待实现
			throw new IllegalArgumentException("待实现 : "+f.getType().getName());
		} else {
			if(value instanceof String) {
				try {
					JSONObject json=JSONObject.parseObject((String) value);
					Object bean=JSONObject.toJavaObject(json,f.getType());
					BeanUtil.setFieldValue(pojo, prop, bean);
				} catch (Exception e) {
					throw new IllegalArgumentException("无法将 String 转换成 " + f.getType().getName()+", 并设置到 "+pojo.getClass().getSimpleName()+"."+prop,e);
				}
			} else if(ReflectUtil.isSubType(f.getType(),value.getClass())) {
				// 如果是子类，直接赋值
				BeanUtil.setFieldValue(pojo, prop, value);
			} else if(value instanceof JSONObject) {
				// 如果是 JSONObject，转换
				//BeanUtil.setFieldValue(pojo, prop, JSONUtil.toJavaBean((JSONObject)value,f.getType()));
				fillPojoBeanProperty(pojo,f,(JSONObject)value);
			}
			else if(value instanceof Entity) {
				// 如果是 Entity，转换
				BeanUtil.setFieldValue(pojo, prop, value);
			}
			else {
				throw new IllegalArgumentException("类型不支持 : " + f.getType().getName());
			}
		}
	}

	/**
	 * 转换成列表
	 * */
	private Object parseList(Field f,Object value) {

		Class cType=ReflectUtil.getListComponentType(f);

		List list=null;
		try {
			list = DataParser.parseList(f, value);
		} catch (Exception e) {
			Logger.exception("数据转换失败",e);
		}

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
			castedList=new ArrayList();
		}
		list = buildList(cType, list,castedList);
		return list;
	}


	private List buildList(Class cType, List list,List castedList) {
		if(cType!=null && !cType.equals(Object.class)  && list!=null && list instanceof JSONArray) {
			Object e=null;
			for (int k = 0; k < list.size(); k++) {
				if(EntityContext.isEntityType(cType) && !EntityContext.isProxyType(cType)) {
					JSONObject item=((JSONArray)list).getJSONObject(k);
					e=EntityContext.create(cType, item);
				} else {
					e=DataParser.parse(cType, list.get(k));
				}
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

	private Object processJSONObjectParameter(Parameter param, Object requestValue, Object value) {
		if(requestValue instanceof JSONObject) {
			return requestValue;
		} else if(requestValue instanceof String) {
			JSONObject json=JSONObject.parseObject((String) requestValue);
			return json;
		} else {
			throw new IllegalArgumentException("待实现 : processMapParameter");
		}

	}

	private Object processMapParameter(RequestParameter requestParameter, Parameter param, Object requestValue, Object value) {

		if(requestValue==null) return null;

		if (requestValue instanceof Map) {
			ParameterizedType parameterizedType = (ParameterizedType) param.getParameterizedType();
			Class keyType = (Class) parameterizedType.getActualTypeArguments()[0];
			Class valueType = (Class) parameterizedType.getActualTypeArguments()[1];
			boolean isEntity = EntityContext.isEntityType(valueType);
			boolean isProxyType = EntityContext.isProxyType(valueType);
			if (isEntity && !isProxyType) {
				JSONObject jsonObject = null;
				if (requestValue instanceof JSONObject) {
					jsonObject = (JSONObject) requestValue;
				} else if (requestValue instanceof String) {
					jsonObject = JSONObject.parseObject((String) requestValue);
				} else {
					throw new IllegalArgumentException("not support");
				}

				if (jsonObject == null) {
					throw new IllegalArgumentException("not support");
				}
				Map map = new HashMap();
				Object bean = null;
				for (String key : jsonObject.keySet()) {
					bean = EntityContext.create(valueType);
					fillBeanProperties(bean, jsonObject.getJSONObject(key));
					map.put(key, bean);
				}
				return map;
			} else {
				return requestValue;
			}
		} else if (requestValue instanceof String) {
			JSONObject json = JSONObject.parseObject((String) requestValue);
			return json;
		} else {
			throw new IllegalArgumentException("待实现 : processMapParameter");
		}

	}

	private Object processJSONArrayParameter(RequestParameter requestParameter,Parameter param, Object requestValue, Object value) {
		if(requestValue instanceof JSONArray) {
			return (JSONArray) requestValue;
		} else {
			String requestStringValue=requestParameter.getString(param.getName());
			try {
				return JSONArray.parseArray(requestStringValue);
			} catch (Exception e) {
				return null;
			}
		}
	}


	private Object processListParameter(RequestParameter requestParameter,Parameter param, Object requestValue, Object value) {

		final Type type = param.getParameterizedType();
		String typename = type.getTypeName();
		int i = typename.indexOf("<");
		int j = typename.indexOf(">", i);
		typename = typename.substring(i + 1, j);
		Class cType = ReflectUtil.forName(typename);
		List list = null;
		if(requestValue instanceof List) {
			list = buildList(cType, (List)requestValue,(List)value);
		} else if(requestValue instanceof CharSequence) {
			Object[] array=ArrayUtil.createArray(cType,0);
			array=DataParser.parseArray(array.getClass(), requestValue);
			list=Arrays.asList(array);
		} else {
			JSONArray jsonArray=null;
			try {
				jsonArray=JSONArray.parseArray(requestParameter.getRequestBody());
				list=new ArrayList();
				Object val =null;
				for (int k = 0; k < jsonArray.size(); k++) {
					Object v=jsonArray.get(k);
					if(v==null) {
						list.add(v);
						continue;
					}
					if(ReflectUtil.isSubType(cType,v.getClass())) {
						val=v;
					} else {
						JSONObject itm = jsonArray.getJSONObject(k);
						val = itm.toJavaObject(cType);
					}
					list.add(val);
				}
			} catch (Exception e) {
				return list;
			}
		}
		return list;

	}

	private Object processArrayParameter(Parameter param, Object requestValue, Object value) {
		JSONArray sourceArray = null;
		Integer size=null;
		//识别源数据
		if(requestValue instanceof JSONArray) {
			sourceArray=(JSONArray)requestValue;
			size=sourceArray.size();
		} else {
			throw new IllegalArgumentException("不支持的源数据类型");
		}
		Class cmpType=param.getType().getComponentType();
		boolean isEntity=EntityContext.isEntityType(cmpType);
		boolean isProxyType=EntityContext.isProxyType(cmpType);
		Object[] array = null;
		if(isEntity && !isProxyType) {
			cmpType = EntityContext.getProxyType((Class<Entity>) cmpType);
			array=ArrayUtil.createArray(param.getType().getComponentType(),size);
			JSONArray jsonArray=null;
			if(requestValue instanceof JSONArray) {
				jsonArray=(JSONArray) requestValue;
			} else if(requestValue instanceof String) {
				try {
					jsonArray=JSONArray.parseArray((String) requestValue);
				} catch (Exception e) {
					Logger.exception(e);
				}
			} else {
				throw new IllegalArgumentException("not support");
			}

			if(jsonArray==null) {
				throw new IllegalArgumentException("not support");
			}

			Object bean = null;
			for (int i = 0; i < jsonArray.size(); i++) {
				bean = EntityContext.create(cmpType);
				fillBeanProperties(bean,jsonArray.getJSONObject(i));
				array[i]=bean;
			}

		} else {
			array=ArrayUtil.createArray(param.getType().getComponentType(),size);

			// 设置数组值
			if(sourceArray!=null) {
				for (int i = 0; i < sourceArray.size(); i++) {
					array[i]=sourceArray.get(i);
				}
			} else {
				throw new IllegalArgumentException("不支持的源数据类型");
			}
		}

		return array;
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

	/**
	 * 从请求中获取数据
	 * */
	private Object getRequestParameterValue(String paramName, ApiImplicitParam ap, RequestParameter requestParameter) {
		if (ap == null || !"header".equals(ap.paramType())) {
			return requestParameter.get(paramName);
		} else {
			return requestParameter.getHeader().get(paramName);
		}
	}

}
