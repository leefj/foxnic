package com.github.foxnic.springboot.api.swagger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.compiler.source.ControllerCompilationUnit;
import com.github.foxnic.commons.compiler.source.JavaCompilationUnit;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.commons.reflect.JavassistUtil;
import com.github.foxnic.springboot.spring.SpringUtil;
import com.github.foxnic.springboot.starter.BootArgs;
import com.github.foxnic.springboot.web.WebContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.spring.web.json.Json;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SwaggerDataHandler {

	public void process(ResponseEntity responseEntity) {
		Json body = (Json) responseEntity.getBody();
		String value = BeanUtil.getFieldValue(body, "value", String.class);
		JSONObject data = JSONObject.parseObject(value);
		if (data.containsKey("swagger") && data.containsKey("paths")) {
			value = process(data);
			BeanUtil.setFieldValue(body, "value", value);
		}
	}

	private String process(JSONObject data) {

		WebContext ctx=SpringUtil.getBean(WebContext.class);

		JSONObject metaData=new JSONObject();

		JSONObject paths=data.getJSONObject("paths");
		for (String path : paths.keySet()) {
			//循环参数，并获得参数对应的注解
			JSONObject cfg=paths.getJSONObject(path);
			for (String httpMethod : cfg.keySet()) {
				JSONObject httpMethodCfg=cfg.getJSONObject(httpMethod);
				HandlerMethod hm=ctx.getHandlerMethod(path,httpMethod);
				Method method=hm.getMethod();
				Class controller=method.getDeclaringClass();
				String location="";
				int line=JavassistUtil.getLineNumber(method);
				if(line>0) {
					location=controller.getSimpleName()+".java:"+line;
				}
				httpMethodCfg.put("javaMethod",controller.getName()+"."+method.getName()+"("+location+")");
				if(BootArgs.isBootInIDE()) {
					ControllerCompilationUnit jcu=new ControllerCompilationUnit(controller);
					if(jcu.isValid()) {

					}
				}

				// 处理参数
				Parameter[] methodParameters = method.getParameters();
				Map<String,Parameter> methodParameterMap=new HashMap<>();
				for (Parameter parameter : methodParameters) {
					methodParameterMap.put(parameter.getName(),parameter);
				}
				JSONArray parameters=httpMethodCfg.getJSONArray("parameters");
				if(parameters!=null) {
					for (int i = 0; i < parameters.size(); i++) {
						JSONObject param=parameters.getJSONObject(i);
						Parameter methodParameter=methodParameterMap.get(param.getString("name"));
						if(methodParameter!=null) {
							param.put("javaType",methodParameter.getType().getName());
							if(param.getString("type")==null) {
								param.put("type", "object");
							}
						}
					}


				}

				System.out.println();

//				List<String> removeNames=new ArrayList<>();
//				for (int i = 0; i < parameters.size(); i++) {
//					String paramName=parameters.getJSONObject(i).getString("name");
//					String description=parameters.getJSONObject(i).getString("description");
//					if(StringUtil.isBlank(description)) {
//						removeNames.add(paramName);
//					}
//					List<ValidateAnnotation> vs=parameterValidateManager.getValidators(method,paramName);
//					if(vs==null) continue;
//					description = "<div style='display: flex;flex-direction:row;'><div class='field-label'>"+description+"</div>";
//					for (ValidateAnnotation va : vs) {
////						ValidateAnnotation va=new ValidateAnnotation(an);
//						String tag=va.getAnnotationName();
//						description+="<div class='validation-tag'>"+tag+"</div>";
//					}
//					description+="</div>";
//					parameters.getJSONObject(i).put("description", description);
//				}
//
//				//移除没有description的参数
//				for (String name : removeNames) {
//					for (int i = 0; i < parameters.size(); i++) {
//						String paramName=parameters.getJSONObject(i).getString("name");
//						if(name.equals(paramName)) {
//							parameters.remove(i);
//							break;
//						}
//					}
//				}

			}


		}

		return data.toJSONString();

	}

}
