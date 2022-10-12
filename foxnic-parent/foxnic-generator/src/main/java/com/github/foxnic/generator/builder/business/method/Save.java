package com.github.foxnic.generator.builder.business.method;

import com.github.foxnic.api.validate.annotations.NotNull;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.builder.business.TemplateJavaFile;
import com.github.foxnic.generator.config.ModuleContext;

import java.util.List;

public class Save extends Method {

	public Save(ModuleContext context,TemplateJavaFile javaFile) {
		super(context,javaFile,"保存");
	}

	@Override
	public String getMethodName() {
		return "save";
	}

	@Override
	public String getMethodComment() {
		return super.getMethodComment();
	}


	@Override
	public CodeBuilder buildServiceInterfaceMethod(TemplateJavaFile javaFile) {
		return null;
	}

	@Override
	public CodeBuilder buildServiceImplementMethod(TemplateJavaFile javaFile) {
		return null;
	}

	public CodeBuilder getControllerValidateAnnotations(TemplateJavaFile javaFile) {
		CodeBuilder code=new CodeBuilder();
		List<DBColumnMeta> cms = tableMeta.getColumns();
		for (DBColumnMeta cm : cms) {
			if(context.isDBTreatyFiled(cm,true)) continue;
			if(!cm.isNullable()) {
				code.ln(1,"@NotNull(name = "+context.getVoMetaClassFile().getSimpleName()+"."+cm.getColumn().toUpperCase()+")");
				javaFile.addImport(NotNull.class);
			}
		}
		return code;
	}

	public CodeBuilder getControllerSwaggerAnnotations(TemplateJavaFile javaFile) {
		CodeBuilder code=new CodeBuilder();

		String opName="保存"+this.context.getTopic();
		String apiOperation="@ApiOperation(value = \""+opName+"\")";
		code.ln(1,apiOperation);
		literalMap.put("ApiOperation.value",opName);

		code.ln(1,"@ApiImplicitParams({");

		List<DBColumnMeta> cms = tableMeta.getColumns();
		int i=0;

		for (DBColumnMeta cm : cms) {

			if(context.isDBTreatyFiled(cm,true)) continue;
			String example=context.getExampleStringValue(cm);

			if(!StringUtil.isBlank(example)) {
				example=" , example = \""+example+"\"";
			} else {
				example="";
			}

			String apiImplicitParamName=context.getVoMetaClassFile().getSimpleName()+"."+cm.getColumn().toUpperCase();
			String line="@ApiImplicitParam(name = "+apiImplicitParamName+" , value = \""+cm.getLabel()+"\" , required = "+!cm.isNullable()+" , dataTypeClass="+cm.getDBDataType().getType().getSimpleName()+".class"+example+")"+(i<=cms.size()-2?",":"");
			code.ln(2,line);
			literalMap.put("ApiImplicitParam."+apiImplicitParamName+".name",cm.getLabel());

			i++;

		}

		code.ln(1,"})");

		return code;
	}


}
