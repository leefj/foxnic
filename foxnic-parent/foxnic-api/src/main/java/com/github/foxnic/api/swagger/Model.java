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
     * 排除某些不需要的属性
     * */
    String[] ignoredProperties() default {};
    /**
     * 主要用于同名属性覆盖
     */
    ApiModelProperty[] properties() default {@ApiModelProperty};

}
