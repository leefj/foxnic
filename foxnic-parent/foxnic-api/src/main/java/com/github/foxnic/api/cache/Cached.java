package com.github.foxnic.api.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD })
@Retention(RUNTIME)
@Documented
/**
 * 对方法返回的数据进行缓存
 * */
public @interface Cached {

    /**
     * 缓存策略名称
     * */
    public abstract String strategy();
    /**
     * 超时时间，默认，毫秒
     * */
    public abstract int expire() default -1;
}
