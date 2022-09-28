package com.github.foxnic.springboot.api.swagger;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.cache.LocalCache;
import com.github.foxnic.commons.compiler.source.ControllerCompilationUnit;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class ControllerSwaggerCompilationUnit extends ControllerCompilationUnit {

    private static LocalCache<String,Map<String,SwaggerAnnotationUnit>> SWAGGER_ANNOTATION_UNIT_CACHE=new LocalCache<>();
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

    public SwaggerAnnotationUnit getSwaggerAnnotationUnit(Method method) {
        Map<String,SwaggerAnnotationUnit> map=SWAGGER_ANNOTATION_UNIT_CACHE.get(this.getJavaClass().getName());
        if(map==null) {
            map = new HashMap<>();
            SWAGGER_ANNOTATION_UNIT_CACHE.put(this.getJavaClass().getName(),map);
        }
        // 重新解析
        if(this.getCompilationUnit()!=null) {



            MethodDeclaration methodDeclaration=this.findMethod(method);
            if(methodDeclaration!=null) {
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
                //
                if(apiImplicitParamArrayExpr!=null) {
                    List<Node>  apiImplicitParamNodes=apiImplicitParamArrayExpr.getChildNodes();
                    for (Node apiImplicitParamNode : apiImplicitParamNodes) {
                        System.out.println();
                        if(apiImplicitParamNode instanceof NormalAnnotationExpr) {
                            SwaggerAnnotationApiImplicitParam swaggerAnnotationApiImplicitParam=SwaggerAnnotationApiImplicitParam.fromSource((NormalAnnotationExpr)apiImplicitParamNode,this);
                        }
                    }
                }
            } else {
                return null;
            }
        } else {
            return map.get(method.toGenericString());
        }
        return null;
    }








    public static class SwaggerAnnotationApiImplicitParam {

        private String name;
        private String value;
        private String defaultValue="";
        private String allowableValues="";
        private boolean required=false;
        private String access="";
        private boolean allowMultiple=false;
        private String dataType="";
        private Class<?> dataTypeClass=Void.class;
        private String paramType="";
        private String example="";
        private String[] examples;
        private String type="";
        private String format="";
        private boolean allowEmptyValue=false;
        private boolean readOnly=false;
        private String collectionFormat;

        public static SwaggerAnnotationApiImplicitParam fromApiImplicitParam(ApiImplicitParam param) {
            SwaggerAnnotationApiImplicitParam swaggerParam=new SwaggerAnnotationApiImplicitParam();
            swaggerParam.name=param.name();
            swaggerParam.value=param.value();
            swaggerParam.defaultValue=param.defaultValue();
            swaggerParam.allowableValues=param.allowableValues();
            swaggerParam.required=param.required();
            swaggerParam.access=param.access();
            swaggerParam.allowMultiple=param.allowMultiple();
            swaggerParam.dataType=param.dataType();
            swaggerParam.dataTypeClass=param.dataTypeClass();
            swaggerParam.paramType=param.paramType();
            swaggerParam.example=param.example();
            if(param.examples()!=null) {
                // 暂不支持
                swaggerParam.examples = new String[0];
            }
            swaggerParam.format=param.format();
            swaggerParam.allowEmptyValue=param.allowEmptyValue();
            swaggerParam.readOnly=param.readOnly();
            swaggerParam.collectionFormat=param.collectionFormat();
            return swaggerParam;
        }

        private static Object readField(FieldAccessExpr expr,ControllerSwaggerCompilationUnit compilationUnit) {
            NameExpr scope=(NameExpr)expr.getScope();
            String simpleClassName=scope.getName().getIdentifier();
            Class type=compilationUnit.getImportedClass(simpleClassName);
            if(type==null) {
                throw new RuntimeException("无法识别 "+simpleClassName);
            }
            try {
                Field field = type.getField(expr.getName().getIdentifier());
                field.setAccessible(true);
                Object value=field.get(null);
                return value;
            } catch (Exception e){
                Logger.exception("读取失败",e);
            }
            return null;
        }

        public static SwaggerAnnotationApiImplicitParam fromSource(NormalAnnotationExpr ann,ControllerSwaggerCompilationUnit compilationUnit) {
            SwaggerAnnotationApiImplicitParam swaggerParam=new SwaggerAnnotationApiImplicitParam();
            for (Node node : ann.getChildNodes()) {
                if(!(node instanceof MemberValuePair)) {
                    continue;
                }
                MemberValuePair mvp=(MemberValuePair) node;
                Object value = null;
                if(mvp.getValue() instanceof FieldAccessExpr) {
                    FieldAccessExpr expr= (FieldAccessExpr) mvp.getValue();
                    value=readField(expr,compilationUnit);
                } else if (mvp.getValue() instanceof StringLiteralExpr) {
                    StringLiteralExpr expr=(StringLiteralExpr) mvp.getValue();
                    value=expr.getValue();
                } else if (mvp.getValue() instanceof BooleanLiteralExpr) {
                    BooleanLiteralExpr expr=(BooleanLiteralExpr) mvp.getValue();
                    value=expr.getValue();
                }
                else if (mvp.getValue() instanceof ClassExpr) {
                    ClassExpr expr=(ClassExpr) mvp.getValue();
                    value=expr.getType().asClassOrInterfaceType();
                }
                else {
                    System.out.printf("");
                }


//                if(value!=null){
//                    swaggerParam.name = value.toString();
//                }

            }
//            swaggerParam.name=param.name();
//            swaggerParam.value=param.value();
//            swaggerParam.defaultValue=param.defaultValue();
//            swaggerParam.allowableValues=param.allowableValues();
//            swaggerParam.required=param.required();
//            swaggerParam.access=param.access();
//            swaggerParam.allowMultiple=param.allowMultiple();
//            swaggerParam.dataType=param.dataType();
//            swaggerParam.dataTypeClass=param.dataTypeClass();
//            swaggerParam.paramType=param.paramType();
//            swaggerParam.example=param.example();
//            if(param.examples()!=null) {
//                // 暂不支持
//                swaggerParam.examples = new String[0];
//            }
//            swaggerParam.format=param.format();
//            swaggerParam.allowEmptyValue=param.allowEmptyValue();
//            swaggerParam.readOnly=param.readOnly();
//            swaggerParam.collectionFormat=param.collectionFormat();
            return swaggerParam;
        }


        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public String getAllowableValues() {
            return allowableValues;
        }

        public boolean isRequired() {
            return required;
        }

        public String getAccess() {
            return access;
        }

        public boolean isAllowMultiple() {
            return allowMultiple;
        }

        public String getDataType() {
            return dataType;
        }

        public Class<?> getDataTypeClass() {
            return dataTypeClass;
        }

        public String getParamType() {
            return paramType;
        }

        public String getExample() {
            return example;
        }

        public String[] getExamples() {
            return examples;
        }

        public String getType() {
            return type;
        }

        public String getFormat() {
            return format;
        }

        public boolean isAllowEmptyValue() {
            return allowEmptyValue;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public String getCollectionFormat() {
            return collectionFormat;
        }


    }

    public static class SwaggerAnnotationUnit {
        private Map<String,SwaggerAnnotationApiImplicitParam> paramMap = new HashMap<>();


    }

}
