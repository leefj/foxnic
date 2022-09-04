package com.github.foxnic.commons.environment;

import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.reflect.EnumUtil;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;

import javax.servlet.ServletRequest;
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
	public static BrowserType parseByRequest(ServletRequest request){
		return parseByRequest((HttpServletRequest)request);
	}
	/**
	 * 判断用户使用的浏览器类型
	 */
	public static BrowserType parseByRequest(HttpServletRequest request){
		Browser browser = getBrowser(request);
		if(browser==null) return UNKNOWN;
		String browserName = browser.getName();
		return UNKNOWN;
	}

	public static UserAgent getUserAgent(HttpServletRequest request){
		if(request==null) return null;
		String ua = ((HttpServletRequest)request).getHeader("User-Agent");
		if(StringUtil.isBlank(ua)) return null;
		return UserAgent.parseUserAgentString(ua);
	}

	public static Browser getBrowser(HttpServletRequest request){
		UserAgent userAgent = getUserAgent(request);
		if(userAgent==null) {
			return null;
		}
		return userAgent.getBrowser();
	}



}
