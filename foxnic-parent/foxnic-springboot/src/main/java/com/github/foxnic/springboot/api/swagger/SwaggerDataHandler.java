package com.github.foxnic.springboot.api.swagger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.reflect.JavassistUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.springboot.spring.SpringUtil;
import com.github.foxnic.springboot.starter.BootArgs;
import com.github.foxnic.springboot.web.WebContext;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.spring.web.json.Json;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
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
        Map<String, String> modelNameMapping = (Map<String, String>) requestParameter.getRequest().getAttribute("SWAGGER_MODEL_NAME_MAPPING");

        WebContext ctx = SpringUtil.getBean(WebContext.class);

        Map<String,ControllerSwaggerCompilationUnit> jcuMap=new HashMap<>();

        JSONObject paths = data.getJSONObject("paths");
        // 遍历路径
        for (String path : paths.keySet()) {
            JSONObject cfg = paths.getJSONObject(path);
            //循环请求方法，并获得参数对应的注解
            for (String httpMethod : cfg.keySet()) {
                JSONObject httpMethodCfg = cfg.getJSONObject(httpMethod);
                HandlerMethod hm = ctx.getHandlerMethod(path, httpMethod);
                Method method = hm.getMethod();
                Class controller = method.getDeclaringClass();
                String location = "";
                int line = JavassistUtil.getLineNumber(method);
                if (line > 0) {
                    location = controller.getSimpleName() + ".java:" + line;
                }
                httpMethodCfg.put("javaMethod", controller.getName() + "." + method.getName() + "(" + location + ")");

                // 获得注解的参数列表
                ApiImplicitParams apiImplicitParams=method.getAnnotation(ApiImplicitParams.class);
                ApiImplicitParam[] apiImplicitParamArr = null;
                if(apiImplicitParams!=null) {
                    apiImplicitParamArr=apiImplicitParams.value();
                }
                if (BootArgs.isBootInIDE()) {
                    ControllerSwaggerCompilationUnit jcu = jcuMap.get(controller.getName());
                    if(jcu==null) {
                        jcu=new ControllerSwaggerCompilationUnit(controller);
                        jcuMap.put(controller.getName(),jcu);
                    }
                    ControllerSwaggerCompilationUnit.SwaggerAnnotationUnit swaggerAnnotationUnit=jcu.getSwaggerAnnotationUnit(method);
                    System.out.println();
                }


                Map<String, Class> paramTypeMap = new HashMap<>();
                Map<String, Parameter> paramMap = new HashMap<>();
                // 处理参数类型
                Parameter[] methodParameters = method.getParameters();
                for (Parameter parameter : methodParameters) {
                    paramTypeMap.put(parameter.getName(), parameter.getType());
                    paramMap.put(parameter.getName(), parameter);
                }

                if (apiImplicitParamArr != null) {
                    for (ApiImplicitParam param : apiImplicitParamArr) {
                        paramTypeMap.put(param.name(), param.dataTypeClass());
                    }
                }

                JSONArray newParameters = new JSONArray();
                JSONArray parameters = httpMethodCfg.getJSONArray("parameters");
                Method m = hm.getMethod();
                if ("newApiName2".equals(m.getName())) {
                    System.out.println();
                }
                if (parameters != null) {
                    for (int i = 0; i < parameters.size(); i++) {
                        JSONObject param = parameters.getJSONObject(i);
                        Class methodParameterType = paramTypeMap.get(param.getString("name"));
                        Parameter methodParameter = paramMap.get(param.getString("name"));
                        // 接口参数与方法参数一致
                        if (methodParameterType != null) {
//                            param.put("javaType", methodParameterType.getName());
//                            if (DataParser.isSimpleType(methodParameterType)) {
//                                //
//                            } else {
                            this.applyType(param,methodParameterType,getTypeArguments(methodParameter));
//                            }
                            newParameters.add(param);
                        } else {
                            // 如果有多个方法参数时，接口参数在方法参数中不存在，则移除
                            if (methodParameters.length <= 1) {
                                newParameters.add(param);
                            }
                        }
                    }
                }
                // 使用 newParameters 纠正参数偏差
                httpMethodCfg.put("parameters", newParameters);

            }
        }

        JSONObject definitions = data.getJSONObject("definitions");
        for (Map.Entry<String, String> e : modelNameMapping.entrySet()) {
            Class type = ReflectUtil.forName(e.getValue());
            List<Field> fields = BeanUtil.getAllFields(type);
            JSONObject definition = null;
            JSONObject properties = null;
            ApiModel apiModel = (ApiModel) type.getAnnotation(ApiModel.class);

            // 处理已存在的 Model
            if (definitions.containsKey(e.getKey())) {
                definition = definitions.getJSONObject(e.getKey());
                if (apiModel != null) {
                    definition.put("description", apiModel.description());
                    definition.put("javaType", type.getName());
                }
                properties = definition.getJSONObject("properties");
                for (Field field : fields) {
                    JSONObject prop = properties.getJSONObject(field.getName());
                    if (prop == null) continue;
                    if (prop.getString("type") == null) {
                        if (!DataParser.isSimpleType(field.getType()) && !DataParser.isCollection(field.getType())) {
                            prop.put("type", "object");
                        }
                    }
                }
                continue;
            }


            // 创建不存在的 Model
            definition = new JSONObject();
            definition.put("type", "object");
            definition.put("javaType", type.getName());
            definition.put("title", type.getSimpleName());
            if (apiModel != null) {
                definition.put("description", apiModel.description());
            }
            properties = new JSONObject();
            JSONArray required = new JSONArray();
            definition.put("required", required);
            definition.put("properties", properties);
            for (Field field : fields) {
                JSONObject prop = new JSONObject();
                this.applyType(prop,field.getType(),getTypeArguments(field));
                ApiModelProperty ann = field.getAnnotation(ApiModelProperty.class);
                if (ann != null) {
                    prop.put("title", ann.value());
                    prop.put("description", ann.notes());
                    if (ann.required()) {
                        required.add(field.getName());
                    }
                }
                properties.put(field.getName(), prop);
            }
            definitions.put(type.getSimpleName(), definition);
        }
        //
        data.put("modelNameMapping", modelNameMapping);
        //
        return data.toJSONString();
    }

    public String getTypeName(Class type) {
        if (String.class.equals(type) || DataParser.isDateTimeType(type)) {
            return "string";
        } else if (Integer.class.equals(type) || Long.class.equals(type) || Short.class.equals(type) || BigInteger.class.equals(type)) {
            return "integer";
        } else if (Float.class.equals(type) || Double.class.equals(type) || BigDecimal.class.equals(type)) {
            return "number";
        } else if (Boolean.class.equals(type)) {
            return "boolean";
        } else if (DataParser.isArray(type)) {
            return "array";
        } else if (DataParser.isList(type) || DataParser.isSet(type)) {
            return "array";
        } else if (DataParser.isMap(type)) {
            return "object";
        } else {
            return "object";
        }
    }

    private Class[] getTypeArguments(Parameter parameter) {
        if(parameter==null) {
            return null;
        }

        if(!DataParser.isCollection(parameter.getType())) return null;

        if(parameter.getType().isArray()) {
            return new Class[] {parameter.getType().getComponentType()};
        }

        if(parameter.getParameterizedType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
            if (parameterizedType == null) return null;
            try {
                Type[] types = parameterizedType.getActualTypeArguments();
                Class[] clzs = new Class[types.length];
                for (int i = 0; i < types.length; i++) {
                    clzs[i] = (Class) types[i];
                }
                return clzs;
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    private Class[] getTypeArguments(Field field) {
        if(!DataParser.isCollection(field.getType())) return null;
        if(field.getType().isArray()) {
            return new Class[] {field.getType().getComponentType()};
        }

        if(field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            if (parameterizedType == null) return null;
            try {
                Type[] types = parameterizedType.getActualTypeArguments();
                Class[] clzs = new Class[types.length];
                for (int i = 0; i < types.length; i++) {
                    clzs[i] = (Class) types[i];
                }
                return clzs;
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public void applyType(JSONObject target,Class type,Class[] typeArguments) {

        String typeName=this.getTypeName(type);
        target.put("type", typeName);
        target.put("javaType", type.getName());
        target.put("originalRef", type.getSimpleName());
        target.put("$ref", "#/definitions/"+type.getSimpleName());
        //
        if("array".equals(typeName) && typeArguments!=null) {
            JSONObject items=new JSONObject();
            Class cmpType = (Class) typeArguments[0];
            items.put("type",getTypeName(cmpType));
            items.put("originalRef", cmpType.getSimpleName());
            items.put("$ref", "#/definitions/" + cmpType.getSimpleName());
            items.put("javaType", cmpType.getName());
            target.put("items", items);
        }

        if("object".equals(typeName) && typeArguments!=null && DataParser.isMap(type)) {
            target.put("keyType",getTypeName(typeArguments[0]));
            target.put("valueType",getTypeName(typeArguments[1]));
            JSONObject items=new JSONObject();
            Class cmpType = (Class) typeArguments[1];
            items.put("type",getTypeName(cmpType));
            items.put("originalRef", cmpType.getSimpleName());
            items.put("$ref", "#/definitions/" + cmpType.getSimpleName());
            items.put("javaType", cmpType.getName());
            target.put("items", items);
        }

        if(DataParser.isMap(type)) {
            target.put("collectionFormat", "map");
        }


    }


//    if (DataParser.isList(methodParameterType) || DataParser.isSet(methodParameterType)) {
//        param.put("type", "array");
//        if (methodParameter != null) {
//            ParameterizedType type = (ParameterizedType) methodParameter.getParameterizedType();
//            Type[] args = type.getActualTypeArguments();
//            Class cmpType = (Class) args[0];
//            param.put("componentOriginalRef", cmpType.getSimpleName());
//            param.put("$componentRef", "#/definitions/" + cmpType.getSimpleName());
//            param.put("componentJavaType", cmpType.getName());
//            modelNameMapping.put(cmpType.getSimpleName(),cmpType.getName());
//        }
//    } else if (DataParser.isArray(methodParameterType)) {
//        param.put("type", "array");
////                                    JSONObject items=new JSONObject();
////                                    items.put("type",)
//        param.put("componentOriginalRef", methodParameterType.getComponentType().getSimpleName());
//        param.put("$componentRef", "#/definitions/" + methodParameterType.getComponentType().getSimpleName());
//        param.put("componentJavaType", methodParameterType.getComponentType().getName());
//        modelNameMapping.put(methodParameterType.getComponentType().getSimpleName(),methodParameterType.getComponentType().getName());
//    } else {
//        param.put("originalRef", methodParameterType.getSimpleName());
//        param.put("$ref", "#/definitions/" + methodParameterType.getSimpleName());
//        param.put("type", "object");
//    }

}
