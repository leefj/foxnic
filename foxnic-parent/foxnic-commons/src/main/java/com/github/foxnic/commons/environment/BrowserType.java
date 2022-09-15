package com.github.foxnic.commons.environment;

import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.commons.bean.BeanUtil;
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
	OPERA("Opera"),
	IE("Internet Explorer"),
	//
	API_FOX("apifox"),
	HTTP_CLIENT("httpclient"),
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
		if(StringUtil.isBlank(browserName)) {
			return UNKNOWN;
		}
		for (BrowserType value : values()) {
			if(browserName.contains(value.code())) {
				return value;
			}
		}
		return UNKNOWN;
	}

	public static UserAgent getUserAgent(HttpServletRequest request){
		if(request==null) return null;
		String ua = ((HttpServletRequest)request).getHeader("User-Agent");
		if(StringUtil.isBlank(ua)) return null;
		UserAgent userAgent=UserAgent.parseUserAgentString(ua);
		if("Unknown".equals(userAgent.getBrowser().getName())) {
			userAgent=replaceUserAgent(userAgent,ua);
		}
		return userAgent;
	}

	private static UserAgent replaceUserAgent(UserAgent userAgent, String ua) {
		if(ua.startsWith(API_FOX.code())) {
			BeanUtil.setFieldValue(userAgent.getBrowser(),"name",API_FOX.code());
			BeanUtil.setFieldValue(userAgent.getBrowser(),"browserType",Browser.CHROME);
		}
		else if(ua.toLowerCase().contains(HTTP_CLIENT.code())) {
			BeanUtil.setFieldValue(userAgent.getBrowser(),"name",HTTP_CLIENT.code());
			BeanUtil.setFieldValue(userAgent.getBrowser(),"browserType",Browser.UNKNOWN);
		}
		return userAgent;
	}

	public static Browser getBrowser(HttpServletRequest request){
		UserAgent userAgent = getUserAgent(request);
		if(userAgent==null) {
			return null;
		}
		Browser browser=userAgent.getBrowser();

		return browser;
	}



}
