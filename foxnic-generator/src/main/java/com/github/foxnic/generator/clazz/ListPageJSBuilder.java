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
import com.github.foxnic.generator.clazz.model.ListFieldInfo;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;
import com.github.foxnic.sql.meta.DBDataType;

public class ListPageJSBuilder extends TemplateFileBuilder {

 
	public ListPageJSBuilder(Context cfg) {
		super(cfg);
 
	}

 
	@Override
	protected void build() {
		
		String temp=this.ctx.getListJSTemplate();
		
		template = engine.getTemplate(temp);
		
		TreeConfig tree=ctx.getTreeConfig();
		DefaultNameConvertor nc=new DefaultNameConvertor();
		 
		// 字符流模式输出到 StringWriter
		//StringWriter sw = new StringWriter();
		
		CodeBuilder code=new CodeBuilder();
		code.ln("/**");
		code.ln(" * "+ctx.getTopic()+" 列表页 JS 脚本");
		code.ln(" * @author "+ctx.getAuthor());
		code.ln(" * @since "+DateUtil.getFormattedTime(false));
		code.ln(" */");
		this.putVar("authorAndTime", code);
		
		this.putVar("topic", ctx.getTopic());
		this.putVar("moduleURL", "/"+ctx.getControllerApiPrefix()+"/"+ctx.getApiContextPart()+"/");
		
		boolean isSinglePK=this.ctx.getTableMeta().getPKColumnCount()==1;
		this.putVar("isSinglePK", isSinglePK);
		
		
		//所有字段
		List<DBColumnMeta> columns=this.ctx.getTableMeta().getColumns();
		List<ListFieldInfo> fields=new ArrayList<>();
		 
		for (DBColumnMeta cm : columns) {
			if(ctx.isDBTreatyFiled(cm)  && !ctx.getDBTreaty().getCreateTimeField().equals(cm.getColumn())) continue;
			
			//不显示自增主键
			if(cm.isPK() && cm.isAutoIncrease()) continue;
			//不显示上级ID
			if(tree!=null && tree.getParentIdField().equalsIgnoreCase(cm.getColumn()))  continue;
			
			
			
			String templet="";
			if(cm.getDBDataType()==DBDataType.DATE) {
				templet=" , templet: function (d) { return util.toDateString(d."+cm.getColumnVarName()+"); }";
			} else if(ctx.isImageIdField(cm)) {
				templet=" , templet: function (d) { return '<img width=\"50px\" height=\"50px\" onclick=\"window.previewImage(this)\"  src=\"/service-tailoring/sys-file/download?id='+ d."+cm.getColumnVarName()+"+'\" />'; }";
			} else if(ctx.isLogicField(cm)) {
				templet=", templet: '#cell-tpl-"+cm.getColumnVarName()+"'";
			}
			
			ListFieldInfo field=new ListFieldInfo();
			field.setVarName(cm.getColumnVarName());
			field.setLabel(cm.getLabel());
			field.setTemplet(templet);
			field.setLogicField(ctx.getLogicField(cm));
			
			fields.add(field);
		}
		this.putVar("fields", fields);
	 
		
		List<DBColumnMeta> pks=this.ctx.getTableMeta().getPKColumns();
		List<String> pkvs=new ArrayList<>();
		for (DBColumnMeta pk : pks) {
			String f=pk.getColumnVarName();
			pkvs.add( f+" : data."+f );
		}
		this.putVar("paramJson", StringUtil.join(pkvs," , "));
		
		
 
		String idPrefix=ctx.getUIModuleFolderName();
		this.putVar("tableId", idPrefix+"-table");
		this.putVar("operationTemplateId", idPrefix+"-table-operation");
		this.putVar("searchFieldId", idPrefix+"-search-field");
		this.putVar("searchValueInputId", idPrefix+"-search-value");
		this.putVar("searchButtonId", idPrefix+"-btn-search");
		this.putVar("addButtonId", idPrefix+"-btn-add");
		this.putVar("deleteButtonId", idPrefix+"-btn-delete");
		this.putVar("formDataKey", ctx.getTableName().toLowerCase().replace('_', '-')+"-form-data");
		
		//
		this.putVar("formPath", "/pages/product/"+idPrefix+"/"+idPrefix+"_form.html");
		this.putVar("jsPath", "/pages/product/"+idPrefix+"/"+idPrefix+"_form.js");
		
		//
		
		if(isSinglePK) {
			this.putVar("idVar", this.ctx.getTableMeta().getPKColumns().get(0).getColumnVarName());
		}
		
		this.putVar("isTree", tree!=null);
		if(tree!=null) {
			this.putVar("parentIdVar", nc.getPropertyName(tree.getParentIdField()));
			
			String rootId=null;
			if(tree.getRootId() instanceof CharSequence) {
				rootId="'"+tree.getRootId()+"'";
			} else {
				rootId=tree.getRootId()+"";
			}
			this.putVar("rootId", rootId);
		}
		
	}

	@Override
	public void buildAndUpdate() {
		
		String moduleBaseSubPath= ctx.getUIPathPrefix();
		String mdu=ctx.getUIModuleFolderName();
		String temp=StringUtil.joinUrl(moduleBaseSubPath,mdu,mdu+"_list.js");
 
		File dir=FileUtil.resolveByPath(ctx.getServiceProject().getMainResourceDir(), temp);
		
		this.buildAndUpdate(dir);
	}
 

}
