package com.github.foxnic.generator.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RequestMapping;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.CodePoint;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.clazz.ControllerMethodReplacer;
import com.github.foxnic.generator.clazz.FileBuilder;
import com.github.foxnic.generatorV2.builder.model.PojoProperty;
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
		
		CodePoint codePoint=ctx.getCodePoint();
		ControllerMethodReplacer controllerMethodReplacer=null;
		String methodName=this.getMethodName(ctx);
		String codePointLocation=ctx.getCtrlFullName()+"."+methodName;
		try {
			if(ctx.isEnableSwagger() &&  builder.getSourceFile()!=null && builder.getSourceFile().exists()) {
				controllerMethodReplacer=new ControllerMethodReplacer(codePoint,ctx.getCtrlFullName(),methodName,ctx.getDefaultVO().getFullName());
				codePoint.addReplacer(controllerMethodReplacer);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("控制器文件存在，但无法找到类型,"+builder.getSourceFile().getName(),e);
		}
		
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * "+this.getApiComment(ctx));
		code.ln(1,"*/");
		if(ctx.getControllerMethodAnnotiationPlugin()!=null) {
			ctx.getControllerMethodAnnotiationPlugin().addMethodAnnotiation(ctx,this,builder,code);
		}
		if(ctx.isEnableSwagger()) {
			
			code.ln(1,"@ApiOperation(value = \""+this.getApiComment(ctx)+"\")");
			codePoint.set(codePointLocation+"@ApiOperation.value", this.getApiComment(ctx));
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
				
				String apiImplicitParamName=ctx.getDefaultVOMeta().getSimpleName()+"."+cm.getColumn().toUpperCase();
				String line="@ApiImplicitParam(name = "+apiImplicitParamName+" , value = \""+cm.getLabel()+"\" , required = "+!cm.isNullable()+" , dataTypeClass="+cm.getDBDataType().getType().getSimpleName()+".class"+example+")"+(i<=cms.size()-2?",":"");
				code.ln(2,line);
				
				codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".value", cm.getLabel());
				codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".required", (!cm.isNullable())+"");
				codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".dataTypeClass", cm.getDBDataType().getType().getSimpleName()+".class");
				codePoint.addApiImplicitParam(codePointLocation, line);
				
				i++;
				builder.addImport(cm.getDBDataType().getType().getName());
				builder.addImport(ctx.getDefaultVOMeta().getFullName());
				
				if(cm.isPK()) {
					notNulls.add(cm);
				}
			}
			code.ln(1,"})");
			
			for (DBColumnMeta cm : notNulls) {
				code.ln(1,"@NotNull(name = "+ctx.getDefaultVOMeta().getSimpleName()+"."+cm.getColumn().toUpperCase()+")");
				builder.addImport(NotNull.class);
			}
		}
		
		List<PojoProperty> list=ctx.getDefaultVO().getProperties();
//		String plist=StringUtil.join(BeanUtil.getFieldValueArray(list, "name", String.class), ",", "\"");
		String plist=list.stream().map(p->{return ctx.getDefaultVO().getSimpleName()+"."+p.getNameConstants();}).collect(Collectors.joining(" , "));
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
		code.ln(1,"public  Result<"+ctx.getPoName()+"> "+this.getMethodName(ctx)+"("+ctx.getDefaultVO().getSimpleName()+" "+ctx.getDefaultVO().getVar()+") {");
		code.ln(2,"Result<"+ctx.getPoName()+"> result=new Result<>();");
		code.ln(2,"boolean suc="+ctx.getIntfVarName()+".update("+ctx.getDefaultVO().getVar()+",SaveMode.NOT_NULL_FIELDS);");
		code.ln(2,"result.success(suc);");
		code.ln(2,"return result;");
		code.ln(1,"}");
		
		builder.addImport(SaveMode.class);
		
		//codePoint.sync();
	}

 

	@Override
	public void buildFeignMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * "+this.getApiComment(ctx));
		code.ln(1,"*/");
		
		code.ln(1,"@RequestMapping("+ctx.getAgentName()+"."+this.getUriConstName()+")");
		code.ln(1,"Result<"+ctx.getPoName()+"> "+this.getMethodName(ctx)+"("+ctx.getDefaultVO().getSimpleName()+" "+ctx.getDefaultVO().getVar()+");");
		
		builder.addImport(List.class);
		builder.addImport(RequestMapping.class);
		
	}

	@Override
	public String getApiComment(Context ctx) {
		return "更新"+getTopic(ctx);
	}
 

}
