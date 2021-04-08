package com.github.foxnic.generator.feature;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.CodePoint;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.Pojo;
import com.github.foxnic.generator.clazz.ControllerMethodReplacer;
import com.github.foxnic.generator.clazz.FileBuilder;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

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

				String apiImplicitParamName=ctx.getDefaultVO().getMetaName()+".PROP_"+cm.getColumn().toUpperCase();
				String line="@ApiImplicitParam(name = "+apiImplicitParamName+" , value = \""+cm.getLabel()+"\" , required = false , dataTypeClass="+cm.getDBDataType().getType().getSimpleName()+".class"+example+")"+(i<=cms.size()-2?",":"");
				code.ln(2,line);

				i++;
				builder.addImport(cm.getDBDataType().getType().getName());

				codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".value", cm.getLabel());
				codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".required", "false");
				codePoint.set(codePointLocation+"@ApiImplicitParam."+apiImplicitParamName+".dataTypeClass", cm.getDBDataType().getType().getSimpleName()+".class");
				codePoint.addApiImplicitParam(codePointLocation, line);

			}
			code.ln(1,"})");
		}
		
		List<Pojo.Property> list=ctx.getDefaultVOProperties();
//		String plist=StringUtil.join(BeanUtil.getFieldValueArray(list, "name", String.class), ",", "\"");
		String plist=list.stream().map(p->{return ctx.getDefaultVO().getMetaName()+"."+p.getNameConst();}).collect(Collectors.joining(" , "));
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
		code.ln(2,"List<"+ctx.getPoName()+"> list="+ctx.getIntfVarName()+".queryList(sample);");
		code.ln(2,"result.success(true).data(list);");
		code.ln(2,"return result;");
		code.ln(1,"}");
		
		builder.addImport(List.class);

		//codePoint.sync();
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
