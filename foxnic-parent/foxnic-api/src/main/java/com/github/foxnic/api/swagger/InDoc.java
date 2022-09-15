package com.github.foxnic.api.swagger;

import java.lang.annotation.*;

/**
 * 将接口纳入扫描范围
 * */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface InDoc {

}
