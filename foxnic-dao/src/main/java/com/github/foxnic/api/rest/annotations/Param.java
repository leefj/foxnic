package com.github.foxnic.api.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param
{
	public static final String IGNOR_DEFAULT_VALUE="#$ignor$#:_&tity-none__";
	
 
	
	/**
	 * 变量名称/取值的Key
	 * */
	public abstract String key() default "";
	/**
	 * 参数名称
	 * */
	public abstract String name() default "";
	
	/**
	 * 参数数据类型
	 * */
	public abstract Class dataType() default String.class;
 
	/**
	 * 参数说明
	 * */
	public abstract String note() default "";
	
	public abstract String defaultValue() default IGNOR_DEFAULT_VALUE;
	
	/**
	 * 验证类型
	 * */
	public abstract String[] vType() default {};
	
	
	/**
	 * 是否进行自动数据校验
	 * */
	public abstract boolean autoValidate() default true;
	
	/**
	 * 示例值,可以是多个
	 * */
	public abstract String[] sample() default {};
	
}
