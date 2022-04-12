package com.github.foxnic.generator.util;

import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

import java.io.File;
import java.util.List;

public class JavaCPUnit {

    private static  JavaParser  javaParser;

    private CompilationUnit compilationUnit;

    private File location;

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public static JavaCPUnit get(Class clazz) {
        MavenProject mp=new MavenProject(clazz);
        File file= mp.getSourceFile(clazz);
        if(!file.exists()) {
            throw new IllegalArgumentException(file.getAbsolutePath() +" 不存在");
        }
        return new JavaCPUnit(file);
    }

    public JavaCPUnit(File javaFile) {
        this.location=javaFile;
        if(javaParser==null) {
            javaParser=new JavaParser();
        }
        try {
            ParseResult<CompilationUnit> result = javaParser.parse(javaFile);
            compilationUnit = result.getResult().get();
            compilationUnit.setStorage(this.location.toPath());
        } catch (Exception e) {
            new RuntimeException(e);
        }

    }

    public  <T extends Node> List<T> find(Class<T> nodeType) {
        return compilationUnit.findAll(nodeType);
    }

    public void save() {
        FileUtil.writeText(location,compilationUnit.toString());
    }




}
