package com.github.foxnic.springboot.web;

import com.github.foxnic.api.web.Forbidden;
import com.github.foxnic.commons.cache.LocalCache;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.springboot.spring.SpringUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;

@Component
public class WebContext {

	private static WebContext INSTANCE = null;

	public static WebContext get() {
		if(INSTANCE==null) {
			INSTANCE=SpringUtil.getBean(WebContext.class);
		}
		return INSTANCE;
	}

	private static Logger lg=LoggerFactory.getLogger(WebContext.class);

	private Map<Method, Map<String,ApiImplicitParam>> methodApiImplicitParamMap=new HashMap<>();

	public Map<String,ApiImplicitParam> getApiImplicitParamMap(Method method) {
		return methodApiImplicitParamMap.get(method);
	}

	public ApiImplicitParam getApiImplicitParam(Method method,String name) {
		Map<String,ApiImplicitParam> map=getApiImplicitParamMap(method);
		if(map==null) return null;
		return map.get(name);
	}

	@Autowired
	private RequestMappingHandlerMapping mapping;

	/**
	 * 根据请求获得MVC处理函数
	 * @param request 请求
	 * @return HandlerMethod
	 * */
	public HandlerMethod getHandlerMethod(HttpServletRequest request) {
		return getHandlerMethod(getRequestPath(request),request.getMethod());
	}

	public static String getRequestPath(HttpServletRequest request) {
		String url = request.getServletPath();
		String pathInfo = request.getPathInfo();
		if (pathInfo != null) {
			url = StringUtils.hasLength(url) ? url + pathInfo : pathInfo;
		}
		return url;
	}

	private LocalCache<String,HandlerMethod> handlerMethodCache = new LocalCache<>();

	public HandlerMethod getHandlerMethod(String url,String method) {
		method=method.trim().toUpperCase();
		url=url.trim();



		String key=method+"@"+url;


		HandlerMethod handlerMethod=handlerMethodCache.get(key);
		if(handlerMethod!=null) {
			return handlerMethod;
		} else {
			// 无需再重新搜索一遍
			if(handlerMethodCache.keys().contains(key)) {
				return null;
			}
		}

		if(mapping==null) {
			mapping = SpringUtil.getBean(RequestMappingHandlerMapping.class);
		}
		Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
		RequestMappingInfo info = null;

		for (Map.Entry<RequestMappingInfo, HandlerMethod> e:map.entrySet()) {
			info=e.getKey();
			Set<String> patterns=info.getPatternsCondition().getPatterns();
			Set<RequestMethod> methods=info.getMethodsCondition().getMethods();
			for (String pattern : patterns) {
				for (RequestMethod rm : methods) {
					AntPathRequestMatcher matcher = new AntPathRequestMatcher(pattern, rm.name());
					if (matcher.matches(url, method)) {
						handlerMethod=e.getValue();
						break;
					}
				}
				if(handlerMethod!=null) {
					break;
				} else {
					// 如果通过 Method 匹配不到，则不指定 Method 去匹配
					if (matcher.match(pattern, url)) {
						handlerMethod = e.getValue();
						break;
					}
				}
			}
		}

		handlerMethodCache.put(key,handlerMethod);


//		 如果通过 Method 匹配不到，则不指定 Method 去匹配
//		List<HandlerMethod> matchedMethods=new ArrayList<>();
//		for (Map.Entry<RequestMappingInfo, HandlerMethod> e:map.entrySet()) {
//			info=e.getKey();
//			Set<String> patterns=info.getPatternsCondition().getPatterns();
//			for (String pattern : patterns) {
//				if (isMatchPattern(pattern, url)) {
//					matchedMethods.add(e.getValue());
//				}
//			}
//		}

//		if(matchedMethods.size()==0) {
//			return null;
//		} else if(matchedMethods.size()==1) {
//			return matchedMethods.get(0);
//		} else if(matchedMethods.size()>1) {
//			throw new RuntimeException(url+" 匹配到多个方法");
//		}
 		return handlerMethod;
	}

	/**
	 * 收集url映射信息
	 * */
	public void initURLMapping()
	{
		if(mapping==null) {
			mapping = SpringUtil.getBean(RequestMappingHandlerMapping.class);
		}
		Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
		StringBuffer cb=new StringBuffer();
		cb.append("Request Mappings: \n\n");
		map.entrySet().parallelStream().forEach((e)->{
			RequestMappingInfo info=e.getKey();
			HandlerMethod hm=e.getValue();
			collectApiImplicitParam(hm.getMethod());
			Set<String> patterns=info.getPatternsCondition().getPatterns();
			Method m=hm.getMethod();
			String ms=StringUtil.join(info.getMethodsCondition().getMethods(),",");
			if(StringUtil.isBlank(ms)) {
				ms="ALL";
			}
			for (String pattern : patterns) {
				cb.append("["+ms+"] "+pattern+" , method = "+m.getDeclaringClass().getName()+"."+m.getName()+"\n");
			}
		});
		lg.info(cb.toString());
	}


	private void collectApiImplicitParam(Method method) {
		Map<String,ApiImplicitParam> map = new HashMap<>();
		ApiImplicitParams aps=method.getAnnotation(ApiImplicitParams.class);
		if(aps!=null) {
			ApiImplicitParam[] apvs=aps.value();
			for (ApiImplicitParam ap : apvs) {
				map.put(ap.name(),ap);
			}
		}
		methodApiImplicitParamMap.put(method,map);
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
		 return isStaticResource(request.getRequestURI());
    }

	/**
	 * 判断是否为静态资源
	 * */
	public static boolean isStaticResource(String uri) {
		if(resourceUrlProvider==null) {
			resourceUrlProvider = (ResourceUrlProvider) SpringUtil.getBean(ResourceUrlProvider.class);
		}
		String staticUri = resourceUrlProvider.getForLookupPath(uri);
		return staticUri != null;
	}

	private LocalCache<String,Boolean> forbiddenCache=new LocalCache<>();

	/**
	 * 检查 rest 接口是否被禁用
	 * */
	public Boolean isForbidden(HttpServletRequest request) {
		String uri=request.getRequestURI();
		Boolean forbidden=forbiddenCache.get(uri);
		if(forbidden!=null) return forbidden;

		HandlerMethod hm=this.getHandlerMethod(request);
		if(hm==null) {
			forbidden=false;
		} else {
			Method m=hm.getMethod();
			if(m!=null) {
				Forbidden methodForbidden = m.getAnnotation(Forbidden.class);
				Forbidden typeForbidden = m.getDeclaringClass().getAnnotation(Forbidden.class);
				forbidden = methodForbidden!=null || typeForbidden!=null;
			} else {
				forbidden=false;
			}
		}
		forbiddenCache.put(uri,forbidden);
		return forbidden;
	}
}
