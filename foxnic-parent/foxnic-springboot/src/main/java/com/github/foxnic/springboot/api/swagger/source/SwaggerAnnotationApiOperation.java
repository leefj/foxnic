package com.github.foxnic.springboot.api.swagger.source;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;
import io.swagger.annotations.*;

import java.lang.reflect.Field;
import java.util.Map;

public class SwaggerAnnotationApiOperation extends SwaggerAnnotation {



    public String getValue() {
        return value;
    }
    private String value;
    private String notes = "";
    private String[] tags = {};
    private Class<?> response = Void.class;
    private String responseContainer = "";
    private String responseReference = "";
    private String httpMethod = "";
    private String nickname = "";
    private String produces = "";
    private String consumes = "";
    private String protocols = "";
    // private Authorization[] authorizations = {};
    private boolean hidden = false;
    // private ResponseHeader[] responseHeaders = {};
    private int code = 200;
   //  private Extension[] extensions = {};
    private boolean ignoreJsonView = false;


    public static SwaggerAnnotationApiOperation fromAnnotation(ApiOperation param) {
        SwaggerAnnotationApiOperation swaggerParam=new SwaggerAnnotationApiOperation();
        swaggerParam.value = param.value();
        swaggerParam.notes = param.notes();
        swaggerParam.tags = param.tags();
        swaggerParam.response = param.response();
        swaggerParam.responseContainer = param.responseContainer();
        swaggerParam.responseReference = param.responseReference();
        swaggerParam.httpMethod = param.httpMethod();
        swaggerParam.nickname = param.nickname();
        swaggerParam.produces = param.produces();
        swaggerParam.consumes = param.consumes();
        swaggerParam.protocols = param.protocols();
        // swaggerParam.authorizations = param.authorizations();
        swaggerParam.hidden = param.hidden();
        // swaggerParam.responseHeaders = param.responseHeaders();
        swaggerParam.code = param.code();
        // swaggerParam.extensions = param.extensions();
        swaggerParam.ignoreJsonView = param.ignoreJsonView();
        return swaggerParam;
    }

    public static SwaggerAnnotationApiOperation fromSource(NormalAnnotationExpr ann, ControllerSwaggerCompilationUnit compilationUnit) {
        SwaggerAnnotationApiOperation apiOperation=new SwaggerAnnotationApiOperation();
        Map<String,Object> values=readAnnotation(ann,compilationUnit);
        BeanUtil.copy(values,apiOperation);
        apiOperation.setSource(ann);
        return apiOperation;
    }


    public String getNotes() {
        return notes;
    }

    public String[] getTags() {
        return tags;
    }

    public Class<?> getResponse() {
        return response;
    }

    public String getResponseContainer() {
        return responseContainer;
    }

    public String getResponseReference() {
        return responseReference;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProduces() {
        return produces;
    }

    public String getConsumes() {
        return consumes;
    }

    public String getProtocols() {
        return protocols;
    }

    public boolean isHidden() {
        return hidden;
    }

    public int getCode() {
        return code;
    }

    public boolean isIgnoreJsonView() {
        return ignoreJsonView;
    }


}
