package com.github.foxnic.generator.builder.view;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.generator.builder.view.config.FormConfig;
import com.github.foxnic.generator.builder.view.config.FormGroupConfig;
import com.github.foxnic.generator.config.ModuleContext;

import java.io.File;
import java.util.List;

public class ExtJSFile extends TemplateViewFile {


	public ExtJSFile(ModuleContext context) {
		super(context,context.getSettings().getExtJSTemplatePath());
	}

	private void applyVars() {
		this.putVar("toolButtons",this.context.getListConfig().getToolButtons());
		this.putVar("opColumnButtons",this.context.getListConfig().getOpColumnButtons());

		List jsonArray=new JSONArray();
		FormConfig fmcfg=this.context.getFormConfig();
		List<FormGroupConfig> groups=fmcfg.getGroups();
		for (FormGroupConfig group : groups) {
			JSONObject gcfg=new JSONObject();
			gcfg.put("type",group.getType());
			gcfg.put("title",group.getTitle());
			if(group.getType().equals("iframe")){
				gcfg.put("title",group.getTitle());
				gcfg.put("iframeLoadJsFunctionName",group.getIframeLoadJsFunctionName());
				jsonArray.add(gcfg);
				continue;
			}
		}
		this.putVar("iframes", jsonArray);
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
