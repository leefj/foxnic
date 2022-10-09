package com.github.foxnic.springboot.api.swagger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.collection.CollectorUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.reflect.JavassistUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.springboot.api.swagger.source.*;
import com.github.foxnic.springboot.spring.SpringUtil;
import com.github.foxnic.springboot.starter.BootArgs;
import com.github.foxnic.springboot.web.WebContext;
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

        Map<String, ControllerSwaggerCompilationUnit> jcuMap=new HashMap<>();
        Map<String, ModelSwaggerCompilationUnit> mcuMap=new HashMap<>();

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
                List<String> paramList=new ArrayList<>();
                for (Parameter parameter : method.getParameters()) {
                    paramList.add(parameter.getType().getSimpleName()+" "+parameter.getName());
                }
                httpMethodCfg.put("javaMethod", controller.getName() + "." + method.getName() + "(" + StringUtil.join(paramList,", ") + ")@"+location);

                if("insertUsingPOST".equals(httpMethodCfg.getString("operationId"))) {
                    System.out.println();
                }
                if("updateUsingPOST".equals(httpMethodCfg.getString("operationId"))) {
                    System.out.println();
                }

                ControllerSwaggerCompilationUnit jcu = jcuMap.get(controller.getName());
                if(jcu==null) {
                    jcu=new ControllerSwaggerCompilationUnit(controller);
                    jcuMap.put(controller.getName(),jcu);
                }
                Map<String,ModelAnnotations> modelAnnotationsMap=new HashMap<>();
                for (Parameter parameter : method.getParameters()) {
                    if(DataParser.isCollection(parameter.getType()) || DataParser.isSimpleType(parameter.getType())) continue;
                    ModelSwaggerCompilationUnit mcu= mcuMap.get(parameter.getType().getName());
                    if(mcu==null) {
                        mcu=new ModelSwaggerCompilationUnit(parameter.getType());
                        mcuMap.put(parameter.getType().getName(),mcu);
                    }
                    ModelAnnotations modelAnnotations= mcu.createFromClassBytes();
                    if (BootArgs.isBootInIDE()) {
                        ModelAnnotations sourceModelAnnotations=mcu.createFromSource();
                        modelAnnotations.merge(sourceModelAnnotations);
                    }
                    modelAnnotationsMap.put(parameter.getType().getName(),modelAnnotations);
                }

                MethodAnnotations methodAnnotations = jcu.createFromClassBytes(method);
                if (BootArgs.isBootInIDE()) {
                    MethodAnnotations sourceSwaggerAnnotations=jcu.createFromSource(method);
                    methodAnnotations.merge(sourceSwaggerAnnotations);
                }

                // 将最新的内容刷入到文档 Json , 当时候一个参数时考虑模型数据
                 updateMethodJson(httpMethodCfg,methodAnnotations,method.getParameterCount()==1?modelAnnotationsMap:new HashMap<>());
            }
        }


        // 处理 Model
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

    private void updateMethodJson(JSONObject httpMethodCfg, MethodAnnotations methodAnnotations,Map<String,ModelAnnotations> modelAnnotationsMap) {
        // 处理抬头
        httpMethodCfg.put("summary",methodAnnotations.getApiOperation().getValue());
        httpMethodCfg.put("description",methodAnnotations.getApiOperation().getNotes());

        if("insertUsingPOST".equals(httpMethodCfg.getString("operationId"))) {
            System.out.println();
        }
        if("updateUsingPOST".equals(httpMethodCfg.getString("operationId"))) {
            System.out.println();
        }

        ModelAnnotations modelAnnotations=null;
        if(modelAnnotationsMap.size()==1) {
            for (ModelAnnotations value : modelAnnotationsMap.values()) {
                modelAnnotations=value;
            }
        }

        Set<String> paramNames=new HashSet<>();
        if(modelAnnotations!=null) {
            paramNames.addAll(modelAnnotations.getApiModelPropertyMap().keySet());
        }
        paramNames.addAll(methodAnnotations.getParamMap().keySet());


        // 处理参数
        JSONArray parameters = httpMethodCfg.getJSONArray("parameters");
        if (parameters != null) {

            Map<String,JSONObject> parametersMap=new HashMap<>();
            // 将JSONArray转JSONObject，忽略同名参数
            for (int i = 0; i < parameters.size(); i++) {
                parametersMap.put(parameters.getJSONObject(i).getString("name"),parameters.getJSONObject(i));
            }
            CollectorUtil.CompareResult<String,String> result=CollectorUtil.compare(parametersMap.keySet(),paramNames);

            Set<String> ignoreParameters=new HashSet<>();
            Set<String> includeParameters=new HashSet<>();
            if(methodAnnotations.getApiOperationSupport()!=null) {
                if(methodAnnotations.getApiOperationSupport().getIgnoreParameters()!=null && methodAnnotations.getApiOperationSupport().getIgnoreParameters().length>0)
                {
                    ignoreParameters.addAll(Arrays.asList(methodAnnotations.getApiOperationSupport().getIgnoreParameters()));
                }

                if(methodAnnotations.getApiOperationSupport().getIncludeParameters()!=null && methodAnnotations.getApiOperationSupport().getIncludeParameters().length>0)
                {
                    includeParameters.addAll(Arrays.asList(methodAnnotations.getApiOperationSupport().getIncludeParameters()));
                }
            }


            // 移除
            // 在 ignoreParameters 的必须 remove
            for (String key : result.getIntersection()) {
                JSONObject param=parametersMap.get(key);
                if (ignoreParameters.contains(key)) {
                    param.put("willRemove", true);
                }
            }
            for (String key : result.getSourceDiff()) {
                JSONObject param=parametersMap.get(key);
                // 不在 includeParameters  内的可以 remove
                if(!includeParameters.contains(key)) {
                    param.put("willRemove", true);
                }
            }
            while(true) {
                boolean hasMore=false;
                for (int i = 0; i < parameters.size(); i++) {
                    Boolean willRemove = parameters.getJSONObject(i).getBoolean("willRemove");
                    if(willRemove!=null && willRemove==true) {
                        parameters.remove(i);
                        hasMore=true;
                    }
                }
                if(!hasMore) break;
            }



            // 更新
            for (String key : result.getIntersection()) {
                JSONObject param=parametersMap.get(key);
                SwaggerAnnotationApiModelProperty apiModelProperty = null;
                if(modelAnnotations!=null) {
                    apiModelProperty=modelAnnotations.getApiModelProperty(key);
                }
                updateParameterJson(param,apiModelProperty,methodAnnotations.getParamMap().get(key));
            }
            // 增加
            for (String key : result.getTargetDiff()) {
                // 不在 ignoreParameters 的才可以添加
                if(!ignoreParameters.contains(key)) {
                    SwaggerAnnotationApiModelProperty apiModelProperty = null;
                    if(modelAnnotations!=null) {
                        apiModelProperty=modelAnnotations.getApiModelProperty(key);
                    }
                    addParameterJson(parameters, apiModelProperty,methodAnnotations.getParamMap().get(key));
                }
            }
        }


        // 处理扩展
        SwaggerAnnotationApiOperationSupport apiOperationSupport=methodAnnotations.getApiOperationSupport();
        if(apiOperationSupport!=null) {
            httpMethodCfg.put("x-order",apiOperationSupport.getOrder());
        }



    }

    private void updateParameterJson(JSONObject param, SwaggerAnnotationApiModelProperty apiModelProperty, SwaggerAnnotationApiImplicitParam apiImplicitParam) {
        if(apiImplicitParam==null) {
            System.out.println();
        }
        //
        if(apiImplicitParam!=null) {
            param.put("description", apiImplicitParam.getValue());
            param.put("example", apiImplicitParam.getExample());
            param.put("required", apiImplicitParam.isRequired());
            param.put("default", apiImplicitParam.getDefaultValue());
            if (StringUtil.isBlank(apiImplicitParam.getParamType())) {
                param.put("in", "query");
            } else {
                param.put("in", apiImplicitParam.getParamType());
            }
            this.applyType(param, apiImplicitParam.getDataTypeClass(), getTypeArguments(apiImplicitParam.getParameter()));
        }
        //
        if(apiModelProperty!=null) {
            param.put("description", apiModelProperty.getNotes());
            param.put("example", apiModelProperty.getExample());
            param.put("required", apiModelProperty.isRequired());
            if(param.getString("in")==null) {
                param.put("in", "body");
            }
            this.applyType(param, apiModelProperty.getField().getType(), getTypeArguments(apiModelProperty.getField()));
        }
    }

    private void addParameterJson(JSONArray parameters, SwaggerAnnotationApiModelProperty apiModelProperty, SwaggerAnnotationApiImplicitParam apiImplicitParam) {
        JSONObject param=new JSONObject();
        param.put("name",apiImplicitParam.getName());
        updateParameterJson(param,apiModelProperty,apiImplicitParam);
        parameters.add(param);
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


//        if (DataParser.isList(type) || DataParser.isSet(type)) {
//            param.put("type", "array");
//            if (methodParameter != null) {
//                ParameterizedType type = (ParameterizedType) methodParameter.getParameterizedType();
//                Type[] args = type.getActualTypeArguments();
//                Class cmpType = (Class) args[0];
//                param.put("componentOriginalRef", cmpType.getSimpleName());
//                param.put("$componentRef", "#/definitions/" + cmpType.getSimpleName());
//                param.put("componentJavaType", cmpType.getName());
//                modelNameMapping.put(cmpType.getSimpleName(),cmpType.getName());
//            }
//        } else if (DataParser.isArray(methodParameterType)) {
//            param.put("type", "array");
////                                    JSONObject items=new JSONObject();
////                                    items.put("type",)
//            param.put("componentOriginalRef", methodParameterType.getComponentType().getSimpleName());
//            param.put("$componentRef", "#/definitions/" + methodParameterType.getComponentType().getSimpleName());
//            param.put("componentJavaType", methodParameterType.getComponentType().getName());
//            modelNameMapping.put(methodParameterType.getComponentType().getSimpleName(),methodParameterType.getComponentType().getName());
//        } else {
//            param.put("originalRef", methodParameterType.getSimpleName());
//            param.put("$ref", "#/definitions/" + methodParameterType.getSimpleName());
//            param.put("type", "object");
//        }


    }




}
