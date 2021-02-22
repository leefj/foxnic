package com.github.foxnic.springboot.mvc;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;

 
@Aspect
@Component
public class ControllerAspector {
	
	@PostConstruct
	private void init() {
		Logger.info("ControllerAspector Init");
	}

	@Pointcut(value = "@annotation(org.springframework.web.bind.annotation.RequestMapping)")
	public void pointCut4RequestMapping() {}
	
	@Pointcut(value = "@annotation(org.springframework.web.bind.annotation.PostMapping)")
	public void pointCut4PostMapping() {}
	
	@Pointcut(value = "@annotation(org.springframework.web.bind.annotation.GetMapping)")
	public void pointCut4GetMapping() {}
 
	@Around("ControllerAspector.pointCut4RequestMapping()")
	public Object processRequestMapping(ProceedingJoinPoint joinPoint) throws Throwable {
		return processControllerMethod(joinPoint,RequestMapping.class);
	}
	
	@Around("ControllerAspector.pointCut4PostMapping()")
	public Object processPostMapping(ProceedingJoinPoint joinPoint) throws Throwable {
		return processControllerMethod(joinPoint,PostMapping.class);
	}
	
	@Around("ControllerAspector.pointCut4GetMapping()")
	public Object processGetMapping(ProceedingJoinPoint joinPoint) throws Throwable {
		return processControllerMethod(joinPoint,GetMapping.class);
	}
 
	/**
	 * 拦截控制器方法，进行预处理
	 * */
	private Object processControllerMethod(ProceedingJoinPoint joinPoint,Class mappingType) throws Throwable {
 
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		RequestParameter requestParameter=new RequestParameter(request);
		String traceId=requestParameter.getTraceId();
		//加入 TID 信息
		Logger.setTID(traceId);
 
		MethodSignature ms=(MethodSignature)joinPoint.getSignature();
		Method method=ms.getMethod();
		
		if(method==null) {
			return joinPoint.proceed();
		}
		if(method.getDeclaringClass().equals(BasicErrorController.class)) {
			return joinPoint.proceed();
		}

		Object[] args=joinPoint.getArgs();
		Parameter[] params=method.getParameters();
		Object arg=null;
		Parameter param=null;
		
		for (int i = 0; i < args.length; i++) {
			arg=args[i];
			param=params[i];
			if(arg==null) {
				args[i]=DataParser.parse(param.getType(),requestParameter.get(param.getName()));
			} else {
				if(arg instanceof Entity) {
					arg=EntityContext.create((Class<Entity>)arg.getClass());
					for (Map.Entry<String, Object> e : requestParameter.entrySet()) {
						Object beanValue=BeanUtil.getFieldValue(arg, e.getKey());
						if(beanValue==null && e.getValue()!=null) {
							BeanUtil.setFieldValue(arg, e.getKey(), e.getValue());
						}
					}
					((Entity)arg).clearModifies();
					args[i]=arg;
				}
			}
		}
		return joinPoint.proceed(args);
	}
 
}
