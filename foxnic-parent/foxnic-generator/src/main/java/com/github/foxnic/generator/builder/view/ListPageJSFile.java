package com.github.foxnic.generator.builder.view;

import com.github.foxnic.generator.config.ModuleContext;

public class ListPageJSFile extends TemplateViewFile {

 
	public ListPageJSFile(ModuleContext context) {
		super(context,context.getSettings().getListJSTemplatePath());
	}

 
	@Override
	public void save() {

		applyCommonVars4List(this);
		super.save();
		
	}
 
	@Override
	protected String getFileName() {
		return beanNameUtil.depart(this.context.getPoClassFile().getSimpleName()).toLowerCase()+"_list.js";
	}

//	@Override
//	public void buildAndUpdate() {
//		
//		String moduleBaseSubPath= ctx.getUIPathPrefix();
//		String mdu=ctx.getUIModuleFolderName();
//		String temp=StringUtil.joinUrl(moduleBaseSubPath,mdu,mdu+"_list.js");
// 
//		File dir=FileUtil.resolveByPath(ctx.getServiceProject().getMainResourceDir(), temp);
//		
//		this.buildAndUpdate(dir);
//	}
 

}
