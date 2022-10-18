package com.github.foxnic.springboot.api.swagger.source;

import com.github.foxnic.api.web.Forbidden;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;

import java.util.Map;

public class SwaggerAnnotationForbidden extends SwaggerAnnotation {

    private String value="";

    public static SwaggerAnnotationForbidden fromAnnotation(Forbidden param) {
        SwaggerAnnotationForbidden swaggerParam=new SwaggerAnnotationForbidden();
        swaggerParam.value = param.value();
        return swaggerParam;
    }

    public static SwaggerAnnotationForbidden fromSource(AnnotationExpr ann, ControllerSwaggerCompilationUnit compilationUnit) {
        SwaggerAnnotationForbidden api=new SwaggerAnnotationForbidden();
        if(ann instanceof NormalAnnotationExpr) {
            Map<String, Object> values = readAnnotation(ann, compilationUnit);
            BeanUtil.copy(values, api);
        } else if(ann instanceof MarkerAnnotationExpr) {

        } else {
            throw new RuntimeException("not support");
        }
        api.setSource(ann);
        return api;
    }

    public String getValue() {
        return value;
    }

}
