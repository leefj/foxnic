package com.github.foxnic.springboot.web;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.github.foxnic.springboot.spring.SpringUtil;
 
@Component
public class WebContext {
	
	 
	private static Logger lg=LoggerFactory.getLogger(WebContext.class);
	
	private HashMap<String, HandlerMethod> patterns=null;
 
	private HashMap<String,HandlerMethod> cache=new HashMap<>();
	
	@Autowired
	private RequestMappingHandlerMapping mapping;
	
	/**
	 * 根据请求获得MVC处理函数
	 * @param request 请求
	 * @return HandlerMethod
	 * */
	public HandlerMethod getHandlerMethod(HttpServletRequest request)
	{
		if(patterns==null) {
			synchronized (this) {
				if(patterns==null) {
					this.gatherUrlMapping();
				}
			}
		}
		
		String uri=request.getRequestURI();
		HandlerMethod hm=cache.get(uri);
		if(hm!=null) return hm;
		
//		get如果有chain的情况是RequestMapping，如果是标准就没法调用了
//		HandlerExecutionChain chain=null;
//		try {
//			chain=mapping.getHandler(request);
//		} catch (Exception e) {}
//		//如果存在chain，返回null，默认处理方式
//		if(chain!=null) return null;
 
		//查找匹配
		for (String pattern : patterns.keySet()) {
			if(WebUtil.isMatchPattern(pattern, uri)) {
				hm=patterns.get(pattern);
				break;
			}
		}
		if(hm!=null) cache.put(uri, hm);
		return hm;
	}
	
	/**
	 * 收集url映射信息
	 * */
	public void gatherUrlMapping()
	{
		if(this.patterns!=null) return;
		this.patterns=new HashMap<String, HandlerMethod>();
 
		mapping = SpringUtil.getBean(RequestMappingHandlerMapping.class);
 
		Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
		StringBuilder cb=new StringBuilder();
		cb.append("Request Mappings: \n\n");
	 
		for (RequestMappingInfo info:map.keySet()) {
			HandlerMethod hm=map.get(info);
        	Set<String> patterns=info.getPatternsCondition().getPatterns();;
        	Method m=hm.getMethod();
        	//RequestMethodsRequestCondition cd=info.getMethodsCondition();
        	for (String pattern : patterns) {
        		this.patterns.put(pattern,hm);
        		cb.append("url = "+pattern+" , method = "+m.getDeclaringClass().getName()+"."+m.getName()+"\n");
			}
        }
		lg.info(cb.toString());
	}
	
	
	 
}
