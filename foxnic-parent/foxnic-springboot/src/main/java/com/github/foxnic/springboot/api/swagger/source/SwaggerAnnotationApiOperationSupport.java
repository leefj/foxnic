package com.github.foxnic.springboot.api.swagger.source;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.DynamicParameters;
import com.github.xiaoymin.knife4j.annotations.DynamicResponseParameters;
import io.swagger.annotations.ApiImplicitParam;

import java.lang.reflect.Field;
import java.util.Map;

public class SwaggerAnnotationApiOperationSupport extends SwaggerAnnotation {

    private int order = 0;
    private String author = "";
    private SwaggerAnnotationDynamicParameters params = null;
    private SwaggerAnnotationDynamicResponseParameters responses = null;
    private String[] ignoreParameters = {};
    private String[] includeParameters = {};

    public static SwaggerAnnotationApiOperationSupport fromAnnotation(ApiOperationSupport param) {
        SwaggerAnnotationApiOperationSupport swaggerParam=new SwaggerAnnotationApiOperationSupport();
        swaggerParam.order=param.order();
        swaggerParam.author=param.author();
        swaggerParam.ignoreParameters=param.ignoreParameters();
        swaggerParam.includeParameters=param.includeParameters();
        if(param.params()!=null) {
            swaggerParam.params = SwaggerAnnotationDynamicParameters.fromAnnotation(param.params());
        }
        if(param.responses()!=null) {
            swaggerParam.responses = SwaggerAnnotationDynamicResponseParameters.fromAnnotation(param.responses());
        }
        return swaggerParam;
    }

    public static SwaggerAnnotationApiOperationSupport fromSource(NormalAnnotationExpr ann, ControllerSwaggerCompilationUnit compilationUnit) {
        SwaggerAnnotationApiOperationSupport swAnn=new SwaggerAnnotationApiOperationSupport();
        Map<String,Object> values=readAnnotation(ann,compilationUnit);
        BeanUtil.copy(values,swAnn);
        //
        Object[] objectArr=(Object[])values.get("ignoreParameters");
        String[] stringArr = null;
        if(objectArr!=null) {
            stringArr = ArrayUtil.castArrayType(objectArr, String.class);
            swAnn.setIgnoreParameters(stringArr);
        }

        objectArr=(Object[])values.get("includeParameters");
        if(objectArr!=null) {
            stringArr = ArrayUtil.castArrayType(objectArr, String.class);
            swAnn.setIncludeParameters(stringArr);
        }

        return swAnn;
    }

    public int getOrder() {
        return order;
    }

    public String getAuthor() {
        return author;
    }

    public SwaggerAnnotationDynamicParameters getParams() {
        return params;
    }

    public SwaggerAnnotationDynamicResponseParameters getResponses() {
        return responses;
    }

    public String[] getIgnoreParameters() {
        return ignoreParameters;
    }

    public String[] getIncludeParameters() {
        return includeParameters;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setParams(SwaggerAnnotationDynamicParameters params) {
        this.params = params;
    }

    public void setResponses(SwaggerAnnotationDynamicResponseParameters responses) {
        this.responses = responses;
    }

    public void setIgnoreParameters(String[] ignoreParameters) {
        this.ignoreParameters = ignoreParameters;
    }

    public void setIncludeParameters(String[] includeParameters) {
        this.includeParameters = includeParameters;
    }

}
