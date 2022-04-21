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


	public static final String SCOPE_SINGLETON=ConfigurableBeanFactory.SCOPE_SINGLETON;
	public static final String SCOPE_PROTOTYPE=ConfigurableBeanFactory.SCOPE_PROTOTYPE;
	public static final String SCOPE_APPLICATION=WebApplicationContext.SCOPE_APPLICATION;
	public static final String SCOPE_SESSION=WebApplicationContext.SCOPE_SESSION;
	public static final String SCOPE_REQUEST=WebApplicationContext.SCOPE_REQUEST;

}
