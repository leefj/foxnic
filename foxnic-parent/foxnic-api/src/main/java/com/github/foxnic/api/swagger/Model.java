package com.github.foxnic.api.swagger;

import io.swagger.annotations.ApiModelProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Model {
    /**
     * 基础模型
     * */
    Class baseModelType() default Void.class;
    /**
     * 模型名称
     * */
    String name() default "";


    /**
     * 默认是否排除全部字段，可以在利用 includeProperties 属性做加法
     * */
    boolean ignoreAllProperties() default false;


    /**
     * 排除非数据库字段的属性
     * */
    boolean ignoreNonDBProperties() default false;

    /**
     * 排除某些不需要的属性
     * */
    String[] ignoredProperties() default {};

    /**
     * 是否排除 DBTreaty 字段 如创建时间，创建人等
     * */
    boolean ignoreDBTreatyProperties() default false;

    /**
     * 是否排除默认的 Vo 字段 如页码、排序等字段
     * */
    boolean ignoreDefaultVoProperties() default false;

    /**
     * 在 ignoreDBTreatyProperties 和 ignoreDefaultVoProperties 基础上保留指定字段
     * */
    String[] includeProperties() default {};

    /**
     * 主要用于同名属性覆盖
     */
    ApiModelProperty[] properties() default {};




}
