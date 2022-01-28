package com.github.foxnic.springboot.mvc;

import com.github.foxnic.api.error.CommonError;
import com.github.foxnic.api.error.ErrorDesc;
import com.github.foxnic.api.proxy.InvokeSource;
import com.github.foxnic.api.proxy.InvokeSourceVar;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.springboot.api.swagger.SwaggerDataHandler;
import com.github.foxnic.springboot.api.validator.ParameterValidateManager;
import com.github.foxnic.springboot.spring.SpringUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;




@Aspect
@Component
public class ControllerAspector {

	@Autowired
	private SwaggerDataHandler swaggerDataHandler;

	@Autowired
	private ParameterValidateManager parameterValidateManager;

	@Autowired
	private ParameterHandler parameterHandler;

	private InvokeLogService invokeLogService;

	@PostConstruct
	private void init() {
		invokeLogService=SpringUtil.getBean(InvokeLogService.class);
		Logger.info("ControllerAspector Init");
	}

	@Pointcut(value = "@annotation(org.springframework.web.bind.annotation.RequestMapping)")
	public void pointCut4RequestMapping() {}

	@Pointcut(value = "@annotation(org.springframework.web.bind.annotation.PostMapping)")
	public void pointCut4PostMapping() {}

	@Pointcut(value = "@annotation(org.springframework.web.bind.annotation.GetMapping)")
	public void pointCut4GetMapping() {}

	@Around("com.github.foxnic.springboot.mvc.ControllerAspector.pointCut4RequestMapping()")
	public Object processRequestMapping(ProceedingJoinPoint joinPoint) throws Throwable {
		return processControllerMethod(joinPoint,RequestMapping.class);
	}

	@Around("com.github.foxnic.springboot.mvc.ControllerAspector.pointCut4PostMapping()")
	public Object processPostMapping(ProceedingJoinPoint joinPoint) throws Throwable {
		return processControllerMethod(joinPoint,PostMapping.class);
	}

	@Around("com.github.foxnic.springboot.mvc.ControllerAspector.pointCut4GetMapping()")
	public Object processGetMapping(ProceedingJoinPoint joinPoint) throws Throwable {
		return processControllerMethod(joinPoint,GetMapping.class);
	}

	public static interface  ArgHandler {
		Object[] handle(Object[] args);
	}

	private List<ArgHandler> argsHandlers=new ArrayList<>();
	public void  addArgHandler(ArgHandler handler) {
		argsHandlers.add(handler);
	}

	/**
	 * 拦截控制器方法，进行预处理
	 * */
	private Object processControllerMethod(ProceedingJoinPoint joinPoint,Class mappingType) throws Throwable {


		RequestParameter requestParameter=RequestParameter.get();
		InvokeSource invokeSource=getInvokeSource(requestParameter,joinPoint);
		String uri=requestParameter.getRequest().getRequestURI();
		String url=requestParameter.getRequest().getRequestURL().toString();
		if(invokeSource==InvokeSource.PROXY_INTERNAL){
			return joinPoint.proceed();
		}

		MethodSignature ms=(MethodSignature)joinPoint.getSignature();
		Method method=ms.getMethod();
		RestController rc=method.getDeclaringClass().getAnnotation(RestController.class);
		if(rc==null) {
			return joinPoint.proceed();
		}


		if(invokeLogService!=null) {
			invokeLogService.start(requestParameter);
		}

		Long t=System.currentTimeMillis();



		String traceId=requestParameter.getTraceId();
		//加入 TID 信息
		Logger.setTID(traceId);

		if(method==null) {
			return joinPoint.proceed();
		}

		if(method.getDeclaringClass().equals(BasicErrorController.class)) {
			return joinPoint.proceed();
		}

		invokeLogService.logRequest(method,uri,url,requestParameter.getRequestBody());

		//转换参数
		Object[] args=parameterHandler.process(method,requestParameter,joinPoint.getArgs());

		//校验参数
		List<Result> results=parameterValidateManager.validate(method,requestParameter,args);
		if(results!=null && !results.isEmpty()) {
			Result r=ErrorDesc.failure(CommonError.PARAM_INVALID);
			r.addErrors(results);
			return r;
		}

		for (ArgHandler argsHandler : argsHandlers) {
			args=argsHandler.handle(args);
		}

		Object ret=null;
		Throwable exception=null;
		try {
			ret = joinPoint.proceed(args);
		} catch (Throwable e) {
			exception=e;
			Logger.error("invoke error", e);
			if(invokeLogService!=null) {
				invokeLogService.exception(exception);
			}
		}

		if(ret instanceof ResponseEntity) {
			swaggerDataHandler.process((ResponseEntity)ret);
		}

		if(ret==null && exception!=null) {
			Result r=null;
			//针对SpringSecurity权限访问异常的处理
			if(exception.getClass().getName().equals("org.springframework.security.access.AccessDeniedException")) {
				StackTraceElement[] es=exception.getStackTrace();
				r=ErrorDesc.failure(CommonError.PERMISSION_REQUIRED);
				for (StackTraceElement e : es) {
					if(e.getClassName().startsWith("org.springframework.")) continue;
					String clsName=e.getClassName().substring(0,e.getClassName().indexOf("$$"));
					String methodName=e.getMethodName();
					Class cls=ReflectUtil.forName(clsName);
					if(cls!=null) {
						 r.message("权限不足，不允许调用 "+clsName+"."+methodName+"() 方法");
						 break;
					}
				}
			}
			else {
				r=ErrorDesc.exception(exception);
				if(StringUtil.hasContent(exception.getMessage())){
					r.message(r.message()+" : "+exception.getMessage());
				}
				Logger.exception("请求异常",exception);
			}
			ret=r;
		}

		//如果是 Result 对象设置额外信息
		if(ret instanceof Result) {
			Result r=(Result)ret;
			r.extra().setCost(System.currentTimeMillis()-t);
			r.extra().setTid(traceId);
			r.extra().setTime(System.currentTimeMillis());
			r.extra().setDataType(EntityContext.getPoClassName(r.extra().getDataType()));
			r.extra().setComponentType(EntityContext.getPoClassName(r.extra().getComponentType()));
			r.extra().setMethod(method.getDeclaringClass().getName()+"."+method.getName());
			//
			if(r.data() instanceof PagedList) {
				//((PagedList)r.data()).clearMeta();
			}
			//做一些适当的补充
			if(StringUtil.isBlank(r.code()) && r.success()) {
				r.code(CommonError.SUCCESS);
				if(StringUtil.isBlank(r.message())) {
					r.message(ErrorDesc.get(CommonError.SUCCESS).getMessage());
				}
			}
		}
		t=System.currentTimeMillis()-t;

		if(invokeLogService!=null) {
			invokeLogService.response(ret);
		}

		return ret;
	}

	private InvokeSource getInvokeSource(RequestParameter requestParameter,ProceedingJoinPoint joinPoint) {
		InvokeSource source=InvokeSource.HTTP_REQUEST;
		if("1".equals(requestParameter.getHeader().get("is-feign")) && requestParameter.getHeader().get("invoke-from")!=null) {
			source=InvokeSource.PROXY_EXTERNAL;
		} else {
			source=InvokeSourceVar.get();
			if(source==null) {
				source=InvokeSource.HTTP_REQUEST;
			}
			//joinPoint.getTarget();
			//joinPoint.getThis();
//			StackTraceElement[] els=(new Throwable()).getStackTrace();
//			StackTraceElement e;
//			for (int i = 0; i < els.length; i++) {
//				e=els[i];
//				System.err.println(e.toString());
//				if(e.getClassName().equals(MethodProxy.class.getName())) {
//					source=InvokeSource.PROXY_INTERNAL;
//					break;
//				}
//			}
		}
		return source;
	}




}
