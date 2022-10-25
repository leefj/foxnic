package com.github.foxnic.springboot.api.swagger.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.api.error.CommonError;
import com.github.foxnic.api.error.ErrorDesc;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.collection.CollectorUtil;
import com.github.foxnic.commons.json.JSONUtil;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.reflect.JavassistUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.springboot.api.swagger.source.*;
import com.github.foxnic.springboot.spring.SpringUtil;
import com.github.foxnic.springboot.web.WebContext;
import com.github.foxnic.sql.meta.DBTable;
import org.springframework.web.method.HandlerMethod;

import java.io.File;
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

        Set<Class> modifiedControllers=this.dataHandler.getGroupMeta().getModifiedControllers();

        // 遍历路径
        for (String path : paths.keySet()) {
            JSONObject cfg = paths.getJSONObject(path);
            Set<String> hiddens=new HashSet<>();
            //循环请求方法，并获得参数对应的注解
            for (String httpMethod : cfg.keySet()) {

                JSONObject httpMethodEl = cfg.getJSONObject(httpMethod);
                HandlerMethod hm = this.getHandlerMethod(path, httpMethod);
                Method method = hm.getMethod();
                Class controller = method.getDeclaringClass();

                // 如果未被修改过，则不处理
                if(this.dataHandler.getGroupMeta().getMode()==GroupMeta.ProcessMode.PART_CACHE) {
                    if(!modifiedControllers.contains(controller)) {
                        continue;
                    }
                }


                // 注册控制器文件以及类型
                File javaFile = this.dataHandler.getJCU(controller).getJavaFile();
                if (javaFile != null && javaFile.exists()) {
                    this.dataHandler.getGroupMeta().registerControllerPath(controller, this.dataHandler.getJCU(controller).getJavaFile(), path);
                }

                httpMethodEl.put("javaMethod", getImpl(controller,method));
                httpMethodEl.put("proxyInvokeCode", getProxyInvokeCode(controller,method));


                // 搜集接口参数中的模型信息
                Map<String, ModelAnnotations> localModelAnnotationsMap=new HashMap<>();
                for (Parameter parameter : method.getParameters()) {
                    if(DataParser.isCollection(parameter.getType()) || DataParser.isSimpleType(parameter.getType())) continue;
                    ModelAnnotations modelAnnotations=  this.dataHandler.getModelAnnotations(parameter.getType());
                    localModelAnnotationsMap.put(parameter.getType().getName(),modelAnnotations);
                }

                //
                MethodAnnotations methodAnnotations = this.dataHandler.getMethodAnnotations(method);

                if (methodAnnotations.getApiOperation() != null && methodAnnotations.getApiOperation().isHidden()) {
                    hiddens.add(httpMethod);
                    continue;
                }
                if(methodAnnotations.getForbidden()!=null) {
                    hiddens.add(httpMethod);
                    continue;
                }

                // 设置默认的 BaseModelType
                if(methodAnnotations.getApiParamSupport()!=null) {
                    if(methodAnnotations.getApiParamSupport().getBaseModelType()==null || methodAnnotations.getApiParamSupport().getBaseModelType().equals(Void.class)) {
                        if(method.getParameterCount()==1) {
                            methodAnnotations.getApiParamSupport().setBaseModelType(method.getParameters()[0].getType());
                        }
                    }
                }


                // 将最新的内容刷入到文档 Json , 当一个参数时考虑模型数据
                updateMethodJson(httpMethodEl,methodAnnotations,method.getParameterCount()==1?localModelAnnotationsMap:new HashMap<>());
                // 处理 DynamicResponseParameter 相关
                this.processDynamicResponse(docket,httpMethodEl,methodAnnotations);

            }
            //
            for (String hidden : hiddens) {
                cfg.remove(hidden);
            }
        }

    }

    private String getProxyInvokeCode(Class controller, Method method) {
        String fullName=controller.getName();
        String[] parts=fullName.split("\\.");
        String mduPkg=parts[parts.length-3];
        String basePkg= StringUtil.join(ArrayUtil.subArray(parts,0,parts.length-4),".");
        String proxyName=controller.getSimpleName();
        proxyName=proxyName.substring(0,proxyName.length()-10);
        proxyName=basePkg+".proxy."+mduPkg+"."+proxyName+"ServiceProxy";
        Class proxy= ReflectUtil.forName(proxyName);
        String code="不支持";
        try {
            Method apiMethod = proxy.getDeclaredMethod("api");
            if(apiMethod==null || apiMethod.getParameterCount()>0 || !apiMethod.getReturnType().equals(proxy)) {
                return code;
            }
            Method proxyMethod=proxy.getDeclaredMethod(method.getName(),method.getParameterTypes());
            if(proxyMethod==null) {
                return code;
            }
            if(!proxyMethod.getReturnType().equals(method.getReturnType())) {
                return code;
            }
            List<String> params=new ArrayList<>();
            for (Parameter parameter : proxyMethod.getParameters()) {
                params.add(parameter.getType().getSimpleName()+" "+parameter.getName());
            }
            code= proxyMethod.getReturnType().getSimpleName()+" result = " + proxyMethod.getDeclaringClass().getName()+".api()."+proxyMethod.getName()+"("+StringUtil.join(params,", ")+");";
        } catch (Exception e) {
            return code;
        }
        return code;
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

//        if(methodAnnotations.getApiOperation().getValue().equals("账户登录")) {
//            System.out.println();
//        }

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

        // 模型的属性+参数属性
        Set<String> paramNames=new HashSet<>();
        if(modelAnnotations!=null) {
            paramNames.addAll(modelAnnotations.getApiModelPropertyMap().keySet());
        }
        paramNames.addAll(methodAnnotations.getParamMap().keySet());


        // 处理参数
        JSONArray parameters = httpMethodEl.getJSONArray("parameters");
        if (parameters != null && modelAnnotations!=null) {
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

    private void processParameterEnumInfo(ModelAnnotations modelAnnotations,String paramName,JSONObject param) {
        if(modelAnnotations==null) return;
        Class enumModel= modelAnnotations.getEnumModel(paramName);
        if(enumModel!=null) {
            String desc= param.getString("description");
            if(desc==null) {
                desc=enumModel.getSimpleName()+"类型 , ";
            } else {
                desc=desc.trim();
                desc=StringUtil.removeLast(desc,";");
                desc=StringUtil.removeLast(desc,",");
                desc=StringUtil.removeLast(desc,"，");
                desc=StringUtil.removeLast(desc,"；");
                desc=StringUtil.removeLast(desc,"。");
                desc+="; "+enumModel.getSimpleName() + "类型 , ";
            }
            desc+=modelAnnotations.getEnumContent(paramName);
            param.put("description",desc);
        }

    }

    private void processParameters(JSONArray parameters,Set<String> paramNames, MethodAnnotations methodAnnotations,ModelAnnotations modelAnnotations) {


        if(methodAnnotations.getApiOperation().getValue().equals("账户登录")) {
            System.out.println();
        }



        Map<String,JSONObject> parametersMap=new HashMap<>();
        // 将JSONArray转JSONObject，忽略同名参数
        for (int i = 0; i < parameters.size(); i++) {
            JSONObject parameter=parameters.getJSONObject(i);
            String name=parameter.getString("name");
            parametersMap.put(name,parameter);
            processParameterEnumInfo(modelAnnotations,name,parameter);
        }
        CollectorUtil.CompareResult<String,String> result=CollectorUtil.compare(parametersMap.keySet(),paramNames);





        // 第一步：参数的合并处理
        // parametersMap 比 paramNames 多的部分
        for (String key : result.getSourceDiff()) {
            JSONObject param=parametersMap.get(key);
            param.put("willRemove", true);
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
            SwaggerAnnotationApiModelProperty apiModelProperty = null;
            if(modelAnnotations!=null) {
                apiModelProperty=modelAnnotations.getApiModelProperty(key);
            }
            SwaggerAnnotationApiImplicitParam apiImplicitParam=methodAnnotations.getSwaggerAnnotationApiImplicitParam(key);
            if(apiImplicitParam!=null) {
                addParameterJson(parameters, apiModelProperty, apiImplicitParam);
            }
        }

//
//        if(methodAnnotations.getApiOperation()!=null && methodAnnotations.getApiOperation().getValue().equals("添加菜单")) {
//            System.out.println();
//        }

        // 第二步：提取注解信息
        Set<String> ignoreParameters=new HashSet<>();
        Set<String> includeParameters=new HashSet<>();
        Set<String> dbFieldVars=null;
        String tableName=null;
        if(methodAnnotations.getApiOperationSupport()!=null) {
            // 指定要忽略的参数集合
            if(methodAnnotations.getApiOperationSupport().getIgnoreParameters()!=null && methodAnnotations.getApiOperationSupport().getIgnoreParameters().length>0) {
                ignoreParameters.addAll(Arrays.asList(methodAnnotations.getApiOperationSupport().getIgnoreParameters()));
            }
            // 指定要包含的参数集合
            if(methodAnnotations.getApiOperationSupport().getIncludeParameters()!=null && methodAnnotations.getApiOperationSupport().getIncludeParameters().length>0) {
                includeParameters.addAll(Arrays.asList(methodAnnotations.getApiOperationSupport().getIncludeParameters()));
            }
        }

        if(methodAnnotations.getApiParamSupport()!=null) {
            if(methodAnnotations.getApiParamSupport().getIgnoredProperties()!=null && methodAnnotations.getApiParamSupport().getIgnoredProperties().length>0) {
                ignoreParameters.addAll(Arrays.asList(methodAnnotations.getApiParamSupport().getIgnoredProperties()));
            }
            if(methodAnnotations.getApiParamSupport().getIncludeProperties()!=null && methodAnnotations.getApiParamSupport().getIncludeProperties().length>0) {
                includeParameters.addAll(Arrays.asList(methodAnnotations.getApiParamSupport().getIncludeProperties()));
            }

            if(methodAnnotations.getApiParamSupport().getBaseModelType()!=null) {
                DBTable table= this.dataHandler.getGroupMeta().getTable(methodAnnotations.getApiParamSupport().getBaseModelType());
                if(table!=null) {
                    tableName=table.name();
                }
                if(table!=null && methodAnnotations.getApiParamSupport().isIgnoreNonDBProperties()) {
                    DBTableMeta tm= this.dataHandler.getGroupMeta().getTableMeta(table);
                    if(tm!=null) {
                        dbFieldVars = new HashSet<>();
                        for (DBColumnMeta column : tm.getColumns()) {
                            dbFieldVars.add(column.getColumnVarName());
                        }
                    }
                }

            }

        }

//        if(methodAnnotations.getApiOperation()!=null && methodAnnotations.getApiOperation().getValue().equals("添加角色")) {
//            System.out.println();
//        }

        //第三部按标记处理参数
        for (int i = 0; i < parameters.size(); i++) {
            JSONObject param=parameters.getJSONObject(i);
            String name=param.getString("name");
            // 不允许排除的
            if(includeParameters.contains(name)) {
                param.put("willRemove", false);
                continue;
            }
            if(methodAnnotations.getApiParamSupport()!=null) {
                boolean rmTag=false;
                // 排除全部字段
                if(!rmTag && methodAnnotations.getApiParamSupport().isIgnoreAllProperties()) {
                    param.put("willRemove", true);
                    rmTag=true;
                }
                // 排除非数据库字段
                if(!rmTag && dbFieldVars!=null) {
                    if(!dbFieldVars.contains(name)) {
                        param.put("willRemove", true);
                        rmTag=true;
                    }
                }
                // 判断排除 DBTreaty 字段
                if(!rmTag && methodAnnotations.getApiParamSupport().isIgnoreDBTreatyProperties()) {
                    if(this.dataHandler.getGroupMeta().isDBTreatyProperty(name)) {
                        param.put("willRemove", true);
                        rmTag=true;
                    }
                }
//                if(methodAnnotations.getApiOperation()!=null && methodAnnotations.getApiOperation().getValue().equals("添加角色")) {
//                    System.out.println();
//                }
                // 判断排除主键
                if(!rmTag && methodAnnotations.getApiParamSupport().isIgnorePrimaryKey()) {
                    if(this.dataHandler.getGroupMeta().isPrimaryKey(tableName,name)) {
                        param.put("willRemove", true);
                        rmTag=true;
                    }
                }
                // 判断是否排除默认VO字段
                if(!rmTag && methodAnnotations.getApiParamSupport().isIgnoreDefaultVoProperties()) {
                    if(this.dataHandler.getGroupMeta().isDefaultVoProperty(tableName,name)) {
                        param.put("willRemove", true);
                        rmTag=true;
                    }
                }
                if(rmTag) continue;
            }

            if(ignoreParameters.contains(name)) {
                param.put("willRemove", true);
                continue;
            }

        }



        // 移除非保留字段
        while(true) {
            boolean hasMore=false;
            for (int i = 0; i < parameters.size(); i++) {
                Boolean willRemove = parameters.getJSONObject(i).getBoolean("willRemove");
                if(willRemove==null || willRemove==false) {
                    parameters.getJSONObject(i).remove("willRemove");
                }
                if(willRemove!=null && willRemove==true) {
                    parameters.remove(i);
                    hasMore=true;
                    break;
                }
            }
            if(!hasMore) break;
        }


        // 处理最终的参数集合
        for (int i = 0; i < parameters.size(); i++) {
            JSONObject parameter=parameters.getJSONObject(i);
            String name=parameter.getString("name");
            processParameterEnumInfo(modelAnnotations,name,parameter);
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
                    err.put("solutions",desc.getSolution());
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

//        if("data".equals(target.getString("name"))) {
//            System.out.println();
//        }

        String typeName = ApiDocket.getTypeName(type);
        target.put("type", typeName);
        target.put("javaType", type.getName());
        if(!DataParser.isSimpleType(type)) {
             target.put("originalRef", type.getSimpleName());
        }
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
