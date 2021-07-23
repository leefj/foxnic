package com.github.foxnic.springboot.application.aware;

 
import com.github.foxnic.commons.code.CodeBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.springboot.spring.SpringUtil;
import com.github.foxnic.springboot.web.WebContext;
 

@Component
public class ApplicationAwareHandler implements ApplicationContextAware,EnvironmentAware,BeanFactoryPostProcessor,ApplicationListener<ApplicationStartedEvent> {

	 
	
	@Override
    public synchronized void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        SpringUtil.setBeanFactoryIf(beanFactory);
    }
 

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringUtil.setContextInWebIf(applicationContext);
		Logger.info(SpringUtil.getEnvProperty("spring.application.name") + "(" + SpringUtil.getActiveProfile() + ") is ready on port " + SpringUtil.getEnvProperty("server.port"));
		
	}

	@Override
	public void setEnvironment(Environment environment) {
		SpringUtil.setEnvironmentIf(environment);
	}
	
	
	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		WebContext webContext=SpringUtil.getBean(WebContext.class);
		webContext.gatherUrlMapping();
		CodeBuilder info=new CodeBuilder();

		info.ln("==========================================");
		info.ln(1,SpringUtil.getApplicationName()+" is boot at "+SpringUtil.getEnvProperty("server.port"));
		info.ln("==========================================");
		Logger.info("\n"+info.toString());

	}
 
}
