package com.github.foxnic.springboot.api.swagger.source;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.collection.CollectorUtil;

import java.util.HashMap;
import java.util.Map;

public class ModelAnnotations {

    private SwaggerAnnotationApiModel apiModel;
    private Map<String,SwaggerAnnotationApiModelProperty> apiModelPropertyMap = new HashMap<>();


    public Map<String, SwaggerAnnotationApiModelProperty> getApiModelPropertyMap() {
        return apiModelPropertyMap;
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

    public SwaggerAnnotationApiModelProperty getApiModelProperty(String name) {
        return apiModelPropertyMap.get(name);
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
