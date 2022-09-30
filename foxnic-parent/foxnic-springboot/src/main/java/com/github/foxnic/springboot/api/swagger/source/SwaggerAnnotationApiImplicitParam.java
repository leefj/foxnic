package com.github.foxnic.springboot.api.swagger.source;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;
import io.swagger.annotations.ApiImplicitParam;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class SwaggerAnnotationApiImplicitParam extends SwaggerAnnotation {

    private String name;
    private String value;
    private String defaultValue="";
    private String allowableValues="";
    private boolean required=false;
    private String access="";
    private boolean allowMultiple=false;
    private String dataType="";
    private Class<?> dataTypeClass=Void.class;
    private String paramType="";
    private String example="";
    private String[] examples;
    private String type="";
    private String format="";
    private boolean allowEmptyValue=false;
    private boolean readOnly=false;
    private String collectionFormat;

    private Parameter parameter;

    public static SwaggerAnnotationApiImplicitParam fromAnnotation(ApiImplicitParam param) {
        SwaggerAnnotationApiImplicitParam swaggerParam=new SwaggerAnnotationApiImplicitParam();
        swaggerParam.name=param.name();
        swaggerParam.value=param.value();
        swaggerParam.defaultValue=param.defaultValue();
        swaggerParam.allowableValues=param.allowableValues();
        swaggerParam.required=param.required();
        swaggerParam.access=param.access();
        swaggerParam.allowMultiple=param.allowMultiple();
        swaggerParam.dataType=param.dataType();
        swaggerParam.dataTypeClass=param.dataTypeClass();
        swaggerParam.paramType=param.paramType();
        swaggerParam.example=param.example();
        if(param.examples()!=null) {
            // 暂不支持
            swaggerParam.examples = new String[0];
        }
        swaggerParam.format=param.format();
        swaggerParam.allowEmptyValue=param.allowEmptyValue();
        swaggerParam.readOnly=param.readOnly();
        swaggerParam.collectionFormat=param.collectionFormat();
        return swaggerParam;
    }

    public static SwaggerAnnotationApiImplicitParam fromSource(NormalAnnotationExpr ann, ControllerSwaggerCompilationUnit compilationUnit) {
        SwaggerAnnotationApiImplicitParam swaggerParam=new SwaggerAnnotationApiImplicitParam();
        Map<String,Object> values=readAnnotation(ann,compilationUnit);
        BeanUtil.copy(values,swaggerParam);
        return swaggerParam;
    }




    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getAllowableValues() {
        return allowableValues;
    }

    public boolean isRequired() {
        return required;
    }

    public String getAccess() {
        return access;
    }

    public boolean isAllowMultiple() {
        return allowMultiple;
    }

    public String getDataType() {
        return dataType;
    }

    public Class<?> getDataTypeClass() {
        return dataTypeClass;
    }

    public String getParamType() {
        return paramType;
    }

    public String getExample() {
        return example;
    }

    public String[] getExamples() {
        return examples;
    }

    public String getType() {
        return type;
    }

    public String getFormat() {
        return format;
    }

    public boolean isAllowEmptyValue() {
        return allowEmptyValue;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public String getCollectionFormat() {
        return collectionFormat;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }
}
