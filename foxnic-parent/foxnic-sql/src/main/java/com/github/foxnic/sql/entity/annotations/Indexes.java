package com.github.foxnic.sql.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Indexes
{
	/**
	 * 索引
	 * */
	public abstract Index[] value() default {};
}
