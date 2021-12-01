package com.github.foxnic.springboot.application.aware;


import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.concurrent.task.SimpleTaskManager;
import com.github.foxnic.commons.log.Logger;
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
		long t0=System.currentTimeMillis();
		WebContext webContext=SpringUtil.getBean(WebContext.class);
		webContext.gatherUrlMapping();
		long t1=System.currentTimeMillis();
		Logger.info("url scan time : "+(t1-t0)+"ms");
		CodeBuilder info=new CodeBuilder();



		info.ln("");
		info.ln("======================== FOX-NIC-WEB IS READY ========================");
		info.ln(1,"Internal Version         		"+ Meta.INTERNAL_VERSION);
		info.ln(1,"Machine Id         		"+ Machine.getIdentity());
		info.ln(1,"Process Id         		"+ SpringUtil.getProcessId());
		info.ln(1,"Application Name	"+SpringUtil.getApplicationName());
		info.ln(1,"Active Profile			"+SpringUtil.getActiveProfile());
		info.ln(1,"Port							"+SpringUtil.getEnvProperty("server.port"));
		info.ln(1,"Boot Time				"+((System.currentTimeMillis()- FoxnicApplication.getStartTime())/1000)+"s");
		info.ln(1,"Visit Local				"+"http://127.0.0.1:"+SpringUtil.getEnvProperty("server.port")+"/");
		List<InetAddress> ips=Machine.getInet4AddressList();
		for (int i = 0; i < ips.size(); i++) {
			info.ln(1,"Visit LAN("+i+")	"+"http://"+ips.get(i).getHostAddress()+":"+SpringUtil.getEnvProperty("server.port")+"/");
		}
		info.ln("======================== FOX-NIC-WEB IS READY ========================");
		Logger.info("\n"+info.toString());


	}

 
}
