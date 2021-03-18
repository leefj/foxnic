package com.github.foxnic.springboot.api.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;

import com.github.foxnic.springboot.api.annotations.NotNull.List;

@Target(ElementType.METHOD) 
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(List.class)
@Documented
@Constraint(validatedBy = { })
public @interface NotNull  {
	/**
	 * 参数名称或参数名称的集合
	 * */
	public abstract String[] name() default {};
	
	/**
	 * 错误提示信息
	 * */
	public abstract String message() default "参数 ${param.name} 不允许为 null";
	
	@Target({ METHOD })
	@Retention(RUNTIME)
	@Documented
	@interface List {

		NotNull[] value();
	}
}
