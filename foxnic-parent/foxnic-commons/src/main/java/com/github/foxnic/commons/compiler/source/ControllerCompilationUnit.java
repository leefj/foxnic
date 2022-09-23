package com.github.foxnic.commons.compiler.source;

import java.io.File;

public class ControllerCompilationUnit extends  JavaCompilationUnit {

    public ControllerCompilationUnit(File javaFile) {
        super(javaFile);
    }

    public ControllerCompilationUnit(Class javaClass) {
        super(javaClass);
    }
}
