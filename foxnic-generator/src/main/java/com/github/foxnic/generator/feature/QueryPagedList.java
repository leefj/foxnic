package com.github.foxnic.generator.feature;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.CodePoint;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.clazz.ControllerMethodReplacer;
import com.github.foxnic.generator.clazz.FileBuilder;
import com.github.foxnic.generatorV2.builder.model.PojoProperty;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;

public class QueryPagedList extends FeatureBuilder {

	
	@Override
	public String getMethodName(Context ctx) {
		return "queryPagedList";
	}
 
	@Override
	public void buildServiceInterfaceMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		 
//		code.ln(1,"");
//		makeJavaDoc(ctx, code);
//		code.ln(1,"PagedList<"+ctx.getPoName()+"> "+getMethodName(ctx)+"("+ctx.getDtoName()+" sample);");
//		builder.addImport(PagedList.class);
//		builder.addImport(ctx.getDtoFullName());
		
		
	}

	

	@Override
	public void buildServiceImplMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		
//		code.ln(1,"");
//		makeJavaDoc(ctx, code);
//		code.ln(1,"public PagedList<"+ctx.getPoName()+"> "+getMethodName(ctx)+"("+ctx.getDtoName()+" sample) {");
//		code.ln(2,"return dao.queryPagedEntities(sample, sample.getPageSize(), sample.getPageIndex());");
//		code.ln(1,"}");
//		builder.addImport(PagedList.class);
//		builder.addImport(ctx.getDtoFullName());
		
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
			for (DBColumnMeta cm : cms) {
				if(ctx.isDBTreatyFiled(cm)) continue;
				
				String example=ctx.getExampleStringValue(cm);
				if(!StringUtil.isBlank(example)) {
					example=" , example = \""+example+"\"";
				} else {
					example="";
				}

				String apiImplicitParamName=ctx.getDefaultVOMeta().getSimpleName()+"."+cm.getColumn().toUpperCase();
				String line="@ApiImplicitParam(name = "+apiImplicitParamName+" , value = \""+cm.getLabel()+"\" , required = false , dataTypeClass="+cm.getDBDataType().getType().getSimpleName()+".class"+example+"),";
				code.ln(2,line);

				i++;
				builder.addImport(cm.getDBDataType().getType().getName());

				codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".value", cm.getLabel());
				codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".required", "false");
				codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".dataTypeClass", cm.getDBDataType().getType().getSimpleName()+".class");
				codePoint.addApiImplicitParam(codePointLocation, line);
			}
			i=0;
			for (PojoProperty p : ctx.getDefaultVO().getProperties()) {
				i++;
				code.ln(2,"@ApiImplicitParam(name = "+ctx.getDefaultVOMeta().getSimpleName()+"."+p.getNameConstants()+" , value = \""+p.label()+"\" , required = false , dataTypeClass="+p.type().getSimpleName()+".class"+")"+(i<=ctx.getDefaultVO().getProperties().size()-1?",":""));
			}
			
			
			code.ln(1,"})");
		}

		code.ln(1, "@ApiOperationSupport(order=6)");
		builder.addImport(ApiOperationSupport.class);
		
		if(ctx.isEnableMicroService()) {
			code.ln(1,"@SentinelResource(value = "+ctx.getAgentName()+"."+this.getUriConstName()+", blockHandlerClass = { SentinelExceptionUtil.class },blockHandler = SentinelExceptionUtil.HANDLER)");
		}
		if(ctx.isEnableMicroService()) {
			code.ln(1,"@PostMapping("+ctx.getAgentName()+"."+this.getUriConstName()+")");
		} else {
			code.ln(1,"@PostMapping(\""+this.getMethodName(ctx)+"\")");
		}
		code.ln(1,"public  Result<PagedList<"+ctx.getPoName()+">> "+this.getMethodName(ctx)+"("+ctx.getDefaultVO().getSimpleName()+" sample) {");
		code.ln(2,"Result<PagedList<"+ctx.getPoName()+">> result=new Result<>();");
		code.ln(2,"PagedList<"+ctx.getPoName()+"> list="+ctx.getIntfVarName()+".queryPagedList(sample,sample.getPageSize(),sample.getPageIndex());");
		code.ln(2,"result.success(true).data(list);");
		code.ln(2,"return result;");
		code.ln(1,"}");
		
		builder.addImport(PagedList.class);

		//codePoint.sync();
	}

 

	@Override
	public void buildFeignMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * "+this.getApiComment(ctx));
		code.ln(1,"*/");
		code.ln(1,"@RequestMapping("+ctx.getAgentName()+"."+this.getUriConstName()+")");
		code.ln(1,"Result<PagedList<"+ctx.getPoName()+">> "+this.getMethodName(ctx)+"("+ctx.getDefaultVO().getSimpleName()+" sample);");
		
		builder.addImport(PagedList.class);
		builder.addImport(ctx.getControllerResult());
		builder.addImport(RequestMapping.class);
		builder.addImport(ctx.getPoFullName());
		builder.addImport(ctx.getDefaultVO().getFullName());
	}

	@Override
	public String getApiComment(Context ctx) {
		return "分页查询符合条件的"+getTopic(ctx);
	}
 

}
