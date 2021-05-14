package com.github.foxnic.springboot.api.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.springboot.api.annotations.NotNull;
import com.github.foxnic.springboot.api.error.CommonError;
import com.github.foxnic.springboot.api.error.ErrorDesc;
import com.github.foxnic.springboot.mvc.Result;

import io.swagger.annotations.ApiImplicitParam;

public abstract class ParameterValidator {
	
	protected static final List<Result> NO_ERROR=Arrays.asList();
 
	public List<Result> validate(String name,ApiImplicitParam ap, ValidateAnnotation va, Object value) {
		List<ValidateAnnotation> prefix=getPrefixAnns();
		List<Result> errs=new ArrayList<>();
		for (ValidateAnnotation pva : prefix) {
			ParameterValidator pvv=ParameterValidateManager.VALIDATORS.get(va.getAnnotationType());
			errs.addAll(pvv.validate(name,ap, pva, value));
		}
		errs.addAll(this.validateCurrent(name,ap, va, value));
		return errs;
	}
 
	protected abstract List<Result> validateCurrent(String name,ApiImplicitParam ap, ValidateAnnotation va, Object value);

	private List<ValidateAnnotation> getPrefixAnns() {
		Method m=null;
		try {
			m=this.getClass().getDeclaredMethod("validateCurrent",  String.class,ApiImplicitParam.class,ValidateAnnotation.class,Object.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		List<ValidateAnnotation> list=new ArrayList<>();
		for (Class annType : ParameterValidateManager.VALIDATORS.keySet()) {
			 Annotation[] anns=m.getAnnotationsByType(annType);
			 for (Annotation ann : anns) {
				 list.add(new ValidateAnnotation(ann));
			}
		}
		return list;
	}
	
	protected Result createResult(String name,ApiImplicitParam ap, ValidateAnnotation va) {
		String msg = processMessage(name, ap, va);
		Result r=ErrorDesc.failure(CommonError.PARAM_INVALID);
		r.message(msg);
		JSONObject detail=new JSONObject();
		if(ap!=null) {
			detail.put("name", ap.name());
			detail.put("value", ap.value());
		} else {
			detail.put("name", name);
			detail.put("value", name);
		}
		detail.put("validator",this.getValidatorJSON(va));
		r.data(detail);
		return r;
	}
	
	
	protected String processMessage(String name,ApiImplicitParam ap, ValidateAnnotation va) {
 
		String msg=va.getMessage();
		if(ap!=null) {
			msg=msg.replace("${param.value}", ap.value());
			msg=msg.replace("${param.name}", ap.name());
		} else {
			msg=msg.replace("${param.value}", name);
			msg=msg.replace("${param.name}", name);
		}
		return msg;
	}
	
	protected JSONObject getValidatorJSON(ValidateAnnotation va) {
		JSONObject json=new JSONObject();
		json.put("rule", va.getAnnotationName());
		return json;
	}
	
}
