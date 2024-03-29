package com.github.foxnic.springboot.config;

import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.springboot.mvc.ParameterFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.github.foxnic.springboot.mvc.AdvanceModelAttributeMethodProcessor;
import com.github.foxnic.springboot.mvc.MessageConverter;



@Configuration
public class WebConfigs implements WebMvcConfigurer {

//	/**
//	 * 添加拦截器
//	 *
//	 * @param registry
//	 */
//	@Override
//	public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new APIInterceptor());
//	}

//	@Bean
//	public HttpMessageConverter<Object> foxnicMessageConverter() {
//		Logger.info("init message converter");
//		MessageConverter converter = new MessageConverter();
//		return converter;
//	}

	private FastJsonConfig getDefaultConfig(boolean writeMapNullValue) {
		FastJsonConfig config = new FastJsonConfig();

		SerializerFeature[] features= {
				// 将String类型的null转成""
				SerializerFeature.WriteNullStringAsEmpty,
				// 将Number类型的null转成0
				SerializerFeature.WriteNullNumberAsZero,
				// 将List类型的null转成[]
				SerializerFeature.WriteNullListAsEmpty,
				// 将Boolean类型的null转成false
				SerializerFeature.WriteNullBooleanAsFalse,
				//格式化日期
				SerializerFeature.WriteDateUseDateFormat,
				// 避免循环引用
				SerializerFeature.DisableCircularReferenceDetect
		};

		if(writeMapNullValue) {
			features=ArrayUtil.merge(features,new SerializerFeature[] {SerializerFeature.WriteMapNullValue});
		}

		config.setSerializerFeatures(features);

		SerializeConfig serializeConfig = SerializeConfig.globalInstance;
		serializeConfig.put(Long.class , ToStringSerializer.instance);
		serializeConfig.put(Long.TYPE , ToStringSerializer.instance);
		config.setSerializeConfig(serializeConfig);
		return config;
	}


	@Bean
    public HttpMessageConverter<Object> foxnicMessageConverter() {
		MessageConverter converter = new MessageConverter();

        converter.setFastJsonConfig(getDefaultConfig(true));
		converter.setMinorFastJsonConfig(getDefaultConfig(false));

        converter.setDefaultCharset(MessageConverter.UTF_8);

        List<MediaType> mediaTypeList =converter.getSupportedMediaTypes();
        // 解决中文乱码问题，相当于在 Controller 上的 @RequestMapping 中加了个属性 produces = "application/json"
        mediaTypeList.add(MediaType.APPLICATION_JSON);
        converter.setSupportedMediaTypes(mediaTypeList);
        return converter;
    }


	@Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AdvanceModelAttributeMethodProcessor(true));
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

	@Bean
	public FilterRegistrationBean parameterFilterFilterRegistration() {
		FilterRegistrationBean registration = new FilterRegistrationBean(new ParameterFilter());
		registration.addUrlPatterns("*");
		registration.setOrder(0);
		registration.setName("ParameterFilter");
		return registration;
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



//	@Bean
//	public CookieSerializer cookieSerializer() {
//		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
//		serializer.setCookieName("JSESSIONIDXXX");
//		serializer.setDomainName("localhost");
//		serializer.setCookiePath("/");
//		serializer.setCookieMaxAge(3600);
//		serializer.setSameSite("Lax");  // 设置SameSite属性
//		serializer.setUseHttpOnlyCookie(true);
//		serializer.setUseSecureCookie(false);
//		return serializer;
//	}



}
