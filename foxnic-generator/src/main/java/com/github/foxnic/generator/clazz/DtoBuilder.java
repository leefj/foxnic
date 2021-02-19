package com.github.foxnic.generator.clazz;

import java.util.Map;

import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.generator.ClassNames;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.DtoConfig;
import com.github.foxnic.generator.DtoConfig.Property;

public class DtoBuilder extends FileBuilder {

	private  DtoConfig cfg;
	private String voName;
	
	public DtoBuilder(Context ctx,DtoConfig cfg) {
		super(ctx);
		this.cfg=cfg;
	}
 
	public void build() {
		
		String superClass=cfg.getSuperClass();
		if(superClass==null) {
			superClass=ctx.getPoFullName();
		}
		String superClassShotName=StringUtil.getLastPart(superClass, ".");
		
		code.ln("package "+ctx.getDtoPackage()+";");
		code.ln("");
//		if(!StringUtil.isBlank(superClass)) {
//			code.ln("import "+superClass+";");
//		}
		code.ln("import java.beans.Transient;");
		code.ln("");
		//加入注释
		code.ln("/**");
		super.appendAuthorAndTime();
		code.ln("*/");
		code.ln("");
		
		
		
		code.ln("public class "+voName+( superClass.length()==0?"":(" extends "+superClassShotName))+" {");
		
		code.ln("");
		code.ln(1,"private static final long serialVersionUID = 1L;");
 
		
		for (Property prop : cfg.getProperties()) {
			buildProperty(prop);
		}
		
		for (Property prop : cfg.getProperties()) {
			buildGetter(prop);
		}
		
		for (Property prop : cfg.getProperties()) {
			buildSetter(prop);
		}
		
		
		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将自己转换成"+ctx.getPoName());
		code.ln(1," * @return "+ctx.getPoName()+" , 转换好的 "+ctx.getPoName()+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public "+ctx.getPoName()+" toPO() {");
		code.ln(2,"return EntityContext.create("+ctx.getPoName()+".class, this);");
		code.ln(1,"}");
		this.addImport(ctx.getPoFullName());
		
		
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
		code.ln(1,"public <T> T toAny(Class<T> pojoType) {");
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
 
		
		
		String prop=ctx.getDtoVarName();
		 
		
			
		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将 "+ctx.getPoName()+" 转换成 "+voName);
		code.ln(1," * @param pojo 任意 Pojo 对象");
		code.ln(1," * @return "+voName+" , 转换好的的 "+voName+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+voName+" createFrom(Object pojo) {");
		code.ln(2,"if(pojo==null) return null;");
		code.ln(2,voName+" vo=new "+voName+"();");
		code.ln(2,"EntityContext.copyProperties(vo, pojo);");
		code.ln(2,"return vo;");
		code.ln(1,"}");
 
		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将 Map 转换成 "+voName); 
		code.ln(1," * @param "+prop+"Map 包含实体信息的 Map 对象");
		code.ln(1," * @return "+voName+" , 转换好的的 "+voName+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+voName+" createFrom(Map<String,Object> "+prop+"Map) {");
		code.ln(2,"if("+prop+"Map==null) return null;");
		code.ln(2,voName+" vo=new "+voName+"();");
		code.ln(2,"EntityContext.copyProperties(vo, "+prop+"Map);");
		code.ln(2,"return vo;");
		code.ln(1,"}");
			
//		this.addImport("java.util.Map.Entry");
		this.addImport(Map.class);
		this.addImport(EntityContext.class);
		
		
		
		code.ln("}");
	 
	}
	
	private void buildProperty(Property prop) {
		 
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * "+prop.getLabel()+(prop.hasNote()?"：":"")+prop.getNote());
		code.ln(1,"*/");
		if(ctx.isEnableSwagger()) {
			code.ln(1,"@ApiModelProperty(value=\""+prop.getLabel()+"\" , dataType=\""+prop.getType().getSimpleName()+"\")");
			this.addImport(ClassNames.ApiModelProperty);
		}
		code.ln(1, "private "+prop.getType().getSimpleName()+" "+prop.getName()+" = null;");
		this.addImport(prop.getType());
	}
	
	
	private void buildGetter(Property prop) {
		 
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 获得 "+prop.getLabel()+(prop.hasNote()?"：":"")+prop.getNote());
		code.ln(1," * @return "+prop.getLabel());
		code.ln(1,"*/");
		
		String getter="";
		if(DataParser.isBooleanType(prop.getType())) {
			if(prop.getName().startsWith("is") && prop.getName().length()>2) {
				String t=prop.getName().substring(2, 3);
				if(Character.isUpperCase(t.charAt(0))) {
					getter="is"+prop.getName().substring(2);
				}
			} else {
				getter= "is"+prop.getName().substring(0,1).toUpperCase()+prop.getName().substring(1);
			}
		}
		
		if(getter.length()==0) {
			getter= "get"+prop.getName().substring(0,1).toUpperCase()+prop.getName().substring(1);
		}
		
		code.ln(1, "public "+prop.getType().getSimpleName()+" "+getter +"() {");
		code.ln(2,"return this."+prop.getName()+";");
		code.ln(1, "}");
	}
	
	private void buildSetter(Property prop) {
		
		String setter="";
		if(DataParser.isBooleanType(prop.getType())) {
			if(prop.getName().startsWith("is") && prop.getName().length()>2) {
				String t=prop.getName().substring(2, 3);
				if(Character.isUpperCase(t.charAt(0))) {
					setter="set"+prop.getName().substring(2);
				}
			}
		}
		if(setter.length()==0) {
			setter= "set"+prop.getName().substring(0,1).toUpperCase()+prop.getName().substring(1);
		}

		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 设置 "+prop.getLabel()+(prop.hasNote()?"：":"")+prop.getNote());
		code.ln(1," * @param "+prop.getName()+" "+prop.getLabel());
		code.ln(1,"*/");
		code.ln(1, "public void "+setter +"("+prop.getType().getSimpleName()+" "+prop.getName()+") {");
		code.ln(2,"this."+prop.getName()+"="+prop.getName()+";");
		code.ln(1, "}");
	}
 
	@Override
	public void buildAndUpdate() {
		voName = cfg.getName();
		if (StringUtil.isBlank(voName)) {
			voName = ctx.getVoName();
		}
		String[] parts = ctx.getDtoFullName().split("\\.");
		parts[parts.length - 1] = voName;
		String fullVoName = StringUtil.join(parts, ".");
		this.buildAndUpdateJava(ctx.getDomainProject().getMainSourceDir(), fullVoName);
	}
}
