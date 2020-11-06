package com.github.foxnic.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.springboot.mvc.MessageConverter;
import com.github.foxnic.springboot.rest.APIInterceptor;
 

@Configuration
public class WebConfigs implements WebMvcConfigurer {

	/**
	 * 添加拦截器
	 * 
	 * @param registry
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new APIInterceptor());
	}

//	@Bean("TityWebFilterRegistrationBean")
//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	public FilterRegistrationBean filterRegistrationBean() {
//		FilterRegistrationBean bean = new FilterRegistrationBean();
//		bean.setFilter(new ParameterFilter());
//		bean.addUrlPatterns("/*");
//		return bean;
//	}

	@Bean
	public HttpMessageConverter<Object> messageConverter() {
		Logger.info("init message converter");
		MessageConverter converter = new MessageConverter();
		return converter;
	}

	/**
	 * 开启跨域访问
	 */
	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", buildConfig());
		return new CorsFilter(source);
	}

	private CorsConfiguration buildConfig() {
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.addAllowedOrigin("*");
		corsConfiguration.addAllowedHeader("*");
		corsConfiguration.addAllowedMethod("*");
		return corsConfiguration;
	}

	
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOrigins("*").allowedMethods("*").allowedHeaders("*")
				.exposedHeaders("access-control-allow-headers", "access-control-allow-methods",
						"access-control-allow-origin", "access-control-max-age", "X-Frame-Options")
				.allowCredentials(true).maxAge(3600);
	}

}
