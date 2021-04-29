package com.github.foxnic.springboot.spring;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.web.context.WebApplicationContext;

public enum BeanScopes {
	
	
	
	SINGLETON(ConfigurableBeanFactory.SCOPE_SINGLETON),
	PROTOTYPE(ConfigurableBeanFactory.SCOPE_PROTOTYPE),
	
	APPLICATION(WebApplicationContext.SCOPE_APPLICATION),
	SESSION(WebApplicationContext.SCOPE_SESSION),
	REQUEST(WebApplicationContext.SCOPE_REQUEST);
	
	private String scope=null;
	private BeanScopes(String scope)
	{
		this.scope=scope;
	}

	public String scope()
	{
		return this.scope;
	}
	
}
