package com.github.foxnic.springboot.rest;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;

@Component
@Scope(WebApplicationContext.SCOPE_REQUEST)
public class APIScopeHolder {
 
	private String tid=null;
	
	private long timestamp=-1;
 
 
	public long getTimestamp() {
		return timestamp;
	}

	private HttpServletRequest request=null;
	
	public void beginRequest() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
		this.request = request;
		if(this.request!=null && !StringUtil.hasContent(tid)) {
			tid=this.request.getHeader(Logger.TIRACE_ID_KEY);
			if(!StringUtil.hasContent(tid)) {
				tid=this.request.getParameter(Logger.TIRACE_ID_KEY);
			}
		}
		if(!StringUtil.hasContent(tid)) {
			tid=UUID.randomUUID().toString();
		}
		
		timestamp=System.currentTimeMillis();
		
		//设置日志MDC
		MDC.put(Logger.TIRACE_ID_KEY, tid);
 
	}
	
	public String getTID() {
		 return tid;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	 

	 

}
