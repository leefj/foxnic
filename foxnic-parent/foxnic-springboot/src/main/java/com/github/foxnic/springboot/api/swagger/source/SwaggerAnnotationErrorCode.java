package com.github.foxnic.springboot.api.swagger.source;

import com.github.foxnic.api.swagger.ErrorCode;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;

import java.util.Map;

public class SwaggerAnnotationErrorCode extends SwaggerAnnotation {

    /**
     * 错误码
     * */
    private String code="";
    /**
     * 错误名称
     * */
    private String name="";
    /**
     * 错误描述
     * */
    private String desc="";

    /**
     * 可能原因与解决办法
     */
    private String[] solutions={};


    public static SwaggerAnnotationErrorCode fromAnnotation(ErrorCode param) {
        SwaggerAnnotationErrorCode swaggerParam=new SwaggerAnnotationErrorCode();
        swaggerParam.name = param.name();
        swaggerParam.code = param.code();
        swaggerParam.desc = param.desc();
        swaggerParam.solutions = param.solutions();
        return swaggerParam;
    }

    public static SwaggerAnnotationErrorCode fromSource(NormalAnnotationExpr ann, ControllerSwaggerCompilationUnit compilationUnit) {
        SwaggerAnnotationErrorCode swAnn=new SwaggerAnnotationErrorCode();
        Map<String,Object> values=readAnnotation(ann,compilationUnit);
        Object[] solutions=(Object[])values.get("solutions");
        solutions=ArrayUtil.castArrayType(solutions,String.class);
        values.put("solutions",solutions);
        BeanUtil.copy(values,swAnn);
        return swAnn;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String[] getSolutions() {
        return solutions;
    }
}
