package com.github.foxnic.dao.relation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Join {
	
	/**
	 * 分组统计函数
	 * */
	public abstract String groupFor() default "";
	
	public abstract String targePoType() default "";
	
	
	
}