package com.github.foxnic.generator.feature;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.clazz.FileBuilder;
import com.github.foxnic.sql.meta.DBDataType;

public class Update extends FeatureBuilder {

	
	@Override
	public String getMethodName(Context ctx) {
		return "update";
	}
	
	@Override
	public void buildRawXMLNode(Context ctx,CodeBuilder code) {
 
		List<String> fields=new ArrayList<String>();
		List<DBColumnMeta> cms = ctx.getTableMeta().getColumns();
		for (DBColumnMeta cm : cms) {
			fields.add("t."+cm.getColumn());
		}
		
		boolean useGeneratedKeys=false;
		String keyPropertyPart="";
		if(ctx.getTableMeta().getPKColumns().size()==1) {
			DBColumnMeta pk=ctx.getTableMeta().getPKColumns().get(0);
			useGeneratedKeys=pk.isAutoIncrease();
			if(useGeneratedKeys) {
				keyPropertyPart="keyProperty=\""+pk.getColumn()+"\"";
			}
		}
		
		
		code.ln(1,"");
		code.ln(1,"<!-- 更新非空字段 -->");
		code.ln(1,"<update id=\""+getMethodName(ctx)+"NotNullFields\" parameterType=\""+ctx.getPoName()+"\" useGeneratedKeys=\""+useGeneratedKeys+"\" "+keyPropertyPart+">");
		code.ln(2,"update "+ctx.getTableName()+" set ");
		int i=0;
		String comma="";
		for (DBColumnMeta cm : cms) {
			if(cm.isPK()) continue;
			comma="";
			if(i<cms.size()-1) comma=" ,";
			String checkEmptyString=" and "+cm.getColumnVarName()+" != ''";
			if(cm.getDBDataType() != DBDataType.STRING) {
				checkEmptyString="";
			}
			String jdbcType=cm.getJDBCDataType();
			if(jdbcType!=null) {
				jdbcType=" , jdbcType="+jdbcType.toUpperCase();
			} else {
				jdbcType="";
			}
			code.ln(2,"<if test=\""+cm.getColumnVarName()+" != null"+checkEmptyString+"\"> "+cm.getColumn() +" = "+ "#{ "+cm.getColumnVarName()+jdbcType+" }" +comma+" </if>");
			i++;
		}
		String condition = ctx.makePKConditionSQL(ctx.getTableMeta().getPKColumns(),null);
		code.ln(2,condition);
		code.ln(1,"</update>");
		
		
		code.ln(1,"");
		code.ln(1,"<!-- 更新所有字段 -->");
		code.ln(1,"<update id=\""+getMethodName(ctx)+"AllFields\" parameterType=\""+ctx.getPoName()+"\" useGeneratedKeys=\""+useGeneratedKeys+"\" "+keyPropertyPart+">");
		code.ln(2,"update "+ctx.getTableName()+" set ");
		i=0;
		comma="";
		for (DBColumnMeta cm : cms) {
			if(cm.isPK()) continue;
			comma="";
			if(i<cms.size()-1) comma=" ,";
			String checkEmptyString=" and "+cm.getColumnVarName()+" != ''";
			if(cm.getDBDataType() != DBDataType.STRING) {
				checkEmptyString="";
			}
			String jdbcType=cm.getJDBCDataType();
			if(jdbcType!=null) {
				jdbcType=" , jdbcType="+jdbcType.toUpperCase();
			} else {
				jdbcType="";
			}
			code.ln(2,cm.getColumn() +" = "+ "#{ "+cm.getColumnVarName()+jdbcType+" }" +comma);
			i++;
		}
		condition = ctx.makePKConditionSQL(ctx.getTableMeta().getPKColumns(),null);
		code.ln(2,condition);
		code.ln(1,"</update>");
		
	}

	@Override
	public void buildRawMapperMethod(FileBuilder builder,Context ctx, CodeBuilder code) {
		
		code.ln(1,"");
		makeJavaDoc(ctx, code,"更新除主键外的所有字段");
		code.ln(1,"int "+getMethodName(ctx)+"AllFields("+ctx.getPoName()+" "+ctx.getPoVarName()+");");
		
		code.ln(1,"");
		makeJavaDoc(ctx, code,"更新除主键外的非空(null)字段");
		code.ln(1,"int "+getMethodName(ctx)+"NotNullFields("+ctx.getPoName()+" "+ctx.getPoVarName()+");");
		
		builder.addImport(List.class);
		builder.addImport(ctx.getPoFullName());
	}

	@Override
	public void buildServiceInterfaceMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		 
		code.ln(1,"");
		makeJavaDoc4Service(ctx, code);
		code.ln(1,"int "+getMethodName(ctx)+"("+ctx.getPoName()+" "+ctx.getPoVarName()+" , boolean withNulls);");
		builder.addImport(List.class);
		builder.addImport(ctx.getPoFullName());
		
	}

	

	@Override
	public void buildServiceImplMethod(FileBuilder builder, Context ctx, CodeBuilder code) {
		
		code.ln(1,"");
		makeJavaDoc4Service(ctx, code);
		code.ln(1,"public int "+getMethodName(ctx)+"("+ctx.getPoName()+" "+ctx.getPoVarName()+" , boolean withNulls) {");
		code.ln(2,"return withNulls?"+ctx.getMapperVarName()+"."+getMethodName(ctx)+"AllFields("+ctx.getPoVarName()+") : " +ctx.getMapperVarName()+"."+getMethodName(ctx)+"NotNullFields("+ctx.getPoVarName()+")" +  ";") ;
		code.ln(1,"}");
		builder.addImport(List.class);
		builder.addImport(ctx.getPoFullName());
		
	}
	
	
	private void makeJavaDoc(Context ctx, CodeBuilder code,String detail) {
		code.ln(1,"/**");
		code.ln(1," * "+this.getApiComment(ctx)+(detail==null?"":(" , "+detail)));
		code.ln(1," *");
		code.ln(1," * @param "+ctx.getPoVarName()+" "+ctx.getPoName()+" 对象");
		code.ln(1," * @return 结果 , 如果返回 0 失败，返回 1 成功");
		code.ln(1," */");
	}
	
	private void makeJavaDoc4Service(Context ctx, CodeBuilder code) {
		code.ln(1,"/**");
		code.ln(1," * "+this.getApiComment(ctx));
		code.ln(1," *");
		code.ln(1," * @param "+ctx.getPoVarName()+" "+ctx.getPoName()+" 对象");
		code.ln(1," * @param withNulls 若 true 则更新所有字段，包括空(null)值字段；若 false 则仅更新非空(null)字段");
		code.ln(1," * @return 结果 , 如果返回 0 失败，返回 1 成功");
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
			
			code.ln(2,"@ApiImplicitParam(name = \""+cm.getColumnVarName()+"\",value = \""+cm.getLabel()+"\" , required = "+!cm.isNullable()+" , dataTypeClass="+cm.getDBDataType().getType().getSimpleName()+".class"+example+")"+(i<=cms.size()-2?",":""));
			i++;
			builder.addImport(cm.getDBDataType().getType().getName());
		}
		code.ln(1,"})");
		
		code.ln(1,"@SentinelResource(value = "+ctx.getAgentName()+"."+this.getUriConstName()+", blockHandlerClass = { SentinelExceptionUtil.class },blockHandler = SentinelExceptionUtil.HANDLER)");
		code.ln(1,"@PostMapping("+ctx.getAgentName()+"."+this.getUriConstName()+")");
		code.ln(1,"public  Result<"+ctx.getPoName()+"> "+this.getMethodName(ctx)+"("+ctx.getVoName()+" "+ctx.getVoVarName()+") {");
		code.ln(2,"Result<"+ctx.getPoName()+"> result=new Result<>();");
		code.ln(2,"int i="+ctx.getIntfVarName()+"."+this.getMethodName(ctx)+"("+ctx.getVoVarName()+",false);");
		code.ln(2,"result.success(i>0).data("+ctx.getVoVarName()+");");
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
		code.ln(1,"Result<"+ctx.getPoName()+"> "+this.getMethodName(ctx)+"("+ctx.getVoName()+" "+ctx.getVoVarName()+");");
		
		builder.addImport(List.class);
		builder.addImport(RequestMapping.class);
		
	}

	@Override
	public String getApiComment(Context ctx) {
		return "更新"+ctx.getTableMeta().getTopic();
	}
 

}
