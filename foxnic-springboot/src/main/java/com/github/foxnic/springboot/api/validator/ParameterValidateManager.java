package com.github.foxnic.springboot.api.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.springboot.api.annotations.NotBlank;
import com.github.foxnic.springboot.api.annotations.NotEmpty;
import com.github.foxnic.springboot.api.annotations.NotNull;
import com.github.foxnic.springboot.api.validator.impl.NotBlankValidator;
import com.github.foxnic.springboot.api.validator.impl.NotEmptyValidator;
import com.github.foxnic.springboot.api.validator.impl.NotNullValidator;
import com.github.foxnic.springboot.mvc.RequestParameter;
import com.github.foxnic.springboot.mvc.Result;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

@Component
public class ParameterValidateManager {
	
	public static final Map<Class,ParameterValidator> VALIDATORS = new HashMap<>();

	static {
		VALIDATORS.put(NotNull.class, new NotNullValidator());
		VALIDATORS.put(NotBlank.class, new NotBlankValidator());
		VALIDATORS.put(NotEmpty.class, new NotEmptyValidator());
	}
 
	private Map<Method,MethodValidateConfig> validators=new HashMap<>();
	
	 

	public List<ValidateAnnotation> getValidators(Method method, String paramName) {
		MethodValidateConfig methodValidator=validators.get(method);
		if(methodValidator==null) return null;
		return methodValidator.getValidators(paramName);
	}

	public void reset() {
		validators.clear();
	}

	public void processMethod(Method method) {
		
		//如果有了，表示已经初始化，无需再次初始化
		MethodValidateConfig methodValidator=validators.get(method);
		if(methodValidator!=null) { return; }
		
		methodValidator=new MethodValidateConfig(method);
		
		
		ApiImplicitParams aps=method.getAnnotation(ApiImplicitParams.class);
		if(aps!=null) {
			ApiImplicitParam[] apvs=aps.value();
			for (ApiImplicitParam ap : apvs) {
				methodValidator.addApiImplicitParam(ap);
			}
		}
		
		//采集注解并结构化缓存
		for (Class annType : ParameterValidateManager.VALIDATORS.keySet()) {
			 Annotation[] anns=method.getAnnotationsByType(annType);
			 for (Annotation ann : anns) {
				 methodValidator.addFieldValidator(new ValidateAnnotation(ann));
			}
		}
		
		validators.put(method, methodValidator);
		
	}
	
	//校验方法参数
	public List<Result> validate(Method method,RequestParameter params) {
		MethodValidateConfig methodValidator=validators.get(method);
		if(methodValidator==null) { 
			processMethod(method);
		}
		if(methodValidator==null) {
			return null;
		}
		Set<String> names=methodValidator.getParamNames();
		List<Result> rs=new ArrayList<>();
		for (String name : names) {
			List<ValidateAnnotation> anns= methodValidator.getValidators(name);
			Object value=params.get(name);
			for (ValidateAnnotation ann : anns) {
				List<Result> r=this.validate(methodValidator.getApiImplicitParam(name),ann,value);
				rs.addAll(r);
			}
		}
		return rs;
	}

	
	private List<Result> validate(ApiImplicitParam ap,ValidateAnnotation va, Object value) {
		ParameterValidator pv=VALIDATORS.get(va.getAnnotationType());
		return pv.validate(ap,va,value);
	}
	
	
	
}
