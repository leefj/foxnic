package com.github.foxnic.springboot.api.swagger.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.json.JSONUtil;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.springboot.api.swagger.source.*;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ModelHandler {
    private  SwaggerDataHandler dataHandler;

    public ModelHandler(SwaggerDataHandler dataHandler) {
        this.dataHandler=dataHandler;
    }

    public void process(ApiDocket docket) {
        //
        this.dataHandler.getPerformanceLogger().collect("P3.1");
        processModelFromBaseDocket(docket);
        this.dataHandler.getPerformanceLogger().collect("P3.2");
        processModelForResponse(docket);
        this.dataHandler.getPerformanceLogger().collect("P3.3");

        // 初始化时进行注册
        Set<Class> models = this.dataHandler.getModels();
        for (Class model : models) {
            ModelSwaggerCompilationUnit mcu = this.dataHandler.getMCU(model);
            if (mcu == null) continue;
            if (mcu.isValid() && mcu.getJavaFile() != null && mcu.getJavaFile().exists()) {
                this.dataHandler.getGroupMeta().registerModel(model, mcu.getJavaFile());
            }
        }

    }

    /**
     * 针对  ApiResponseSupport 中  Model 的处理
     * */
    private void processModelForResponse(ApiDocket docket) {

        JSONObject definitions=docket.getApiDefinitions();
        JSONObject paths=docket.getApiPaths();

        Set<Class> modifiedModels=this.dataHandler.getGroupMeta().getModifiedModels();

        // 遍历路径
        for (String path : paths.keySet()) {
            JSONObject cfg = paths.getJSONObject(path);
            //循环请求方法，并获得参数对应的注解
            String originalRef=null;
            JSONObject schema=null;
            for (String httpMethod : cfg.keySet()) {
                JSONObject httpMethodEl = cfg.getJSONObject(httpMethod);

                HandlerMethod hm = this.dataHandler.getPathsHandler().getHandlerMethod(path, httpMethod);
                Method method = hm.getMethod();

                //
                if(this.dataHandler.getGroupMeta().getMode()== GroupMeta.ProcessMode.PART_CACHE) {
                    if (!modifiedModels.contains(method.getDeclaringClass())) {
                        continue;
                    }
                }

                JSONObject r200=docket.getApiResponse200(httpMethodEl);
                if(r200==null) continue;
                schema=r200.getJSONObject("schema");
                if(schema==null) continue;
                originalRef=schema.getString("originalRef");
                // 无需处理
                if(StringUtil.isBlank(originalRef)) {
                    continue;
                }


                MethodAnnotations methodAnnotations=this.dataHandler.getMethodAnnotations(method);
                Map<String, SwaggerAnnotationApiCustomModel> responseModelMap=methodAnnotations.getResponseModelMap();
                // 无需处理
                if(responseModelMap==null || responseModelMap.isEmpty()) {
                    continue;
                }

                //
                String newOriginalRef=originalRef;
                JSONObject topModel=JSONUtil.duplicate(definitions.getJSONObject(originalRef));
                JSONObject topModelProps=topModel.getJSONObject("properties");

                //
                for (SwaggerAnnotationApiCustomModel m : responseModelMap.values()) {
                    this.processTopModel(m,topModelProps);
                    newOriginalRef=this.processBaseModel(definitions,m,newOriginalRef);
                }
                //
                schema.put("originalRef",newOriginalRef);
                schema.put("$ref","#/definitions/"+newOriginalRef);
                topModel.put("title",newOriginalRef);
                definitions.put(newOriginalRef,topModel);
            }
        }

    }

    private void processTopModel(SwaggerAnnotationApiCustomModel m, JSONObject topModelProps) {

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
        }

    }

    private String processBaseModel(JSONObject definitions, SwaggerAnnotationApiCustomModel m, String newOriginalRef) {

        // 处理基础模型
        JSONObject baseModel=definitions.getJSONObject(m.getBaseModelType().getSimpleName());
        if(baseModel!=null) {
            baseModel= JSONUtil.duplicate(baseModel);
            if (newOriginalRef.contains("«" + m.getBaseModelType().getSimpleName() + "»")) {
                newOriginalRef=newOriginalRef.replaceAll("«" + m.getBaseModelType().getSimpleName() + "»","«" + m.getName()+ "»");
            }
        }

        JSONObject baseProperties=baseModel.getJSONObject("properties");
        Set<String> includeProps= ArrayUtil.toSet(m.getIncludeProperties());
        Set<String> rmProps=new HashSet<>();
        // 采集需要移除的字段
        for (String s : baseProperties.keySet()) {
            //
            if(m.isIgnoreDBTreatyProperties()) {
                if (this.dataHandler.getGroupMeta().isDBTreatyProperty(s)) {
                    rmProps.add(s);
                }
            }
            //
            if(m.isIgnoreDefaultVoProperties()) {
                if (this.dataHandler.getGroupMeta().isDefaultVoProperty(s)) {
                    rmProps.add(s);
                }
            }
        }
        rmProps.addAll(Arrays.asList(m.getIgnoredProperties()));
        // 排除不要移除的字段
        rmProps.removeAll(includeProps);
        for (String p : rmProps) {
            baseProperties.remove(p);
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

        definitions.put(m.getName(),baseModel);

        return newOriginalRef;

    }



    /**
     * 处理默认 Swagger Docket 中就存在的 Model
     * */
    private void processModelFromBaseDocket(ApiDocket docket) {

        com.github.foxnic.springboot.mvc.RequestParameter requestParameter = com.github.foxnic.springboot.mvc.RequestParameter.get();
        Map<String, String> modelNameMapping = (Map<String, String>) requestParameter.getRequest().getAttribute("SWAGGER_MODEL_NAME_MAPPING");
        // 如果 request 里没有 mapping 就从 meta 里拿
        if(modelNameMapping!=null) {
            this.dataHandler.getGroupMeta().setModelNameMapping(modelNameMapping);
        } else {
            modelNameMapping=this.dataHandler.getGroupMeta().getModelNameMapping();
        }

        Set<Class> modifiedModels=this.dataHandler.getGroupMeta().getModifiedModels();

        JSONObject definitions=docket.getApiDefinitions();

        // 处理 Model
        for (Map.Entry<String, String> e : modelNameMapping.entrySet()) {
            //
            Class type = ReflectUtil.forName(e.getValue());

            if(this.dataHandler.getGroupMeta().getMode()== GroupMeta.ProcessMode.PART_CACHE) {
                if (!modifiedModels.contains(type)) {
                    continue;
                }
            }

            List<Field> fields = BeanUtil.getAllFields(type);
            JSONObject definition = definitions.getJSONObject(e.getKey());
            JSONObject properties = null;
            if(definition!=null) {
                properties = definition.getJSONObject("properties");
            }

            ModelAnnotations modelAnnotations= this.dataHandler.getModelAnnotations(type);

            // 处理已存在的 Model
            if (definitions.containsKey(e.getKey())) {
                processExistsModel(definition,properties,type,fields,modelAnnotations);
            } else {
                processNotExistsModel(definitions,type,fields,modelAnnotations);
            }

        }

        //
        docket.setApiElement("modelNameMapping", modelNameMapping);


    }

    private void processNotExistsModel(JSONObject definitions,Class type,List<Field> fields,ModelAnnotations modelAnnotations) {
        // 创建不存在的 Model
        JSONObject definition = new JSONObject();
        definition.put("type", "object");
        definition.put("javaType", type.getName());
        definition.put("title", type.getSimpleName());

        if(modelAnnotations!=null && modelAnnotations.getApiModel()!=null) {
            definition.put("description", modelAnnotations.getApiModel().getDescription());
        }

        JSONObject properties = new JSONObject();
        JSONArray required = new JSONArray();
        definition.put("required", required);
        definition.put("properties", properties);
        //
        for (Field field : fields) {
            SwaggerAnnotationApiModelProperty apiModelProperty = modelAnnotations.getApiModelProperty(field.getName());
            JSONObject prop=createModelProperty(field,apiModelProperty);
            properties.put(field.getName(), prop);
            if (apiModelProperty!=null && apiModelProperty.isRequired()) {
                required.add(apiModelProperty.getName());
            }
        }
        definitions.put(type.getSimpleName(), definition);
    }

    private JSONObject createModelProperty(Field field,SwaggerAnnotationApiModelProperty apiModelProperty) {
        JSONObject prop = new JSONObject();
        this.dataHandler.getPathsHandler().applyType(prop,field.getType(),this.dataHandler.getPathsHandler().getTypeArguments(field));
        if (apiModelProperty != null) {
            prop.put("title", apiModelProperty.getValue());
            prop.put("description", apiModelProperty.getNotes());
        } else {
            prop.put("title", field.getName());
        }

        // 如果最终没有 title 则使用 description 作为 title
        if(StringUtil.isBlank(prop.getString("title"))) {
            prop.put("title",  prop.getString("description"));
        }

        return prop;
    }

    private void processExistsModel(JSONObject definition,JSONObject properties,Class type,List<Field> fields,ModelAnnotations modelAnnotations) {

//        if("SessionUser".equals(type.getSimpleName())) {
//            System.out.println();
//        }

        definition.put("javaType", type.getName());
        if(modelAnnotations!=null && modelAnnotations.getApiModel()!=null) {
            definition.put("description", modelAnnotations.getApiModel().getDescription());
        }
        Set<String> processedProps=new HashSet<>();

        // 按字段处理
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


            // 如果最终没有 title 则使用 description 作为 title
            if(StringUtil.isBlank(prop.getString("title"))) {
                prop.put("title",  prop.getString("description"));
            }

            String originalRef=prop.getString("originalRef");
            if(StringUtil.isBlank(originalRef)) {
                if (!DataParser.isSimpleType(field.getType()) && !DataParser.isCollection(field.getType())) {
                    prop.put("originalRef",type.getSimpleName());
                }
            }

            processedProps.add(field.getName());

        }

        // 处理剩余部分
        for (String propName : properties.keySet()) {
            if(processedProps.contains(propName)) continue;
            JSONObject prop=properties.getJSONObject(propName);
            String originalRef=prop.getString("originalRef");
            if(originalRef!=null && prop.getString("type")==null) {
                prop.put("type","object");
            }
        }

    }


}
