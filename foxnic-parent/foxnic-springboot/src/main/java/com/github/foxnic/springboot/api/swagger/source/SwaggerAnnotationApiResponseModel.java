package com.github.foxnic.springboot.api.swagger.source;

import com.github.foxnic.api.swagger.ApiResponseSupport;
import com.github.foxnic.api.swagger.Model;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

public class SwaggerAnnotationApiResponseModel extends SwaggerAnnotation {

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
     * 主要用于同名属性覆盖
     */
    private  SwaggerAnnotationApiModelProperty[] properties={};


    public static SwaggerAnnotationApiResponseModel fromAnnotation(Model param) {
        SwaggerAnnotationApiResponseModel swaggerParam=new SwaggerAnnotationApiResponseModel();
        swaggerParam.baseModelType=param.baseModelType();
        swaggerParam.name=param.name();
        swaggerParam.ignoredProperties=param.ignoredProperties();
        SwaggerAnnotationApiModelProperty[] properties=new SwaggerAnnotationApiModelProperty[param.properties().length];
        int i=0;
        for (ApiModelProperty property : param.properties()) {
            SwaggerAnnotationApiModelProperty p=SwaggerAnnotationApiModelProperty.fromAnnotation(property);
            properties[i]=p;
        }
        swaggerParam.properties=properties;
        return swaggerParam;
    }

    public static SwaggerAnnotationApiResponseModel fromSource(NormalAnnotationExpr ann, ControllerSwaggerCompilationUnit compilationUnit) {
        SwaggerAnnotationApiResponseModel swAnn=new SwaggerAnnotationApiResponseModel();
        Map<String,Object> values=readAnnotation(ann,compilationUnit);
        BeanUtil.copy(values,swAnn);
        //
        Object[] objectArr=(Object[])values.get("ignoredProperties");
        String[] stringArr = null;
        if(objectArr!=null) {
            stringArr = ArrayUtil.castArrayType(objectArr, String.class);
            swAnn.ignoredProperties=stringArr;
        }

        return swAnn;
    }

    public String[] getIgnoreProperties() {
        return ignoredProperties;
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

    public SwaggerAnnotationApiModelProperty[] getProperties() {
        return properties;
    }
}
