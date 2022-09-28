package com.github.foxnic.commons.compiler.source;

import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaCompilationUnit {

    private static String[] BASIC_PACKAGES={ "java.lang","java.io","java.net","java.awt" };

    private static  JavaParser  javaParser;

    private CompilationUnit compilationUnit;

    private File javaFile;

    private boolean valid = false;

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public static JavaCompilationUnit get(Class clazz,boolean init) {
        return new JavaCompilationUnit(clazz,init);
    }

    private Class javaClass=null;

    private Map<String,Class> importedClasses=new HashMap<>();

    public  JavaCompilationUnit(Class clazz,boolean init) {
        MavenProject mp=new MavenProject(clazz);
        this.javaClass=clazz;
        if(!mp.hasPomFile() && !mp.hasMainSourceDir()) {
            throw new IllegalArgumentException("project error");
        };
        this.javaFile = mp.getSourceFile(clazz);
        if(!this.javaFile.exists()) {
            throw new IllegalArgumentException("class file not exists");
        }
        if(init) {
            init();
        }
    }

    public JavaCompilationUnit(File javaFile,boolean init) {
        if(init) {
            init();
        }
    }

    public void init() {
        if(javaParser==null) {
            javaParser=new JavaParser();
        }
        try {
            ParseResult<CompilationUnit> result = javaParser.parse(javaFile);
            compilationUnit = result.getResult().get();
            compilationUnit.setStorage(this.javaFile.toPath());
            this.initImports();
            valid=true;
        } catch (Exception e) {
            new RuntimeException(e);
        }
    }

    private void initImports() {
        List<ImportDeclaration> imports=this.find(ImportDeclaration.class);
        for (ImportDeclaration imp : imports) {
            Name name= (Name) imp.getChildNodes().get(0);
            String className=name.toString();
            String[] parts=className.split("\\.");
            String simpleName=parts[parts.length-1];
            Class clazz= ReflectUtil.forName(className,true);
            importedClasses.put(simpleName,clazz);
        }
    }

    public boolean isValid() {
        return valid;
    }

    public  <T extends Node> List<T> find(Class<T> nodeType) {
        return compilationUnit.findAll(nodeType);
    }

    public void save() {
        FileUtil.writeText(javaFile,compilationUnit.toString());
    }

    public Class getJavaClass() {
        return javaClass;
    }

    public File getJavaFile() {
        return javaFile;
    }

    public Class getImportedClass(String simpleName) {
        Class cls=importedClasses.get(simpleName);
        if(cls==null){
            for(String basicPackage : BASIC_PACKAGES) {
                cls=ReflectUtil.forName(basicPackage+"."+simpleName,true);
                if(cls!=null) {
                    break;
                }
            }
        }
        return cls;
    }

    /**
     * 查找方法
     * */
    public MethodDeclaration findMethod(Method method) {

        List<MethodDeclaration> matchedMethods=new ArrayList<>();
        List<MethodDeclaration> methods=this.find(MethodDeclaration.class);
        for (MethodDeclaration m : methods) {
            if(!m.getName().getIdentifier().equals(method.getName())) continue;
            ClassOrInterfaceType returnType=(ClassOrInterfaceType)m.getType();
            if(!returnType.getName().getIdentifier().equals(method.getReturnType().getSimpleName())) continue;
            if(m.getParameters().size()!=method.getParameterCount()) continue;
            boolean isParameterMatch=true;
            for (int i = 0; i < method.getParameters().length ; i++) {
                Parameter parameter = method.getParameters()[i];
                com.github.javaparser.ast.body.Parameter param=m.getParameters().get(i);
                ClassOrInterfaceType paramType=(ClassOrInterfaceType)param.getType();
                if(!parameter.getType().getSimpleName().equals(paramType.getName().getIdentifier())) {
                    isParameterMatch=false;
                    break;
                }
            }
            if(!isParameterMatch) {
                continue;
            }
            matchedMethods.add(m);
        }

        if(matchedMethods.size()==0) {
            return null;
        } else if(matchedMethods.size()==1) {
            return matchedMethods.get(0);
        } else {
            throw new RuntimeException(method.toGenericString()+" 匹配到多个方法，要求1个");
        }
    }
}
