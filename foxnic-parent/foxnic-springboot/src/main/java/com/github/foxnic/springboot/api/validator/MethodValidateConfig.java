package com.github.foxnic.springboot.api.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.foxnic.commons.bean.BeanUtil;

import io.swagger.annotations.ApiImplicitParam;

public class MethodValidateConfig {

	private Method method=null;
	
	private Map<String,List<ValidateAnnotation>> validators=new HashMap<>();
	private Map<String,String> labels=new HashMap<>();
	private Map<String,ApiImplicitParam> params=new HashMap<>();
	
	public MethodValidateConfig(Method method) {
		this.method=method;
	}

	public void addFieldValidator(ValidateAnnotation validateAnn) {
		
		String[] names=validateAnn.getNames();
		for (String name : names) {
			
			if(!params.isEmpty() && !params.containsKey(name)) {
				throw new RuntimeException( this.method.getDeclaringClass().getName()+"."+this.method.getName()+"(..) , 校验参数 "+name+" 未定义");
			}
			
			List<ValidateAnnotation> list=validators.get(name);
			if(list==null) {
				list=new ArrayList<ValidateAnnotation>();
				validators.put(name, list);
			}
			list.add(validateAnn);
		}
	}

	public Method getMethod() {
		return method;
	}

	public List<ValidateAnnotation> getValidators(String paramName) {
		return validators.get(paramName);
	}
	
	/**
	 * 有校验器的参数名称集合
	 * */
	public Set<String> getParamNames() {
		return validators.keySet();
	}

	public void addApiImplicitParam(ApiImplicitParam ap) {
		if(params.containsKey(ap.name())) {
			throw new RuntimeException(this.method.getDeclaringClass().getName()+"."+this.method.getName()+"(..) 参数 "+ap.name()+" 的  ApiImplicitParam 重复");
		}
		params.put(ap.name(), ap);
	}
	
	public ApiImplicitParam getApiImplicitParam(String paramName) {
		return params.get(paramName);
	}

}
