package com.github.foxnic.springboot.api.validator;

import com.github.foxnic.commons.bean.BeanUtil;

public class ValidateAnnotation {
	
	private Class annType;
	private Object ann;
	private String[] names;
	
	private String message;
	
	public ValidateAnnotation(Object ann) {
		if(ann==null) {
			System.out.println();
		}
		this.ann=ann;
		Object h=BeanUtil.getFieldValue(ann,"h");
	 
		annType=BeanUtil.getFieldValue(h, "type",Class.class);
		 
		this.names=BeanUtil.getFieldValue(ann, "name", String[].class);
		this.message=BeanUtil.getFieldValue(ann, "message", String.class);
	}
	
	public String[] getNames() {
		return names;
	}
	
	public String getAnnotationName() {
		return annType.getSimpleName();
	}
	
	public Class getAnnotationType() {
		return annType;
	}

	public String getMessage() {
		return message;
	}
	
}
