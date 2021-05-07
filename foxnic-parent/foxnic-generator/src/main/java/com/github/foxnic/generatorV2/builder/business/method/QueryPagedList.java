package com.github.foxnic.generatorV2.builder.business.method;

import java.util.List;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generatorV2.builder.business.CodePoint;
import com.github.foxnic.generatorV2.builder.business.ControllerMethodReplacer;
import com.github.foxnic.generatorV2.builder.business.TemplateJavaFile;
import com.github.foxnic.generatorV2.config.MduCtx;
import com.github.foxnic.springboot.api.annotations.NotNull;

public class QueryPagedList extends Method {

	public QueryPagedList(MduCtx context) {
		super(context);
	}

	@Override
	public String getMethodName() {
		return "queryPagedList";
	}

	@Override
	public String getMethodComment() {
		return "分页查询 "+ this.context.getTopic();
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
			if(context.isDBTreatyFiled(cm)) continue;
			if(!cm.isNullable()) {
				code.ln(1,"@NotNull(name = "+context.getVoMetaClassFile().getSimpleName()+"."+cm.getColumn().toUpperCase()+")");
				javaFile.addImport(NotNull.class);
			}
		}
		return code;
	}
	
	public CodeBuilder getControllerSwagerAnnotations(TemplateJavaFile javaFile,CodePoint codePoint) {
		CodeBuilder code=new CodeBuilder();
		ControllerMethodReplacer controllerMethodReplacer=null;
		String controllerMethodName="queryPagedList";
		String codePointLocation=javaFile.getFullName()+"."+controllerMethodName;
		try {
			if(context.getSettings().isEnableSwagger() &&  javaFile.getSourceFile()!=null && javaFile.getSourceFile().exists()) {
				controllerMethodReplacer=new ControllerMethodReplacer(codePoint,javaFile.getFullName(),controllerMethodName,context.getVoClassFile().getFullName());
				codePoint.addReplacer(controllerMethodReplacer);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("控制器文件存在，但无法找到类型,"+javaFile.getSourceFile().getName(),e);
		}
		
		String opName="分页查询"+this.context.getTopic();
		String apiOperation="@ApiOperation(value = \""+opName+"\")";
		code.ln(1,apiOperation);
		codePoint.set(codePointLocation+"@ApiOperation.value", opName);
		code.ln(1,"@ApiImplicitParams({");
		
		List<DBColumnMeta> cms = tableMeta.getColumns();
		int i=0;
		 
		for (DBColumnMeta cm : cms) {
			
			if(context.isDBTreatyFiled(cm)) continue;
			String example=context.getExampleStringValue(cm);
			
			if(!StringUtil.isBlank(example)) {
				example=" , example = \""+example+"\"";
			} else {
				example="";
			}
			
			String apiImplicitParamName=context.getVoMetaClassFile().getSimpleName()+"."+cm.getColumn().toUpperCase();
			String line="@ApiImplicitParam(name = "+apiImplicitParamName+" , value = \""+cm.getLabel()+"\" , required = "+!cm.isNullable()+" , dataTypeClass="+cm.getDBDataType().getType().getSimpleName()+".class"+example+")"+(i<=cms.size()-2?",":"");
			code.ln(2,line);
			codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".value", cm.getLabel());
			codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".required", (!cm.isNullable())+"");
			codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".dataTypeClass", cm.getDBDataType().getType().getSimpleName()+".class");
			codePoint.addApiImplicitParam(codePointLocation, line);
			
			i++;
 
		}
		
		code.ln(1,"})");
		
		return code;
	}
	

}