package com.github.foxnic.springboot.api.swagger.source;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import java.util.Map;

public class SwaggerAnnotationApi extends SwaggerAnnotation {

    private String value="";
    private String[] tags={};
    private String produces="";
    private String consumes="";
    private  String protocols="";
    // 暂不支持
    private  Authorization[] authorizations=null;
    private boolean hidden=false;


    public static SwaggerAnnotationApi fromAnnotation(Api param) {
        SwaggerAnnotationApi swaggerParam=new SwaggerAnnotationApi();
        swaggerParam.value = param.value();
        swaggerParam.tags = param.tags();
        swaggerParam.produces = param.produces();
        swaggerParam.consumes = param.consumes();
        swaggerParam.protocols = param.protocols();
        swaggerParam.authorizations = param.authorizations();
        swaggerParam.hidden = param.hidden();
        return swaggerParam;
    }

    public static SwaggerAnnotationApi fromSource(NormalAnnotationExpr ann, ControllerSwaggerCompilationUnit compilationUnit) {
        SwaggerAnnotationApi api=new SwaggerAnnotationApi();
        Map<String,Object> values=readAnnotation(ann,compilationUnit);
        BeanUtil.copy(values,api);
        api.setSource(ann);
        return api;
    }

    public String getValue() {
        return value;
    }

    public String[] getTags() {
        return tags;
    }

    public String getProduces() {
        return produces;
    }

    public String getConsumes() {
        return consumes;
    }

    public String getProtocols() {
        return protocols;
    }

    public Authorization[] getAuthorizations() {
        return authorizations;
    }

    public boolean isHidden() {
        return hidden;
    }
}
