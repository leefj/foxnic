package com.github.foxnic.api.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) 
@Retention(RetentionPolicy.RUNTIME)
public @interface API
{
	public static enum AccessType {
		/**
		 * accType 的默认值，最终由配置文件指定
		 * */
		DEFAULT,
		/**
		 * 公开，无需登录即可访问
		 * */
		PUBLIC,
		/**
		 * 需要身份认证及授权才能访问
		 * */
		PROTECTED,
		/**
		 * 本地仅对本机以及集群内机器开放
		 * */
		INTERNAL,
		/**
		 * 仅对本机开放
		 * */
		PRIVATE;
	}
//	/**
//	 * accType 的默认值，最终由配置文件指定
//	 * */
//	public static final String DEFAULT = "default";
//	
//	/**
//	 * 公开，无需登录即可访问
//	 * */
//	public static final String PUBLIC = "public";
//	
//	/**
//	 * 需要身份认证及授权才能访问
//	 * */
//	public static final String PROTECTED = "protected";
//	
//	/**
//	 * 本地仅对本机以及集群内机器开放
//	 * */
//	public static final String INTERNAL = "internal";
//	
//	/**
//	 * 仅对本机开放
//	 * */
//	public static final String PRIVATE = "private";
	
	
	/**
	 * API名称
	 * */
	public abstract String name() default "";
	/**
	 * API说明
	 * */
	public abstract String note() default "";
	
	/**
	 * API说明，默认 DEFAULT ，由配置决定
	 * */
	public abstract AccessType accType() default AccessType.DEFAULT;
	
	/**
	 * 顺序号
	 * */
	public int sort() default 0;
	
	/**
	 * 是否从doc文档排除
	 * */
	public boolean excludeFromDoc() default false;
	
	/**
	 * 是否应用默认通用参数
	 * */
	public boolean applyCommonParams() default true;
 
	
	
}
