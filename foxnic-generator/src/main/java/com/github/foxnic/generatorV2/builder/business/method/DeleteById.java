package com.github.foxnic.generatorV2.builder.business.method;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generatorV2.builder.business.TemplateJavaFile;
import com.github.foxnic.generatorV2.config.MduCtx;

public class DeleteById extends Method {
	
	public DeleteById(MduCtx context) {
		super(context);
	}

	@Override
	public String getMethodName() {
		return  "deleteById";
	}
	
	@Override
	public String getMethodComment() {
		return "按主键删除 "+ this.getTopic();
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
		code.ln(1,"boolean "+this.getMethodName()+"Physical("+params+");");
		if(tableMeta.isColumnExists(context.getDAO().getDBTreaty().getDeletedField())) {
			code.ln(1,"");
			makeJavaDoc(code);
			code.ln(1,"boolean "+this.getMethodName()+"Logical("+params+");");
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
		code.ln(1,"public boolean "+this.getMethodName()+"Physical("+params+") {");
		code.ln(2,poSimpleName+" "+poVarName+" = new "+poSimpleName+"();");
		String setter;
		//校验主键
		for (DBColumnMeta pk : tableMeta.getPKColumns()) {
			setter=convertor.getSetMethodName(pk.getColumn(), pk.getDBDataType());
			code.ln(2,"if("+pk.getColumnVarName()+"==null) throw new IllegalArgumentException(\""+pk.getColumnVarName()+" 不允许为 null \");");
		}
		//设置主键
		for (DBColumnMeta pk : tableMeta.getPKColumns()) {
			setter=convertor.getSetMethodName(pk.getColumn(), pk.getDBDataType());
			code.ln(2,poVarName+"."+setter+"("+pk.getColumnVarName()+");");
		}
		code.ln(2,"return dao.deleteEntity("+poVarName+");");
		code.ln(1,"}");
		
		//如果有删除字段
		if(tableMeta.isColumnExists(this.context.getDAO().getDBTreaty().getDeletedField())) {
			code.ln(1,"");
			makeJavaDoc(code);
			code.ln(1,"public boolean "+this.getMethodName()+"Logical("+params+") {");
			code.ln(2,poSimpleName+" "+poVarName+" = new "+poSimpleName+"();");
			//校验主键
			for (DBColumnMeta pk : tableMeta.getPKColumns()) {
				setter=convertor.getSetMethodName(pk.getColumn(), pk.getDBDataType());
				code.ln(2,"if("+pk.getColumnVarName()+"==null) throw new IllegalArgumentException(\""+pk.getColumnVarName()+" 不允许为 null 。\");");
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
			
			code.ln(2,"return dao.updateEntity("+poVarName+",SaveMode.NOT_NULL_FIELDS);");
			code.ln(1,"}");
 
		}
		return code;
	}
	
	

}