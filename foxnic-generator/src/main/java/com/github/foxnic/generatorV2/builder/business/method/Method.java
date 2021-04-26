package com.github.foxnic.generatorV2.builder.business.method;

import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.generatorV2.builder.business.TemplateJavaFile;
import com.github.foxnic.generatorV2.config.MduCtx;

public abstract class Method {
	
	protected DBTableMeta tableMeta;
	protected MduCtx context;
	public Method(MduCtx context) {
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
}
