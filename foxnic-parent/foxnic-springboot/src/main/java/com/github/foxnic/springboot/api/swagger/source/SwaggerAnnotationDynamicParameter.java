package com.github.foxnic.springboot.api.swagger.source;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import io.swagger.annotations.ApiOperation;

import java.util.Map;

public class SwaggerAnnotationDynamicParameter extends SwaggerAnnotation {

    private String name="";
    private String value="";
    private boolean required=false;
    private Class<?> dataTypeClass=Void.class;
    private String example="";


    public static SwaggerAnnotationDynamicParameter fromAnnotation(DynamicParameter param) {
        SwaggerAnnotationDynamicParameter swaggerParam=new SwaggerAnnotationDynamicParameter();
        swaggerParam.name = param.name();
        swaggerParam.value = param.value();
        swaggerParam.required = param.required();
        swaggerParam.dataTypeClass = param.dataTypeClass();
        swaggerParam.example = param.example();
        return swaggerParam;
    }

    public static SwaggerAnnotationDynamicParameter fromSource(NormalAnnotationExpr ann, ControllerSwaggerCompilationUnit compilationUnit) {
        SwaggerAnnotationDynamicParameter swAnn=new SwaggerAnnotationDynamicParameter();
        Map<String,Object> values=readAnnotation(ann,compilationUnit);
        BeanUtil.copy(values,swAnn);
        return swAnn;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isRequired() {
        return required;
    }

    public Class<?> getDataTypeClass() {
        return dataTypeClass;
    }

    public String getExample() {
        return example;
    }




}
