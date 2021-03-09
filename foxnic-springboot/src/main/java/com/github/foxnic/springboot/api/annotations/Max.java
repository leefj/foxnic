package com.github.foxnic.springboot.api.annotations;


import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import com.github.foxnic.springboot.api.annotations.Max.List;

//javax.validation.constraints.MAX

 
@Target({ METHOD })
@Retention(RUNTIME)
@Repeatable(List.class)
@Documented
@Constraint(validatedBy = { })
public @interface Max {

	/**
	 * 参数名称或参数名称的集合
	 * */
	public abstract String[] name() default {};

	
	/**
	 * 错误提示信息
	 * */
	public abstract String message() default "参数 ${param} 的值不允许超过 ${value}";
	
	/**
	 * 指定最大值
	 * @return value the element must be lower or equal to
	 */
	String value();
	
	/**
	 * 指定 value 值的类型
	 * */
	Class<?> valueTypeClass() default Void.class;

	/**
	 * Defines several {@link Max} annotations on the same element.
	 *
	 * @see Max
	 */
	@Target({ METHOD })
	@Retention(RUNTIME)
	@Documented
	@interface List {

		Max[] value();
	}
}
