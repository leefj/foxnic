package com.github.foxnic.generator.builder.business.method;

import java.util.List;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.builder.business.CodePoint;
import com.github.foxnic.generator.builder.business.TemplateJavaFile;
import com.github.foxnic.generator.config.MduCtx;
import com.github.foxnic.sql.meta.DBField;

public class UpdateById extends Method {

	public UpdateById(MduCtx context) {
		super(context);
	}

	@Override
	public String getMethodName() {
		return "update";
	}

	@Override
	public String getMethodComment() {
		return "按主键更新字段 "+ this.context.getTopic();
	}
	
 
	
	
	private void makeJavaDoc(CodeBuilder code) {
		List<DBColumnMeta> pks=tableMeta.getPKColumns();
		code.ln(1,"/**");
		code.ln(1," * "+this.getMethodComment());
		code.ln(1," *");
		for (DBColumnMeta pk : pks) {
			code.ln(1," * @param "+pk.getColumnVarName()+" "+pk.getLabel()+( displayDetail(pk)?(" , 详情 : "+pk.getDetail()):"") );
		}
		code.ln(1," * @return 是否更新成功");
		code.ln(1," */");
	}

	@Override
	public CodeBuilder buildServiceInterfaceMethod(TemplateJavaFile javaFile) {
		CodeBuilder code=new CodeBuilder();
		String params = makeParamStr(tableMeta.getPKColumns(),true);
		code.ln(1,"");
		makeJavaDoc(code);
		code.ln(1,"boolean "+this.getMethodName()+"(DBField field,Object value , "+params+");");
		return code;
	}

	@Override
	public CodeBuilder buildServiceImplementMethod(TemplateJavaFile javaFile) {
		CodeBuilder code=new CodeBuilder();
		List<DBColumnMeta> pks=tableMeta.getPKColumns();
		String params = makeParamStr(pks,true);
		code.ln(1,"");
		makeJavaDoc(code);
		
		String cdr="";
		for (DBColumnMeta pk : pks) {
			cdr=pk.getColumn()+" = ? and ";
		}
		cdr=cdr.substring(0,cdr.length()-4);
		code.ln(1,"public boolean "+this.getMethodName()+"(DBField field,Object value , "+params+") {");
		//校验主键
		for (DBColumnMeta pk : pks) {
			code.ln(2,"if("+pk.getColumnVarName()+"==null) throw new IllegalArgumentException(\""+pk.getColumnVarName()+" 不允许为 null \");");
		}
		params = makeParamStr(pks,false);
		code.ln(2,"if(!field.table().name().equals(this.table())) throw new IllegalArgumentException(\"更新的数据表[\"+field.table().name()+\"]与服务对应的数据表[\"+this.table()+\"]不一致\");");
		code.ln(2,"int suc=dao.update(field.table().name()).set(field.name(), value).where().and(\""+cdr+"\","+params+").top().execute();");
		code.ln(2,"return suc>0;");
		code.ln(1,"}");
		return code;
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
