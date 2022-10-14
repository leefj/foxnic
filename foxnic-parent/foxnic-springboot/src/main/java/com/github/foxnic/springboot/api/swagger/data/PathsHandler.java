package com.github.foxnic.springboot.api.swagger.data;

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
import com.github.foxnic.commons.reflect.JavassistUtil;
import com.github.foxnic.springboot.api.swagger.source.*;
import com.github.foxnic.springboot.spring.SpringUtil;
import com.github.foxnic.springboot.web.WebContext;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.spring.web.plugins.Docket;


import java.lang.reflect.*;
import java.util.*;

public class PathsHandler {

    private  SwaggerDataHandler dataHandler;
    private WebContext context = null;
    public PathsHandler(SwaggerDataHandler dataHandler) {
        this.dataHandler=dataHandler;
        this.context = SpringUtil.getBean(WebContext.class);
    }

    public HandlerMethod getHandlerMethod(String url,String httpMethod) {
        return this.context.getHandlerMethod(url,httpMethod);
    }

    public void process(ApiDocket docket) {

        JSONObject paths = docket.getApiPaths();

        // 遍历路径
        for (String path : paths.keySet()) {
            JSONObject cfg = paths.getJSONObject(path);
            //循环请求方法，并获得参数对应的注解
            for (String httpMethod : cfg.keySet()) {

                JSONObject httpMethodEl = cfg.getJSONObject(httpMethod);
                HandlerMethod hm = this.getHandlerMethod(path, httpMethod);
                Method method = hm.getMethod();
                Class controller = method.getDeclaringClass();

                httpMethodEl.put("javaMethod", getImpl(controller,method));

                // 搜集接口参数中的模型信息
                Map<String, ModelAnnotations> localModelAnnotationsMap=new HashMap<>();
                for (Parameter parameter : method.getParameters()) {
                    if(DataParser.isCollection(parameter.getType()) || DataParser.isSimpleType(parameter.getType())) continue;
                    ModelAnnotations modelAnnotations=  this.dataHandler.getModelAnnotations(parameter.getType());
                    localModelAnnotationsMap.put(parameter.getType().getName(),modelAnnotations);
                }

                //
                MethodAnnotations methodAnnotations = this.dataHandler.getMethodAnnotations(method);

                // 将最新的内容刷入到文档 Json , 当一个参数时考虑模型数据
                updateMethodJson(httpMethodEl,methodAnnotations,method.getParameterCount()==1?localModelAnnotationsMap:new HashMap<>());
                // 处理 DynamicResponseParameter 相关
                this.processDynamicResponse(docket,httpMethodEl,methodAnnotations);

            }
        }

    }

    private  String getImpl(Class controller,Method method) {
        String location = "";
        int line = JavassistUtil.getLineNumber(method);
        if (line > 0) {
            location = controller.getSimpleName() + ".java:" + line;
        }
        List<String> paramList=new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            paramList.add(parameter.getType().getSimpleName()+" "+parameter.getName());
        }

        // String methodImpl2=controller.getName() + "." + method.getName() + "(" + location + ")";
        return controller.getName() + "." + method.getName() + "(" + StringUtil.join(paramList,", ") + ")@"+location;
    }

    private void processDynamicResponse(ApiDocket docket, JSONObject httpMethodEl,MethodAnnotations methodAnnotations) {
        // 处理响应码
        JSONObject r200=docket.getApiResponse200(httpMethodEl);
        // 处理 DynamicResponseParameter 相应模型
        if(r200!=null && r200.getJSONObject("schema")!=null && r200.getJSONObject("schema").getString("originalRef")==null) {
            SwaggerAnnotationDynamicResponseParameters dynamicResponseParameters=methodAnnotations.getDynamicResponseParameters();
            if(dynamicResponseParameters!=null) {
                r200.getJSONObject("schema").put("originalRef",dynamicResponseParameters.getName()+"Response");
                r200.getJSONObject("schema").put("$ref","#/definitions/"+dynamicResponseParameters.getName()+"Response");
            }
        }
    }

    private void updateMethodJson(JSONObject httpMethodEl, MethodAnnotations methodAnnotations,Map<String,ModelAnnotations> modelAnnotationsMap) {

        // 处理抬头
        if(methodAnnotations.getApiOperation()!=null) {
            httpMethodEl.put("summary", methodAnnotations.getApiOperation().getValue());
            httpMethodEl.put("description", methodAnnotations.getApiOperation().getNotes());
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
        JSONArray parameters = httpMethodEl.getJSONArray("parameters");
        if (parameters != null) {
            processParameters(parameters,paramNames,methodAnnotations,modelAnnotations);
        }

        // 处理扩展
        SwaggerAnnotationApiOperationSupport apiOperationSupport=methodAnnotations.getApiOperationSupport();
        if(apiOperationSupport!=null) {
            httpMethodEl.put("x-order",apiOperationSupport.getOrder());
        }

        // 加入错误码
        processErrorCodes(httpMethodEl,methodAnnotations);

    }

    private void processParameters(JSONArray parameters,Set<String> paramNames, MethodAnnotations methodAnnotations,ModelAnnotations modelAnnotations) {

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

    private void processErrorCodes(JSONObject httpMethodEl, MethodAnnotations methodAnnotations) {

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
            JSONObject err= JSONUtil.toJSONObject(e.getValue());
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

        httpMethodEl.put("x-errors",errors);

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
            this.applyType(param, apiImplicitParam.getDataTypeClass(), this.getTypeArguments(apiImplicitParam.getParameter()));
        }
        //
        if(apiModelProperty!=null) {
            param.put("description", apiModelProperty.getNotes());
            param.put("example", apiModelProperty.getExample());
            param.put("required", apiModelProperty.isRequired());
            if(param.getString("in")==null) {
                param.put("in", "body");
            }
            this.applyType(param, apiModelProperty.getField().getType(), this.getTypeArguments(apiModelProperty.getField()));
        }
    }

    private void addParameterJson(JSONArray parameters, SwaggerAnnotationApiModelProperty apiModelProperty, SwaggerAnnotationApiImplicitParam apiImplicitParam) {
        JSONObject param = new JSONObject();
        param.put("name", apiImplicitParam.getName());
        updateParameterJson(param, apiModelProperty, apiImplicitParam);
        parameters.add(param);
    }

    public Class[] getTypeArguments(Field field) {
        if (!DataParser.isCollection(field.getType())) return null;
        if (field.getType().isArray()) {
            return new Class[]{field.getType().getComponentType()};
        }

        if (field.getGenericType() instanceof ParameterizedType) {
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


    public Class[] getTypeArguments(Parameter parameter) {
        if (parameter == null) {
            return null;
        }

        if (!DataParser.isCollection(parameter.getType())) return null;

        if (parameter.getType().isArray()) {
            return new Class[]{parameter.getType().getComponentType()};
        }

        if (parameter.getParameterizedType() instanceof ParameterizedType) {
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

    public void applyType(JSONObject target, Class type, Class[] typeArguments) {

        String typeName = ApiDocket.getTypeName(type);
        target.put("type", typeName);
        target.put("javaType", type.getName());
        // target.put("originalRef", type.getSimpleName());
        target.put("$ref", "#/definitions/" + type.getSimpleName());
        //
        if ("array".equals(typeName) && typeArguments != null) {
            JSONObject items = new JSONObject();
            Class cmpType = (Class) typeArguments[0];
            items.put("type", ApiDocket.getTypeName(cmpType));
            items.put("originalRef", cmpType.getSimpleName());
            items.put("$ref", "#/definitions/" + cmpType.getSimpleName());
            items.put("javaType", cmpType.getName());
            target.put("items", items);
        }

        if ("object".equals(typeName) && typeArguments != null && DataParser.isMap(type)) {
            target.put("keyType", ApiDocket.getTypeName(typeArguments[0]));
            target.put("valueType", ApiDocket.getTypeName(typeArguments[1]));
            JSONObject items = new JSONObject();
            Class cmpType = (Class) typeArguments[1];
            items.put("type", ApiDocket.getTypeName(cmpType));
            items.put("originalRef", cmpType.getSimpleName());
            items.put("$ref", "#/definitions/" + cmpType.getSimpleName());
            items.put("javaType", cmpType.getName());
            target.put("items", items);
        }

        if (DataParser.isMap(type)) {
            target.put("collectionFormat", "map");
        }

    }

}
