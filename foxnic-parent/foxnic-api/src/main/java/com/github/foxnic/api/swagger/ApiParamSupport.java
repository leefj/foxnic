package com.github.foxnic.api.swagger;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiParamSupport {

    /**
     * 默认模型名称
     * */
    String name() default "";

    /**
     * 默认是否排除 DBTreaty 字段 如创建时间，创建人等
     * */
    boolean ignoreDBTreatyProperties() default false;

    /**
     * 默认是否排除默认的 Vo 字段 如页码、排序等字段
     * */
    boolean ignoreDefaultVoProperties() default false;

    /**
     * 默认排除某些不需要的属性
     * */
    String[] ignoredProperties() default {};

    /**
     * 默认在 ignoreDBTreatyProperties 和 ignoreDefaultVoProperties 基础上保留指定字段
     * */
    String[] includeProperties() default {};




    Model[] value() default  {};
}
