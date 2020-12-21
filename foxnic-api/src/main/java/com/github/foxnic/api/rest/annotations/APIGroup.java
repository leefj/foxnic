package com.github.foxnic.api.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
 
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface APIGroup
{
	/**
	 * API组的名称
	 * */
	public abstract String name() default "";
	/**
	 * API说明
	 * */
	public abstract String note() default "";
	
	/**
	 * 顺序号
	 * */
	public int sort() default 0;
	 
	/**
	 * 是否从doc文档排除
	 * */
	public boolean excludeFromDoc() default false;
}
