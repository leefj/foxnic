package com.github.foxnic.springboot.api.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.github.foxnic.springboot.api.annotations.NotBlank;
import com.github.foxnic.springboot.api.annotations.NotEmpty;
import com.github.foxnic.springboot.api.annotations.NotNull;

@Component
public class ParameterValidateManager {
	
	public static final Class[] VALIDATE_TYPES= {NotNull.class,NotBlank.class,NotEmpty.class};

	private Map<Method,MethodValidateConfig> validators=new HashMap<>();
	
	private void addMethodValidator(Method method, Annotation validateAnn) {
		MethodValidateConfig methodValidator=validators.get(method);
		if(methodValidator==null) {
			methodValidator=new MethodValidateConfig(method);
			validators.put(method, methodValidator);
		}
		methodValidator.addFieldValidator(validateAnn);
	}

	public List<Object> getValidators(Method method, String paramName) {
		MethodValidateConfig methodValidator=validators.get(method);
		if(methodValidator==null) return null;
		return methodValidator.getValidators(paramName);
	}

	public void reset() {
		validators.clear();
	}

	public void processMethod(Method method) {
		MethodValidateConfig methodValidator=validators.get(method);
		if(methodValidator!=null) { return; }
		//采集注解并结构化缓存
		for (Class annType : ParameterValidateManager.VALIDATE_TYPES) {
			 Annotation[] anns=method.getAnnotationsByType(annType);
			 for (Annotation ann : anns) {
				 this.addMethodValidator(method,ann);
			}
		}
		
	}
	
}
