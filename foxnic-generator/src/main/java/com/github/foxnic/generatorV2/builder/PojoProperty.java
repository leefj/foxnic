package com.github.foxnic.generatorV2.builder;

import java.util.List;
import java.util.Map;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;
import com.github.foxnic.sql.meta.DBDataType;

import io.swagger.annotations.ApiModelProperty;

public class PojoProperty {
	
	private static final DefaultNameConvertor nameConvertor=new DefaultNameConvertor(false);
	private static final BeanNameUtil beanNameUtil=new BeanNameUtil();
	
	private static enum Catalog {
		SIMPLE,LIST,MAP;
	}
	private Catalog catalog=Catalog.SIMPLE;
	//
	private String name=null;
	private Class type=null;
	private JavaClassFile typeFile=null;
	private Class keyType=null;
	private String label=null;
	private String note=null;
	private boolean isPK=false;
	private boolean isAutoIncrease=false;
	private boolean nullable=true;
	//所在类
	private PojoClassFile classFile=null;
	
	/**
	 * 示例值
	 * */
	private String example;
 
	
	public String getTypeName() {
		if(this.type!=null) {
			this.classFile.addImport(type);
			return this.type.getSimpleName(); 
		} else if(this.typeFile!=null) {
			this.classFile.addImport(typeFile.getFullName());
			return this.typeFile.getSimpleName();
		}
		return null;
	}
	
	/**
	 * 定义一个非集合类型的属性
	 * */
	public static PojoProperty simple(Class type,String name,String label,String note) {
		PojoProperty p=new PojoProperty();
		p.catalog=Catalog.SIMPLE;
		p.type=type;
		p.name=name;
		p.label=label;
		p.note=note;
		return p;
	}
	
	/**
	 * 定义一个 List 类型的属性
	 * */
	public static PojoProperty list(Class type,String name,String label,String note) {
		PojoProperty p=new PojoProperty();
		p.catalog=Catalog.LIST;
		p.type=type;
		p.name=name;
		p.label=label;
		p.note=note;
		return p;
	}
	
	
	/**
	 * 定义一个 List 类型的属性
	 * */
	public static PojoProperty list(JavaClassFile typeFile,String name,String label,String note) {
		PojoProperty p=new PojoProperty();
		p.catalog=Catalog.LIST;
		p.typeFile=typeFile;
		p.name=name;
		p.label=label;
		p.note=note;
		return p;
	}
	
	/**
	 * 定义一个 Map 类型的属性
	 * */
	public static PojoProperty map(Class keyType,Class type,String name,String label,String note) {
		PojoProperty p=new PojoProperty();
		p.catalog=Catalog.MAP;
		p.keyType=keyType;
		p.type=type;
		p.name=name;
		p.label=label;
		p.note=note;
		return p;
	}
	
	public boolean hasNote() {
		return !StringUtil.isBlank(this.note);
	}

	/**
	 * 生成属性定义代码
	 * */
	public CodeBuilder getDefineCode(int tabs) {
		CodeBuilder code=new CodeBuilder();
		code.ln(tabs,"");
		code.ln(tabs,"/**");
		code.ln(tabs," * "+this.label+(this.hasNote()?("："+this.note):""));
		code.ln(tabs,"*/");
		
 
		if(this.isPK) {
			code.ln(1, "@Id");
			this.classFile.addImport(Id.class);
		}
		if(this.isAutoIncrease) {
			code.ln(1, "@GeneratedValue(strategy=GenerationType.IDENTITY)");
			this.classFile.addImport(GeneratedValue.class);
			this.classFile.addImport(GenerationType.class);
		}
		
	 
		if(!StringUtil.isBlank(example)) {
			example=" , example = \""+example+"\"";
		} else {
			example="";
		}
		if(this.classFile.context.getSettings().isEnableSwagger()) {
//			if(ctx.isDBTreatyFiled(cm)) {
//				code.ln(1,"@ApiModelProperty(required = "+!cm.isNullable()+",notes = \""+cm.getLabel()+"\""+example+")");
//			}else {
				code.ln(1,"@ApiModelProperty(required = "+!isNullable()+",value=\""+this.label+"\" , notes = \""+this.note+"\""+example+")");
//			}
			this.classFile.addImport(ApiModelProperty.class);
		}
		
		
		
		
		if(this.catalog==Catalog.SIMPLE) {
			code.ln(tabs,"private "+this.getTypeName()+" "+this.name+";");
		} else if(this.catalog==Catalog.LIST) {
			code.ln(tabs,"private List<"+this.getTypeName()+"> "+this.name+";");
			this.classFile.addImport(List.class);
		} else if(this.catalog==Catalog.MAP) {
			code.ln(tabs,"private Map<"+this.getTypeName()+","+this.type.getSimpleName()+"> "+this.name+";");
			this.classFile.addImport(Map.class);
		}
		
		this.classFile.addImport(this.type);
		
		return code;
	}
	
	/**
	 * 生成 get 方法代码 
	 * */
	public CodeBuilder getGetterCode(int tabs) {
		
		String mainGetter=nameConvertor.getGetMethodName(this.name, DBDataType.OBJECT);
		String subGetter=null;
		
		
		CodeBuilder code=new CodeBuilder();

		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 获得 "+this.label+"<br>");
//		if(subGetterName!=null) {
//			code.ln(1," * 等价于 "+subGetterName+" 方法，为兼容 Swagger 需要");
//		}
		if(this.hasNote()) {
			code.ln(1," * 属性说明 : "+this.note);
		}
		code.ln(1," * @return "+this.label);
		code.ln(1,"*/");
		
		
		if(this.catalog==Catalog.SIMPLE) {
			boolean isBoolean=DataParser.isBooleanType(this.type);
			if(isBoolean) {
				mainGetter=nameConvertor.getGetMethodName(this.name,DBDataType.BOOL);
				if(this.classFile.context.getSettings().isEnableSwagger()) {
					subGetter=nameConvertor.getGetMethodName(this.name, DBDataType.STRING);
				}
			}
			code.ln(tabs, "public "+this.getTypeName()+" "+mainGetter +"() {");
		} else if(this.catalog==Catalog.LIST) {
			code.ln(tabs, "public List<"+this.getTypeName()+"> "+mainGetter +"() {");
		} else if(this.catalog==Catalog.MAP) {
			code.ln(tabs, "public Map<"+this.getTypeName()+","+this.type.getSimpleName()+"> "+mainGetter +"() {");
		}
		code.ln(tabs+1, "return "+this.name+";");
		code.ln(tabs,"}");
		
		
		if(subGetter!=null) {
			code.ln(1,"");
			code.ln(1,"/**");
			code.ln(1," * 获得 "+this.label+"<br>");
			code.ln(1," * 等价于 "+mainGetter+" 方法，为兼容 Swagger 需要");
			if(this.hasNote()) {
				code.ln(1," * 属性说明 : "+this.note);
			}
			code.ln(1," * @return "+this.label);
			code.ln(1,"*/");
			code.ln(1, "public "+this.type.getSimpleName()+" "+ subGetter +"() {");
			code.ln(2,"return this."+this.name+";");
			code.ln(1, "}");
		}
		
		return code;
	}
	
	public CodeBuilder getSetterCode(int tabs) {
		CodeBuilder code=new CodeBuilder();
		
		code.ln(1,"");
		code.ln(1,"/**");
		code.ln(1," * 设置 "+this.label);
		code.ln(1," * @param "+this.name+" "+this.label);
		code.ln(1," * @return 当前对象");
		code.ln(1,"*/");
		
		String setter=nameConvertor.getSetMethodName(this.name, DBDataType.OBJECT);
		if(this.catalog==Catalog.SIMPLE) {
			boolean isBoolean=DataParser.isBooleanType(this.type);
			if(isBoolean) {
				setter=nameConvertor.getSetMethodName(this.name,DBDataType.BOOL);
			}
			code.ln(tabs, "public "+this.classFile.getSimpleName()+" "+setter +"("+this.getTypeName()+" "+this.name+") {");
		} else if(this.catalog==Catalog.LIST) {
			code.ln(tabs, "public "+this.classFile.getSimpleName()+" "+setter +"(List<"+this.getTypeName()+"> "+this.name+") {");
		} else if(this.catalog==Catalog.MAP) {
			code.ln(tabs, "public "+this.classFile.getSimpleName()+" "+setter +"(Map<"+this.getTypeName()+","+this.type.getSimpleName()+"> "+this.name+") {");
		}
		code.ln(tabs+1, "this."+this.name+"="+this.name+";");
		code.ln(tabs+1, "return this;");
		code.ln(tabs,"}");
		return code;
	}

	public void setClassFile(PojoClassFile classFile) {
		this.classFile = classFile;
	}

	public boolean isPK() {
		return isPK;
	}

	public void setPK(boolean isPK) {
		this.isPK = isPK;
	}

	public boolean isAutoIncrease() {
		return isAutoIncrease;
	}

	public void setAutoIncrease(boolean isAutoIncrease) {
		this.isAutoIncrease = isAutoIncrease;
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public String getExample() {
		return example;
	}

	public void setExample(String example) {
		this.example = example;
	}
	
	
	public String getSign() {
	 
		String sign=StringUtil.join(new Object[] {catalog.name(),name,type.getName(),
				keyType==null?"":keyType.getName(),label,note,isPK,isAutoIncrease,nullable});
		return MD5Util.encrypt32(sign);
		
	}

	public Catalog catalog() {
		return catalog;
	}

	public String name() {
		return name;
	}

	public Class type() {
		return type;
	}

	public String label() {
		return label;
	}

	public String note() {
		return note;
	}

	public String getNameConstants() {
		return beanNameUtil.depart(this.name).toUpperCase();
	}

	public String getJavaDocInfo() {
		String m=this.label();
		if(this.hasNote() && !this.label.equals(this.note())) {
			m+=" , "+this.note();
		}
		return m;
	}

}
