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
 
	public List<Result> validate(ApiImplicitParam ap, ValidateAnnotation va, Object value) {
		List<ValidateAnnotation> prefix=getPrefixAnns();
		List<Result> errs=new ArrayList<>();
		for (ValidateAnnotation pva : prefix) {
			ParameterValidator pvv=ParameterValidateManager.VALIDATORS.get(va.getAnnotationType());
			errs.addAll(pvv.validate(ap, pva, value));
		}
		errs.addAll(this.validateCurrent(ap, va, value));
		return errs;
	}
 
	protected abstract List<Result> validateCurrent(ApiImplicitParam ap, ValidateAnnotation va, Object value);

	private List<ValidateAnnotation> getPrefixAnns() {
		Method m=null;
		try {
			m=this.getClass().getDeclaredMethod("validateCurrent", ApiImplicitParam.class,ValidateAnnotation.class,Object.class);
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
	
	protected Result createResult(ApiImplicitParam ap, ValidateAnnotation va) {
		String msg = processMessage(ap, va);
		Result r=ErrorDesc.getResult(CommonError.INVALID_PARAM);
		r.message(msg);
		JSONObject detail=new JSONObject();
		detail.put("name", ap.name());
		detail.put("value", ap.value());
		detail.put("validator",this.getValidatorJSON(va));
		r.data(detail);
		return r;
	}
	
	
	protected String processMessage(ApiImplicitParam ap, ValidateAnnotation va) {
		String msg=va.getMessage();
		msg=msg.replace("${value}", ap.value());
		msg=msg.replace("${name}", ap.name());
		return msg;
	}
	
	protected JSONObject getValidatorJSON(ValidateAnnotation va) {
		JSONObject json=new JSONObject();
		json.put("rule", va.getAnnotationName());
		return json;
	}
	
}
