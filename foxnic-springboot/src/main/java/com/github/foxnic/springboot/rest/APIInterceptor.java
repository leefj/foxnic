package com.github.foxnic.springboot.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.github.foxnic.springboot.spring.SpringUtil;

 
public class APIInterceptor extends HandlerInterceptorAdapter {
 
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		APIScopeHolder holder=SpringUtil.getBean(APIScopeHolder.class);
		holder.beginRequest();
		return super.preHandle(request, response, handler);
	}
 

}
