package com.github.foxnic.springboot.application.aware;

 
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.springboot.spring.SpringUtil;
 

@Component
public class ApplicationAwareHandler implements ApplicationContextAware,EnvironmentAware,BeanFactoryPostProcessor {

	@Override
    public synchronized void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        SpringUtil.setBeanFactoryIf(beanFactory);
    }
 

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringUtil.setContextInWebIf(applicationContext);
		Logger.info(SpringUtil.getEnvProperty("spring.application.name") + "(" + SpringUtil.getActiveProfile() + ") is boot at " + SpringUtil.getEnvProperty("server.port"));
	}

	@Override
	public void setEnvironment(Environment environment) {
		SpringUtil.setEnvironmentIf(environment);
	}
 
}
