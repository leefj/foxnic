package com.github.foxnic.generatorV2.builder.business.method;

import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
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
		return "按主键删除 "+ tableMeta.getTopic();
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
	
	

}
