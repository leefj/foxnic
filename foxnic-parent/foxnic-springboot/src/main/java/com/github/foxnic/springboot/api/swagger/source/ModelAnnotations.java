package com.github.foxnic.springboot.api.swagger.source;

import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.collection.CollectorUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.reflect.EnumUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ModelAnnotations {

    private SwaggerAnnotationApiModel apiModel;
    private Map<String,SwaggerAnnotationApiModelProperty> apiModelPropertyMap = new HashMap<>();

    private Map<String,SwaggerAnnotationEnumFor> enumForMap = new HashMap<>();
    public Map<String, SwaggerAnnotationApiModelProperty> getApiModelPropertyMap() {
        return apiModelPropertyMap;
    }

    public Map<String, SwaggerAnnotationEnumFor> getEnumForMap() {
        return enumForMap;
    }

    public void setApiModel(SwaggerAnnotationApiModel apiModel) {
        this.apiModel = apiModel;
    }

    public SwaggerAnnotationApiModel getApiModel() {
        return apiModel;
    }

    public void addApiModelProperty(SwaggerAnnotationApiModelProperty swaggerAnnotationApiModelProperty) {
        apiModelPropertyMap.put(swaggerAnnotationApiModelProperty.getName(),swaggerAnnotationApiModelProperty);
    }

    public void addEnumFor(SwaggerAnnotationEnumFor swaggerAnnotationEnumFor) {
        enumForMap.put(swaggerAnnotationEnumFor.getField().getName(),swaggerAnnotationEnumFor);
    }

    public SwaggerAnnotationApiModelProperty getApiModelProperty(String name) {
        return apiModelPropertyMap.get(name);
    }

    public Class getEnumModel(String name) {
        Field field = null;
        for (Map.Entry<String, SwaggerAnnotationEnumFor> e : enumForMap.entrySet()) {
            if(e.getValue().getValue().equals(name)) {
                field=e.getValue().getField();
                break;
            }
        }
        if(field==null) return null;
        return field.getType();
    }

    public String getEnumContent(String name) {
        Class enumType=this.getEnumModel(name);
        if(enumType==null) return "";
        return getEnumContent(enumType);
    }


    public static String getEnumContent(Class enumType) {
        ArrayList list=new ArrayList();
        if(ReflectUtil.isSubType(CodeTextEnum.class,enumType)) {
            CodeTextEnum[] values=EnumUtil.getValues(enumType);
            if(values==null) return "";
            for (CodeTextEnum value : values) {
                list.add(value.code()+" : "+value.text());
            }
        } else if(Boolean.class.equals(enumType)) {
            list.add(true);
            list.add(false);
        }  else {
            Enum[] values =    EnumUtil.getEnumValues(enumType);
            if(values==null) return "";
            for (Enum value : values) {
                list.add(value.name());
            }
        }
        return StringUtil.join(list,"; ");
    }






    public void merge(ModelAnnotations sourceModelAnnotations) {

        if(this.apiModel==null) {
            this.apiModel=sourceModelAnnotations.getApiModel();
        } else {
            if(sourceModelAnnotations.getApiModel()!=null) {
                BeanUtil.copy(sourceModelAnnotations.getApiModel(),this.apiModel,true);
            }
        }

        // 字段清单合并
        CollectorUtil.CompareResult<String,String> result=CollectorUtil.compare(this.apiModelPropertyMap.keySet(),sourceModelAnnotations.getApiModelPropertyMap().keySet());
        for (String key : result.getSourceDiff()) {
            this.apiModelPropertyMap.remove(key);
        }
        for (String key : result.getIntersection()) {
            this.apiModelPropertyMap.put(key,sourceModelAnnotations.getApiModelPropertyMap().get(key));
        }
        for (String key : result.getTargetDiff()) {
            this.apiModelPropertyMap.put(key,sourceModelAnnotations.getApiModelPropertyMap().get(key));
        }

    }

}
