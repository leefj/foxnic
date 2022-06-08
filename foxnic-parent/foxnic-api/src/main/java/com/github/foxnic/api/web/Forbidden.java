package com.github.foxnic.api.web;

import org.springframework.stereotype.Controller;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.TYPE, METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
/**
 * 接口禁用标记
 * */
public @interface Forbidden {
    /**
     * 禁用说明
     * */
    String value() default "";
}
