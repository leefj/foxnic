package com.github.foxnic.springboot.api.swagger.source;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.compiler.source.JavaCompilationUnit;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;

import java.lang.reflect.Field;
import java.util.Map;

public class SwaggerAnnotationApiModelProperty extends SwaggerAnnotation {

    /**
     * A brief description of this property.
     */
    private String value= "";

    /**
     * Allows overriding the name of the property.
     *
     * @return the overridden property name
     */
    private String name= "";

    /**
     * Limits the acceptable values for this parameter.
     * <p>
     * There are three ways to describe the allowable values:
     * <ol>
     * <li>To set a list of values, provide a comma-separated list.
     * For example: {@code first, second, third}.</li>
     * <li>To set a range of values, start the value with "range", and surrounding by square
     * brackets include the minimum and maximum values, or round brackets for exclusive minimum and maximum values.
     * For example: {@code range[1, 5]}, {@code range(1, 5)}, {@code range[1, 5)}.</li>
     * <li>To set a minimum/maximum value, use the same format for range but use "infinity"
     * or "-infinity" as the second value. For example, {@code range[1, infinity]} means the
     * minimum allowable value of this parameter is 1.</li>
     * </ol>
     */
    private String allowableValues= "";

    /**
     * Allows for filtering a property from the API documentation. See io.swagger.core.filter.SwaggerSpecFilter.
     */
    private String access= "";

    /**
     * Currently not in use.
     */
    private String notes= "";

    /**
     * The data type of the parameter.
     * <p>
     * This can be the class name or a primitive. The value will override the data type as read from the class
     * property.
     */
    private String dataType= "";

    /**
     * Specifies if the parameter is required or not.
     */
    private boolean required= false;

    /**
     * Allows explicitly ordering the property in the model.
     */
    private int position= 0;

    /**
     * Allows a model property to be hidden in the Swagger model definition.
     */
    private boolean hidden= false;

    /**
     * A sample value for the property.
     */
    private String example= "";


    /**
     * Allows to specify the access mode of a model property (AccessMode.READ_ONLY, READ_WRITE)
     *
     * @since 1.5.19
     */
    private  ApiModelProperty.AccessMode accessMode= ApiModelProperty.AccessMode.AUTO;


    /**
     * Specifies a reference to the corresponding type definition, overrides any other metadata specified
     */

    private String reference= "";

    /**
     * Allows passing an empty value
     *
     * @since 1.5.11
     */
    private boolean allowEmptyValue= false;

    /**
     * @return an optional array of extensions
     */
    // Extension[] extensions= @Extension(properties = @ExtensionProperty(name = "", value = ""));

    private Field field;

    public static SwaggerAnnotationApiModelProperty fromAnnotation(ApiModelProperty param) {
        SwaggerAnnotationApiModelProperty swaggerParam=new SwaggerAnnotationApiModelProperty();
        swaggerParam.name = param.name();
        swaggerParam.value = param.value();
        swaggerParam.required = param.required();
        swaggerParam.allowableValues = param.allowableValues();
        swaggerParam.example = param.example();
        swaggerParam.access = param.access();
        swaggerParam.notes = param.notes();
        swaggerParam.dataType = param.dataType();
        swaggerParam.position = param.position();
        swaggerParam.hidden = param.hidden();
        swaggerParam.reference = param.reference();
        swaggerParam.allowEmptyValue = param.allowEmptyValue();
        return swaggerParam;
    }

    public static SwaggerAnnotationApiModelProperty fromSource(NormalAnnotationExpr ann, JavaCompilationUnit compilationUnit) {
        SwaggerAnnotationApiModelProperty swAnn=new SwaggerAnnotationApiModelProperty();
        Map<String,Object> values=readAnnotation(ann,compilationUnit);
        BeanUtil.copy(values,swAnn);
        return swAnn;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public String getAllowableValues() {
        return allowableValues;
    }

    public String getAccess() {
        return access;
    }

    public String getNotes() {
        return notes;
    }

    public String getDataType() {
        return dataType;
    }

    public boolean isRequired() {
        return required;
    }

    public int getPosition() {
        return position;
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getExample() {
        return example;
    }

    public ApiModelProperty.AccessMode getAccessMode() {
        return accessMode;
    }

    public String getReference() {
        return reference;
    }

    public boolean isAllowEmptyValue() {
        return allowEmptyValue;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public void setName(String name) {
        this.name = name;
    }
}
