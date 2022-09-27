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
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.spring.web.json.Json;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

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

				Map<String,Class> paramTypeMap=new HashMap<>();
				// 处理参数类型
				Parameter[] methodParameters = method.getParameters();
				for (Parameter parameter : methodParameters) {
					paramTypeMap.put(parameter.getName(),parameter.getType());
				}
				ApiImplicitParams apiImplicitParams=method.getAnnotation(ApiImplicitParams.class);
				if(apiImplicitParams!=null) {
					ApiImplicitParam[] apiImplicitParamArr=apiImplicitParams.value();
					for (ApiImplicitParam param : apiImplicitParamArr) {
						paramTypeMap.put(param.name(),param.dataTypeClass());
					}
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
						Class methodParameterType=paramTypeMap.get(param.getString("name"));
						// 接口参数与方法参数一致
						if(methodParameterType!=null) {
							param.put("javaType",methodParameterType.getName());
							if(DataParser.isSimpleType(methodParameterType)) {
								//
							} else {
								if (DataParser.isList(methodParameterType) || DataParser.isSet(methodParameterType)) {
									param.put("type", "array");
								} else {
									param.put("originalRef",methodParameterType.getSimpleName());
									param.put("$ref","#/definitions/"+methodParameterType.getSimpleName());
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
			Class type=ReflectUtil.forName(e.getValue());
			List<Field> fields=BeanUtil.getAllFields(type);
			JSONObject definition = null;
			JSONObject properties = null;
			ApiModel apiModel=(ApiModel)type.getAnnotation(ApiModel.class);
			if(definitions.containsKey(e.getKey())) {
				definition=definitions.getJSONObject(e.getKey());
				if(apiModel!=null) {
					definition.put("description",apiModel.description());
					definition.put("javaType",type.getName());
				}
				properties=definition.getJSONObject("properties");
				for (Field field : fields) {
					JSONObject prop=properties.getJSONObject(field.getName());
					if(prop==null) continue;
					if(prop.getString("type")==null) {
						if(!DataParser.isSimpleType(field.getType()) && !DataParser.isCollection(field.getType())) {
							prop.put("type","object");
						}
					}
				}
				continue;
			}


			definition = new JSONObject();
			definition.put("type","object");
			definition.put("javaType",type.getName());
			definition.put("title",type.getSimpleName());
			if(apiModel!=null) {
				definition.put("description",apiModel.description());
			}
			properties=new JSONObject();
			JSONArray required=new JSONArray();
			definition.put("required",required);
			definition.put("properties",properties);
			for (Field field : fields) {
				JSONObject prop=new JSONObject();
				if(String.class.equals(field.getType())) {
					prop.put("type", "string");
				} else {
					prop.put("type", "unknown");
				}
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
