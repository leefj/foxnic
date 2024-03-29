package com.github.foxnic.generator.util;

import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.generator.builder.view.config.FillByUnit;
import com.github.foxnic.generator.builder.view.config.FillWithUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigCollector {

    /**
     * 搜集控制器中的 fill with 语句
     * */
    public static FillWithUnit collectControllerFillWith(StackTraceElement el,String fn) {
        FillWithUnit unit = null;
        Class cls= ReflectUtil.forName(el.getClassName());
        JavaCPUnit finder= JavaCPUnit.get(cls);
        List<MethodCallExpr> callExprs=finder.find(MethodCallExpr.class);
        for (MethodCallExpr callExpr : callExprs) {
            if (!fn.equals(callExpr.getName().asString())) continue;
            unit=new FillWithUnit();
            String code=null;
            MethodCallExpr expr=callExpr;
            while (true) {
                code = expr.toString();
                if(code.startsWith("{") && code.endsWith("}")) {
                    break;
                }
                if(expr.getParentNode()==null) break;
                if(!(expr.getParentNode().get() instanceof MethodCallExpr)) {
                    break;
                }
                expr=(MethodCallExpr) expr.getParentNode().get();
                if(expr==null) break;
            }
            callExprs=expr.findAll(MethodCallExpr.class);
            for (MethodCallExpr withExpr : callExprs) {
                if (!"with".equals(withExpr.getName().asString())) continue;
                NodeList<Expression> args= withExpr.getArguments();
                for (Expression arg : args) {
                    String impType=findImport(finder,arg.asFieldAccessExpr().toString());
                    unit.addImport(impType);
                }
            }
            code=code.substring(code.indexOf(fn)+fn.length()+2);
            code=code.replaceAll (".with" ,"\n\t\t\t.with");
            code=code.trim();
            unit.setCode(code);
        }
        return unit;
    }

    /**
     * 搜集 fillBy 和 fillWith 代码
     * */
    public static List<FillByUnit> collectFills(StackTraceElement el) {
        List<FillByUnit> bys=gatherFillsInternal(el,"fillBy");
        List<FillByUnit> withs=gatherFillsInternal(el,"fillWith");
        bys.addAll(withs);
        return  bys;
    }

    private static  List<FillByUnit> gatherFillsInternal(StackTraceElement el,String fn) {
        List<FillByUnit> units=new ArrayList<>();
        Class cls= ReflectUtil.forName(el.getClassName());
        JavaCPUnit finder= JavaCPUnit.get(cls);
        List<MethodCallExpr> callExprs=finder.find(MethodCallExpr.class);
        Set<String> keys=new HashSet<>();
        for (MethodCallExpr callExpr : callExprs) {
            if(!fn.equals(callExpr.getName().asString())) continue;
            String code=callExpr.getParentNode().get().toString();
            if(!code.contains("."+fn+"(")) continue;
            FillByUnit unit=new FillByUnit();
            NodeList<Expression> args= callExpr.getArguments();
            for (Expression arg : args) {
                String argName=null;
                if(arg instanceof StringLiteralExpr){
                    argName=arg.asStringLiteralExpr().toString();
                    unit.add(argName,null);
                } else if(arg instanceof FieldAccessExpr){
                    argName=arg.asFieldAccessExpr().toString();
                    String impType=findImport(finder,arg.asFieldAccessExpr().toString());
                    unit.add(argName,impType);
                } else {
                    throw new IllegalArgumentException("不支持 "+arg.getClass().getName()+" 类型的解析");
                }
            }
            if(fn.equals("fillWith")) {
                unit.add(null,null);
            }
            //避免重复
            if(keys.contains(unit.getKey())) continue;
            //
            keys.add(unit.getKey());
            units.add(unit);
        }
        return units;
    }

    private static String findImport(JavaCPUnit finder, String p) {
        p=p.split("\\.")[0];
        List<ImportDeclaration> imps=finder.find(ImportDeclaration.class);
        for (ImportDeclaration imp : imps) {
            String impName=imp.getName().asString();
            impName=impName.trim();
            if(impName.endsWith("."+p)) {
                return impName;
            }
        }

        for (ImportDeclaration imp : imps) {
            String impName=imp.getName().asString();
            if (impName.equals(".meta")) {
                System.out.println();
            }
            String cn=impName+"."+p;
            Class cls=ReflectUtil.forName(cn);
            if(cls!=null) {
                return cls.getName();
            }
        }

        return null;
    }

}
