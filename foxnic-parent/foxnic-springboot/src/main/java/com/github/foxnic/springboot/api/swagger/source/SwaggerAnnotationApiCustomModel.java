package com.github.foxnic.springboot.api.swagger.source;

import com.github.foxnic.api.swagger.Model;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

public class SwaggerAnnotationApiCustomModel extends SwaggerAnnotation {

    /**
     * 基础模型
     * */
    private Class baseModelType=Void.class;
    /**
     * 模型名称
     * */
    private String name="";
    /**
     * 排除某些不需要的属性
     * */
    private String[] ignoredProperties={};

    /**
     * 在 ignoreDBTreatyProperties 和 ignoreDefaultVoProperties 基础上保留指定字段
     * */

    private String[] includeProperties={};

    /**
     * 是否排除 DBTreaty 字段 如创建时间，创建人等
     * */
    private boolean ignoreDBTreatyProperties=false;

    /**
     * 是否排除默认的 Vo 字段 如页码、排序等字段
     * */
    private boolean ignoreDefaultVoProperties=false;

    /**
     * 主要用于同名属性覆盖
     */
    private  SwaggerAnnotationApiModelProperty[] properties={};


    public static SwaggerAnnotationApiCustomModel fromAnnotation(Model param) {
        SwaggerAnnotationApiCustomModel swaggerParam=new SwaggerAnnotationApiCustomModel();
        swaggerParam.baseModelType=param.baseModelType();
        swaggerParam.name=param.name();
        swaggerParam.ignoredProperties=param.ignoredProperties();
        swaggerParam.includeProperties=param.includeProperties();
        swaggerParam.ignoreDefaultVoProperties=param.ignoreDefaultVoProperties();
        swaggerParam.ignoreDBTreatyProperties=param.ignoreDBTreatyProperties();
        SwaggerAnnotationApiModelProperty[] properties=new SwaggerAnnotationApiModelProperty[param.properties().length];
        int i=0;
        for (ApiModelProperty property : param.properties()) {
            SwaggerAnnotationApiModelProperty p=SwaggerAnnotationApiModelProperty.fromAnnotation(property);
            properties[i]=p;
        }
        swaggerParam.properties=properties;
        return swaggerParam;
    }

    public static SwaggerAnnotationApiCustomModel fromSource(NormalAnnotationExpr ann, ControllerSwaggerCompilationUnit compilationUnit) {
        SwaggerAnnotationApiCustomModel swAnn=new SwaggerAnnotationApiCustomModel();
        Map<String,Object> values=readAnnotation(ann,compilationUnit);
        BeanUtil.copy(values,swAnn);
        //
        Object[] objectArr=(Object[])values.get("ignoredProperties");
        String[] stringArr = null;
        if(objectArr!=null) {
            stringArr = ArrayUtil.castArrayType(objectArr, String.class);
            swAnn.ignoredProperties=stringArr;
        }

        objectArr=(Object[])values.get("includeProperties");
        if(objectArr!=null) {
            stringArr = ArrayUtil.castArrayType(objectArr, String.class);
            swAnn.includeProperties=stringArr;
        }

        return swAnn;
    }


    public Class getBaseModelType() {
        return baseModelType;
    }

    public String getName() {
        return name;
    }

    public String[] getIgnoredProperties() {
        return ignoredProperties;
    }

    public String[] getIncludeProperties() {
        return includeProperties;
    }

    public SwaggerAnnotationApiModelProperty[] getProperties() {
        return properties;
    }

    public boolean isIgnoreDefaultVoProperties() {
        return ignoreDefaultVoProperties;
    }

    public boolean isIgnoreDBTreatyProperties() {
        return ignoreDBTreatyProperties;
    }
}
