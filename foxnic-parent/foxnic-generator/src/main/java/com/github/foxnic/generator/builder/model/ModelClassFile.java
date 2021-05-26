package com.github.foxnic.generator.builder.model;

import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.generator.config.MduCtx;

public abstract class ModelClassFile extends JavaClassFile {
	
	protected MduCtx context=null;
	
	public ModelClassFile(MduCtx context, MavenProject project, String packageName, String simpleName) {
		super(project, packageName, simpleName);
		this.context=context;
	}

	public MduCtx getContext() {
		return context;
	}

}
