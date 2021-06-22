package com.github.foxnic.generator.builder.view;

import com.github.foxnic.generator.config.ModuleContext;

public class ListPageHTMLFile extends TemplateViewFile {

 
	public ListPageHTMLFile(ModuleContext context) {
		super(context,context.getSettings().getListHTMLTemplatePath());
	}

	@Override
	public void save() {

		this.putVar("jsURI", this.context.getListPageJSFile().getFullURI());
		applyCommonVars4List(this);
		super.save();

	}
 
	@Override
	protected String getFileName() {
		return beanNameUtil.depart(this.context.getPoClassFile().getSimpleName()).toLowerCase()+"_list.html";
	}
 
}
