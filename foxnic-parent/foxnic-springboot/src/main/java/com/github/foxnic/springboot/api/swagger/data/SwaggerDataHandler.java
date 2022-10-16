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
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.log.PerformanceLogger;
import com.github.foxnic.commons.reflect.JavassistUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.springboot.api.swagger.source.*;
import com.github.foxnic.springboot.mvc.RequestParameter;
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

import javax.annotation.PostConstruct;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Component
public class SwaggerDataHandler {

    private TagsHandler tagsHandler = null;
    private PathsHandler pathsHandler = null;
    private ModelHandler modelHandler = null;

    private Map<String, ControllerSwaggerCompilationUnit> jcuMap = new HashMap<>();
    private Map<String, ModelSwaggerCompilationUnit> mcuMap = new HashMap<>();
    private Map<Method, MethodAnnotations> methodAnnotationMap = new HashMap<>();
    private Map<String, ModelAnnotations> modelAnnotationsMap = new HashMap<>();

    private PerformanceLogger performanceLogger=null;

    @PostConstruct
    private void initHandlers() {
        tagsHandler = new TagsHandler(this);
        pathsHandler = new PathsHandler(this);
        modelHandler = new ModelHandler(this);
    }

    public ModelHandler getModelHandler() {
        return modelHandler;
    }
    public PathsHandler getPathsHandler() {
        return pathsHandler;
    }
    public TagsHandler getTagsHandler() {
        return tagsHandler;
    }

    public PerformanceLogger getPerformanceLogger() {
        return performanceLogger;
    }

    public ControllerSwaggerCompilationUnit getJCU(Class controller) {
        ControllerSwaggerCompilationUnit jcu = jcuMap.get(controller.getName());
        if (jcu == null) {
            jcu = new ControllerSwaggerCompilationUnit(controller);
            jcuMap.put(controller.getName(), jcu);
        }
        return jcu;
    }

    public ModelSwaggerCompilationUnit getMCU(Class modelType) {
        ModelSwaggerCompilationUnit mcu = mcuMap.get(modelType.getName());
        if (mcu == null) {
            mcu = new ModelSwaggerCompilationUnit(modelType);
            mcuMap.put(modelType.getName(), mcu);
        }
        return mcu;
    }

    public MethodAnnotations getMethodAnnotations(Method method) {
        MethodAnnotations methodAnnotations = methodAnnotationMap.get(method);
        if (methodAnnotations == null) {
            //
            ControllerSwaggerCompilationUnit jcu = this.getJCU(method.getDeclaringClass());
            methodAnnotations = jcu.createMethodAnnotationsFromClassBytes(method);
            if (BootArgs.isBootInIDE()) {
                if (jcu.isValid()) {
                    MethodAnnotations sourceSwaggerAnnotations = jcu.createMethodAnnotationsFromSource(method);
                    methodAnnotations.merge(sourceSwaggerAnnotations);
                }
            }
            methodAnnotationMap.put(method, methodAnnotations);
        }
        return methodAnnotations;
    }


    public ModelAnnotations getModelAnnotations(Class moduleType) {
        ModelAnnotations modelAnnotations = modelAnnotationsMap.get(moduleType);
        if (modelAnnotations == null) {
            ModelSwaggerCompilationUnit mcu = getMCU(moduleType);
            modelAnnotations = mcu.createFromClassBytes();
            if (BootArgs.isBootInIDE()) {
                if (mcu.isValid()) {
                    ModelAnnotations sourceModelAnnotations = mcu.createFromSource();
                    modelAnnotations.merge(sourceModelAnnotations);
                }
            }
        }
        return modelAnnotations;
    }



    public synchronized void process(ResponseEntity responseEntity) {

        String group=(String) RequestParameter.get().get("group");

        this.performanceLogger=new PerformanceLogger();
        jcuMap.clear();
        mcuMap.clear();
        methodAnnotationMap.clear();
        modelAnnotationsMap.clear();

        Json body = (Json) responseEntity.getBody();
        String value = BeanUtil.getFieldValue(body, "value", String.class);
        JSONObject data = null;
        try {
            data = JSONObject.parseObject(value);
        } catch (Exception e) {
            Logger.exception(e);
        }
        if (data != null && data.containsKey("swagger") && data.containsKey("paths")) {
            value = process(new ApiDocket(data));
            BeanUtil.setFieldValue(body, "value", value);
        }
    }

    private String process(ApiDocket docket) {
        this.performanceLogger.collect("P1");
        this.tagsHandler.process(docket);
        this.performanceLogger.collect("P2");
        this.pathsHandler.process(docket);
        this.performanceLogger.collect("P3");
        this.modelHandler.process(docket);
        this.performanceLogger.collect("P4");
        this.performanceLogger.info("Docket");
        return docket.getApiDoc();
    }


}
