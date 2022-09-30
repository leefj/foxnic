package com.github.foxnic.springboot.api.swagger.source;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.compiler.source.JavaCompilationUnit;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import io.swagger.annotations.ApiModel;

import java.util.Map;

public class SwaggerAnnotationApiModel extends SwaggerAnnotation {

    /**
     * Provide an alternative name for the model.
     * <p>
     * By default, the class name is used.
     */
    private String value= "";

    /**
     * Provide a longer description of the class.
     */
    private String description= "";

    /**
     * Provide a superclass for the model to allow describing inheritance.
     */
    private Class<?> parent= Void.class;

    /**
     * Supports model inheritance and polymorphism.
     * <p>
     * This is the name of the field used as a discriminator. Based on this field,
     * it would be possible to assert which sub type needs to be used.
     */
    private String discriminator= "";

    /**
     * An array of the sub types inheriting from this model.
     */
    private Class<?>[] subTypes= {};

    /**
     * Specifies a reference to the corresponding type definition, overrides any other metadata specified
     */

    private String reference= "";


    public static SwaggerAnnotationApiModel fromAnnotation(ApiModel param) {
        SwaggerAnnotationApiModel swaggerParam=new SwaggerAnnotationApiModel();
        swaggerParam.description = param.description();
        swaggerParam.value = param.value();
        swaggerParam.parent = param.parent();
        swaggerParam.discriminator = param.discriminator();
        swaggerParam.subTypes = param.subTypes();
        swaggerParam.reference = param.reference();
        return swaggerParam;
    }

    public static SwaggerAnnotationApiModel fromSource(NormalAnnotationExpr ann, JavaCompilationUnit compilationUnit) {
        SwaggerAnnotationApiModel swAnn=new SwaggerAnnotationApiModel();
        Map<String,Object> values=readAnnotation(ann,compilationUnit);
        BeanUtil.copy(values,swAnn);
        return swAnn;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public Class<?> getParent() {
        return parent;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public Class<?>[] getSubTypes() {
        return subTypes;
    }

    public String getReference() {
        return reference;
    }
}
