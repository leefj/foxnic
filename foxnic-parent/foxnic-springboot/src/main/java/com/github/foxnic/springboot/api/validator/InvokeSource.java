package com.github.foxnic.springboot.api.validator;

/**
 * 调用来源
 * */
public enum InvokeSource {
	/**
	 *  原始的 HTTP 请求
	 * */
	HTTP_REQUEST,
	/**
	 * 单体应用内部请求
	 * */
	PROXY_INTERNAL,
	/**
	 * 微服务应用类，似 Feign 的调用
	 * */
	PROXY_EXTERNAL,
	/**
	 * 无法识别
	 * */
	UNKNOW;
}
