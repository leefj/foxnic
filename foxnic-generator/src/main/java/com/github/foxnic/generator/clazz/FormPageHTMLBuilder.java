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
import com.github.foxnic.generator.ModuleConfig.TreeConfig;
import com.github.foxnic.generator.clazz.model.FormFieldInfo;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;
import com.github.foxnic.sql.meta.DBDataType;

public class FormPageHTMLBuilder extends TemplateFileBuilder {
 
	public FormPageHTMLBuilder(Context cfg) {
		super(cfg);
 
	}

	@Override
	protected void build() {
		
		String temp=this.ctx.getFormHTMLTemplate();
		
		template = engine.getTemplate(temp);
		 
		TreeConfig tree=ctx.getTreeConfig();
		DefaultNameConvertor nc=new DefaultNameConvertor();
		
		CodeBuilder code=new CodeBuilder();
		code.ln(1,ctx.getTopic()+" 表单 HTML 页面");
		code.ln(1,"@author "+ctx.getAuthor());
		code.ln(1,"@since "+DateUtil.getFormattedTime(false));
		this.putVar("authorAndTime", code);
		
		this.putVar("topic", ctx.getTopic());
		
		
		List<DBColumnMeta> pks=this.ctx.getTableMeta().getPKColumns();
		List<String> hiddenFields=new ArrayList<>();
		for (DBColumnMeta pk : pks) {
			String f=pk.getColumnVarName();
			hiddenFields.add( f );
		}
		
		if(tree!=null) {
			hiddenFields.add(nc.getPropertyName(tree.getParentIdField()));
		}
		
		
		
		
		
		
		
		List<DBColumnMeta> columns=this.ctx.getTableMeta().getColumns();
		List<FormFieldInfo> fields=new ArrayList<>();
		for (DBColumnMeta cm : columns) {
			if(ctx.isDBTreatyFiled(cm)) continue;
			if(cm.isPK()) continue;
			if(tree!=null  &&  tree.getParentIdField().equalsIgnoreCase(cm.getColumn())) continue;
			
			String layVerify="";
			String required="";
			
			if(!cm.isNullable()) {
				required="required";
			}
			
			if(!StringUtil.isBlank(required)) {
				layVerify="lay-verify='required'";
			}
			
			
			String maxLen="";
			if(cm.getDBDataType()==DBDataType.STRING && cm.getCharLength()>0) {
				maxLen="maxlength='"+cm.getCharLength()+"'";
			}
			
			boolean displayImageUploadField=false;
			if(ctx.isImageIdField(cm)) {
				hiddenFields.add(cm.getColumnVarName());
				displayImageUploadField=true;
			}  
			
			
			FormFieldInfo field=new FormFieldInfo();
			field.setVarName(cm.getColumnVarName());
			field.setLabel(cm.getLabel());
			field.setLayVerifyHtml(layVerify);
			field.setMaxLenHtml(maxLen);
			field.setRequiredHtml(required);
			field.setImageField(displayImageUploadField);
			field.setLogicField(ctx.getLogicField(cm));
			field.setMulitiLine(ctx.isMulitiLineField(cm));
			
			fields.add(field);
		}
		this.putVar("fields", fields);
 
		String idPrefix=ctx.getUIModuleFolderName();
		this.putVar("tableId", idPrefix+"-table");
		this.putVar("formId", idPrefix+"-form");
		this.putVar("submitButtonId", idPrefix+"-form-submit");
		this.putVar("hiddenFields", hiddenFields);
		
//		this.putVar("operationTemplateId", idPrefix+"-table-operation");
//		this.putVar("searchFieldId", idPrefix+"-search-field");
//		this.putVar("searchValueInputId", idPrefix+"-search-value");
//		this.putVar("searchButtonId", idPrefix+"-btn-search");
//		this.putVar("addButtonId", idPrefix+"-btn-add");
//		
		
		String moduleBaseSubPath= ctx.getUIPathPrefix();
		String mdu=ctx.getUIModuleFolderName();
		String jsPath=StringUtil.joinUrl(moduleBaseSubPath,mdu,mdu+"_form.js");
		jsPath=jsPath.substring(jsPath.indexOf('/'));
		this.putVar("jsPath", jsPath);
		
	}

	@Override
	public void buildAndUpdate() {
		 
		//String temp="public/pages/product/label/label_index.html"; //this.ctx.getIndexHTMLSubPath();
 
		
		String moduleBaseSubPath= ctx.getUIPathPrefix();
		String mdu=ctx.getUIModuleFolderName();
		String temp=StringUtil.joinUrl(moduleBaseSubPath,mdu,mdu+"_form.html");
 
		File dir=FileUtil.resolveByPath(ctx.getServiceProject().getMainResourceDir(), temp);
		
		this.buildAndUpdate(dir);
	}
 
}
