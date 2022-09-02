package com.github.foxnic.springboot.mvc;

import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.commons.reflect.EnumUtil;

/**
 * 日志类型
 * */
public enum BrowserType implements CodeTextEnum {
	Edge("任务日志","cron"),
	Chrome("配置日志","config"),
	Safari("配置日志","config"),
	Firefox("配置日志","config")
	;

	private String code;
	private String text;

	private BrowserType(String text, String code) {
		this.code=code;
		this.text=text;
	}

	public String code() {
		return this.code;
	}

	public String text() {
		return this.text;
	}

	public static BrowserType parseByCode(String code) {
		return (BrowserType) EnumUtil.parseByCode(BrowserType.values(),code);
	}
}
