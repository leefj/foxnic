package com.github.foxnic.springboot.api.swagger;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

import java.util.HashMap;
import java.util.Map;


/**
 * Swagger API相关配置
 */
@Configuration
@EnableSwagger2
@EnableKnife4j
public class SwaggerGlobalConfig {

	private static final String SPRINGFOX_DOCUMENTATION_AUTO_STARTUP = "springfox.documentation.auto-startup";

	static {
		SwaggerAssistant.inject();
	}

	@Autowired
	private final Environment environment = null;

	private static Boolean isAutoStartup = null;

	public static Boolean isAutoStartup() {
		return isAutoStartup;
	}

	private void preventDefaultBootstrapper() {
		if(isAutoStartup!=null) return;
		isAutoStartup = DataParser.parseBoolean(environment.getProperty(SPRINGFOX_DOCUMENTATION_AUTO_STARTUP,"true"));
		boolean flag =false;
		OriginTrackedMapPropertySource ms = null;
		MutablePropertySources propertySources = (MutablePropertySources) BeanUtil.getFieldValue(environment,"propertySources");
		for (PropertySource<?> propertySource : propertySources) {
			if(propertySource instanceof OriginTrackedMapPropertySource) {
				ms=(OriginTrackedMapPropertySource)propertySource;
				if(propertySource.containsProperty(SPRINGFOX_DOCUMENTATION_AUTO_STARTUP)) {
					Map<String, Object> source=new HashMap<>(ms.getSource());
					source.put(SPRINGFOX_DOCUMENTATION_AUTO_STARTUP,"false");
					BeanUtil.setFieldValue(ms,"source",source);
					flag=true;
					break;
				}
			}
		}
		if(!flag && ms!=null) {
			Map<String, Object> source=new HashMap<>(ms.getSource());
			source.put(SPRINGFOX_DOCUMENTATION_AUTO_STARTUP,"false");
			BeanUtil.setFieldValue(ms,"source",source);
		}
	}

	@Bean
	@Primary
	public ServiceModelToSwagger2Mapper getServiceModelToSwagger2Mapper() {
		this.preventDefaultBootstrapper();
		return new FoxnicServiceModelToSwagger2MapperImpl();
	}

}
