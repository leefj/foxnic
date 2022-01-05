package com.github.foxnic.springboot.web;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.springboot.api.validator.ParameterValidateManager;
import com.github.foxnic.springboot.spring.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;

@Component
public class WebContext {


	private static Logger lg=LoggerFactory.getLogger(WebContext.class);

	private HashMap<String, HandlerMethod> patterns=null;

	private HashMap<String,HandlerMethod> cache=new HashMap<>();

	@Autowired
	private RequestMappingHandlerMapping mapping;



	@Autowired
	private ParameterValidateManager  parameterValidateManager;

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
		return getHandlerMethod(uri);
	}

	public HandlerMethod getHandlerMethod(String uri) {
		uri=uri.trim();
		HandlerMethod hm=cache.get(uri);
		if(hm!=null) return hm;

//		get如果有chain的情况是RequestMapping，如果是标准就没法调用了
//		HandlerExecutionChain chain=null;
//		try {
//			chain=mapping.getHandler(request);
//		} catch (Exception e) {}
//		//如果存在chain，返回null，默认处理方式
//		if(chain!=null) return null;

		List<String> matchs=new ArrayList<>();
		List<String> equals=new ArrayList<>();
		//查找匹配
		for (String pattern : patterns.keySet()) {
			if(isMatchPattern(pattern, uri)) {
				matchs.add(pattern);
			}
			if(pattern.equals(uri)) {
				equals.add(pattern);
			}
		}

		if(equals.size()>0) {
			hm = patterns.get(equals.get(0));
		} else {
			hm = null; //patterns.get(matchs.get(0));
		}

		if(hm==null && uri.endsWith("/")) {
			uri=StringUtil.removeLast(uri, "/");
			hm=getHandlerMethod(uri);
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

        	parameterValidateManager.processMethod(m);

        }
		lg.info(cb.toString());
	}



	private static AntPathMatcher matcher = new AntPathMatcher();

	/**
	 * 是否和Spring路径模式匹配
	 * */
	public static boolean isMatchPattern(String pattern,String path)
	{
		return matcher.match(pattern, path);
	}



	private static ResourceUrlProvider  resourceUrlProvider;

	/**
	 * 判断是否为静态资源
	 * */
	public static boolean isStaticResource(HttpServletRequest request) {
		if(resourceUrlProvider==null) {
			resourceUrlProvider = (ResourceUrlProvider) SpringUtil.getBean(ResourceUrlProvider.class);
		}
        String staticUri = resourceUrlProvider.getForLookupPath(request.getRequestURI());
        return staticUri != null;
    }

}
