package com.github.foxnic.generatorV2.builder.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.generatorV2.config.MduCtx;

public class PageControllerFile extends TemplateJavaFile {

	public PageControllerFile(MduCtx context,MavenProject project, String packageName, String simpleName) {
		super(context,project, packageName, simpleName, "templates/PageController.java.vm","模版页面控制器");
	}
	
	@Override
	protected void buildBody() {
		 this.addImport(Controller.class);
		 this.addImport(RequestMapping.class);
		 this.addImport(Autowired.class);
		 this.addImport(Model.class);
 
		 this.putVar("beanName",beanNameUtil.getClassName(this.getContext().getTableMeta().getTableName())+"PageController");
		 
 
		this.putVar("serviceSimpleName", this.context.getServiceInterfaceFile().getSimpleName());
		this.putVar("serviceVarName", this.context.getServiceInterfaceFile().getVar());	
		
		this.addImport(this.context.getServiceInterfaceFile().getFullName());
		
		String prefix=this.getContext().getUriPrefix4Ui();
 
		this.putVar("uriPrefix", prefix);
		
		this.putVar("listPageName", getListPageName());
		this.putVar("formPageName", getFormPageName());
		
		
		
	}
	
	public String getListPageName() {
		return beanNameUtil.depart( this.getContext().getPoClassFile().getSimpleName() ).toLowerCase()+"_list";
	}
	
	public String getFormPageName() {
		return beanNameUtil.depart( this.getContext().getPoClassFile().getSimpleName() ).toLowerCase()+"_form";
	}
	
	
	
	

}
