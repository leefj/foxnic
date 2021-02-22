package com.github.foxnic.generator.clazz;

import java.util.List;
import java.util.Map;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.ClassNames;
import com.github.foxnic.generator.Context;

public class PoBuilder extends FileBuilder {
 
 
	public PoBuilder(Context cfg) {
		super(cfg);
	}

	protected void build() {
		
		code.ln("package "+ctx.getPoPackage()+";");
		code.ln("");
		code.ln("import javax.persistence.Table;");
		code.ln("import java.beans.Transient;");
		code.ln("");
		
		//加入注释
		code.ln("/**");
		super.appendAuthorAndTime();
		code.ln(" * 此文件由工具自动生成，请勿修改。若表结构变动，请使用工具重新生成。");
		code.ln("*/");
		code.ln("");
		
		
		code.ln("@Table(name = \""+this.ctx.getTableName()+"\")");
		code.ln("public class "+ctx.getPoName()+" extends Entity {");
		
		code.ln("");
		code.ln(1,"private static final long serialVersionUID = 1L;");
 
		
		List<DBColumnMeta> cms=ctx.getTableMeta().getColumns();
		
		for (DBColumnMeta cm : cms) {
			buildProperty(cm);
		}
		
		for (DBColumnMeta cm : cms) {
			buildGetter(cm);
			buildSetter(cm);
		}
		
		
		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将自己转换成指定类型的PO");
		code.ln(1," * @param poType  PO类型");
		code.ln(1," * @return "+ctx.getPoName()+" , 转换好的 "+ctx.getPoName()+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public <T extends Entity> T toPO(Class<T> poType) {");
		code.ln(2,"return EntityContext.create(poType, this);");
		code.ln(1,"}");
		this.addImport(Entity.class);
		
		
		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将自己转换成任意指定类型");
		code.ln(1," * @param pojoType  Pojo类型");
		code.ln(1," * @return "+ctx.getPoName()+" , 转换好的 PoJo 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public <T> T toPojo(Class<T> pojoType) {");
		code.ln(2,"if(Entity.class.isAssignableFrom(pojoType)) {");
		code.ln(3,"return (T)this.toPO((Class<Entity>)pojoType);");
		code.ln(2,"}");
		code.ln(2,"try {");
		code.ln(3,"T pojo=pojoType.newInstance();");
		code.ln(3,"EntityContext.copyProperties(pojo, this);");
		code.ln(3,"return pojo;");
		code.ln(2,"} catch (Exception e) {");
		code.ln(3,"throw new RuntimeException(e);");
		code.ln(2,"}");
		code.ln(1,"}");
		
		
		String prop=ctx.getDefaultVO().getVarName();
		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将 Map 转换成 "+ctx.getPoName()); 
		code.ln(1," * @param "+prop+"Map 包含实体信息的 Map 对象");
		code.ln(1," * @return "+ctx.getPoName()+" , 转换好的的 "+ctx.getDefaultVO().getClassName()+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+ctx.getPoName()+" createFrom(Map<String,Object> "+prop+"Map) {");
		code.ln(2,"if("+prop+"Map==null) return null;");
		code.ln(2,ctx.getPoName()+" po = EntityContext.create("+ctx.getPoName()+".class, "+prop+"Map);");
		code.ln(2,"return po;");
		code.ln(1,"}");
		
		
//		this.addImport("java.util.Map.Entry");
		this.addImport(Map.class);
		this.addImport(EntityContext.class);
		
		
		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将 Pojo 转换成 "+ctx.getPoName()); 
		code.ln(1," * @param pojo 包含实体信息的 Pojo 对象");
		code.ln(1," * @return "+ctx.getPoName()+" , 转换好的的 "+ctx.getDefaultVO().getClassName()+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+ctx.getPoName()+" createFrom(Object pojo) {");
		code.ln(2,"if(pojo==null) return null;");
		code.ln(2,ctx.getPoName()+" po = EntityContext.create("+ctx.getPoName()+".class,pojo);");
		code.ln(2,"return po;");
		code.ln(1,"}");
		
		
 
		code.ln("");
		code.ln("}");
		 
	}
	
	
	
	private void buildProperty(DBColumnMeta cm) {
		
 
		
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * "+cm.getLabel()+"<br>");
		code.ln(1," * "+cm.getDetail());
		code.ln(1,"*/");
		if(cm.isPK()) {
			code.ln(1, "@Id");
			this.addImport(Id.class);
		}
		if(cm.isAutoIncrease()) {
			code.ln(1, "@GeneratedValue(strategy=GenerationType.IDENTITY)");
			this.addImport(GeneratedValue.class);
			this.addImport(GenerationType.class);
		}
		
		String example=ctx.getExampleStringValue(cm);
		if(!StringUtil.isBlank(example)) {
			example=" , example = \""+example+"\"";
		} else {
			example="";
		}
		
		
		if(ctx.isEnableSwagger()) {
			if(ctx.isDBTreatyFiled(cm)) {
				code.ln(1,"@ApiModelProperty(hidden = true , required = "+!cm.isNullable()+",notes = \""+cm.getLabel()+"\""+example+")");
			}else {
				code.ln(1,"@ApiModelProperty(required = "+!cm.isNullable()+",notes = \""+cm.getLabel()+"\""+example+")");
			}
			this.addImport(ClassNames.ApiModelProperty);
		}
		
		code.ln(1, "private "+cm.getDBDataType().getType().getSimpleName()+" "+cm.getColumnVarName()+" = null;");
		this.addImport(cm.getDBDataType().getType().getName());
		
		
	}
	
	
	private void buildGetter(DBColumnMeta cm) {
		
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 获得 "+cm.getLabel()+"<br>");
		if(!StringUtil.isBlank(cm.getDetail())) {
			code.ln(1," * 属性说明 : "+cm.getDetail());
		}
		code.ln(1," * @return "+cm.getDBDataType().getType().getSimpleName()+" , "+cm.getLabel());
		code.ln(1,"*/");
		code.ln(1, "public "+cm.getDBDataType().getType().getSimpleName()+" "+convertor.getGetMethodName(cm.getColumn(), cm.getDBDataType()) +"() {");
		code.ln(2,"return this."+cm.getColumnVarName()+";");
		code.ln(1, "}");
		
	}
	
	private void buildSetter(DBColumnMeta cm) {
		 
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 设置 "+cm.getLabel());
		code.ln(1," * @param "+cm.getColumnVarName()+" "+cm.getLabel());
		code.ln(1,"*/");
		code.ln(1, "public void "+convertor.getSetMethodName(cm.getColumn(), cm.getDBDataType()) +"("+cm.getDBDataType().getType().getSimpleName()+" "+cm.getColumnVarName()+") {");
		code.ln(2,"this."+cm.getColumnVarName()+"="+cm.getColumnVarName()+";");
		code.ln(1, "}");
		
	}

	@Override
	public void buildAndUpdate() {
		this.buildAndUpdateJava(ctx.getDomainProject().getMainSourceDir(),ctx.getPoFullName());
	}
 
	
	
	 
	
}
