package com.github.foxnic.api.swagger;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiResponseSupport {
    Model[] value() default  {};
}
