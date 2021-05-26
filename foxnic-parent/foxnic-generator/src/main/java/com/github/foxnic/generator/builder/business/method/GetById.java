package com.github.foxnic.generator.builder.business.method;

import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.builder.business.CodePoint;
import com.github.foxnic.generator.builder.business.TemplateJavaFile;
import com.github.foxnic.generator.config.ModuleContext;

public class GetById extends Method {

	public GetById(ModuleContext context) {
		super(context);
	}

	@Override
	public String getMethodName() {
		return "getById";
	}

	@Override
	public String getMethodComment() {
		return "按主键获取 "+ this.context.getTopic();
	}
	
 
	
	
	private void makeJavaDoc(CodeBuilder code) {
		List<DBColumnMeta> pks=tableMeta.getPKColumns();
		code.ln(1,"/**");
		code.ln(1," * "+this.getMethodComment());
		code.ln(1," *");
		for (DBColumnMeta pk : pks) {
			code.ln(1," * @param "+pk.getColumnVarName()+" "+pk.getLabel()+( displayDetail(pk)?(" , 详情 : "+pk.getDetail()):"") );
		}
		code.ln(1," * @return "+context.getPoClassFile().getSimpleName()+" 数据对象");
		code.ln(1," */");
	}

	@Override
	public CodeBuilder buildServiceInterfaceMethod(TemplateJavaFile javaFile) {
		CodeBuilder code=new CodeBuilder();
		String params = makeParamStr(tableMeta.getPKColumns(),true);
		code.ln(1,"");
		makeJavaDoc(code);
		code.ln(1,context.getPoClassFile().getSimpleName()+" "+this.getMethodName()+"("+params+");");
		return code;
	}

	@Override
	public CodeBuilder buildServiceImplementMethod(TemplateJavaFile javaFile) {
		String poSimpleName=this.context.getPoClassFile().getSimpleName();
		CodeBuilder code=new CodeBuilder();
		List<DBColumnMeta> pks = tableMeta.getPKColumns();
		String params = makeParamStr(pks,true);
		code.ln(1,"");
		makeJavaDoc(code);
		code.ln(1,"public "+poSimpleName+" "+this.getMethodName()+"("+params+") {");
		code.ln(2,poSimpleName+" sample = new "+poSimpleName+"();");
		String setter;
		//校验主键
		for (DBColumnMeta pk : pks) {
			setter=convertor.getSetMethodName(pk.getColumn(), pk.getDBDataType());
			code.ln(2,"if("+pk.getColumnVarName()+"==null) throw new IllegalArgumentException(\""+pk.getColumnVarName()+" 不允许为 null \");");
		}
		//设置主键
		for (DBColumnMeta pk : tableMeta.getPKColumns()) {
			setter=convertor.getSetMethodName(pk.getColumn(), pk.getDBDataType());
			code.ln(2,"sample."+setter+"("+pk.getColumnVarName()+");");
		}
		code.ln(2,"return dao.queryEntity(sample);");
		code.ln(1,"}");
		return code;
		
	}
	
	public String getControllerMethodParameterDeclare() {
		return makeParamStr(tableMeta.getPKColumns(),true);
	}
	
	public String getControllerMethodParameterPassIn() {
		return makeParamStr(tableMeta.getPKColumns(),false);
	}

	@Override
	public CodeBuilder getControllerValidateAnnotations(TemplateJavaFile javaFile) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CodeBuilder getControllerSwagerAnnotations(TemplateJavaFile javaFile, CodePoint codePoint) {
		// TODO Auto-generated method stub
		return null;
	}

}
