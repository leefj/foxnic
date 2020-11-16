package com.github.foxnic.api.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) 
@Retention(RetentionPolicy.RUNTIME)
public @interface ReturnData
{
	/**
	 * data 中的 key
	 * */
	public abstract String key() default "";
	/**
	 * data 中的 key 对应的结果名称
	 * */
	public abstract String name() default "";
	/**
	 * data 中的 key 对应的结果说明
	 * */
	public abstract String note() default "";
	
	/**
	 * 示例值,可以是多个
	 * */
	public abstract String[] sample() default {};
	
}
