package com.github.foxnic.generator.builder.business.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.generator.builder.business.TemplateJavaFile;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;
import io.swagger.models.Swagger;

public abstract class Method {

	public static enum SwaggerApiImplicitParamsMode {
		IGNORE,IDS,ID,ALL;
	}

	protected final DefaultNameConvertor convertor = new DefaultNameConvertor();

	protected DBTableMeta tableMeta;
	protected ModuleContext context;
	protected TemplateJavaFile javaFile;
	protected String commentPrefix="";

	protected JSONObject literalMap=new JSONObject();

	protected  SwaggerApiImplicitParamsMode  swaggerApiImplicitParamsMode=SwaggerApiImplicitParamsMode.ALL;
	public Method(ModuleContext context,TemplateJavaFile javaFile,String commentPrefix) {
		this.context=context;
		this.tableMeta=context.getTableMeta();
		this.javaFile=javaFile;
		this.commentPrefix=commentPrefix;
	}

	protected String makeParamStr(List<DBColumnMeta> cms, boolean withType) {
		List<String> fields=new ArrayList<String>();
		for (DBColumnMeta pk : cms) {
			fields.add((withType?(pk.getDBDataType().getType().getSimpleName()+" "):"")+pk.getColumnVarName());
			javaFile.addImport(pk.getDBDataType().getType());
		}
		String params=StringUtil.join(fields," , ");
		return params;
	}

	protected boolean displayDetail(DBColumnMeta cm) {
		if(StringUtil.isBlank(cm.getDetail()) || cm.getLabel().equals(cm.getDetail())) return false;
		return true;
	}


	public JSONObject getLiteralMap() {
		return literalMap;
	}

	public SwaggerApiImplicitParamsMode getSwaggerApiImplicitParamsMode() {
		return swaggerApiImplicitParamsMode;
	}

	public abstract String getMethodName();

	public String getMethodComment() {
		return commentPrefix+this.context.getTopic();
	}

	public String getOperationName() {
		return commentPrefix+this.context.getTopic();
	}

	public abstract CodeBuilder buildServiceInterfaceMethod(TemplateJavaFile javaFile);

	public abstract CodeBuilder buildServiceImplementMethod(TemplateJavaFile javaFile);

	public abstract CodeBuilder getControllerValidateAnnotations(TemplateJavaFile javaFile);

	public abstract CodeBuilder getControllerSwaggerAnnotations(TemplateJavaFile javaFile);
}
