package com.github.foxnic.generator.builder.model;

import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.generator.config.ModuleContext;

public abstract class ModelClassFile extends JavaClassFile {
	
	protected ModuleContext context=null;
	
	public ModelClassFile(ModuleContext context, MavenProject project, String packageName, String simpleName) {
		super(project, packageName, simpleName);
		this.context=context;
	}

	public ModuleContext getContext() {
		return context;
	}

}
