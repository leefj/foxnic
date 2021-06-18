package com.github.foxnic.springboot.api.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.foxnic.api.transter.Result;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.api.validate.annotations.NotBlank;
import com.github.foxnic.api.validate.annotations.NotEmpty;
import com.github.foxnic.api.validate.annotations.NotNull;
import com.github.foxnic.springboot.api.validator.impl.NotBlankValidator;
import com.github.foxnic.springboot.api.validator.impl.NotEmptyValidator;
import com.github.foxnic.springboot.api.validator.impl.NotNullValidator;
import com.github.foxnic.springboot.mvc.RequestParameter;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

@Component
public class ParameterValidateManager {
	
	public static final Map<Class,ParameterValidator> VALIDATORS = new HashMap<>();
	
	private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

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
	
	public ApiImplicitParam getApiImplicitParam(Method method,String paramName) {
		MethodValidateConfig methodValidator = getMethodValidateConfig(method);
		ApiImplicitParam ap=methodValidator.getApiImplicitParam(paramName);
		return ap;
	}

	
	
	//校验方法参数
	public List<Result> validate(Method method, RequestParameter params, Object[] args) {
		
		String[] paramNames=parameterNameDiscoverer.getParameterNames(method);
		Map<String,Object> methodParamMap=new HashMap<>();
		for (int i = 0; i < paramNames.length; i++) {
			methodParamMap.put(paramNames[i],args[i]);
		}
		
		
		MethodValidateConfig methodValidator = getMethodValidateConfig(method);
		Set<String> names=methodValidator.getParamNames();
		int i=0;
		List<Result> rs=new ArrayList<>();
		//循环校验单位
		for (String name : names) {
			List<ValidateAnnotation> anns= methodValidator.getValidators(name);
			ApiImplicitParam ap=methodValidator.getApiImplicitParam(name);
			Object value=null;
			if(ap!=null && "header".equals(ap.paramType())) {
				value=params.getHeader().get(name);
			} else {
				value=params.get(name);
			}
			if(value==null) {
				value=methodParamMap.get(name);
			}
			
			//Feign
			if(value==null && method.getParameterCount()==1 && !StringUtil.isBlank(params.getRequestBody())) {
				value=params.getRequestBody();
			}
			
			
			for (ValidateAnnotation ann : anns) {
				List<Result> r=this.validate(name,ap,ann,value);
				rs.addAll(r);
			}
			i++;
		}
		return rs;
	}

	
	/**
	 * 校验参数是否符合规则
	 * */
	private List<Result> validate(String name,ApiImplicitParam ap,ValidateAnnotation va, Object value) {
		ParameterValidator pv=VALIDATORS.get(va.getAnnotationType());
		return pv.validate(name,ap,va,value);
	}
	
	
	/**
	 * 获得方法校验器，如果未初始化则先初始化
	 * */
	private MethodValidateConfig getMethodValidateConfig(Method method) {
		MethodValidateConfig methodValidator=validators.get(method);
		if(methodValidator==null) { 
			processMethod(method);
		}
		if(methodValidator==null) {
			return null;
		}
		return methodValidator;
	}
	
	
}
