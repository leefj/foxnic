package com.github.foxnic.generator.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RequestMapping;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.CodePoint;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.Pojo;
import com.github.foxnic.generator.clazz.ControllerMethodReplacer;
import com.github.foxnic.generator.clazz.FileBuilder;
import com.github.foxnic.springboot.api.annotations.NotNull;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;

public class Insert extends FeatureBuilder {

	
	@Override
	public String getMethodName(Context ctx) {
		return "insert";
	}
	
	 

	@Override
	public void buildServiceInterfaceMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		 
//		code.ln(1,"");
//		makeJavaDoc(ctx, code);
//		code.ln(1,"boolean "+getMethodName(ctx)+"("+ctx.getPoName()+" "+ctx.getPoVarName()+");");
//		builder.addImport(List.class);
//		builder.addImport(ctx.getPoFullName());
		
	}

	

	@Override
	public void buildServiceImplMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		
//		code.ln(1,"");
//		makeJavaDoc(ctx, code);
//		code.ln(1,"public boolean "+getMethodName(ctx)+"("+ctx.getPoName()+" "+ctx.getPoVarName()+") {");
//		code.ln(2,"return dao.insertEntity("+ctx.getPoVarName()+");");
//		code.ln(1,"}");
//		builder.addImport(List.class);
//		builder.addImport(ctx.getPoFullName());
		
	}
	
	
//	private void makeJavaDoc(Context ctx, CodeBuilder code) {
//		code.ln(1,"/**");
//		code.ln(1," * "+this.getApiComment(ctx));
//		code.ln(1," *");
//		code.ln(1," * @param "+ctx.getPoVarName()+" "+ctx.getPoName()+" 对象");
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
				//如果自增，则无需作为参数传入
				if(cm.isAutoIncrease()) continue;
				String example=ctx.getExampleStringValue(cm);
				if(!StringUtil.isBlank(example)) {
					example=" , example = \""+example+"\"";
				} else {
					example="";
				}
				String apiImplicitParamName=ctx.getDefaultVO().getMetaName()+".PROP_"+cm.getColumn().toUpperCase();
				code.ln(2,"@ApiImplicitParam(name = "+apiImplicitParamName+" , value = \""+cm.getLabel()+"\" , required = "+!cm.isNullable()+" , dataTypeClass="+cm.getDBDataType().getType().getSimpleName()+".class"+example+")"+(i<=cms.size()-2?",":""));
				
				codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".value", cm.getLabel());
				codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".required", (!cm.isNullable())+"");
				codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".dataTypeClass", cm.getDBDataType().getType().getSimpleName()+".class");

				i++;
				builder.addImport(cm.getDBDataType().getType().getName());
				builder.addImport(ctx.getDefaultVO().getMetaFullName());
				
				if(!cm.isNullable()) {
					notNulls.add(cm);
				}
				
			}
 
			code.ln(1,"})");
			
			for (DBColumnMeta cm : notNulls) {
				//如果自增，则无需由前端指定
				if(cm.isAutoIncrease()) continue;
				code.ln(1,"@NotNull(name = "+ctx.getDefaultVO().getMetaName()+".PROP_"+cm.getColumn().toUpperCase()+")");
				builder.addImport(NotNull.class);
			}
			
		}
		
		List<Pojo.Property> list=ctx.getDefaultVOProperties();
		//String plist=list.stream().map(p->{return ctx.getDefaultVO().getMetaName()+"."+p.getNameConst();}).collect(Collectors.joining(" , "));
//		String plist=StringUtil.join(BeanUtil.getFieldValueArray(list, "name", String.class), ",", "\"");
//		code.ln(1, "@ApiOperationSupport(ignoreParameters = {"+plist+"},order=1)");
		code.ln(1, "@ApiOperationSupport(order=1)");
		builder.addImport(ApiOperationSupport.class);
		builder.addImport(ctx.getDefaultVO().getMetaFullName());
		
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
		code.ln(2,"boolean suc="+ctx.getIntfVarName()+".insertEntity("+ctx.getDefaultVO().getVarName()+");");
		code.ln(2,"result.success(suc);");
		code.ln(2,"return result;");
		code.ln(1,"}");
		
		builder.addImport(List.class);
		
		codePoint.sync();
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
		return "添加"+getTopic(ctx);
	}
 

}
