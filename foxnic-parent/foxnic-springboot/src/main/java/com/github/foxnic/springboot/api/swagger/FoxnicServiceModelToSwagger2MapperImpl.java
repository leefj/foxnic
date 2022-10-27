package com.github.foxnic.springboot.api.swagger;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.environment.BrowserType;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.springboot.web.WebContext;
import eu.bitwalker.useragentutils.UserAgent;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.schema.Model;
import springfox.documentation.service.*;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2MapperImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class FoxnicServiceModelToSwagger2MapperImpl extends ServiceModelToSwagger2MapperImpl {

    private WebContext context = null;



    public Swagger mapDocumentation(Documentation from) {

        com.github.foxnic.springboot.mvc.RequestParameter parameter = com.github.foxnic.springboot.mvc.RequestParameter.get();



        if (context == null) {
            context = WebContext.get();
        }
        //
        Map<String, String> modelNameMap= new HashMap<>();
        // 搜集已有的模型
        Map<String, List<ApiListing>> apiMap = from.getApiListings();
        for (Map.Entry<String, List<ApiListing>> entry : apiMap.entrySet()) {
            List<ApiListing> list = entry.getValue();
            for (ApiListing listing : list) {
                Map<String, springfox.documentation.schema.Model> modelMap = listing.getModels();
                for (Map.Entry<String, springfox.documentation.schema.Model>  e : modelMap.entrySet()) {
                    modelNameMap.put(e.getKey(),e.getValue().getQualifiedType());
                }
            }
        }


        Map<String, springfox.documentation.schema.Model> modelMap = null;
        for (Map.Entry<String, List<ApiListing>> entry : apiMap.entrySet()) {
            List<ApiListing> list = entry.getValue();
            for (ApiListing listing : list) {
                modelMap = listing.getModels();
                List<ApiDescription> apiDescriptionList = listing.getApis();
                for (ApiDescription apiDescription : apiDescriptionList) {
                    List<Operation> operations=apiDescription.getOperations();
                    for (Operation operation : operations) {
                        HandlerMethod hm=context.getHandlerMethod(apiDescription.getPath(), operation.getMethod().name());
                        Method m=hm.getMethod();
                        // if("newApiName2".equals(m.getName())) {
                        //     System.out.println();
                        // }

                        for (java.lang.reflect.Parameter mp : m.getParameters()) {
                            if(DataParser.isSimpleType(mp.getType())) continue;
                                if(!modelNameMap.values().contains(mp.getType().getName())) {
                                    //
                                    if(DataParser.isCollection(mp.getType())) {
                                        if(mp.getParameterizedType() instanceof  ParameterizedType) {
                                            ParameterizedType type = (ParameterizedType) mp.getParameterizedType();
                                            Type[] args=type.getActualTypeArguments();
                                            for (Type arg : args) {
                                                Class cType = (Class)arg;
                                                if(!DataParser.isSimpleType(cType) && !modelNameMap.values().contains(cType.getName())) {
                                                    springfox.documentation.schema.Model model = createModel(cType);
                                                    if(model!=null) {
                                                        modelMap.put(model.getName(), model);
                                                        modelNameMap.put(model.getName(), model.getQualifiedType());
                                                    }
                                                }
                                            }
                                        } else {
                                            //System.out.println();
                                        }
                                    } else {
                                        springfox.documentation.schema.Model model = createModel(mp.getType());
                                        if(model!=null) {
                                            modelMap.put(model.getName(), model);
                                            modelNameMap.put(model.getName(), model.getQualifiedType());
                                        }
                                    }
                            }
                        }
                    }
                }

            }
        }

        // 深度采集模型
        collectMoreModels(modelNameMap,modelMap);


        parameter.getRequest().setAttribute("SWAGGER_MODEL_NAME_MAPPING",modelNameMap);



        return super.mapDocumentation(from);
    }

    private void collectMoreModels(Map<String, String> modelNameMap, Map<String, Model> modelMap) {
        if(modelMap==null) return;
        Map<String, String> nameMap=new HashMap<>();
        for (String typeName : modelNameMap.values()) {
            Class type=ReflectUtil.forName(typeName);
            if(DataParser.isSimpleType(type)) continue;
            collectMoreModels(type,nameMap,modelMap,0);
        }
        modelNameMap.putAll(nameMap);
    }

    private void collectMoreModels(Class type, Map<String, String> modelNameMap, Map<String, Model> modelMap , int depth) {
        if(depth>5) return;
        List<Field> fields=BeanUtil.getAllFields(type);
        for (Field field : fields) {

            if(!DataParser.isSimpleType(field.getType())) {
                Model model=createModel(field.getType());
                if(model!=null) {
                    modelMap.put(model.getName(), model);
                    modelNameMap.put(model.getName(), model.getQualifiedType());
                    collectMoreModels(field.getType(), modelNameMap, modelMap, depth + 1);
                }
            }
        }

    }

    private springfox.documentation.schema.Model createModel(Class type) {

        if(DataParser.isCollection(type) || DataParser.isArray(type)) {
            return null;
        }
        if(Object.class.equals(type)) return null;

        String simpleName=type.getSimpleName();
        String fullName=type.getName();

        springfox.documentation.schema.Model model = new Model(
                fullName, simpleName, null, fullName,
                null, null, null, null, null, null, null);

        return model;

    }



    protected Map<String, Path> mapApiListings(Map<String, List<ApiListing>> apiListings) {
        return super.mapApiListings(apiListings);
    }

    /**
     * 逐个处理 API 接口
     */
    protected io.swagger.models.Operation mapOperation(springfox.documentation.service.Operation from, ModelNamesRegistry modelNames) {


        for (RequestParameter parameter : from.getRequestParameters()) {
            // 若未设置，设置一个默认值
            if (parameter.getIn() == null) {
                BeanUtil.setFieldValue(parameter, "in", ParameterType.BODY);
            }
        }


        return super.mapOperation(from, modelNames);
    }


    /**
     * 处理 tag
     */
    protected List<io.swagger.models.Tag> tagSetToTagList(Set<Tag> set) {

        com.github.foxnic.springboot.mvc.RequestParameter parameter = com.github.foxnic.springboot.mvc.RequestParameter.get();

        BrowserType browserType = parameter.getBrowserType();
        UserAgent userAgent = parameter.getUserAgent();

        // 提取 tag 名称的各个组成部分，独立目录
        if (browserType == BrowserType.API_FOX || browserType == BrowserType.HTTP_CLIENT) {
            Set<String> existsNames = new HashSet<>();
            Set<String> splitNames = new HashSet<>();
            for (Tag tag : set) {
                existsNames.add(tag.getName());
                String[] ns = tag.getName().split("/");
                for (String n : ns) {
                    splitNames.add(n);
                }
            }

            for (String splitName : splitNames) {
                if (existsNames.contains(splitName)) continue;
                Tag newTag = new Tag(splitName, "");
                set.add(newTag);
                existsNames.add(splitName);
            }
        }


        return super.tagSetToTagList(set);
    }


}
