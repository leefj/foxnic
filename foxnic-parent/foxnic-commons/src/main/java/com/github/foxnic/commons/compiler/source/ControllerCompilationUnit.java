package com.github.foxnic.commons.compiler.source;

import java.io.File;

public class ControllerCompilationUnit extends  JavaCompilationUnit {

    public ControllerCompilationUnit(File javaFile,boolean init) {
        super(javaFile,init);
    }

    public ControllerCompilationUnit(Class javaClass,boolean init) {
        super(javaClass,init);
    }
}
