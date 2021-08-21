package com.github.foxnic.generator.builder.view;

import com.github.foxnic.generator.config.ModuleContext;

import java.io.File;

public class ExtJSFile extends TemplateViewFile {


	public ExtJSFile(ModuleContext context) {
		super(context,context.getSettings().getExtJSTemplatePath());
	}

	private void applyVars() {
		this.putVar("toolButtons",this.context.getListConfig().getToolButtons());
		this.putVar("opColumnButtons",this.context.getListConfig().getOpColumnButtons());
	}
 
	@Override
	public void save() {
		applyVars();
		File file=this.getSourceFile();
		if(file.exists()) return;
		super.save();
	}

	@Override
	protected String getFileName() {
		return beanNameUtil.depart(this.context.getPoClassFile().getSimpleName()).toLowerCase()+"_ext.js";
	}
	
}
