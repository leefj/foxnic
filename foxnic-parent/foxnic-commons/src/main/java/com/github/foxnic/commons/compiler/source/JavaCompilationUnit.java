package com.github.foxnic.commons.compiler.source;

import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

import java.io.File;
import java.util.List;

public class JavaCompilationUnit {

    private static  JavaParser  javaParser;

    private CompilationUnit compilationUnit;

    private File location;

    private boolean valid = false;

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public static JavaCompilationUnit get(Class clazz) {
        return new JavaCompilationUnit(clazz);
    }

    public  JavaCompilationUnit(Class clazz) {
        MavenProject mp=new MavenProject(clazz);
        if(!mp.hasPomFile() && !mp.hasMainSourceDir()) {
            throw new IllegalArgumentException("project error");
        };
        File file= mp.getSourceFile(clazz);
        if(!file.exists()) {
            throw new IllegalArgumentException("class file not exists");
        }
        init(file);
    }

    public JavaCompilationUnit(File javaFile) {
        init(javaFile);
    }

    public void init(File javaFile) {
        this.location=javaFile;
        if(javaParser==null) {
            javaParser=new JavaParser();
        }
        try {
            ParseResult<CompilationUnit> result = javaParser.parse(javaFile);
            compilationUnit = result.getResult().get();
            compilationUnit.setStorage(this.location.toPath());
            valid=true;
        } catch (Exception e) {
            new RuntimeException(e);
        }
    }

    public boolean isValid() {
        return valid;
    }

    public  <T extends Node> List<T> find(Class<T> nodeType) {
        return compilationUnit.findAll(nodeType);
    }

    public void save() {
        FileUtil.writeText(location,compilationUnit.toString());
    }



}
