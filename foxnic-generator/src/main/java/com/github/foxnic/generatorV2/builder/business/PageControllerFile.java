package com.github.foxnic.generatorV2.builder.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.generatorV2.config.MduCtx;

public class PageControllerFile extends TemplateJavaFile {

	public PageControllerFile(MduCtx context,MavenProject project, String packageName, String simpleName) {
		super(context,project, packageName, simpleName, "templates/PageController.java.vm");
	}
	
	@Override
	protected void buildBody() {
		 this.addImport(Controller.class);
		 this.addImport(RequestMapping.class);
		 this.addImport(Autowired.class);
		 this.addImport(Model.class);
		 this.addImport(this.context.getSettings().getSuperController());
	}
	
	

}
