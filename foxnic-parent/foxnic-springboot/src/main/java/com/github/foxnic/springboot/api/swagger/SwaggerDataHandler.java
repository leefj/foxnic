package com.github.foxnic.springboot.api.swagger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.api.error.CommonError;
import com.github.foxnic.api.error.ErrorDefinition;
import com.github.foxnic.api.error.ErrorDesc;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.collection.CollectorUtil;
import com.github.foxnic.commons.json.JSONUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.reflect.JavassistUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.springboot.api.swagger.source.*;
import com.github.foxnic.springboot.spring.SpringUtil;
import com.github.foxnic.springboot.starter.BootArgs;
import com.github.foxnic.springboot.web.WebContext;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import io.swagger.annotations.Api;
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
        JSONObject data = null;
        try {
             data = JSONObject.parseObject(value);
        } catch (Exception e) {
            Logger.exception(e);
        }
        if (data!=null && data.containsKey("swagger") && data.containsKey("paths")) {
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

        JSONObject definitions = data.getJSONObject("definitions");
        if(definitions==null) definitions=new JSONObject();

        Set<String> tagNames=new HashSet<>();
        JSONArray tags = data.getJSONArray("tags");
        if(tags==null) tags=new JSONArray();
        for (int i = 0; i < tags.size() ; i++) {
            tagNames.add(tags.getJSONObject(i).getString("name"));
        }

        JSONObject paths = data.getJSONObject("paths");

        Map<Method,MethodAnnotations> methodAnnotationMap=new HashMap<>();
        Map<Class,String> controllerMainTagMap=new HashMap<>();
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

                String methodImpl=controller.getName() + "." + method.getName() + "(" + StringUtil.join(paramList,", ") + ")@"+location;
                // String methodImpl2=controller.getName() + "." + method.getName() + "(" + location + ")";

                httpMethodCfg.put("javaMethod", methodImpl);


                ControllerSwaggerCompilationUnit jcu = jcuMap.get(controller.getName());
                if(jcu==null) {
                    jcu=new ControllerSwaggerCompilationUnit(controller);
                    jcuMap.put(controller.getName(),jcu);
                }

                // 处理 tag
                List<AnnotationExpr> anns=jcu.findClassAnnotation(controller.getSimpleName(), Api.class.getSimpleName());
                SwaggerAnnotationApi apiAnSrc = null;
                if(anns!=null && anns.size()>0) {
                    apiAnSrc=SwaggerAnnotationApi.fromSource((NormalAnnotationExpr) anns.get(0),jcu);
                }
                Api api=(Api)controller.getAnnotation(Api.class);
                SwaggerAnnotationApi apiAnn=SwaggerAnnotationApi.fromAnnotation(api);
                if (apiAnSrc!=null) {
                    apiAnn=apiAnSrc;
                }
                String mainTag=controllerMainTagMap.get(controller);
                for (String tag : apiAnn.getTags()) {
                    if(!tagNames.contains(tag)) {
                        tagNames.add(tag);
                        JSONObject tagItem=new JSONObject();
                        tagItem.put("name",tag);
                        tags.add(tagItem);
                        if(mainTag==null) {
                            mainTag=tag;
                            controllerMainTagMap.put(controller,mainTag);
                        }
                    }
                }
                if(mainTag!=null) {
                    httpMethodCfg.getJSONArray("tags").set(0,mainTag);
                }






                //
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
                        if(mcu.isValid()) {
                            ModelAnnotations sourceModelAnnotations = mcu.createFromSource();
                            modelAnnotations.merge(sourceModelAnnotations);
                        }
                    }
                    modelAnnotationsMap.put(parameter.getType().getName(),modelAnnotations);
                }

                //
                MethodAnnotations methodAnnotations = jcu.createMethodAnnotationsFromClassBytes(method);
                if (BootArgs.isBootInIDE()) {
                    if(jcu.isValid()) {
                        MethodAnnotations sourceSwaggerAnnotations = jcu.createMethodAnnotationsFromSource(method);
                        methodAnnotations.merge(sourceSwaggerAnnotations);
                    }
                }
                methodAnnotationMap.put(method,methodAnnotations);


                // 将最新的内容刷入到文档 Json , 当时候一个参数时考虑模型数据
                updateMethodJson(httpMethodCfg,methodAnnotations,method.getParameterCount()==1?modelAnnotationsMap:new HashMap<>());



                // 处理响应码
                JSONObject responses = httpMethodCfg.getJSONObject("responses");
                JSONObject r200=responses.getJSONObject("200");



                // 处理 DynamicResponseParameter 相应模型
                if(r200!=null && r200.getJSONObject("schema")!=null && r200.getJSONObject("schema").getString("originalRef")==null) {
                    SwaggerAnnotationDynamicResponseParameters dynamicResponseParameters=methodAnnotations.getDynamicResponseParameters();
                    if(dynamicResponseParameters!=null) {
                        r200.getJSONObject("schema").put("originalRef",dynamicResponseParameters.getName()+"Response");
                        r200.getJSONObject("schema").put("$ref","#/definitions/"+dynamicResponseParameters.getName()+"Response");
                    }

                }
            }
        }


        // 处理 Model
        for (Map.Entry<String, String> e : modelNameMapping.entrySet()) {
            Class type = ReflectUtil.forName(e.getValue());
            List<Field> fields = BeanUtil.getAllFields(type);
            JSONObject definition = null;
            JSONObject properties = null;
            ApiModel apiModel = (ApiModel) type.getAnnotation(ApiModel.class);

            ModelSwaggerCompilationUnit mcu=new ModelSwaggerCompilationUnit(type);
            ModelAnnotations modelAnnotations= mcu.createFromClassBytes();
            if (BootArgs.isBootInIDE()) {
                if(mcu.isValid()) {
                    ModelAnnotations sourceModelAnnotations = mcu.createFromSource();
                    modelAnnotations.merge(sourceModelAnnotations);
                }
            }

            // 处理已存在的 Model
            if (definitions.containsKey(e.getKey())) {
                definition = definitions.getJSONObject(e.getKey());
                definition.put("javaType", type.getName());
                if (apiModel != null) {
                    definition.put("description", apiModel.description());
                }
                properties = definition.getJSONObject("properties");
                for (Field field : fields) {
                    JSONObject prop = properties.getJSONObject(field.getName());
                    if (prop == null) continue;
                    if(modelAnnotations!=null) {
                        SwaggerAnnotationApiModelProperty apiModelProperty = modelAnnotations.getApiModelProperty(field.getName());
                        if (apiModelProperty != null) {
                            prop.put("title", apiModelProperty.getValue());
                            prop.put("description", apiModelProperty.getNotes());
                        }
                    }
                    if (prop.getString("type") == null) {
                        if (!DataParser.isSimpleType(field.getType()) && !DataParser.isCollection(field.getType())) {
                            prop.put("type", "object");
                        }
                    }
                    if(StringUtil.isBlank(prop.getString("type"))) {
                        prop.put("title",  prop.getString("description"));
                    }


                    if(StringUtil.isBlank(prop.getString("title"))) {
                        prop.put("title",  prop.getString("description"));
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
                SwaggerAnnotationApiModelProperty apiModelProperty = modelAnnotations.getApiModelProperty(field.getName());

                if(ann!=null && apiModelProperty==null) {
                    System.out.println();
                }

                if (apiModelProperty != null) {
                    prop.put("title", apiModelProperty.getValue());
                    prop.put("description", apiModelProperty.getNotes());
                    if (apiModelProperty.isRequired()) {
                        required.add(field.getName());
                    }
                } else {
                    prop.put("title", field.getName());
                }

                if(StringUtil.isBlank(prop.getString("title"))) {
                    prop.put("title",  prop.getString("description"));
                }

                if(StringUtil.isBlank(prop.getString("type"))) {
                    prop.put("title",  prop.getString("description"));
                }

                properties.put(field.getName(), prop);
            }
            definitions.put(type.getSimpleName(), definition);
        }

        // 遍历路径
        for (String path : paths.keySet()) {
            JSONObject cfg = paths.getJSONObject(path);
            //循环请求方法，并获得参数对应的注解
            for (String httpMethod : cfg.keySet()) {
                JSONObject httpMethodCfg = cfg.getJSONObject(httpMethod);
                // 处理响应码
                JSONObject responses = httpMethodCfg.getJSONObject("responses");
                JSONObject r200=responses.getJSONObject("200");

                HandlerMethod hm = ctx.getHandlerMethod(path, httpMethod);
                Method method = hm.getMethod();
                MethodAnnotations methodAnnotations=methodAnnotationMap.get(method);

                // 处理常规响应模型覆盖
                Map<String, SwaggerAnnotationApiResponseModel> responseModelMap=methodAnnotations.getResponseModelMap();
                if(responseModelMap!=null && !responseModelMap.isEmpty()) {
                    if(r200!=null && r200.getJSONObject("schema")!=null && r200.getJSONObject("schema").getString("originalRef")!=null) {
                        JSONObject schema=   r200.getJSONObject("schema");
                        String originalRef=schema.getString("originalRef");
                        String newOriginalRef=originalRef;
                        JSONObject topModel=definitions.getJSONObject(originalRef);
                        // topModel= topModel.clone();
                        topModel= JSONUtil.parseJSONObject(topModel.toJSONString());
                        JSONObject topModelProps=topModel.getJSONObject("properties");
                        for (SwaggerAnnotationApiResponseModel m : responseModelMap.values()) {
                            // 处理基础模型
                            JSONObject baseModel=definitions.getJSONObject(m.getBaseModelType().getSimpleName());
                            if(baseModel!=null) {
                                baseModel= JSONUtil.parseJSONObject(baseModel.toJSONString());
                                if (newOriginalRef.contains("«" + m.getBaseModelType().getSimpleName() + "»")) {
                                    newOriginalRef=newOriginalRef.replaceAll("«" + m.getBaseModelType().getSimpleName() + "»","«" + m.getName()+ "»");
                                }
                            }


                            for (String pName : topModelProps.keySet()) {
                                JSONObject topModelProp=topModelProps.getJSONObject(pName);
                                String propOriginalRef=topModelProp.getString("originalRef");
                                if(propOriginalRef!=null && propOriginalRef.equals(m.getBaseModelType().getSimpleName())) {
                                    topModelProp.put("originalRef",m.getName());
                                    topModelProp.put("$ref","#/definitions/"+m.getName());
                                } else if(propOriginalRef!=null && propOriginalRef.contains("«" +m.getBaseModelType().getSimpleName()+ "»")) {
                                    propOriginalRef=propOriginalRef.replaceAll("«" +m.getBaseModelType().getSimpleName()+ "»","«" +m.getName()+ "»");
                                    topModelProp.put("originalRef",propOriginalRef);
                                    topModelProp.put("$ref","#/definitions/"+propOriginalRef);
                                }

                                JSONObject items=topModelProp.getJSONObject("items");
                                if(items!=null) {
                                    String itemOriginalRef=items.getString("originalRef");
                                    if(itemOriginalRef!=null && itemOriginalRef.equals(m.getBaseModelType().getSimpleName())) {
                                        items.put("originalRef",m.getName());
                                        items.put("$ref","#/definitions/"+m.getName());
                                    } else if(itemOriginalRef!=null && itemOriginalRef.contains("«" +m.getBaseModelType().getSimpleName()+ "»")) {
                                        itemOriginalRef=itemOriginalRef.replaceAll("«" +m.getBaseModelType().getSimpleName()+ "»","«" +m.getName()+ "»");
                                        items.put("originalRef",itemOriginalRef);
                                        items.put("$ref","#/definitions/"+itemOriginalRef);
                                    }
                                }

                                // 处理目标模型
                                for (String ignoredProperty : m.getIgnoredProperties()) {
                                    baseModel.getJSONObject("properties").remove(ignoredProperty);
                                }
                                //覆盖
                                for (SwaggerAnnotationApiModelProperty property : m.getProperties()) {
                                    JSONObject prop=baseModel.getJSONObject("properties").getJSONObject(property.getName());
                                    if(prop==null) continue;
                                    if(!StringUtil.isBlank(property.getNotes())) {
                                        prop.put("title", property.getValue());
                                        prop.put("description", property.getNotes());
                                    }
                                }

                            }

                            definitions.put(m.getName(),baseModel);
                        }
                        schema.put("originalRef",newOriginalRef);
                        schema.put("$ref","#/definitions/"+newOriginalRef);
                        topModel.put("title",newOriginalRef);


                        definitions.put(newOriginalRef,topModel);
                    }
                }

            }
        }




        //
        data.put("modelNameMapping", modelNameMapping);
        //
        return data.toJSONString();
    }




    private void updateMethodJson(JSONObject httpMethodCfg, MethodAnnotations methodAnnotations,Map<String,ModelAnnotations> modelAnnotationsMap) {
        // 处理抬头

        if(methodAnnotations.getApiOperation()!=null) {
            httpMethodCfg.put("summary", methodAnnotations.getApiOperation().getValue());
            httpMethodCfg.put("description", methodAnnotations.getApiOperation().getNotes());
        }

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
                updateParameterJson(param,apiModelProperty,methodAnnotations.getSwaggerAnnotationApiImplicitParam(key));
            }
            // 增加
            for (String key : result.getTargetDiff()) {
                // 不在 ignoreParameters 的才可以添加
                if(!ignoreParameters.contains(key)) {
                    SwaggerAnnotationApiModelProperty apiModelProperty = null;
                    if(modelAnnotations!=null) {
                        apiModelProperty=modelAnnotations.getApiModelProperty(key);
                    }
                    SwaggerAnnotationApiImplicitParam apiImplicitParam=methodAnnotations.getSwaggerAnnotationApiImplicitParam(key);
                    if(apiImplicitParam!=null) {
                        addParameterJson(parameters, apiModelProperty, apiImplicitParam);
                    }
                }
            }
        }


        // 处理扩展
        SwaggerAnnotationApiOperationSupport apiOperationSupport=methodAnnotations.getApiOperationSupport();
        if(apiOperationSupport!=null) {
            httpMethodCfg.put("x-order",apiOperationSupport.getOrder());
        }


         Set<ErrorDefinition> errorDefinitions= ErrorDefinition.getDefinitionBeans();


        // 加入错误码
        LinkedHashMap<String, SwaggerAnnotationErrorCode> errorCodes=new LinkedHashMap<>();

        // 加入默认
        String[] defaultErrors={CommonError.SUCCESS,CommonError.FALIURE,CommonError.EXCEPTOPN};
        for (String defaultError : defaultErrors) {
            if(errorCodes.get(defaultError)==null) {
                SwaggerAnnotationErrorCode success=new SwaggerAnnotationErrorCode();
                BeanUtil.setFieldValue(success,"code",defaultError);
                errorCodes.put(defaultError,success);
            }
        }

        errorCodes.putAll(methodAnnotations.getErrorCodesMap());

        JSONArray errors=new JSONArray();

        // System.out.println();
        for (Map.Entry<String, SwaggerAnnotationErrorCode> e : errorCodes.entrySet()) {
            ErrorDesc desc=ErrorDesc.get(e.getValue().getCode());
            JSONObject err=JSONUtil.toJSONObject(e.getValue());
            if(desc!=null) {
                if (StringUtil.isBlank(err.getString("desc"))) {
                    err.put("desc",desc.getMessage());
                }
                JSONArray solutions=err.getJSONArray("solutions");
                if (solutions==null || solutions.isEmpty()) {
                    err.put("solutions",desc.getSolutions());
                }
                String constName=desc.getDefinition().getConstsName(desc.getCode());
                err.put("const",constName);
            }
            errors.add(err);
        }

        httpMethodCfg.put("x-errors",errors);


    }

    private void updateParameterJson(JSONObject param, SwaggerAnnotationApiModelProperty apiModelProperty, SwaggerAnnotationApiImplicitParam apiImplicitParam) {
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
        JSONObject param = new JSONObject();
        param.put("name", apiImplicitParam.getName());
        updateParameterJson(param, apiModelProperty, apiImplicitParam);
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
        // target.put("originalRef", type.getSimpleName());
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
