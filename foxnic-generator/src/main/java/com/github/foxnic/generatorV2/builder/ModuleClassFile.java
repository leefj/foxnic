package com.github.foxnic.generatorV2.builder;

import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.generatorV2.config.MduCtx;

public abstract class ModuleClassFile extends JavaClassFile {
	
	protected MduCtx context=null;
	
	public ModuleClassFile(MduCtx context, MavenProject project, String packageName, String simpleName) {
		super(project, packageName, simpleName);
		this.context=context;
	}

}
