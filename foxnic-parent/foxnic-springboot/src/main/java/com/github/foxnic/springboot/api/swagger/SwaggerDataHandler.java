package com.github.foxnic.springboot.api.swagger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.compiler.source.ControllerCompilationUnit;
import com.github.foxnic.commons.compiler.source.JavaCompilationUnit;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.commons.reflect.JavassistUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.springboot.spring.SpringUtil;
import com.github.foxnic.springboot.starter.BootArgs;
import com.github.foxnic.springboot.web.WebContext;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.spring.web.json.Json;

import java.lang.reflect.Field;
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

		com.github.foxnic.springboot.mvc.RequestParameter requestParameter = com.github.foxnic.springboot.mvc.RequestParameter.get();
		Map<String, String> modelNameMapping = (Map<String, String>)requestParameter.getRequest().getAttribute("SWAGGER_MODEL_NAME_MAPPING");

		data.put("modelNameMapping",modelNameMapping);

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
				JSONArray newParameters=new JSONArray();
				JSONArray parameters=httpMethodCfg.getJSONArray("parameters");
				Method m=hm.getMethod();
				if("newApiName2".equals(m.getName())) {
					System.out.println();
				}
				if(parameters!=null) {
					for (int i = 0; i < parameters.size(); i++) {
						JSONObject param=parameters.getJSONObject(i);
						Parameter methodParameter=methodParameterMap.get(param.getString("name"));
						// 接口参数与方法参数一致
						if(methodParameter!=null) {
							param.put("javaType",methodParameter.getType().getName());
							if(DataParser.isSimpleType(methodParameter.getType())) {
								//
							} else {
								if (ReflectUtil.isSubType(List.class, methodParameter.getType())) {
									param.put("type", "array");
								} else {
									param.put("type", "object");
								}
							}
							newParameters.add(param);
						} else {
							// 如果有多个方法参数时，接口参数在方法参数中不存在，则移除
							if(methodParameters.length<=1) {
								newParameters.add(param);
							}
						}
					}
				}
				// 使用 newParameters 纠正参数偏差
				httpMethodCfg.put("parameters",newParameters);





			}


		}

		JSONObject definitions=data.getJSONObject("definitions");
		for (Map.Entry<String, String> e : modelNameMapping.entrySet()) {
			if(definitions.containsKey(e.getValue())) continue;
			Class type=ReflectUtil.forName(e.getValue());
			List<Field> fields=BeanUtil.getAllFields(type);
			JSONObject definition = new JSONObject();
			definition.put("type","object");
			definition.put("title",type.getSimpleName());
			JSONObject properties=new JSONObject();
			JSONArray required=new JSONArray();
			definition.put("required",required);
			definition.put("properties",properties);
			for (Field field : fields) {
				JSONObject prop=new JSONObject();
				prop.put("type",field.getType().getSimpleName());
				prop.put("javaType",field.getType().getName());
				ApiModelProperty ann=field.getAnnotation(ApiModelProperty.class);
				if(ann!=null) {
					prop.put("title", ann.value());
					prop.put("description", ann.notes());
					if(ann.required()) {
						required.add(field.getName());
					}
				}
				properties.put(field.getName(),prop);
			}

			definitions.put(type.getSimpleName(),definition);

		}

		return data.toJSONString();

	}

}
