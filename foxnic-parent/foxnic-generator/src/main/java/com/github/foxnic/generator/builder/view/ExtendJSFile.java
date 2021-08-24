package com.github.foxnic.generator.builder.view;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.generator.builder.view.config.FormConfig;
import com.github.foxnic.generator.builder.view.config.FormGroupConfig;
import com.github.foxnic.generator.config.ModuleContext;

import java.util.List;

public class ExtendJSFile extends TemplateViewFile {


	public ExtendJSFile(ModuleContext context) {
		super(context,context.getSettings().getExtJSTemplatePath());
	}

	private void applyVars() {

		CodeBuilder code=new CodeBuilder();
		code.ln("/**");
		code.ln(" * "+this.context.getTopic()+" 列表页 JS 脚本");
		code.ln(" * @author "+this.context.getSettings().getAuthor());
		code.ln(" * @since "+ DateUtil.getFormattedTime(false));
		code.ln(" */");
		this.putVar("authorAndTime", code);

		this.putVar("toolButtons",this.context.getListConfig().getToolButtons());
		this.putVar("opColumnButtons",this.context.getListConfig().getOpColumnButtons());

		List iframes=new JSONArray();
		List tabs=new JSONArray();
		FormConfig fmcfg=this.context.getFormConfig();
		List<FormGroupConfig> groups=fmcfg.getGroups();
		for (FormGroupConfig group : groups) {
			JSONObject gcfg=new JSONObject();
			gcfg.put("type",group.getType());
			gcfg.put("title",group.getTitle());
			if(group.getType().equals("iframe")){
				gcfg.put("title",group.getTitle());
				gcfg.put("iframeLoadJsFunctionName",group.getIframeLoadJsFunctionName());
				iframes.add(gcfg);
				continue;
			} else if(group.getType().equals("tab")){
				tabs.addAll(group.getTabs());
			}
		}
		this.putVar("iframes", iframes);
		this.putVar("tabs", tabs);
		this.putVar("fields", this.context.getFields());
	}
 
	@Override
	public void save() {
		applyVars();
		super.save();
	}

	@Override
	protected String getFileName() {
		return beanNameUtil.depart(this.context.getPoClassFile().getSimpleName()).toLowerCase()+"_ext.js";
	}
	
}
