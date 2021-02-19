package com.github.foxnic.generator.feature;

import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.ClassNames;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.clazz.FileBuilder;
import com.github.foxnic.sql.expr.ConditionExpr;

public class SelectById extends FeatureBuilder {

	@Override
	public String getMethodName(Context ctx) {
		List<String> fields=new ArrayList<String>();
		List<DBColumnMeta> pks = ctx.getTableMeta().getPKColumns();
		for (DBColumnMeta pk : pks) {
			fields.add(pk.getColumn());
		}
//		String name="select_by_"+StringUtil.join(fields,"_and_");
		String name="select_by_id";
		name=convertor.getPropertyName(name);
		return name;
	}
	
 
	@Override
	public void buildServiceInterfaceMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		
		String params = makeParamStr(builder,ctx,true);
		code.ln(1,"");
		makeJavaDoc(ctx, code);
		code.ln(1,ctx.getPoName()+" "+this.getMethodName(ctx)+"("+params+");");
		builder.addImport(ctx.getPoFullName());
	}

 
	@Override
	public void buildServiceImplMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		String params = makeParamStr(builder,ctx,true);
		List<DBColumnMeta> pks = ctx.getTableMeta().getPKColumns();
		code.ln(1,"");
		makeJavaDoc(ctx, code);
		code.ln(1,"public "+ctx.getPoName()+" "+this.getMethodName(ctx)+"("+params+") {");
		code.ln(2,"ConditionExpr ce=new ConditionExpr();");
		for (DBColumnMeta pk : pks) {
			code.ln(2,"ce.and(\""+pk.getColumn()+"=?\", "+pk.getColumnVarName()+");");
		}
		code.ln(2,ctx.getPoName()+" "+ctx.getPoVarName()+"=dao.queryEntity("+ctx.getPoName()+".class, ce);");
		code.ln(2,"return "+ctx.getPoVarName()+";");
		code.ln(1,"}");
		builder.addImport(ConditionExpr.class);
		builder.addImport(ctx.getPoFullName());
	}
	
	private String makeParamStr(FileBuilder builder, Context ctx,boolean withType) {
		List<String> fields=new ArrayList<String>();
		List<DBColumnMeta> pks = ctx.getTableMeta().getPKColumns();
		for (DBColumnMeta pk : pks) {
			fields.add((withType?(pk.getDBDataType().getType().getSimpleName()+" "):"")+pk.getColumnVarName());
			builder.addImport(pk.getDBDataType().getType());
		}
		String params=StringUtil.join(fields," , ");
		return params;
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
			code.ln(1," * @param "+pk.getColumnVarName()+" "+pk.getLabel()+detail);
		}
		code.ln(1," * @return 查询结果 , "+ctx.getPoName()+"对象");
		code.ln(1," */");
	}

	
	
	@Override
	public void buildControllerMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * "+this.getApiComment(ctx));
		code.ln(1,"*/");
		if(ctx.isEnableSwagger()) {
			code.ln(1,"@ApiOperation(value = \""+this.getApiComment(ctx)+"\")");
			code.ln(1,"@ApiImplicitParams({");
			List<DBColumnMeta> pks = ctx.getTableMeta().getPKColumns();
			int i=0;
			for (DBColumnMeta pk : pks) {
				
				String example=ctx.getExampleStringValue(pk);
				if(!StringUtil.isBlank(example)) {
					example=" , example = \""+example+"\"";
				} else {
					example="";
				}
				
				code.ln(2,"@ApiImplicitParam(name = \""+pk.getColumnVarName()+"\",value = \""+pk.getLabel()+"\" , required = true , dataTypeClass="+pk.getDBDataType().getType().getSimpleName()+".class"+example+")"+(i<=pks.size()-2?",":""));
				i++;
				builder.addImport(pk.getDBDataType().getType().getName());
			}
			code.ln(1,"})");
		}
		if(ctx.isEnableMicroService()) {
			code.ln(1,"@SentinelResource(value = "+ctx.getAgentName()+"."+this.getUriConstName()+", blockHandlerClass = { SentinelExceptionUtil.class },blockHandler = SentinelExceptionUtil.HANDLER)");
		}
		String result=StringUtil.getLastPart(ctx.getControllerResult(), ".");
		
		if(ctx.isEnableMicroService()) {
			code.ln(1,"@PostMapping("+ctx.getAgentName()+"."+this.getUriConstName()+")");
		} else {
			code.ln(1,"@PostMapping(\""+this.getMethodName(ctx)+"\")");
		}
		
		code.ln(1,"public  "+result+"<"+ctx.getPoName()+"> "+this.getMethodName(ctx)+"("+this.makeParamStr(builder,ctx, true)+") {");
		code.ln(2,result+"<"+ctx.getPoName()+"> result=new Result<>();");
		code.ln(2,ctx.getPoName()+" "+ctx.getDtoVarName()+"="+ctx.getIntfVarName()+"."+this.getMethodName(ctx)+"("+this.makeParamStr(builder,ctx, false)+");");
		code.ln(2,"result.success(true).data("+ctx.getDtoVarName()+");");
		code.ln(2,"return result;");
		code.ln(1,"}");
		
		if(ctx.isEnableSwagger()) {
			builder.addImport(ClassNames.ApiOperation);
			builder.addImport(ClassNames.ApiImplicitParam);
			builder.addImport(ClassNames.ApiImplicitParams);
		}
		
		builder.addImport(ctx.getControllerResult());
		
	}

	

	@Override
	public void buildFeignMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * "+this.getApiComment(ctx));
		code.ln(1,"*/");
		code.ln(1,"@RequestMapping("+ctx.getAgentName()+"."+this.getUriConstName()+")");
		code.ln(1,"Result<"+ctx.getPoName()+"> "+this.getMethodName(ctx)+"("+this.makeParamStr(builder,ctx, true)+");");
		
	}

	@Override
	public String getApiComment(Context ctx) {
		return "按主键获取"+ctx.getTableMeta().getTopic();
	}
 

}
