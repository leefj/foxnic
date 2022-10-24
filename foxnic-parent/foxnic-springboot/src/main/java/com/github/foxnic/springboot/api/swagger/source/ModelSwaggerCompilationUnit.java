package com.github.foxnic.springboot.api.swagger.source;

import com.github.foxnic.api.swagger.EnumFor;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.cache.LocalCache;
import com.github.foxnic.commons.compiler.source.ControllerCompilationUnit;
import com.github.foxnic.commons.compiler.source.ModelCompilationUnit;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModelSwaggerCompilationUnit extends ModelCompilationUnit {

    private static LocalCache<String,ModelAnnotations> SWAGGER_ANNOTATION_UNIT_CACHE=new LocalCache<>();
    private static Map<String,Long> SWAGGER_FILE_LAST_MODIFY=new HashMap<>();

    public ModelSwaggerCompilationUnit(Class javaClass) {
        super(javaClass,false);
        initIf(javaClass);

    }

    private void initIf(Class javaClass) {
        Long lastModify=SWAGGER_FILE_LAST_MODIFY.get(javaClass.getName());
        boolean doInit=false;
        if(lastModify==null) {
            doInit = true;
        } else {
            if(lastModify!=this.getJavaFile().lastModified()) {
                doInit = true;
            }
        }
        if(this.getJavaFile()!=null) {
            SWAGGER_FILE_LAST_MODIFY.put(javaClass.getName(), this.getJavaFile().lastModified());
            if (doInit) {
                this.init();
                SWAGGER_ANNOTATION_UNIT_CACHE.remove(this.getJavaClass().getName());
            }
        }
    }

    /**
     * 从编译后的字节码读取注解
     * */
    public ModelAnnotations createFromClassBytes() {
        Class modelType=this.getJavaClass();
        ModelAnnotations modelAnnotations=new ModelAnnotations();

        ApiModel apiModelAnn=(ApiModel)modelType.getAnnotation(ApiModel.class);
        if(apiModelAnn!=null) {
            SwaggerAnnotationApiModel apiModel = SwaggerAnnotationApiModel.fromAnnotation(apiModelAnn);
            modelAnnotations.setApiModel(apiModel);
        }

        List<Field> fields = BeanUtil.getAllFields(modelType);

        for (Field field : fields) {
            ApiModelProperty apiModelPropertyAnn=field.getAnnotation(ApiModelProperty.class);
            if(apiModelPropertyAnn!=null) {
                SwaggerAnnotationApiModelProperty swaggerAnnotationApiModelProperty = SwaggerAnnotationApiModelProperty.fromAnnotation(apiModelPropertyAnn);
                swaggerAnnotationApiModelProperty.setField(field);
                modelAnnotations.addApiModelProperty(swaggerAnnotationApiModelProperty);
            }
            EnumFor enumForAnn=field.getAnnotation(EnumFor.class);
            if(enumForAnn!=null) {
                SwaggerAnnotationEnumFor swaggerAnnotationEnumFor = SwaggerAnnotationEnumFor.fromAnnotation(enumForAnn);
                swaggerAnnotationEnumFor.setField(field);
                modelAnnotations.addEnumFor(swaggerAnnotationEnumFor);
            }
        }

        return modelAnnotations;
    }

    /**
     * 从源码读取注解
     * */
    public ModelAnnotations createFromSource() {

        initIf(this.getJavaClass());

        ModelAnnotations modelAnnotations=SWAGGER_ANNOTATION_UNIT_CACHE.get(this.getJavaClass().getName());

        if(modelAnnotations==null && this.getCompilationUnit()==null) {
            init();
        }

        if(this.getCompilationUnit()==null && modelAnnotations!=null) {
            return modelAnnotations;
        }

        // 从源码解析
        modelAnnotations = new ModelAnnotations();

        List<NormalAnnotationExpr> anns=this.find(NormalAnnotationExpr.class);
        NormalAnnotationExpr apiModel=null;
        for (NormalAnnotationExpr ann : anns) {
            if(ann.getName().getIdentifier().equals(ApiModel.class.getSimpleName())) {
                apiModel=ann;
                break;
            }
        }

        if(apiModel!=null) {
            SwaggerAnnotationApiModel swaggerAnnotationApiModel=SwaggerAnnotationApiModel.fromSource(apiModel,this);
            modelAnnotations.setApiModel(swaggerAnnotationApiModel);
        }


        // 获得父类属性
        if(this.getJavaClass().getSuperclass()!=null) {
            ModelSwaggerCompilationUnit superUnit = new ModelSwaggerCompilationUnit(this.getJavaClass().getSuperclass());
            if(superUnit.isValid()) {
                ModelAnnotations superModel = superUnit.createFromSource();
                for (SwaggerAnnotationApiModelProperty value : superModel.getApiModelPropertyMap().values()) {
                    modelAnnotations.addApiModelProperty(value);
                }
            }
        }

        // 处理当前类的属性
       List<FieldDeclaration> fields= this.find(FieldDeclaration.class);
        for (FieldDeclaration field : fields) {
            String name = null;
            NormalAnnotationExpr apiModelProperty = null;
            NormalAnnotationExpr enumForAnn = null;
            for (Node n : field.getChildNodes()) {

                if(n instanceof NormalAnnotationExpr) {
                    NormalAnnotationExpr ann=(NormalAnnotationExpr)n;
                    if(ann.getName().getIdentifier().equals(ApiModelProperty.class.getSimpleName())) {
                        apiModelProperty=ann;
                    }
                    if(ann.getName().getIdentifier().equals(EnumFor.class.getSimpleName())) {
                        enumForAnn=ann;
                    }
                } else if(n instanceof VariableDeclarator) {
                    // 获得属性名称
                    VariableDeclarator var=(VariableDeclarator)n;
                    name=var.getName().getIdentifier();
                }
            }

            if(name==null) continue;


            Field clsField = null;
            try {
                clsField = getJavaClass().getDeclaredField(name);
            } catch (Exception e) {}


            if(apiModelProperty!=null) {
                SwaggerAnnotationApiModelProperty annotationApiModelProperty  = SwaggerAnnotationApiModelProperty.fromSource(apiModelProperty, this);
                annotationApiModelProperty.setField(clsField);
                if(StringUtil.isBlank(annotationApiModelProperty.getName())) {
                    annotationApiModelProperty.setName(name);
                }
                modelAnnotations.addApiModelProperty(annotationApiModelProperty);
            }

            if(enumForAnn!=null) {
                SwaggerAnnotationEnumFor swaggerAnnotationEnumFor  = SwaggerAnnotationEnumFor.fromSource(enumForAnn, this);
                swaggerAnnotationEnumFor.setField(clsField);
                modelAnnotations.addEnumFor(swaggerAnnotationEnumFor);
            }

        }




        SWAGGER_ANNOTATION_UNIT_CACHE.put(this.getJavaClass().getName(),modelAnnotations);
        return modelAnnotations;
    }










}
