package com.github.foxnic.springboot.web;

import org.springframework.util.AntPathMatcher;

/**
 * Web工具类
 * */
public class WebUtil {
	
	private static AntPathMatcher matcher = new AntPathMatcher();
 
	/**
	 * 是否和Spring路径模式匹配
	 * */
	public static boolean isMatchPattern(String pattern,String path)
	{
		return matcher.match(pattern, path);
	}
}
