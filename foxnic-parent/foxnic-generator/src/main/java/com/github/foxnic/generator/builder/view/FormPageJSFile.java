package com.github.foxnic.generator.builder.view;

import com.github.foxnic.generator.config.ModuleContext;

public class FormPageJSFile extends TemplateViewFile {

 
	public FormPageJSFile(ModuleContext context) {
		super(context,context.getSettings().getFormJSTemplatePath());
	}

 
	@Override
	public void save() {
		applyCommonVars4Form(this);
		super.save();
	}

	@Override
	protected String getFileName() {
		return beanNameUtil.depart(this.context.getPoClassFile().getSimpleName()).toLowerCase()+"_form.js";
	}
	
}
