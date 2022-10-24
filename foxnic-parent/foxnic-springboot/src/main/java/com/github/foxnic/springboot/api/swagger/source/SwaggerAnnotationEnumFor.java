package com.github.foxnic.springboot.api.swagger.source;

import com.github.foxnic.api.swagger.EnumFor;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.compiler.source.JavaCompilationUnit;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import io.swagger.annotations.ApiModel;

import java.lang.reflect.Field;
import java.util.Map;

public class SwaggerAnnotationEnumFor extends SwaggerAnnotation {

    /**
     * Provide an alternative name for the model.
     * <p>
     * By default, the class name is used.
     */
    private String value= "";

    private Field field;



    public static SwaggerAnnotationEnumFor fromAnnotation(EnumFor param) {
        SwaggerAnnotationEnumFor swaggerParam=new SwaggerAnnotationEnumFor();
        swaggerParam.value = param.value();
        return swaggerParam;
    }

    public static SwaggerAnnotationEnumFor fromSource(NormalAnnotationExpr ann, JavaCompilationUnit compilationUnit) {
        SwaggerAnnotationEnumFor swAnn=new SwaggerAnnotationEnumFor();
        Map<String,Object> values=readAnnotation(ann,compilationUnit);
        BeanUtil.copy(values,swAnn);
        return swAnn;
    }

    public String getValue() {
        return value;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }


}
