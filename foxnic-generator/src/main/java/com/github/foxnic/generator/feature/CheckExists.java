package com.github.foxnic.generator.feature;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.ClassNames;
import com.github.foxnic.generator.CodePoint;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.clazz.ControllerMethodReplacer;
import com.github.foxnic.generator.clazz.FileBuilder;
import com.github.foxnic.springboot.api.annotations.NotNull;
import com.github.foxnic.springboot.api.error.ErrorDesc;
import com.github.foxnic.springboot.mvc.Result;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;

import java.util.ArrayList;
import java.util.List;

public class CheckExists extends FeatureBuilder {

	@Override
	public String getMethodName(Context ctx) {
		return "checkExists";
	}
	
 
	@Override
	public void buildServiceInterfaceMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		
	 
		code.ln(1,"");
		makeJavaDoc(ctx, code);
		code.ln(1,"Result<"+ctx.getPoName()+"> "+this.getMethodName(ctx)+"("+ctx.getDefaultVO().getClassName()+" "+ctx.getDefaultVO().getVarName()+");");
		builder.addImport(ctx.getDefaultVO().getFullName());
		builder.addImport(ctx.getPoFullName());
		builder.addImport(Result.class);
	}

 
	@Override
	public void buildServiceImplMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		 
		code.ln(1,"");
		makeJavaDoc(ctx, code);
		code.ln(1,"public Result<"+ctx.getPoName()+"> "+this.getMethodName(ctx)+"("+ctx.getDefaultVO().getClassName()+" "+ctx.getDefaultVO().getVarName()+") {");
		
		code.ln(2,"return ErrorDesc.success();");
		code.ln(1,"}");
		builder.addImport(ctx.getDefaultVO().getFullName());
		builder.addImport(ctx.getPoFullName());
		builder.addImport(Result.class);
		builder.addImport(ErrorDesc.class);
	}
 
	private void makeJavaDoc(Context ctx, CodeBuilder code) {
		List<DBColumnMeta> pks=ctx.getTableMeta().getPKColumns();
		code.ln(1,"/**");
		code.ln(1," * "+this.getApiComment(ctx));
		code.ln(1," *");
		for (DBColumnMeta pk : pks) {
			String detail=pk.getDetail();
			if(detail!=null && detail.equals(pk.getLabel())) {
				detail="";
			}
			if(!StringUtil.isBlank(detail)) {
				detail=" , "+detail;
			}
			code.ln(1," * @param "+ctx.getDefaultVO().getVarName()+" 数据对象");
		}
		code.ln(1," * @return 判断结果");
		code.ln(1," */");
	}

	
	
	@Override
	public void buildControllerMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
 
	}

	

	@Override
	public void buildFeignMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		 
		
	}

	@Override
	public String getApiComment(Context ctx) {
		return "检查 "+getTopic(ctx)+" 是否已经存在";
	}
 

}
