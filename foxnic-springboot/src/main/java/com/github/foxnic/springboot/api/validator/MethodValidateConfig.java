package com.github.foxnic.springboot.api.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.foxnic.commons.bean.BeanUtil;

public class MethodValidateConfig {

	private Method method=null;
	
	private Map<String,List<Object>> validators=new HashMap<>();
	
	public MethodValidateConfig(Method method) {
		this.method=method;
	}

	public void addFieldValidator(Annotation validateAnn) {
		
		String[] names=BeanUtil.getFieldValue(validateAnn, "name", String[].class);
		for (String name : names) {
			List<Object> list=validators.get(name);
			if(list==null) {
				list=new ArrayList<Object>();
				validators.put(name, list);
			}
			list.add(validateAnn);
		}
	}

	public Method getMethod() {
		return method;
	}

	public List<Object> getValidators(String paramName) {
		return validators.get(paramName);
	}

}
