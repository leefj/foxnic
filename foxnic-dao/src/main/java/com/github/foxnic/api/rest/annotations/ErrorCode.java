package com.github.foxnic.api.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ErrorCode
{
	/**
	 * 代码
	 * */
	public abstract String code() default "";
	/**
	 * 详细说明
	 * */
	public abstract String detail() default "";
	
}
