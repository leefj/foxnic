package com.github.foxnic.springboot.mvc;

import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.commons.environment.OSType;
import com.github.foxnic.commons.reflect.EnumUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * 日志类型
 * */
public enum BrowserType implements CodeTextEnum {
	EDGE("Edge"),
	CHROME("Chrome"),
	SAFARI("Safari"),
	FIREFOX("Firefox"),
	OPERA("Firefox"),
	IE("IE"),
	UNKNOWN("UNKNOWN")
	;
	private String code;
	private String text;

	private BrowserType(String code) {
		this.code=code;
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

	/**
	 * 判断用户使用的浏览器类型
	 */
	public static BrowserType parseByRequest(HttpServletRequest request){
		String agent=request.getHeader("user-agent");
		for (BrowserType value : BrowserType.values()) {
			if (agent.contains(value.code())) {
				return value;
			}
		}
		return UNKNOWN;
	}

	//判断用户使用的浏览器类型
	public static OSType getOSType(HttpServletRequest request){
		String agent=request.getHeader("user-agent");
		for (OSType value : OSType.values()) {
//			if (agent.contains(value.code())) {
				return value;
//			}
		}
		return OSType.UNKNOW;
	}

}
