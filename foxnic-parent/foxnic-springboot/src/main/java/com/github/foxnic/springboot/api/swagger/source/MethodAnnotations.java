package com.github.foxnic.springboot.api.swagger.source;


import com.github.foxnic.api.swagger.Model;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.collection.CollectorUtil;

import java.util.*;

public class MethodAnnotations {

    private SwaggerAnnotationApiOperation apiOperation = null;
    private Map<String,SwaggerAnnotationApiImplicitParam> paramMap = new LinkedHashMap<>();

    private SwaggerAnnotationApiOperationSupport apiOperationSupport = null;

    private  Map<String, SwaggerAnnotationApiResponseModel> responseModelMap = new HashMap<>();

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

        if(methodAnnotations==null) {
            return;
        }

        // 基础信息合并
        if(this.apiOperation==null) {
            this.apiOperation=methodAnnotations.getApiOperation();
        } else {
            if(methodAnnotations.getApiOperation()!=null) {
                BeanUtil.copy(methodAnnotations.getApiOperation(),this.apiOperation,true);
            }
        }

        CollectorUtil.CompareResult<String,String> result = null;

        if(this.apiOperation.getValue().equals("jdk-Map-动态参数")) {
            System.out.println();
        }


        // DynamicParameter 合并
        result=CollectorUtil.compare(this.dynamicParameterMap.keySet(),methodAnnotations.getDynamicParameterMap().keySet());
        for (String key : result.getSourceDiff()) {
            this.dynamicParameterMap.remove(key);
        }
        for (String key : result.getIntersection()) {
            this.dynamicParameterMap.put(key,methodAnnotations.getDynamicParameterMap().get(key));
        }
        for (String key : result.getTargetDiff()) {
            this.dynamicParameterMap.put(key,methodAnnotations.getDynamicParameterMap().get(key));
        }


        for (Map.Entry<String, SwaggerAnnotationDynamicParameter> e : this.dynamicParameterMap.entrySet()) {
            this.addAnnotationApiImplicitParam(SwaggerAnnotationApiImplicitParam.fromSwaggerAnnotationDynamicParameter(e.getValue()));
            methodAnnotations.addAnnotationApiImplicitParam(SwaggerAnnotationApiImplicitParam.fromSwaggerAnnotationDynamicParameter(e.getValue()));
        }


        // 参数清单合并
        result=CollectorUtil.compare(this.paramMap.keySet(),methodAnnotations.getParamMap().keySet());
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


        // ResponseModel 合并
        result=CollectorUtil.compare(this.responseModelMap.keySet(),methodAnnotations.getResponseModelMap().keySet());
        for (String key : result.getSourceDiff()) {
            this.responseModelMap.remove(key);
        }
        for (String key : result.getIntersection()) {
            this.responseModelMap.put(key,methodAnnotations.getResponseModelMap().get(key));
        }
        for (String key : result.getTargetDiff()) {
            this.responseModelMap.put(key,methodAnnotations.getResponseModelMap().get(key));
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

        // DynamicResponseParameter 合并
        result=CollectorUtil.compare(this.dynamicResponseParameterMap.keySet(),methodAnnotations.getDynamicResponseParameterMap().keySet());
        for (String key : result.getSourceDiff()) {
            this.dynamicResponseParameterMap.remove(key);
        }
        for (String key : result.getIntersection()) {
            this.dynamicResponseParameterMap.put(key,methodAnnotations.getDynamicResponseParameterMap().get(key));
        }
        for (String key : result.getTargetDiff()) {
            this.dynamicResponseParameterMap.put(key,methodAnnotations.getDynamicResponseParameterMap().get(key));
        }

        //
        this.dynamicResponseParameters=methodAnnotations.dynamicResponseParameters;


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

    private Map<String,SwaggerAnnotationDynamicParameter> dynamicParameterMap=new LinkedHashMap<>();
    private Map<String,SwaggerAnnotationDynamicParameter> dynamicResponseParameterMap=new LinkedHashMap<>();

    public void addDynamicParameter(SwaggerAnnotationDynamicParameter dynamicParameter) {
        dynamicParameterMap.put(dynamicParameter.getName(),dynamicParameter);
    }

    public void addDynamicResponseParameter(SwaggerAnnotationDynamicParameter dynamicParameter) {
        dynamicResponseParameterMap.put(dynamicParameter.getName(),dynamicParameter);
    }

    public Map<String, SwaggerAnnotationDynamicParameter> getDynamicParameterMap() {
        return dynamicParameterMap;
    }

    public Map<String, SwaggerAnnotationDynamicParameter> getDynamicResponseParameterMap() {
        return dynamicResponseParameterMap;
    }

    private SwaggerAnnotationDynamicResponseParameters dynamicResponseParameters;
    public void setDynamicResponseParameters(SwaggerAnnotationDynamicResponseParameters swaggerAnnotationDynamicResponseParameters) {
        this.dynamicResponseParameters=swaggerAnnotationDynamicResponseParameters;
    }

    public SwaggerAnnotationDynamicResponseParameters getDynamicResponseParameters() {
        return dynamicResponseParameters;
    }

    public void addResponseModel(SwaggerAnnotationApiResponseModel swaggerAnnotationApiResponseModel) {
        if(this.responseModelMap.containsKey(swaggerAnnotationApiResponseModel.getBaseModelType().getName())) return;
        this.responseModelMap.put(swaggerAnnotationApiResponseModel.getBaseModelType().getName(),swaggerAnnotationApiResponseModel);
    }

    public Map<String, SwaggerAnnotationApiResponseModel> getResponseModelMap() {
        return responseModelMap;
    }
}
