package com.github.foxnic.sql.entity.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({METHOD, FIELD})
@Retention(RUNTIME)
public  @interface Index {
	String name() default "";
	boolean unique() default false;
	boolean primary() default false;
	int order() default 0;
}
