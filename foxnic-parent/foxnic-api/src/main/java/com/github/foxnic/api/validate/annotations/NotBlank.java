package com.github.foxnic.api.validate.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;

import com.github.foxnic.api.validate.annotations.NotBlank.List;

@Target(ElementType.METHOD) 
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(List.class)
@Documented
@Constraint(validatedBy = { })
public @interface NotBlank  {
 
	/**
	 * 参数名称或参数名称的集合
	 * */
	public abstract String[] name() default {};
	
	/**
	 * 错误提示信息
	 * */
	public abstract String message() default "参数 ${param.name} 不允许为空白字符串";
	
	
	/**
	 * Defines several {@link MaxNumber} annotations on the same element.
	 *
	 * @see MaxNumber
	 */
	@Target({ METHOD })
	@Retention(RUNTIME)
	@Documented
	@interface List {

		NotBlank[] value();
	}
}
