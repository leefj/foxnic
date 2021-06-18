package com.github.foxnic.springboot.api.validator.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.api.validate.annotations.NotNull;
import com.github.foxnic.springboot.api.validator.ParameterValidator;
import com.github.foxnic.springboot.api.validator.ValidateAnnotation;

import com.github.foxnic.api.transter.Result;
import io.swagger.annotations.ApiImplicitParam;


public class NotBlankValidator extends ParameterValidator {

	@NotNull
	@Override
	public List<Result> validateCurrent(String name, ApiImplicitParam ap, ValidateAnnotation va, Object value) {
 
		List<Result> rs=new ArrayList<>();
		if(value instanceof String) {
			if(StringUtil.isBlank((String)value)) {
				Result r = createResult(name,ap, va);
				return Arrays.asList(r);
			}
		}
		return NO_ERROR;
		
	}

	
	
	
	
}
