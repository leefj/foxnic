package com.github.foxnic.springboot.api.validator.impl;

import java.util.Arrays;
import java.util.List;

import com.github.foxnic.springboot.api.validator.ParameterValidator;
import com.github.foxnic.springboot.api.validator.ValidateAnnotation;

import com.github.foxnic.springboot.mvc.Result;
import io.swagger.annotations.ApiImplicitParam;

public class NotNullValidator extends ParameterValidator {

	
	@Override
	public List<Result> validateCurrent(String name, ApiImplicitParam ap, ValidateAnnotation va, Object value) {
		if(value==null) {
			Result r = createResult(name,ap, va);
			return Arrays.asList(r);
		} else {
			return NO_ERROR;
		}
	}



	

	
	
	
	
}
