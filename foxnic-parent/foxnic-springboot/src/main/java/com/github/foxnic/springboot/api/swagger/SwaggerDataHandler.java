package com.github.foxnic.springboot.api.swagger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.springboot.api.validator.ParameterValidateManager;
import com.github.foxnic.springboot.api.validator.ValidateAnnotation;
import com.github.foxnic.springboot.spring.SpringUtil;
import com.github.foxnic.springboot.web.WebContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.spring.web.json.Json;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Component
public class SwaggerDataHandler {
	
	@Autowired
	private ParameterValidateManager parameterValidateManager;
 
	public void process(ResponseEntity responseEntity) {
		Json body = (Json) responseEntity.getBody();
		String value = BeanUtil.getFieldValue(body, "value", String.class);
		System.out.println(value);
		JSONObject data = JSONObject.parseObject(value);
		if (data.containsKey("swagger") && data.containsKey("paths")) {
			value = process(data);
			BeanUtil.setFieldValue(body, "value", value);
		}
	}

	private String process(JSONObject data) {
 
		WebContext ctx=SpringUtil.getBean(WebContext.class);
		
		JSONObject paths=data.getJSONObject("paths");
		for (String path : paths.keySet()) {
			HandlerMethod hm=ctx.getHandlerMethod(path);
			Method method=hm.getMethod();
			parameterValidateManager.processMethod(method);
 
			//循环参数，并获得参数对应的注解
			JSONObject cfg=paths.getJSONObject(path);
			for (String httpMethod : cfg.keySet()) {
				JSONObject httpMethodCfg=cfg.getJSONObject(httpMethod);
				JSONArray parameters=httpMethodCfg.getJSONArray("parameters");
				if(parameters==null) {
					continue;
				}
				
				List<String> removeNames=new ArrayList<>();
				for (int i = 0; i < parameters.size(); i++) {
					String paramName=parameters.getJSONObject(i).getString("name");
					String description=parameters.getJSONObject(i).getString("description");
					if(StringUtil.isBlank(description)) {
						removeNames.add(paramName);
					}
					List<ValidateAnnotation> vs=parameterValidateManager.getValidators(method,paramName);
					if(vs==null) continue;
					description = "<div style='display: flex;flex-direction:row;'><div class='field-label'>"+description+"</div>";
					for (ValidateAnnotation va : vs) {
//						ValidateAnnotation va=new ValidateAnnotation(an);
						String tag=va.getAnnotationName();
						description+="<div class='validation-tag'>"+tag+"</div>";
					}
					description+="</div>";
					parameters.getJSONObject(i).put("description", description);
				}
				
				//移除没有description的参数
				for (String name : removeNames) {
					for (int i = 0; i < parameters.size(); i++) {
						String paramName=parameters.getJSONObject(i).getString("name");
						if(name.equals(paramName)) {
							parameters.remove(i);
							break;
						}
					}
				}
				
			}
			
			
		}
		
		return data.toJSONString();

	}

}
