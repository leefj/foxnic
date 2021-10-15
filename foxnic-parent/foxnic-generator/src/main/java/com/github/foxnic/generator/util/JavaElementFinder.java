package com.github.foxnic.generator.util;

import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

import java.io.File;
import java.util.List;

public class JavaElementFinder {

    private static  JavaParser  javaParser;

    private CompilationUnit compilationUnit;

    public static JavaElementFinder get(Class clazz) {
        MavenProject mp=new MavenProject(clazz);
        File file= mp.getSourceFile(clazz);
        if(!file.exists()) {
            throw new IllegalArgumentException(file.getAbsolutePath() +" 不存在");
        }
        return new JavaElementFinder(file);
    }

    public JavaElementFinder(File javaFile) {
        if(javaParser==null) {
            javaParser=new JavaParser();
        }
        try {
            ParseResult<CompilationUnit> result = javaParser.parse(javaFile);
            compilationUnit = result.getResult().get();
        } catch (Exception e) {
            new RuntimeException(e);
        }
    }

    public  <T extends Node> List<T> find(Class<T> nodeType) {
        return compilationUnit.findAll(nodeType);
    }




}
