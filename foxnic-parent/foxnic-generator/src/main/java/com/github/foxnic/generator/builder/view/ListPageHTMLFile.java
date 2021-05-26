package com.github.foxnic.generator.builder.view;

import java.io.File;

import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.generator.config.ModuleContext;

public class ListPageHTMLFile extends TemplateViewFile {

 
	public ListPageHTMLFile(ModuleContext context) {
		super(context,context.getSettings().getListHTMLTemplatePath());
	}

	@Override
	public void save() {
		
//		String temp=this.ctx.getListHTMLTemplate();
//		
//		template = engine.getTemplate(temp);
//		 
//		TreeConfig tree=ctx.getTreeConfig();
//		
//		boolean isSinglePK=this.ctx.getTableMeta().getPKColumnCount()==1;
//		this.putVar("isSinglePK", isSinglePK);
//		this.putVar("pkVarName", this.ctx.getTableMeta().getPKColumns().get(0).getColumnVarName());
//		
//		CodeBuilder code=new CodeBuilder();
//		code.ln(1,ctx.getTopic()+" 列表 HTML 页面");
//		code.ln(1,"@author "+ctx.getAuthor());
//		code.ln(1,"@since "+DateUtil.getFormattedTime(false));
//		this.putVar("authorAndTime", code);
//		
//		this.putVar("topic", ctx.getTopic());
//		
//		List<DBColumnMeta> columns=this.ctx.getTableMeta().getColumns();
//		List<String[]> searchOptions=new ArrayList<>();
//		for (DBColumnMeta cm : columns) {
//			if(ctx.isDBTreatyFiled(cm)) continue;
//			searchOptions.add(new String[] {cm.getColumnVarName(),cm.getLabel()});
//		}
//		this.putVar("searchOptions", searchOptions);
//		
//		
//		List<ListFieldInfo> fields=new ArrayList<>();
//		for (DBColumnMeta cm : columns) {
//			if(ctx.isDBTreatyFiled(cm)  && !ctx.getDBTreaty().getCreateTimeField().equals(cm.getColumn())) continue;
//			
//			//不显示自增主键
//			if(cm.isPK() && cm.isAutoIncrease()) continue;
//			//不显示上级ID
//			if(tree!=null && tree.getParentIdField().name().equalsIgnoreCase(cm.getColumn()))  continue;
//			
//			
//			ListFieldInfo field=new ListFieldInfo();
//			field.setVarName(cm.getColumnVarName());
//			field.setLabel(cm.getLabel());
//			field.setLogicField(ctx.getLogicField(cm));
//			
//			fields.add(field);
//			
//		}
//		this.putVar("fields", fields);
//		
// 
//		String idPrefix=ctx.getUIModuleFolderName();
//		this.putVar("tableId", idPrefix+"-table");
//		this.putVar("operationTemplateId", idPrefix+"-table-operation");
//		this.putVar("searchFieldId", idPrefix+"-search-field");
//		this.putVar("searchValueInputId", idPrefix+"-search-value");
//		this.putVar("searchButtonId", idPrefix+"-btn-search");
//		this.putVar("addButtonId", idPrefix+"-btn-add");
//		this.putVar("deleteButtonId", idPrefix+"-btn-delete");
//		
//		
//		String moduleBaseSubPath= ctx.getUIPathPrefix();
//		String mdu=ctx.getUIModuleFolderName();
//		String jsPath=StringUtil.joinUrl(moduleBaseSubPath,mdu,mdu+"_list.js");
//		jsPath=jsPath.substring(jsPath.indexOf('/'));
//		
//		this.putVar("jsPath", jsPath);
//		
//		//
//		this.putVar("isTree", tree!=null);
		
		this.putVar("jsURI", this.context.getListPageJSFile().getFullURI());
		applyCommonVars4List(this);
		super.save();
		
		
	}
 
	@Override
	protected String getFileName() {
		return beanNameUtil.depart(this.context.getPoClassFile().getSimpleName()).toLowerCase()+"_list.html";
	}
 
}
