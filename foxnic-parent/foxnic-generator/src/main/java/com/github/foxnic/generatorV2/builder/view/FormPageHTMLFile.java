package com.github.foxnic.generatorV2.builder.view;

import java.io.File;

import com.github.foxnic.generatorV2.config.MduCtx;

public class FormPageHTMLFile extends TemplateViewFile {
 
	public FormPageHTMLFile(MduCtx context) {
		super(context,context.getSettings().getFormHTMLTemplatePath());
	}

	@Override
	public void save() {
		
//		String temp=this.ctx.getFormHTMLTemplate();
//		
//		template = engine.getTemplate(temp);
//		 
//		TreeConfig tree=ctx.getTreeConfig();
//		DefaultNameConvertor nc=new DefaultNameConvertor();
//		
//		CodeBuilder code=new CodeBuilder();
//		code.ln(1,ctx.getTopic()+" 表单 HTML 页面");
//		code.ln(1,"@author "+ctx.getAuthor());
//		code.ln(1,"@since "+DateUtil.getFormattedTime(false));
//		this.putVar("authorAndTime", code);
//		
//		this.putVar("topic", ctx.getTopic());
//		
//		
//		List<DBColumnMeta> pks=this.ctx.getTableMeta().getPKColumns();
//		List<String> hiddenFields=new ArrayList<>();
//		for (DBColumnMeta pk : pks) {
//			String f=pk.getColumnVarName();
//			hiddenFields.add( f );
//		}
//		
//		if(tree!=null) {
//			hiddenFields.add(nc.getPropertyName(tree.getParentIdField().name()));
//		}
//		
//		
//		
//		
//		
//		
//		
//		List<DBColumnMeta> columns=this.ctx.getTableMeta().getColumns();
//		List<FormFieldInfo> fields=new ArrayList<>();
//		for (DBColumnMeta cm : columns) {
//			if(ctx.isDBTreatyFiled(cm)) continue;
//			if(cm.isPK()) continue;
//			if(tree!=null  &&  tree.getParentIdField().name().equalsIgnoreCase(cm.getColumn())) continue;
//			
//			String layVerify="";
//			String required="";
//			
//			if(!cm.isNullable()) {
//				required="required";
//			}
//			
//			if(!StringUtil.isBlank(required)) {
//				layVerify="lay-verify='required'";
//			}
//			
//			
//			String maxLen="";
//			if(cm.getDBDataType()==DBDataType.STRING && cm.getCharLength()>0) {
//				maxLen="maxlength='"+cm.getCharLength()+"'";
//			}
//			
//			boolean displayImageUploadField=false;
//			if(ctx.isImageIdField(cm)) {
//				hiddenFields.add(cm.getColumnVarName());
//				displayImageUploadField=true;
//			}  
//			
//			
//			FormFieldInfo field=new FormFieldInfo();
//			field.setVarName(cm.getColumnVarName());
//			field.setLabel(cm.getLabel());
//			field.setLayVerifyHtml(layVerify);
//			field.setMaxLenHtml(maxLen);
//			field.setRequiredHtml(required);
//			field.setImageField(displayImageUploadField);
//			field.setLogicField(ctx.getLogicField(cm));
//			field.setMulitiLine(ctx.isMulitiLineField(cm));
//			
//			fields.add(field);
//		}
//		this.putVar("fields", fields);
// 
//		String idPrefix=ctx.getUIModuleFolderName();
//		this.putVar("tableId", idPrefix+"-table");
//		this.putVar("formId", idPrefix+"-form");
//		this.putVar("submitButtonId", idPrefix+"-form-submit");
//		this.putVar("hiddenFields", hiddenFields);
//		
////		this.putVar("operationTemplateId", idPrefix+"-table-operation");
////		this.putVar("searchFieldId", idPrefix+"-search-field");
////		this.putVar("searchValueInputId", idPrefix+"-search-value");
////		this.putVar("searchButtonId", idPrefix+"-btn-search");
////		this.putVar("addButtonId", idPrefix+"-btn-add");
////		
//		
//		String moduleBaseSubPath= ctx.getUIPathPrefix();
//		String mdu=ctx.getUIModuleFolderName();
//		String jsPath=StringUtil.joinUrl(moduleBaseSubPath,mdu,mdu+"_form.js");
//		jsPath=jsPath.substring(jsPath.indexOf('/'));
//		this.putVar("jsPath", jsPath);
		
		applyCommonVars4Form(this);
		super.save();
		
	}

	@Override
	protected File getSourceFile() {
		// TODO Auto-generated method stub
		return null;
	}
	
 

	@Override
	protected String getFileName() {
		// TODO Auto-generated method stub
		return null;
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
