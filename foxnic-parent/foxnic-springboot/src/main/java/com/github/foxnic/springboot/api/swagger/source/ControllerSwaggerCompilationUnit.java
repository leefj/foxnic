package com.github.foxnic.springboot.api.swagger.source;

import com.github.foxnic.commons.cache.LocalCache;
import com.github.foxnic.commons.compiler.source.ControllerCompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.lang.reflect.Method;
import java.util.*;

public class ControllerSwaggerCompilationUnit extends ControllerCompilationUnit {

    private static LocalCache<String,Map<String, MethodAnnotations>> SWAGGER_ANNOTATION_UNIT_CACHE=new LocalCache<>();
    private static Map<String,Long> SWAGGER_FILE_LAST_MODIFY=new HashMap<>();

    public ControllerSwaggerCompilationUnit(Class javaClass) {
        super(javaClass,false);
        Long lastModify=SWAGGER_FILE_LAST_MODIFY.get(javaClass.getName());
        boolean doInit=false;
        if(lastModify==null) {
            doInit = true;
        } else {
            if(lastModify!=this.getJavaFile().lastModified()) {
                doInit = true;
            }
        }
        doInit = true;
        SWAGGER_FILE_LAST_MODIFY.put(javaClass.getName(),this.getJavaFile().lastModified());
        if(doInit) {
            this.init();
        }

    }

    /**
     * 从编译后的字节码读取注解
     * */
    public MethodAnnotations getMethodAnnotationsFromClass(Method method) {
        MethodAnnotations methodAnnotations=new MethodAnnotations();

//        if("insert".equals(method.getName())) {
//            System.out.println();
//        }

        ApiOperation apiOperation=method.getAnnotation(ApiOperation.class);
        if(apiOperation!=null) {
            SwaggerAnnotationApiOperation swaggerAnnotationApiOperation=SwaggerAnnotationApiOperation.fromAnnotation(apiOperation);
            methodAnnotations.setApiOperation(swaggerAnnotationApiOperation);
        }

        // 获得注解的参数列表
        ApiImplicitParams apiImplicitParams=method.getAnnotation(ApiImplicitParams.class);
        ApiImplicitParam[] apiImplicitParamArr = null;
        if(apiImplicitParams!=null) {
            apiImplicitParamArr=apiImplicitParams.value();
            for (ApiImplicitParam apiImplicitParam : apiImplicitParamArr) {
                SwaggerAnnotationApiImplicitParam annotationApiImplicitParam=SwaggerAnnotationApiImplicitParam.fromAnnotation(apiImplicitParam);
                methodAnnotations.addAnnotationApiImplicitParam(annotationApiImplicitParam);
            }
        }

        ApiOperationSupport apiOperationSupport=method.getAnnotation(ApiOperationSupport.class);
        if(apiOperationSupport!=null) {
            SwaggerAnnotationApiOperationSupport swaggerAnnotationApiOperationSupport=SwaggerAnnotationApiOperationSupport.fromAnnotation(apiOperationSupport);
            methodAnnotations.setApiOperationSupport(swaggerAnnotationApiOperationSupport);
        }
        return methodAnnotations;
    }

    /**
     * 从源码读取注解
     * */
    public MethodAnnotations getMethodAnnotationsFromSource(Method method) {
        Map<String, MethodAnnotations> map=SWAGGER_ANNOTATION_UNIT_CACHE.get(this.getJavaClass().getName());
        if(map==null) {
            map = new HashMap<>();
            SWAGGER_ANNOTATION_UNIT_CACHE.put(this.getJavaClass().getName(),map);
        }

        MethodAnnotations methodAnnotations =  map.get(method.toGenericString());
        if(methodAnnotations==null && this.getCompilationUnit()==null) {
            init();
        }

        if(this.getCompilationUnit()==null && methodAnnotations!=null) {
            return methodAnnotations;
        }


        // 从源码解析
        methodAnnotations = new MethodAnnotations();
        MethodDeclaration methodDeclaration=this.findMethod(method);
        if(methodDeclaration==null) return null;

        // ApiOperation
        AnnotationExpr apiOperation = methodDeclaration.getAnnotationByClass(ApiOperation.class).get();
        if(apiOperation!=null) {
            SwaggerAnnotationApiOperation swaggerAnnotationApiOperation=SwaggerAnnotationApiOperation.fromSource((NormalAnnotationExpr)apiOperation,this);
            methodAnnotations.setApiOperation(swaggerAnnotationApiOperation);
        }


        // ApiImplicitParams
        AnnotationExpr apiImplicitParams = methodDeclaration.getAnnotationByClass(ApiImplicitParams.class).get();
        List<Node> nodes= apiImplicitParams.getChildNodes();
        ArrayInitializerExpr apiImplicitParamArrayExpr=null;
        for (Node node : nodes) {
            if(node instanceof ArrayInitializerExpr) {
                apiImplicitParamArrayExpr=(ArrayInitializerExpr)node;
            } else  if(node instanceof MemberValuePair) {
                MemberValuePair memberValuePair=(MemberValuePair)node;
                if("value".equals(memberValuePair.getName().getIdentifier())) {
                    Expression expression= memberValuePair.getValue();
                    if(expression instanceof ArrayInitializerExpr) {
                        apiImplicitParamArrayExpr=(ArrayInitializerExpr)expression;
                    }
                }
            }
        }

        if(apiImplicitParamArrayExpr!=null) {
            List<Node>  apiImplicitParamNodes=apiImplicitParamArrayExpr.getChildNodes();
            for (Node apiImplicitParamNode : apiImplicitParamNodes) {
                System.out.println();
                if(apiImplicitParamNode instanceof NormalAnnotationExpr) {
                    SwaggerAnnotationApiImplicitParam swaggerAnnotationApiImplicitParam=SwaggerAnnotationApiImplicitParam.fromSource((NormalAnnotationExpr)apiImplicitParamNode,this);
                    if(swaggerAnnotationApiImplicitParam!=null) {
                        methodAnnotations.addAnnotationApiImplicitParam(swaggerAnnotationApiImplicitParam);
                    }
                }
            }
        }


        // ApiOperationSupport
        AnnotationExpr apiOperationSupport = methodDeclaration.getAnnotationByClass(ApiOperationSupport.class).get();
        if(apiOperationSupport!=null) {
            SwaggerAnnotationApiOperationSupport swaggerAnnotationApiOperation=SwaggerAnnotationApiOperationSupport.fromSource((NormalAnnotationExpr)apiOperationSupport,this);
            methodAnnotations.setApiOperationSupport(swaggerAnnotationApiOperation);
        }

        map.put(method.toGenericString(),methodAnnotations);
        return methodAnnotations;
    }










}
