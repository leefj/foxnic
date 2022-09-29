package com.github.foxnic.springboot.api.swagger.source;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import com.github.xiaoymin.knife4j.annotations.DynamicParameters;
import io.swagger.annotations.ApiOperation;

import java.util.Map;

public class SwaggerAnnotationDynamicParameters extends SwaggerAnnotation {

    private String name = "";
    private SwaggerAnnotationDynamicParameter[] properties = null;

    public static SwaggerAnnotationDynamicParameters fromAnnotation(DynamicParameters param) {
        SwaggerAnnotationDynamicParameters swaggerParam=new SwaggerAnnotationDynamicParameters();
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

    public static SwaggerAnnotationDynamicParameters fromSource(NormalAnnotationExpr ann, ControllerSwaggerCompilationUnit compilationUnit) {
        SwaggerAnnotationDynamicParameters swAnn=new SwaggerAnnotationDynamicParameters();
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
