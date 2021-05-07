package com.github.foxnic.generator.clazz;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.clazz.model.FormFieldInfo;

public class FormPageJSBuilder extends TemplateFileBuilder {

 
	public FormPageJSBuilder(Context cfg) {
		super(cfg);
 
	}

 
	@Override
	protected void build() {
		
		String temp=this.ctx.getFormJSTemplate();
		
		template = engine.getTemplate(temp);
		 
		// 字符流模式输出到 StringWriter
		//StringWriter sw = new StringWriter();
		
		CodeBuilder code=new CodeBuilder();
		code.ln("/**");
		code.ln(" * "+ctx.getTopic()+" 表单 JS 脚本");
		code.ln(" * @author "+ctx.getAuthor());
		code.ln(" * @since "+DateUtil.getFormattedTime(false));
		code.ln(" */");
		this.putVar("authorAndTime", code);
		
		this.putVar("topic", ctx.getTopic());
		this.putVar("moduleURL", "/"+ctx.getControllerApiPrefix()+"/"+ctx.getApiContextPart()+"/");
		this.putVar("pkVarName", this.ctx.getTableMeta().getPKColumns().get(0).getColumnVarName());
//		//所有字段
		List<DBColumnMeta> columns=this.ctx.getTableMeta().getColumns();
		List<FormFieldInfo> fields=new ArrayList<>();
		for (DBColumnMeta cm : columns) {
//			if(ctx.isDBTreatyFiled(cm)  && !ctx.getDBTreaty().getCreateTimeField().equals(cm.getColumn())) continue;
//			
			boolean displayImageUploadField=false;
			if(ctx.isImageIdField(cm)) {
				displayImageUploadField=true;
			}  
			
			
			FormFieldInfo field=new FormFieldInfo();
			field.setVarName(cm.getColumnVarName());
			field.setLabel(cm.getLabel());
			field.setImageField(displayImageUploadField);
			field.setLogicField(ctx.getLogicField(cm));
			fields.add(field);
//			String templet="";
//			if(cm.getDBDataType()==DBDataType.DATE) {
//				templet=" , templet: function (d) { return util.toDateString(d."+cm.getColumnVarName()+"); }";
//			}
//			
//			searchOptions.add(new String[] {cm.getColumnVarName(),cm.getLabel(),templet});
		}
		this.putVar("fields", fields);
 
		String idPrefix=ctx.getUIModuleFolderName();
		this.putVar("submitButtonId", idPrefix+"-form-submit");
		this.putVar("formDataKey", ctx.getTableName().toLowerCase().replace('_', '-')+"-form-data");
		this.putVar("formId", idPrefix+"-form");
		
//		this.putVar("operationTemplateId", idPrefix+"-table-operation");
//		this.putVar("searchFieldId", idPrefix+"-search-field");
//		this.putVar("searchValueInputId", idPrefix+"-search-value");
//		this.putVar("searchButtonId", idPrefix+"-btn-search");
//		this.putVar("addButtonId", idPrefix+"-btn-add");
		//
//		this.putVar("formPath", "/pages/product/label/"+idPrefix+"_form.html");
//		this.putVar("jsPath", "/pages/product/label/"+idPrefix+"_form.js");
		
	}

	@Override
	public void buildAndUpdate() {
		
		String moduleBaseSubPath= ctx.getUIPathPrefix();
		String mdu=ctx.getUIModuleFolderName();
		String temp=StringUtil.joinUrl(moduleBaseSubPath,mdu,mdu+"_form.js");
 
		File dir=FileUtil.resolveByPath(ctx.getServiceProject().getMainResourceDir(), temp);
		
		this.buildAndUpdate(dir);
	}
 

}