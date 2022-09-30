package com.github.foxnic.commons.compiler.source;

import java.io.File;

public class ModelCompilationUnit extends  JavaCompilationUnit {

    public ModelCompilationUnit(File javaFile, boolean init) {
        super(javaFile,init);
    }

    public ModelCompilationUnit(Class javaClass, boolean init) {
        super(javaClass,init);
    }
}
