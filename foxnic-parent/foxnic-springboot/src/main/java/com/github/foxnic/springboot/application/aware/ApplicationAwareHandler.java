package com.github.foxnic.springboot.application.aware;


import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.concurrent.task.SimpleTaskManager;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.log.PerformanceLogger;
import com.github.foxnic.commons.network.Machine;
import com.github.foxnic.dao.Meta;
import com.github.foxnic.springboot.spring.SpringUtil;
import com.github.foxnic.springboot.starter.FoxnicApplication;
import com.github.foxnic.springboot.web.WebContext;
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

import java.net.InetAddress;
import java.util.List;


@Component
public class ApplicationAwareHandler implements ApplicationContextAware,EnvironmentAware,BeanFactoryPostProcessor,ApplicationListener<ApplicationStartedEvent>  {



	@Override
    public synchronized void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        SpringUtil.setBeanFactoryIf(beanFactory);
    }


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringUtil.setContextInWebIf(applicationContext);
		SimpleTaskManager.doParallelTask(new Runnable() {
			@Override
			public void run() {
				Machine.getInet4AddressList();
				Machine.getIdentity();
				Machine.getHostName();
				Machine.getIp();
				SpringUtil.getProcessId();
			}
		});
		Logger.info(SpringUtil.getEnvProperty("spring.application.name") + "(" + SpringUtil.getActiveProfile() + ") is ready on port " + SpringUtil.getEnvProperty("server.port"));

	}

	@Override
	public void setEnvironment(Environment environment) {
		SpringUtil.setEnvironmentIf(environment);
	}


	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {

		PerformanceLogger logger=new PerformanceLogger(false);
		logger.collect("A");
		long t0=System.currentTimeMillis();
		WebContext webContext=SpringUtil.getBean(WebContext.class);
		webContext.initURLMapping();
		long t1=System.currentTimeMillis();
		Logger.info("url scan time : "+(t1-t0)+"ms");
		logger.collect("B");

		logger.collect("C");
		CodeBuilder info=new CodeBuilder();
		info.ln("");
		info.ln("======================== FOX-NIC-WEB IS READY ========================");
		info.ln(1,space("Internal Version",2)+ Meta.INTERNAL_VERSION);
		info.ln(1,space("Machine Id",4)+ Machine.getIdentity());
		info.ln(1,space("Process Id",4)+ SpringUtil.getProcessId());
		info.ln(1,space("Application Name",2)+SpringUtil.getApplicationName());
		info.ln(1,space("Active Profile",3)+SpringUtil.getActiveProfile());
		info.ln(1,space("Port",5)+SpringUtil.getEnvProperty("server.port"));
		info.ln(1,space("Boot Time",4)+((System.currentTimeMillis()- FoxnicApplication.getStartTime())/1000)+"s");
		info.ln(1,space("Visit Local",4)+"http://127.0.0.1:"+SpringUtil.getEnvProperty("server.port")+"/");
		logger.collect("D");

		List<InetAddress> ips=Machine.getInet4AddressList();
		logger.collect("E");
		for (int i = 0; i < ips.size(); i++) {
			info.ln(1,space("Visit LAN("+i+")",3)+"http://"+ips.get(i).getHostAddress()+":"+SpringUtil.getEnvProperty("server.port")+"/");
		}
		logger.collect("F");
		info.ln("======================== FOX-NIC-WEB IS READY ========================");

		Logger.info("\n"+info.toString());
		logger.collect("G");
		logger.info("onApplicationEvent");

	}

	private String space(String word,int tabs) {
		tabs+=2;
		for (int i = 0; i < tabs; i++) {
			word+="\t";
		}
		return word;
	}


}
