package com.github.foxnic.api.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) 
@Retention(RetentionPolicy.RUNTIME)
public @interface Modify
{
 
	public abstract String version() default "";
	public abstract String author();
	//修改时间
	public abstract String time();
	public abstract String note() default "";
	
}
