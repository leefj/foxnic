package com.github.foxnic.generator.clazz;

import java.util.Map;

import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.generator.ClassNames;
import com.github.foxnic.generator.Context;
import com.github.foxnic.generator.Pojo;
import com.github.foxnic.generator.Pojo.Property;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;
import com.github.foxnic.sql.meta.DBDataType;

public class PojoBuilder extends FileBuilder {

	private  Pojo cfg;
	private DefaultNameConvertor nc=new DefaultNameConvertor(false);
	
	public PojoBuilder(Context ctx,Pojo cfg) {
		super(ctx);
		this.cfg=cfg;
	}
 
	public void build() {
		
		String superClass=cfg.getSuperClass();
		if(superClass==null) {
			superClass=ctx.getPoFullName();
		}
		String superClassShotName=StringUtil.getLastPart(superClass, ".");
		
		code.ln("package "+cfg.getPackage()+";");
		code.ln("");
 
		code.ln("import java.beans.Transient;");
		code.ln("");
		//加入注释
		code.ln("/**");
		super.appendAuthorAndTime();
		code.ln("*/");
		code.ln("");
		
		
		
		code.ln("public class "+cfg.getClassName()+( superClass.length()==0?"":(" extends "+superClassShotName))+" {");
		
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
 
		
		
		String prop=cfg.getVarName();
		String pojoName=cfg.getClassName();
		
			
		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将 "+ctx.getPoName()+" 转换成 "+pojoName);
		code.ln(1," * @param pojo 任意 Pojo 对象");
		code.ln(1," * @return "+pojoName+" , 转换好的的 "+pojoName+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+pojoName+" createFrom(Object pojo) {");
		code.ln(2,"if(pojo==null) return null;");
		code.ln(2,pojoName+" vo=new "+pojoName+"();");
		code.ln(2,"EntityContext.copyProperties(vo, pojo);");
		code.ln(2,"return vo;");
		code.ln(1,"}");
 
		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将 Map 转换成 "+pojoName); 
		code.ln(1," * @param "+prop+"Map 包含实体信息的 Map 对象");
		code.ln(1," * @return "+pojoName+" , 转换好的的 "+pojoName+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+pojoName+" createFrom(Map<String,Object> "+prop+"Map) {");
		code.ln(2,"if("+prop+"Map==null) return null;");
		code.ln(2,pojoName+" vo=new "+pojoName+"();");
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
		code.ln(1, "private "+prop.getType().getSimpleName()+" "+nc.getPropertyName(prop.getName())+" = null;");
		this.addImport(prop.getType());
	}
	
	
	private void buildGetter(Property prop) {
		 
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 获得 "+prop.getLabel()+(prop.hasNote()?"：":"")+prop.getNote());
		code.ln(1," * @return "+prop.getLabel());
		code.ln(1,"*/");
		
		String name=nc.getPropertyName(prop.getName());
		String getter=nc.getGetMethodName(prop.getName(), DBDataType.parseFromType(prop.getType()));
		code.ln(1, "public "+prop.getType().getSimpleName()+" "+getter +"() {");
		code.ln(2,"return this."+name+";");
		code.ln(1, "}");
	}
	
	private void buildSetter(Property prop) {
		
		String name=nc.getPropertyName(prop.getName());
		String setter=nc.getSetMethodName(prop.getName(), DBDataType.parseFromType(prop.getType()));
	

		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 设置 "+prop.getLabel()+(prop.hasNote()?"：":"")+prop.getNote());
		code.ln(1," * @param "+name+" "+prop.getLabel());
		code.ln(1,"*/");
		code.ln(1, "public void "+setter +"("+prop.getType().getSimpleName()+" "+name+") {");
		code.ln(2,"this."+name+"="+name+";");
		code.ln(1, "}");
	}
 
	@Override
	public void buildAndUpdate() {
		this.buildAndUpdateJava(ctx.getDomainProject().getMainSourceDir(), cfg.getFullName());
	}
}
