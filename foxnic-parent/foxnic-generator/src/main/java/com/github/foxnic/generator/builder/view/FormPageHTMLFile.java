package com.github.foxnic.generator.builder.view;

import com.github.foxnic.generator.config.ModuleContext;

public class FormPageHTMLFile extends TemplateViewFile {
 
	public FormPageHTMLFile(ModuleContext context) {
		super(context,context.getSettings().getFormHTMLTemplatePath());
	}

	@Override
	public void save() {

		
		applyCommonVars4Form(this);
		super.save();
		
	}

 
	@Override
	protected String getFileName() {
		return beanNameUtil.depart(this.context.getPoClassFile().getSimpleName()).toLowerCase()+"_form.html";
	}

//	@Override
//	public void buildAndUpdate() {
//		 
//		//String temp="public/pages/product/label/label_index.html"; //this.ctx.getIndexHTMLSubPath();
// 
//		
//		String moduleBaseSubPath= ctx.getUIPathPrefix();
//		String mdu=ctx.getUIModuleFolderName();
//		String temp=StringUtil.joinUrl(moduleBaseSubPath,mdu,mdu+"_form.html");
// 
//		File dir=FileUtil.resolveByPath(ctx.getServiceProject().getMainResourceDir(), temp);
//		
//		this.buildAndUpdate(dir);
//	}
 
}
