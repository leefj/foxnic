package com.github.foxnic.springboot.api.swagger.source;

import com.github.foxnic.commons.compiler.source.JavaCompilationUnit;
import com.github.foxnic.commons.log.Logger;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class SwaggerAnnotation {

    private  transient AnnotationExpr source;

    public AnnotationExpr getSource() {
        return source;
    }

    public void setSource(AnnotationExpr source) {
        this.source = source;
    }



    public static Map<String,Object> readAnnotation(AnnotationExpr ann, JavaCompilationUnit compilationUnit) {
        Map<String,Object> values=new HashMap<>();
        for (Node node : ann.getChildNodes()) {
            if (!(node instanceof MemberValuePair)) {
                continue;
            }
            MemberValuePair mvp = (MemberValuePair) node;
            String name = mvp.getName().getIdentifier();
            Object value = null;
            if (mvp.getValue() instanceof FieldAccessExpr) {
                FieldAccessExpr expr = (FieldAccessExpr) mvp.getValue();
                value = compilationUnit.readField(expr);
            } else if (mvp.getValue() instanceof StringLiteralExpr) {
                StringLiteralExpr expr = (StringLiteralExpr) mvp.getValue();
                value = expr.getValue();
            } else if (mvp.getValue() instanceof IntegerLiteralExpr) {
                IntegerLiteralExpr expr = (IntegerLiteralExpr) mvp.getValue();
                value = expr.getValue();
            } else if (mvp.getValue() instanceof BooleanLiteralExpr) {
                BooleanLiteralExpr expr = (BooleanLiteralExpr) mvp.getValue();
                value = expr.getValue();
            } else if (mvp.getValue() instanceof ArrayInitializerExpr) {
                ArrayInitializerExpr expr = (ArrayInitializerExpr) mvp.getValue();
                Object[] array=new Object[expr.getValues().size()];
                int i=0;
                for (Expression exprValue : expr.getValues()) {
                    if(exprValue instanceof FieldAccessExpr) {
                        FieldAccessExpr fieldAccessExpr=(FieldAccessExpr) exprValue;
                        array[i]=compilationUnit.readField(fieldAccessExpr);
                    } else if (exprValue instanceof StringLiteralExpr) {
                        StringLiteralExpr stringLiteralExpr=(StringLiteralExpr) exprValue;
                        array[i]=stringLiteralExpr.getValue();
                    } else if (exprValue instanceof NormalAnnotationExpr) {
                        NormalAnnotationExpr annotationExpr=(NormalAnnotationExpr) exprValue;
                        array[i]=annotationExpr;
                    }else {
                        throw new RuntimeException("不支持的值类型");
                    }
                    i++;
                }
                value=array;
            } else if (mvp.getValue() instanceof ClassExpr) {
                ClassExpr expr = (ClassExpr) mvp.getValue();
                value = expr.getType().asClassOrInterfaceType();
                if (value != null) {
                    value = compilationUnit.getImportedClass(value.toString());
                }
            } else if (mvp.getValue() instanceof NormalAnnotationExpr) {
                // 暂不支持
            } else {
                throw new RuntimeException("不支持的值类型");
            }
            values.put(name,value);
        }
        return values;
    }

}
