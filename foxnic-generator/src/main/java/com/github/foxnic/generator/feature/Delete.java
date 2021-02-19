package com.github.foxnic.generator.feature;

import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.ClassNames;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.clazz.FileBuilder;

public class Delete extends FeatureBuilder {

	@Override
	public String getMethodName(Context ctx) {
		List<String> fields=new ArrayList<String>();
		List<DBColumnMeta> pks = ctx.getTableMeta().getPKColumns();
		for (DBColumnMeta pk : pks) {
			fields.add(pk.getColumn());
		}
		String name="delete_by_"+StringUtil.join(fields,"_and_");
		name=convertor.getPropertyName(name);
		return name;
	}
 
	@Override
	public void buildServiceInterfaceMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		
		String params = makeParamStr(ctx,true);
		code.ln(1,"");
		makeJavaDoc(ctx, code);
		code.ln(1,"int "+this.getMethodName(ctx)+"Physical("+params+");");
		if(ctx.getTableMeta().isColumnExists(ctx.getDBTreaty().getDeletedField())) {
			code.ln(1,"");
			makeJavaDoc(ctx, code);
			code.ln(1,"int "+this.getMethodName(ctx)+"Logical("+params+");");
		}
	}

	

	@Override
	public void buildServiceImplMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		String params = makeParamStr(ctx,true);
		String paramsIn = makeParamStr(ctx,false);
		code.ln(1,"");
		makeJavaDoc(ctx, code);
		code.ln(1,"public int "+this.getMethodName(ctx)+"Physical("+params+") {");
//		code.ln(2,"return "+ctx.getMapperVarName()+"."+this.getMethodName(ctx)+"("+paramsIn+");");
		code.ln(1,"}");
		
		//如果有删除字段
		if(ctx.getTableMeta().isColumnExists(ctx.getDBTreaty().getDeletedField())) {
			code.ln(1,"");
			makeJavaDoc(ctx, code);
			code.ln(1,"public int "+this.getMethodName(ctx)+"Logical("+params+") {");
			code.ln(2,ctx.getPoName()+" "+ctx.getDtoVarName()+" = new "+ctx.getPoName()+"();");
			String getter;
			//设置主键
			for (DBColumnMeta pk : ctx.getTableMeta().getPKColumns()) {
				getter=convertor.getSetMethodName(pk.getColumn(), pk.getDBDataType());
				code.ln(2,ctx.getDtoVarName()+"."+getter+"(id);");
			}
			//删除控制字段
			DBColumnMeta cm=ctx.getTableMeta().getColumn(ctx.getDBTreaty().getDeletedField());
			getter=convertor.getSetMethodName(cm.getColumn(), cm.getDBDataType());
			code.ln(2,ctx.getDtoVarName()+"."+getter+"("+ctx.getDBTreaty().getTrueValue()+");");
//			code.ln(2,"return "+ctx.getMapperVarName()+".updateNotNullFields("+ctx.getPoVarName()+");");
			code.ln(1,"}");
		}
		
		
	}
	
	private String makeParamStr(Context ctx,boolean withType) {
		List<String> fields=new ArrayList<String>();
		List<DBColumnMeta> pks = ctx.getTableMeta().getPKColumns();
		for (DBColumnMeta pk : pks) {
			fields.add((withType?(pk.getDBDataType().getType().getSimpleName()+" "):"")+pk.getColumnVarName());
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
			code.ln(1," * @param "+pk.getColumnVarName()+" "+pk.getLabel()+(StringUtil.isBlank(pk.getDetail())?"":(" , 详情 : "+pk.getDetail())));
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
		
		code.ln(1,"@SentinelResource(value = "+ctx.getAgentName()+"."+this.getUriConstName()+", blockHandlerClass = { SentinelExceptionUtil.class },blockHandler = SentinelExceptionUtil.HANDLER)");
		code.ln(1,"@PostMapping("+ctx.getAgentName()+"."+this.getUriConstName()+")");
		code.ln(1,"public  Result<Integer> "+this.getMethodName(ctx)+"("+this.makeParamStr(ctx, true)+") {");
		code.ln(2,"Result<Integer> result=new Result<>();");
		if(ctx.getTableMeta().isColumnExists(ctx.getDBTreaty().getDeletedField())) {
			code.ln(2,"int i="+ctx.getIntfVarName()+"."+this.getMethodName(ctx)+"Logical("+this.makeParamStr(ctx, false)+");");
		} else {
			code.ln(2,"int i="+ctx.getIntfVarName()+"."+this.getMethodName(ctx)+"Physical("+this.makeParamStr(ctx, false)+");");
		}
		code.ln(2,"result.success(i==1).data(i);");
		code.ln(2,"return result;");
		code.ln(1,"}");
		
		builder.addImport(ClassNames.ApiOperation);
		builder.addImport(ClassNames.ApiImplicitParam);
		builder.addImport(ClassNames.ApiImplicitParams);
		
	}

	

	@Override
	public void buildFeignMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * "+this.getApiComment(ctx));
		code.ln(1,"*/");
		code.ln(1,"@RequestMapping("+ctx.getAgentName()+"."+this.getUriConstName()+")");
		code.ln(1,"Result<"+ctx.getPoName()+"> "+this.getMethodName(ctx)+"("+this.makeParamStr(ctx, true)+");");
		
	}

	@Override
	public String getApiComment(Context ctx) {
		return "按主键删除"+ctx.getTableMeta().getTopic();
	}
 

}
