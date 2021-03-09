package com.github.foxnic.generator.feature;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.Pojo;
import com.github.foxnic.generator.clazz.FileBuilder;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;

public class QueryList extends FeatureBuilder {

	
	@Override
	public String getMethodName(Context ctx) {
		return "queryList";
	}
 
	@Override
	public void buildServiceInterfaceMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		 
//		code.ln(1,"");
//		makeJavaDoc(ctx, code);
//		code.ln(1,"List<"+ctx.getPoName()+"> "+getMethodName(ctx)+"("+ctx.getPoName()+" sample);");
//		builder.addImport(List.class);
//		builder.addImport(ctx.getPoFullName());
		
	}

	

	@Override
	public void buildServiceImplMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		
//		code.ln(1,"");
//		makeJavaDoc(ctx, code);
//		code.ln(1,"public List<"+ctx.getPoName()+"> "+getMethodName(ctx)+"("+ctx.getPoName()+" sample) {");
//		code.ln(2,"return dao.queryEntities(sample);");
//		code.ln(1,"}");
//		builder.addImport(List.class);
//		builder.addImport(ctx.getPoFullName());
		
	}
	
	
//	private void makeJavaDoc(Context ctx, CodeBuilder code) {
//		code.ln(1,"/**");
//		code.ln(1," * "+this.getApiComment(ctx));
//		code.ln(1," *");
//		code.ln(1," * @param sample 查询条件");
//		code.ln(1," * @return 查询结果 , "+ctx.getPoName()+"清单");
//		code.ln(1," */");
//	}

	@Override
	public void buildControllerMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * "+this.getApiComment(ctx));
		code.ln(1,"*/");
		
		if(ctx.isEnableSwagger()) {
			code.ln(1,"@ApiOperation(value = \""+this.getApiComment(ctx)+"\")");
			code.ln(1,"@ApiImplicitParams({");
			List<DBColumnMeta> cms = ctx.getTableMeta().getColumns();
			int i=0;
			for (DBColumnMeta cm : cms) {
				if(ctx.isDBTreatyFiled(cm)) continue;
				
				String example=ctx.getExampleStringValue(cm);
				if(!StringUtil.isBlank(example)) {
					example=" , example = \""+example+"\"";
				} else {
					example="";
				}
				
				code.ln(2,"@ApiImplicitParam(name = \""+cm.getColumnVarName()+"\",value = \""+cm.getLabel()+"\" , required = false , dataTypeClass="+cm.getDBDataType().getType().getSimpleName()+".class"+example+")"+(i<=cms.size()-2?",":""));
				i++;
				builder.addImport(cm.getDBDataType().getType().getName());
			}
			code.ln(1,"})");
		}
		
		List<Pojo.Property> list=ctx.getDefaultVOProperties();
		String plist=StringUtil.join(BeanUtil.getFieldValueArray(list, "name", String.class), ",", "\"");
		code.ln(1, "@ApiOperationSupport(ignoreParameters = {"+plist+"},order=5)");
		builder.addImport(ApiOperationSupport.class);
		
		if(ctx.isEnableMicroService()) {
			code.ln(1,"@SentinelResource(value = "+ctx.getAgentName()+"."+this.getUriConstName()+", blockHandlerClass = { SentinelExceptionUtil.class },blockHandler = SentinelExceptionUtil.HANDLER)");
		}
		if(ctx.isEnableMicroService()) {
			code.ln(1,"@PostMapping("+ctx.getAgentName()+"."+this.getUriConstName()+")");
		} else {
			code.ln(1,"@PostMapping(\""+this.getMethodName(ctx)+"\")");
		}
		code.ln(1,"public  Result<List<"+ctx.getPoName()+">> "+this.getMethodName(ctx)+"("+ctx.getDefaultVO().getClassName()+" sample) {");
		code.ln(2,"Result<List<"+ctx.getPoName()+">> result=new Result<>();");
		code.ln(2,"List<"+ctx.getPoName()+"> list="+ctx.getIntfVarName()+".queryEntities(sample);");
		code.ln(2,"result.success(true).data(list);");
		code.ln(2,"return result;");
		code.ln(1,"}");
		
		builder.addImport(List.class);
	}

 

	@Override
	public void buildFeignMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * "+this.getApiComment(ctx));
		code.ln(1,"*/");
		code.ln(1,"@RequestMapping("+ctx.getAgentName()+"."+this.getUriConstName()+")");
		code.ln(1,"Result<List<"+ctx.getPoName()+">> "+this.getMethodName(ctx)+"("+ctx.getDefaultVO().getClassName()+" sample);");
		
		builder.addImport(List.class);
		builder.addImport(ctx.getControllerResult());
		builder.addImport(RequestMapping.class);
		builder.addImport(ctx.getPoFullName());
		builder.addImport(ctx.getDefaultVO().getFullName());
	}

	@Override
	public String getApiComment(Context ctx) {
		return "查询全部符合条件的"+getTopic(ctx);
	}
 

}
