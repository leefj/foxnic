package com.github.foxnic.springboot.mvc;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.springboot.api.error.CommonError;
import com.github.foxnic.springboot.api.error.ErrorDesc;
import com.github.foxnic.springboot.api.swagger.SwaggerDataHandler;
import com.github.foxnic.springboot.api.validator.ParameterValidateManager;
import io.swagger.annotations.ApiImplicitParam;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

 

 
@Aspect
@Component
public class ControllerAspector {
	
	@Autowired
	private SwaggerDataHandler swaggerDataHandler;
	
	@Autowired
	private ParameterValidateManager parameterValidateManager;
	
	private ParameterNameDiscoverer parameterNameDiscoverer =  new LocalVariableTableParameterNameDiscoverer();
 	
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
 
		 
		Long t=System.currentTimeMillis();
		
		MethodSignature ms=(MethodSignature)joinPoint.getSignature();
		Method method=ms.getMethod();
		RestController rc=method.getDeclaringClass().getAnnotation(RestController.class);
		if(rc==null) {
			return joinPoint.proceed();
		}
		
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		RequestParameter requestParameter=new RequestParameter(request);
		String traceId=requestParameter.getTraceId();
		//加入 TID 信息
		Logger.setTID(traceId);
 
		request.setAttribute("", method);
		
		
		
		if(method==null) {
			return joinPoint.proceed();
		}
		if(method.getDeclaringClass().equals(BasicErrorController.class)) {
			return joinPoint.proceed();
		}
		
		//校验参数
		List<Result> results=parameterValidateManager.validate(method,requestParameter);
		if(results!=null && !results.isEmpty()) {
			Result r=ErrorDesc.failure(CommonError.PARAM_INVALID);
			r.addErrors(results);
			return r;
		}
		

		Object[] args=joinPoint.getArgs();
		Parameter[] params=method.getParameters();
		Object arg=null;
		Parameter param=null;
		
		String[] paramNames = parameterNameDiscoverer.getParameterNames(method);
		
		for (int i = 0; i < args.length; i++) {
			arg=args[i];
			param=params[i];
			ApiImplicitParam ap=parameterValidateManager.getApiImplicitParam(method, paramNames[i]);
			if(arg==null) {
				if(ap==null || !"header".equals(ap.paramType())) {
					args[i]=DataParser.parse(param.getType(),requestParameter.get(paramNames[i]));
				} else {
					args[i]=DataParser.parse(param.getType(),requestParameter.getHeader().get(paramNames[i]));
				}
			} else {
				if(arg instanceof Entity) {
					arg=EntityContext.create((Class<Entity>)arg.getClass());
					for (Map.Entry<String, Object> e : requestParameter.entrySet()) {
						Object beanValue=BeanUtil.getFieldValue(arg, e.getKey());
						if(beanValue==null && e.getValue()!=null) {
							Object value=e.getValue();
							Field f=ReflectUtil.getField(arg.getClass(),e.getKey());
							//如果能找到属性，则对属性值做一定的处理
							if(f!=null) {
								if (ReflectUtil.isSubType(List.class,f.getType())) {
									value = DataParser.parseList(f, value);
								} else if (f.getType().isArray()) {
									value = DataParser.parseArray(f.getType(), value);
								}
							}
							BeanUtil.setFieldValue(arg, e.getKey(), value);
						}
					}
					((Entity)arg).clearModifies();
					args[i]=arg;
				}
			}
		}
		Object ret=null;
		Throwable exception=null;
		try {
			ret = joinPoint.proceed(args);
		} catch (Throwable e) {
			exception=e;
			Logger.error("invoke error", e);
		}
		
		if(ret instanceof ResponseEntity) {
			swaggerDataHandler.process((ResponseEntity)ret);
		}
		
		if(ret==null && exception!=null) {
			Result r=new Result();
			r.extra().setException(StringUtil.toString(exception));
			r=ErrorDesc.exception(r);
			ret=r;
		}
		
		//如果是 Result 对象设置额外信息
		if(ret instanceof Result) {
			Result r=(Result)ret;
			r.extra().setCost(System.currentTimeMillis()-t);
			r.extra().setTid(traceId);
			r.extra().setTime(System.currentTimeMillis());
			r.extra().setDataType(EntityContext.convertProxyName(r.extra().getDataType()));
			r.extra().setComponentType(EntityContext.convertProxyName(r.extra().getComponentType()));
			r.extra().setMethod(method.getDeclaringClass().getName()+"."+method.getName());
			//
			if(r.data() instanceof PagedList) {
				((PagedList)r.data()).clearMeta();
			}
			//做一些适当的补充
			if(StringUtil.isBlank(r.code()) && r.success()) {
				r.code(CommonError.SUCCESS);
			}
		}
		t=System.currentTimeMillis()-t;
		return ret;
	}
 
}
