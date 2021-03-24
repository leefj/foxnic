package com.github.foxnic.generator.clazz;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private String sign=null;
	
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
		this.sign=cfg.getSignature();
		code.ln("/**");
		super.appendAuthorAndTime();
		code.ln(" * @sign "+sign);
		code.ln(" * 此文件由工具自动生成，请勿修改。若表结构变动，请使用工具重新生成。");
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
			buildAdder(prop);
			buildPutter(prop);
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

		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 创建一个 "+pojoName+"，等同于 new");
		code.ln(1," * @return "+pojoName+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+pojoName+" create() {");
		code.ln(2,"return new "+pojoName+"();");
		code.ln(1,"}");
			
		this.addImport(Map.class);
		this.addImport(EntityContext.class);
		
		
		
		code.ln("}");
	 
	}
	
	private void buildProperty(Property prop) {

		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * "+prop.getLabel()+(prop.hasNote()?"：":"")+prop.getNote());
		code.ln(1,"*/");
		
//		考虑在接口加入注解
		if(ctx.isEnableSwagger()) {
			code.ln(1,"@ApiModelProperty(value=\""+prop.getLabel()+"\" , dataType=\""+prop.getType().getSimpleName()+"\")");
			this.addImport(ClassNames.ApiModelProperty); 
		}
		
		if("list".equals(prop.getCata())) {
			String cmpTypeName=prop.getTypeName();
			if(cmpTypeName==null && prop.getType()!=null) {
				cmpTypeName=prop.getType().getSimpleName();
			}
			cmpTypeName=StringUtil.getLastPart(cmpTypeName, ".");
			code.ln(1, "private List<"+cmpTypeName+"> "+nc.getPropertyName(prop.getName())+" = null;");
			
			this.addImport(List.class);
			this.addImport(ArrayList.class);
			if(prop.getTypeName()!=null && prop.getTypeName().contains(".")) {
				this.addImport(prop.getTypeName());
			}
 
		} else if("map".equals(prop.getCata())) {
			
			String valueTypeName=prop.getTypeName();
			if(valueTypeName==null && prop.getType()!=null) {
				valueTypeName=prop.getType().getSimpleName();
			}
			valueTypeName=StringUtil.getLastPart(valueTypeName, ".");
			
			String keyTypeName=prop.getMapKeyType().getName();
			keyTypeName=StringUtil.getLastPart(keyTypeName, ".");
			
			code.ln(1, "private Map<"+keyTypeName+","+valueTypeName+"> "+nc.getPropertyName(prop.getName())+" = null;");
			
			this.addImport(List.class);
			this.addImport(ArrayList.class);
			if(prop.getTypeName()!=null && prop.getTypeName().contains(".")) {
				this.addImport(prop.getTypeName());
			}
 
		} else {
			code.ln(1, "private "+prop.getType().getSimpleName()+" "+nc.getPropertyName(prop.getName())+" = null;");
		}
		if(prop.getType()!=null) {
			this.addImport(prop.getType());
		}
	}
	
	
	private void buildGetter(Property prop) {
		 
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 获得 "+prop.getLabel()+(prop.hasNote()?"：":"")+prop.getNote());
		code.ln(1," * @return "+prop.getLabel());
		code.ln(1,"*/");
		
		String name=nc.getPropertyName(prop.getName());
		String getter=null;
		if("list".equals(prop.getCata())) {
			String cmpTypeName=prop.getTypeName();
			if(cmpTypeName==null && prop.getType()!=null) {
				cmpTypeName=prop.getType().getSimpleName();
			}
			cmpTypeName=StringUtil.getLastPart(cmpTypeName, ".");
			getter=nc.getGetMethodName(prop.getName(), DBDataType.STRING);
			code.ln(1, "public List<"+cmpTypeName+"> "+getter +"() {");
		} else if("map".equals(prop.getCata())) {
			String valueTypeName=prop.getTypeName();
			if(valueTypeName==null && prop.getType()!=null) {
				valueTypeName=prop.getType().getSimpleName();
			}
			valueTypeName=StringUtil.getLastPart(valueTypeName, ".");
			getter=nc.getGetMethodName(prop.getName(), DBDataType.STRING);
			
			String keyTypeName=prop.getMapKeyType().getName();
			keyTypeName=StringUtil.getLastPart(keyTypeName, ".");
			
			code.ln(1, "public Map<"+keyTypeName+","+valueTypeName+"> "+getter +"() {");
		} else {
			getter=nc.getGetMethodName(prop.getName(), DBDataType.parseFromType(prop.getType()));
			code.ln(1, "public "+prop.getType().getSimpleName()+" "+getter +"() {");
		}
		
		code.ln(2,"return this."+name+";");
		code.ln(1, "}");
	}
	
	private void buildSetter(Property prop) {
		
		String name=nc.getPropertyName(prop.getName());
		String setter=null;

	

		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 设置 "+prop.getLabel()+(prop.hasNote()?"：":"")+prop.getNote());
		code.ln(1," * @param "+name+" "+prop.getLabel());
		code.ln(1," * @return 当前对象");
		code.ln(1,"*/");
		if("list".equals(prop.getCata())) {
			String cmpTypeName=prop.getTypeName();
			if(cmpTypeName==null && prop.getType()!=null) {
				cmpTypeName=prop.getType().getSimpleName();
			}
			cmpTypeName=StringUtil.getLastPart(cmpTypeName, ".");
			setter=nc.getSetMethodName(prop.getName(), DBDataType.STRING);
			code.ln(1, "public "+cfg.getClassName()+" "+setter +"(List<"+cmpTypeName+"> "+name+") {");
		} else if("map".equals(prop.getCata())) {
			String valueTypeName=prop.getTypeName();
			if(valueTypeName==null && prop.getType()!=null) {
				valueTypeName=prop.getType().getSimpleName();
			}
			valueTypeName=StringUtil.getLastPart(valueTypeName, ".");
			setter=nc.getSetMethodName(prop.getName(), DBDataType.STRING);
			
			String keyTypeName=prop.getMapKeyType().getName();
			keyTypeName=StringUtil.getLastPart(keyTypeName, ".");
			
			code.ln(1, "public "+cfg.getClassName()+" "+setter +"(Map<"+keyTypeName+","+valueTypeName+"> "+name+") {");
		} 
		else {
			setter=nc.getSetMethodName(prop.getName(), DBDataType.parseFromType(prop.getType()));
			code.ln(1, "public "+cfg.getClassName()+" "+setter +"("+prop.getType().getSimpleName()+" "+name+") {");
		}
		
		code.ln(2,"this."+name+"="+name+";");
		code.ln(2,"return this;");
		code.ln(1, "}");
	}
	
	
	private void buildAdder(Property prop) {
		
		if(!"list".equals(prop.getCata())) return;
	
		String name=nc.getPropertyName(prop.getName());
		String setter=null;
 
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 添加 "+prop.getLabel()+(prop.hasNote()?"：":"")+prop.getNote());
		code.ln(1," * @param elem 列表元素");
		code.ln(1," * @return 当前对象");
		code.ln(1,"*/");
		if("list".equals(prop.getCata())) {
			String cmpTypeName=prop.getTypeName();
			if(cmpTypeName==null && prop.getType()!=null) {
				cmpTypeName=prop.getType().getSimpleName();
			}
			cmpTypeName=StringUtil.getLastPart(cmpTypeName, ".");
			setter=nc.getSetMethodName(prop.getName(), DBDataType.STRING);
			setter="add"+StringUtil.removeLast(StringUtil.removeLast(setter.substring(3),"List"),"s");
			code.ln(1, "public "+cfg.getClassName()+" "+setter +"("+cmpTypeName+" elem) {");
			code.ln(2,"if(this."+name+"==null) this."+name+" = new ArrayList<>();");
		}
		
		code.ln(2,"this."+name+".add(elem);");
		code.ln(2,"return this;");
		code.ln(1, "}");
	}
	
	
	private void buildPutter(Property prop) {
		
		if(!"map".equals(prop.getCata())) return;
	
		String name=nc.getPropertyName(prop.getName());
		String setter=null;
 
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 添加 "+prop.getLabel()+(prop.hasNote()?"：":"")+prop.getNote());
		code.ln(1," * @param elem 列表元素");
		code.ln(1," * @return 当前对象");
		code.ln(1,"*/");
		if("map".equals(prop.getCata())) {
			String valueTypeName=prop.getTypeName();
			if(valueTypeName==null && prop.getType()!=null) {
				valueTypeName=prop.getType().getSimpleName();
			}
			valueTypeName=StringUtil.getLastPart(valueTypeName, ".");
			
			String keyTypeName=prop.getMapKeyType().getName();
			keyTypeName=StringUtil.getLastPart(keyTypeName, ".");
			
			setter=nc.getSetMethodName(prop.getName(), DBDataType.STRING);
			setter="put"+StringUtil.removeLast(StringUtil.removeLast(setter.substring(3),"Map"),"s");
			code.ln(1, "public "+cfg.getClassName()+" "+setter +"("+keyTypeName+" key,"+valueTypeName+" value) {");
			code.ln(2,"if(this."+name+"==null) this."+name+" = new HashMap<>();");
			this.addImport(HashMap.class);
		}
		
		code.ln(2,"this."+name+".put(key,value);");
		code.ln(2,"return this;");
		code.ln(1, "}");
	}
	
 
	@Override
	public void buildAndUpdate() {
		this.buildAndUpdateJava(ctx.getDomainProject().getMainSourceDir(), cfg.getFullName());
	}
	
	@Override
	protected File processOverride(File sourceFile) {
		//如果模型变化，则覆盖原始文件；否则不处理
		if(PoBuilder.isSignatureChanged(sourceFile,this.sign)) {
			return sourceFile;
		} else {
			return null;
		}
	}
}
