package com.github.foxnic.generator.feature;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.Pojo;
import com.github.foxnic.generator.clazz.FileBuilder;
import com.github.foxnic.springboot.api.annotations.NotNull;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;

public class Update extends FeatureBuilder {

	
	@Override
	public String getMethodName(Context ctx) {
		return "update";
	}
 
	@Override
	public void buildServiceInterfaceMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		 
//		code.ln(1,"");
//		makeJavaDoc4Service(ctx, code);
//		code.ln(1,"boolean "+getMethodName(ctx)+"("+ctx.getPoName()+" "+ctx.getPoVarName()+" , SaveMode mode);");
//		builder.addImport(SaveMode.class);
//		builder.addImport(ctx.getPoFullName());
		
	}

	

	@Override
	public void buildServiceImplMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		
//		code.ln(1,"");
//		makeJavaDoc4Service(ctx, code);
//		code.ln(1,"public boolean "+getMethodName(ctx)+"("+ctx.getPoName()+" "+ctx.getPoVarName()+" , SaveMode mode) {");
//		code.ln(2,"return dao.updateEntity("+ctx.getPoVarName()+", mode);") ;
//		code.ln(1,"}");
//		builder.addImport(SaveMode.class);
//		builder.addImport(ctx.getPoFullName());
		
	}
 
//	private void makeJavaDoc4Service(Context ctx, CodeBuilder code) {
//		code.ln(1,"/**");
//		code.ln(1," * "+this.getApiComment(ctx));
//		code.ln(1," *");
//		code.ln(1," * @param "+ctx.getPoVarName()+" "+ctx.getPoName()+" 对象");
//		code.ln(1," * @param mode SaveMode,数据更新的模式");
//		code.ln(1," * @return 结果 , 如果失败返回 false，成功返回 true");
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
			List<DBColumnMeta> notNulls=new ArrayList<>();
			for (DBColumnMeta cm : cms) {
				if(ctx.isDBTreatyFiled(cm)) continue;
				
				String example=ctx.getExampleStringValue(cm);
				if(!StringUtil.isBlank(example)) {
					example=" , example = \""+example+"\"";
				} else {
					example="";
				}
				
				code.ln(2,"@ApiImplicitParam(name = "+ctx.getDefaultVO().getMetaName()+".PROP_"+cm.getColumn().toUpperCase()+" , value = \""+cm.getLabel()+"\" , required = "+!cm.isNullable()+" , dataTypeClass="+cm.getDBDataType().getType().getSimpleName()+".class"+example+")"+(i<=cms.size()-2?",":""));
				i++;
				builder.addImport(cm.getDBDataType().getType().getName());
				
				if(!cm.isNullable()) {
					notNulls.add(cm);
				}
			}
			code.ln(1,"})");
			
			for (DBColumnMeta cm : notNulls) {
				code.ln(1,"@NotNull(name = "+ctx.getDefaultVO().getMetaName()+".PROP_"+cm.getColumn().toUpperCase()+")");
				builder.addImport(NotNull.class);
			}
		}
		
		List<Pojo.Property> list=ctx.getDefaultVOProperties();
		String plist=StringUtil.join(BeanUtil.getFieldValueArray(list, "name", String.class), ",", "\"");
		code.ln(1, "@ApiOperationSupport(ignoreParameters = {"+plist+"},order=3)");
		builder.addImport(ApiOperationSupport.class);
		
		if(ctx.isEnableMicroService()) {
			code.ln(1,"@SentinelResource(value = "+ctx.getAgentName()+"."+this.getUriConstName()+", blockHandlerClass = { SentinelExceptionUtil.class },blockHandler = SentinelExceptionUtil.HANDLER)");
		}
		
		if(ctx.isEnableMicroService()) {
			code.ln(1,"@PostMapping("+ctx.getAgentName()+"."+this.getUriConstName()+")");
		} else {
			code.ln(1,"@PostMapping(\""+this.getMethodName(ctx)+"\")");
		}
		code.ln(1,"public  Result<"+ctx.getPoName()+"> "+this.getMethodName(ctx)+"("+ctx.getDefaultVO().getClassName()+" "+ctx.getDefaultVO().getVarName()+") {");
		code.ln(2,"Result<"+ctx.getPoName()+"> result=new Result<>();");
		code.ln(2,"boolean suc="+ctx.getIntfVarName()+".updateEntity("+ctx.getDefaultVO().getVarName()+",SaveMode.NOT_NULL_FIELDS);");
		code.ln(2,"result.success(suc);");
		code.ln(2,"return result;");
		code.ln(1,"}");
		
		builder.addImport(SaveMode.class);
	}

 

	@Override
	public void buildFeignMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * "+this.getApiComment(ctx));
		code.ln(1,"*/");
		
		code.ln(1,"@RequestMapping("+ctx.getAgentName()+"."+this.getUriConstName()+")");
		code.ln(1,"Result<"+ctx.getPoName()+"> "+this.getMethodName(ctx)+"("+ctx.getDefaultVO().getClassName()+" "+ctx.getDefaultVO().getVarName()+");");
		
		builder.addImport(List.class);
		builder.addImport(RequestMapping.class);
		
	}

	@Override
	public String getApiComment(Context ctx) {
		return "更新"+getTopic(ctx);
	}
 

}
