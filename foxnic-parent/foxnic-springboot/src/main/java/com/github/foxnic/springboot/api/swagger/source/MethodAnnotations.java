package com.github.foxnic.springboot.api.swagger.source;


import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.collection.CollectorUtil;

import java.util.*;

public class MethodAnnotations {

    private SwaggerAnnotationApiOperation apiOperation = null;
    private Map<String,SwaggerAnnotationApiImplicitParam> paramMap = new LinkedHashMap<>();

    private SwaggerAnnotationApiOperationSupport apiOperationSupport = null;

    private Map<String,SwaggerAnnotationErrorCode> errorCodesMap = new LinkedHashMap<>();
    public Map<String, SwaggerAnnotationApiImplicitParam> getParamMap() {
        return paramMap;
    }

    public void addAnnotationApiImplicitParam(SwaggerAnnotationApiImplicitParam swaggerAnnotationApiImplicitParam) {
        paramMap.put(swaggerAnnotationApiImplicitParam.getName(),swaggerAnnotationApiImplicitParam);
    }

    public SwaggerAnnotationApiImplicitParam getSwaggerAnnotationApiImplicitParam(String name) {
        return this.paramMap.get(name);
    }

    /**
     * 把目标对象合入当前对象
     * */
    public void merge(MethodAnnotations methodAnnotations) {

        // 基础信息合并
        if(this.apiOperation==null) {
            this.apiOperation=methodAnnotations.getApiOperation();
        } else {
            if(methodAnnotations.getApiOperation()!=null) {
                BeanUtil.copy(methodAnnotations.getApiOperation(),this.apiOperation,true);
            }
        }

        // 参数清单合并
        CollectorUtil.CompareResult<String,String> result=CollectorUtil.compare(this.paramMap.keySet(),methodAnnotations.getParamMap().keySet());
        for (String key : result.getSourceDiff()) {
            this.paramMap.remove(key);
        }
        for (String key : result.getIntersection()) {
            this.paramMap.put(key,methodAnnotations.getParamMap().get(key));
        }
        for (String key : result.getTargetDiff()) {
            this.paramMap.put(key,methodAnnotations.getParamMap().get(key));
        }

        if(this.apiOperationSupport==null) {
            this.apiOperationSupport=methodAnnotations.getApiOperationSupport();
        } else {
            if(methodAnnotations.getApiOperationSupport()!=null) {
                 BeanUtil.copy(methodAnnotations.getApiOperationSupport(),this.apiOperationSupport,true);
            }
        }

        // 错误码合并
        result=CollectorUtil.compare(this.errorCodesMap.keySet(),methodAnnotations.getErrorCodesMap().keySet());
        for (String key : result.getSourceDiff()) {
            this.errorCodesMap.remove(key);
        }
        for (String key : result.getIntersection()) {
            this.errorCodesMap.put(key,methodAnnotations.getErrorCodesMap().get(key));
        }
        for (String key : result.getTargetDiff()) {
            this.errorCodesMap.put(key,methodAnnotations.getErrorCodesMap().get(key));
        }

    }

    public SwaggerAnnotationApiOperation getApiOperation() {
        return apiOperation;
    }

    public void setApiOperation(SwaggerAnnotationApiOperation apiOperation) {
        this.apiOperation = apiOperation;
    }

    public void setApiOperationSupport(SwaggerAnnotationApiOperationSupport apiOperationSupport) {
        this.apiOperationSupport = apiOperationSupport;
    }

    public SwaggerAnnotationApiOperationSupport getApiOperationSupport() {
        return apiOperationSupport;
    }

    public void addErrorCode(SwaggerAnnotationErrorCode swaggerAnnotationErrorCode) {
        errorCodesMap.put(swaggerAnnotationErrorCode.getCode(),swaggerAnnotationErrorCode);
    }

    public Map<String, SwaggerAnnotationErrorCode> getErrorCodesMap() {
        return errorCodesMap;
    }
}
