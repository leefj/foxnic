package com.github.foxnic.generator.builder.business.method;

import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.generator.builder.business.CodePoint;
import com.github.foxnic.generator.builder.business.TemplateJavaFile;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;

public abstract class Method {
	
	protected final DefaultNameConvertor convertor = new DefaultNameConvertor(); 
	
	protected DBTableMeta tableMeta;
	protected ModuleContext context;
	public Method(ModuleContext context) {
		this.context=context;
		this.tableMeta=context.getTableMeta();
	}
	
	protected String makeParamStr(List<DBColumnMeta> cms, boolean withType) {
		List<String> fields=new ArrayList<String>();
		for (DBColumnMeta pk : cms) {
			fields.add((withType?(pk.getDBDataType().getType().getSimpleName()+" "):"")+pk.getColumnVarName());
		}
		String params=StringUtil.join(fields," , ");
		return params;
	}
	
	protected boolean displayDetail(DBColumnMeta cm) {
		if(StringUtil.isBlank(cm.getDetail()) || cm.getLabel().equals(cm.getDetail())) return false;
		return true;
	}
	
	
	
	public abstract String getMethodName();
	
	public abstract String getMethodComment();
	
	public abstract CodeBuilder buildServiceInterfaceMethod(TemplateJavaFile javaFile);
	
	public abstract CodeBuilder buildServiceImplementMethod(TemplateJavaFile javaFile);
	
	public abstract CodeBuilder getControllerValidateAnnotations(TemplateJavaFile javaFile);
	
	public abstract CodeBuilder getControllerSwagerAnnotations(TemplateJavaFile javaFile,CodePoint codePoint);
}
