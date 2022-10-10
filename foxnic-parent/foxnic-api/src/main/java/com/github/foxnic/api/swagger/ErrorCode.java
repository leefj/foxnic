package com.github.foxnic.api.swagger;

import io.swagger.annotations.ApiModelProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ErrorCode {
    /**
     * 错误码
     * */
    String code() default "";
    /**
     * 错误名称
     * */
    String name() default "";
    /**
     * 错误描述
     * */
    String desc() default "";

    /**
     * 可能原因与解决办法
     */
    String[] solutions() default {};
}
