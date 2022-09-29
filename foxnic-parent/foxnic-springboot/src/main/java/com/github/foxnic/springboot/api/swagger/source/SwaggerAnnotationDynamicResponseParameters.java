package com.github.foxnic.springboot.api.swagger.source;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import com.github.xiaoymin.knife4j.annotations.DynamicResponseParameters;
import io.swagger.annotations.ApiOperation;

import java.util.Map;

public class SwaggerAnnotationDynamicResponseParameters extends SwaggerAnnotation {

    private String name = "";
    SwaggerAnnotationDynamicParameter[] properties={};


    public static SwaggerAnnotationDynamicResponseParameters fromAnnotation(DynamicResponseParameters param) {
        SwaggerAnnotationDynamicResponseParameters swaggerParam=new SwaggerAnnotationDynamicResponseParameters();
        swaggerParam.name = param.name();
        if(param.properties()!=null) {
            swaggerParam.properties=new SwaggerAnnotationDynamicParameter[param.properties().length];
            int i=0;
            for (DynamicParameter property : param.properties()) {
                swaggerParam.properties[i]=SwaggerAnnotationDynamicParameter.fromAnnotation(property);
                i++;
            }
        }
        return swaggerParam;
    }

    public static SwaggerAnnotationDynamicResponseParameters fromSource(NormalAnnotationExpr ann, ControllerSwaggerCompilationUnit compilationUnit) {
        SwaggerAnnotationDynamicResponseParameters swAnn=new SwaggerAnnotationDynamicResponseParameters();
        Map<String,Object> values=readAnnotation(ann,compilationUnit);
        BeanUtil.copy(values,swAnn);
        return swAnn;
    }

    public String getName() {
        return name;
    }

    public SwaggerAnnotationDynamicParameter[] getProperties() {
        return properties;
    }


}
