package com.github.foxnic.generator.builder.business.method;

import com.github.foxnic.api.validate.annotations.NotNull;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.builder.business.CodePoint;
import com.github.foxnic.generator.builder.business.ControllerMethodReplacer;
import com.github.foxnic.generator.builder.business.TemplateJavaFile;
import com.github.foxnic.generator.config.ModuleContext;

import java.util.Date;
import java.util.List;

public class DeleteById extends Method {
	
	public DeleteById(ModuleContext context) {
		super(context);
	}

	@Override
	public String getMethodName() {
		return  "deleteById";
	}
	
	@Override
	public String getMethodComment() {
		return "按主键删除 "+ this.context.getTopic();
	}
	 
	
	

	private void makeJavaDoc(CodeBuilder code) {
		List<DBColumnMeta> pks=tableMeta.getPKColumns();
		code.ln(1,"/**");
		code.ln(1," * "+this.getMethodComment());
		code.ln(1," *");
		for (DBColumnMeta pk : pks) {
			code.ln(1," * @param "+pk.getColumnVarName()+" "+pk.getLabel()+( displayDetail(pk)?(" , 详情 : "+pk.getDetail()):"") );
		}
		code.ln(1," * @return 删除是否成功");
		code.ln(1," */");
	}
	
	@Override
	public CodeBuilder buildServiceInterfaceMethod(TemplateJavaFile javaFile) {
		CodeBuilder code=new CodeBuilder();
		String params = makeParamStr(tableMeta.getPKColumns(),true);
		code.ln(1,"");
		makeJavaDoc(code);
		code.ln(1,"Result "+this.getMethodName()+"Physical("+params+");");
		if(tableMeta.isColumnExists(context.getDAO().getDBTreaty().getDeletedField())) {
			code.ln(1,"");
			makeJavaDoc(code);
			code.ln(1,"Result "+this.getMethodName()+"Logical("+params+");");
		}
		return code;
	}

	@Override
	public CodeBuilder buildServiceImplementMethod(TemplateJavaFile javaFile) {
		String poSimpleName=this.context.getPoClassFile().getSimpleName();
		String poVarName=this.context.getPoClassFile().getVar();
		CodeBuilder code=new CodeBuilder();
		String params = makeParamStr(tableMeta.getPKColumns(),true);
		code.ln(1,"");
		makeJavaDoc(code);
		code.ln(1,"public Result "+this.getMethodName()+"Physical("+params+") {");
		code.ln(2,poSimpleName+" "+poVarName+" = new "+poSimpleName+"();");
		String setter;
		//校验主键
		for (DBColumnMeta pk : tableMeta.getPKColumns()) {
			setter=convertor.getSetMethodName(pk.getColumn(), pk.getDBDataType());
			code.ln(2,"if("+pk.getColumnVarName()+"==null) return ErrorDesc.failure().message(\""+pk.getColumnVarName()+" 不允许为 null 。\");");
		}
		//设置主键
		for (DBColumnMeta pk : tableMeta.getPKColumns()) {
			setter=convertor.getSetMethodName(pk.getColumn(), pk.getDBDataType());
			code.ln(2,poVarName+"."+setter+"("+pk.getColumnVarName()+");");
		}

		code.ln(2,"try {");
		code.ln(3,"boolean suc = dao.deleteEntity("+poVarName+");");
		code.ln(3,"return suc?ErrorDesc.success():ErrorDesc.failure();");
		code.ln(2,"}");
		code.ln(2,"catch(Exception e) {");
		code.ln(3,"Result r= ErrorDesc.failure();");
		code.ln(3,"r.extra().setException(e);");
		code.ln(3,"return r;");
		code.ln(2,"}");
		code.ln(1,"}");
		
		//如果有删除字段
		if(tableMeta.isColumnExists(this.context.getDAO().getDBTreaty().getDeletedField())) {
			code.ln(1,"");
			makeJavaDoc(code);
			code.ln(1,"public Result "+this.getMethodName()+"Logical("+params+") {");
			code.ln(2,poSimpleName+" "+poVarName+" = new "+poSimpleName+"();");
			//校验主键
			for (DBColumnMeta pk : tableMeta.getPKColumns()) {
				setter=convertor.getSetMethodName(pk.getColumn(), pk.getDBDataType());
				code.ln(2,"if("+pk.getColumnVarName()+"==null) return ErrorDesc.failure().message(\""+pk.getColumnVarName()+" 不允许为 null 。\");");
			}
			//设置主键
			for (DBColumnMeta pk : tableMeta.getPKColumns()) {
				setter=convertor.getSetMethodName(pk.getColumn(), pk.getDBDataType());
				code.ln(2,poVarName+"."+setter+"("+pk.getColumnVarName()+");");
			}
			//删除控制字段
			DBColumnMeta cm=tableMeta.getColumn(context.getDAO().getDBTreaty().getDeletedField());
			setter=convertor.getSetMethodName(cm.getColumn(), cm.getDBDataType());
			if(context.getDAO().getDBTreaty().isAutoCastLogicField()) {
				code.ln(2,poVarName+"."+setter+"(true);");
			} else {
				code.ln(2,poVarName+"."+setter+"(dao.getDBTreaty().getTrueValue());");
			}
			
			cm=tableMeta.getColumn(context.getDAO().getDBTreaty().getDeleteUserIdField());
			if(cm!=null) {
				setter=convertor.getSetMethodName(cm.getColumn(), cm.getDBDataType());
				code.ln(2,poVarName+"."+setter+"(("+cm.getDBDataType().getType().getSimpleName()+")dao.getDBTreaty().getLoginUserId());");
			}
			
			cm=tableMeta.getColumn(context.getDAO().getDBTreaty().getDeleteTimeField());
			if(cm!=null) {
				setter=convertor.getSetMethodName(cm.getColumn(), cm.getDBDataType());
				code.ln(2,poVarName+"."+setter+"(new Date());");
				javaFile.addImport(Date.class);
			}
			
//			code.ln(2,"boolean suc = dao.updateEntity("+poVarName+",SaveMode.NOT_NULL_FIELDS);");
//			code.ln(2,"return suc?ErrorDesc.success():ErrorDesc.failure();");
//			code.ln(1,"}");

			code.ln(2,"try {");
			code.ln(3,"boolean suc = dao.updateEntity("+poVarName+",SaveMode.NOT_NULL_FIELDS);");
			code.ln(3,"return suc?ErrorDesc.success():ErrorDesc.failure();");
			code.ln(2,"}");
			code.ln(2,"catch(Exception e) {");
			code.ln(3,"Result r= ErrorDesc.failure();");
			code.ln(3,"r.extra().setException(e);");
			code.ln(3,"return r;");
			code.ln(2,"}");
			code.ln(1,"}");
 
		}
		return code;
	}
	
	public String getControllerMethodParameterDeclare() {
		return makeParamStr(tableMeta.getPKColumns(),true);
	}
	
	public String getControllerMethodParameterPassIn() {
		return makeParamStr(tableMeta.getPKColumns(),false);
	}
	
	public String getImplMethod() {
		if(tableMeta.isColumnExists(this.context.getDAO().getDBTreaty().getDeletedField())) {
			return this.getMethodName()+"Logical";
		} else {
			return this.getMethodName()+"Physical";
		}
	}
	
	
	
	

	@Override
	public CodeBuilder getControllerValidateAnnotations(TemplateJavaFile javaFile) {
		CodeBuilder code=new CodeBuilder();
		List<DBColumnMeta> cms = tableMeta.getPKColumns();
		for (DBColumnMeta cm : cms) {
			code.ln(1,"@NotNull(name = "+context.getVoMetaClassFile().getSimpleName()+"."+cm.getColumn().toUpperCase()+")");
			javaFile.addImport(NotNull.class);
		}
		return code;
	}

	@Override
	public CodeBuilder getControllerSwagerAnnotations(TemplateJavaFile javaFile, CodePoint codePoint) {
 
		ControllerMethodReplacer controllerMethodReplacer=null;
		String controllerMethodName="deleteById";
		String codePointLocation=javaFile.getFullName()+"."+controllerMethodName;
		try {
			if(context.getSettings().isEnableSwagger() &&  javaFile.getSourceFile()!=null && javaFile.getSourceFile().exists()) {
				List<DBColumnMeta> pks = tableMeta.getPKColumns();
				String[] pTypes=new String[pks.size()];
				for (int i = 0; i < pks.size(); i++) {
					pTypes[i]=pks.get(i).getDBDataType().getType().getName();
				}
				controllerMethodReplacer=new ControllerMethodReplacer(codePoint,javaFile.getFullName(),controllerMethodName,pTypes);
				codePoint.addReplacer(controllerMethodReplacer);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("控制器文件存在，但无法找到类型,"+javaFile.getSourceFile().getName(),e);
		}
		
		CodeBuilder code=new CodeBuilder();
		
		String opName="删除"+this.context.getTopic();
		String apiOperation="@ApiOperation(value = \""+opName+"\")";
		code.ln(1,apiOperation);
		codePoint.set(codePointLocation+"@ApiOperation.value", opName);
		code.ln(1,"@ApiImplicitParams({");
		
		List<DBColumnMeta> pks =tableMeta.getPKColumns();
		int i=0;
		for (DBColumnMeta pk : pks) {
			
			String example=context.getExampleStringValue(pk);
			if(!StringUtil.isBlank(example)) {
				example=" , example = \""+example+"\"";
			} else {
				example="";
			}
			
			String apiImplicitParamName=context.getVoMetaClassFile().getSimpleName()+"."+pk.getColumn().toUpperCase();
			String line="@ApiImplicitParam(name = "+apiImplicitParamName+" , value = \""+pk.getLabel()+"\" , required = true , dataTypeClass="+pk.getDBDataType().getType().getSimpleName()+".class"+example+")"+(i<=pks.size()-2?",":"");
			code.ln(2,line);
			i++;
			 
			
			codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".value", pk.getLabel());
			codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".required", "true");
			codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".dataTypeClass", pk.getDBDataType().getType().getSimpleName()+".class");
			codePoint.addApiImplicitParam(codePointLocation, line);
		}
		code.ln(1,"})");
		
		return code;
	}
	
	

}
