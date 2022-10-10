package com.github.foxnic.springboot.api.swagger.source;

import com.github.foxnic.api.swagger.ErrorCode;
import com.github.foxnic.api.swagger.ErrorCodes;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import com.github.xiaoymin.knife4j.annotations.DynamicParameters;

import java.util.Map;

public class SwaggerAnnotationErrorCodes extends SwaggerAnnotation {

    private SwaggerAnnotationErrorCode[] value = null;

    public static SwaggerAnnotationErrorCodes fromAnnotation(ErrorCodes param) {
        SwaggerAnnotationErrorCodes swaggerParam=new SwaggerAnnotationErrorCodes();

        if(param.value()!=null) {
            swaggerParam.value=new SwaggerAnnotationErrorCode[param.value().length];
            int i=0;
            for (ErrorCode property : param.value()) {
                swaggerParam.value[i]=SwaggerAnnotationErrorCode.fromAnnotation(property);
                i++;
            }
        }
        return swaggerParam;
    }

    public static SwaggerAnnotationErrorCodes fromSource(NormalAnnotationExpr ann, ControllerSwaggerCompilationUnit compilationUnit) {
        SwaggerAnnotationErrorCodes swAnn=new SwaggerAnnotationErrorCodes();
        Map<String,Object> values=readAnnotation(ann,compilationUnit);
        BeanUtil.copy(values,swAnn);
        return swAnn;
    }

    public SwaggerAnnotationErrorCode[] getValue() {
        return value;
    }
}
