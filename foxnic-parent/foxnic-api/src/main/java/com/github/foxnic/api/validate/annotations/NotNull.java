package com.github.foxnic.api.validate.annotations;

import com.github.foxnic.api.validate.annotations.NotNull.List;

import javax.validation.Constraint;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

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
