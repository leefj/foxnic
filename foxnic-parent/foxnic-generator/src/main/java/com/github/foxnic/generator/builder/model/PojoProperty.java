package com.github.foxnic.generator.builder.model;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.reflect.EnumUtil;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;
import com.github.foxnic.sql.meta.DBDataType;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.*;

public class PojoProperty {

	private static final DefaultNameConvertor nameConvertor=new DefaultNameConvertor(false);
	private static final BeanNameUtil beanNameUtil=new BeanNameUtil();

    public String getGetterMethodName(DBDataType type) {
		return nameConvertor.getGetMethodName(this.name, type);
    }

    private static enum Catalog {
		SIMPLE,LIST,MAP;
	}
	private Catalog catalog=Catalog.SIMPLE;
	//
	private String name=null;
	private Class type=null;

	private boolean isFromTable=false;
	/**
	 * 当前属性的类型文件
	 */
	private JavaClassFile typeFile=null;
	private Class keyType=null;
	private String label=null;
	private String note=null;
	private boolean isPK=false;
	private boolean isAutoIncrease=false;
	private boolean nullable=true;
	private PojoClassFile.Shadow shadow;
	/**
	 * 当前属性所在的类文件
	 */
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

	public boolean isFromTable() {
		return isFromTable;
	}

	public void setFromTable(boolean fromTable) {
		isFromTable = fromTable;
	}

	public String getTypeFullName() {
		if(this.type!=null) {
			this.classFile.addImport(type);
			return this.type.getName();
		} else if(this.typeFile!=null) {
			this.classFile.addImport(typeFile.getFullName());
			return this.typeFile.getFullName();
		}
		return null;
	}

	public String getTypeName4Proxy(PojoMetaClassFile file) {
		if(this.type!=null) {
			file.addImport(type);
			return this.type.getSimpleName();
		} else if(this.typeFile!=null) {
			file.addImport(typeFile.getFullName());
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
			code.ln(tabs,"private Map<"+this.keyType.getSimpleName()+","+this.type.getSimpleName()+"> "+this.name+";");
			this.classFile.addImport(Map.class);
		}

		if(this.type!=null) {
			this.classFile.addImport(this.type);
		}

		if(this.shadow!=null) {

			code.ln(1,"@Transient");
			if(this.shadow.getEnumType()!=null) {
				code.ln(1, "private " + this.shadow.getEnumType().getSimpleName() + " " + this.shadow.propName + ";");
			} else {
				code.ln(1, "private Boolean " + this.shadow.propName + ";");
			}
			this.classFile.addImport(this.shadow.getEnumType());
			this.classFile.addImport(Transient.class);
		}


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
			code.ln(1," * "+this.note);
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
			code.ln(tabs, "public Map<"+this.keyType.getSimpleName()+","+this.type.getSimpleName()+"> "+mainGetter +"() {");
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

		if(this.shadow!=null) {

			String getter=null;
			if(this.shadow.getEnumType()!=null) {
				getter=nameConvertor.getGetMethodName(this.shadow.getPropName(), DBDataType.OBJECT);
			} else {
				getter=nameConvertor.getGetMethodName(this.shadow.getPropName(), DBDataType.BOOL);
				if(getter.endsWith("Bool")) {
					getter = getter.substring(0,getter.length()-4);
				}
			}
			code.ln(1,"");
			code.ln(1,"/**");
			code.ln(1," * 获得 "+this.label+" 的投影属性<br>");
			code.ln(1," * 等价于 "+mainGetter+" 方法，获得对应的枚举类型");
			code.ln(1," * @return "+this.label);
			code.ln(1,"*/");
			code.ln(tabs,"@Transient");
			if(this.shadow.getEnumType()!=null) {
				code.ln(tabs, "public " + this.shadow.getEnumType().getSimpleName() + " " + getter + "() {");
			} else {
				code.ln(tabs, "public Boolean " + getter + "() {");
			}
			code.ln(tabs+1, "if(this."+this.shadow.getPropName()+"==null) {");
			if(this.shadow.getEnumType()!=null) {
				code.ln(tabs + 2, "this." + this.shadow.getPropName() + " = (" + this.shadow.getEnumType().getSimpleName() + ") EnumUtil.parseByCode(" + this.shadow.getEnumType().getSimpleName() + ".values()," + this.name + ");");
			} else {
				code.ln(tabs + 2, "this." + this.shadow.getPropName() + "=DataParser.parseBoolean("+this.name+");");
			}
			code.ln(tabs+1, "}");
			code.ln(tabs+1,"return this."+this.shadow.getPropName()+" ;");
			code.ln(tabs,"}");
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
			try {
//				if(this.classFile!=null) {
				if(this.shadow!=null) {
					code.ln(tabs,"@JsonProperty(\""+this.name+"\")");
					this.classFile.addImport("com.fasterxml.jackson.annotation.JsonProperty");
				}
				code.ln(tabs, "public " + this.classFile.getSimpleName() + " " + setter + "(" + this.getTypeName() + " " + this.name + ") {");
//				} else {
//					code.ln(tabs, "public " + this.type.getSimpleName() + " " + setter + "(" + this.getTypeName() + " " + this.name + ") {");
//				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if(this.catalog==Catalog.LIST) {
			code.ln(tabs, "public "+this.classFile.getSimpleName()+" "+setter +"(List<"+this.getTypeName()+"> "+this.name+") {");
		} else if(this.catalog==Catalog.MAP) {
			code.ln(tabs, "public "+this.classFile.getSimpleName()+" "+setter +"(Map<"+this.keyType.getSimpleName()+","+this.type.getSimpleName()+"> "+this.name+") {");
		}
		code.ln(tabs+1, "this."+this.name+"="+this.name+";");
		if(this.shadow!=null) {
			if(this.shadow.getEnumType()!=null) {
				code.ln(tabs + 1, "this." + this.shadow.getPropName() + "= (" + this.shadow.getEnumType().getSimpleName() + ") EnumUtil.parseByCode(" + this.shadow.getEnumType().getSimpleName() + ".values()," + this.name + ") ;");
				code.ln(tabs + 1, "if(StringUtil.hasContent(" + this.name + ") && this." + this.shadow.getPropName() + "==null) {");
				code.ln(tabs + 2, "throw new IllegalArgumentException( " + this.name + " + \" is not one of " + this.shadow.getEnumType().getSimpleName() + "\");");
				code.ln(tabs + 1, "}");
				this.classFile.addImport(EnumUtil.class);
				this.classFile.addImport(StringUtil.class);
			} else {
				code.ln(tabs + 1, "this." + this.shadow.getPropName() + "=DataParser.parseBoolean("+this.name+");");
				this.classFile.addImport(DataParser.class);
			}
		}
		code.ln(tabs+1, "return this;");
		code.ln(tabs,"}");

		if(this.shadow!=null) {
			String shadowSetter=nameConvertor.getSetMethodName(this.shadow.getPropName(), DBDataType.OBJECT);
			if(this.shadow.getEnumType()==null) {
				if (shadowSetter.endsWith("Bool")) {
					shadowSetter = shadowSetter.substring(0, shadowSetter.length() - 4);
				}
			}
			code.ln(1,"");
			code.ln(1,"/**");
			code.ln(1," * 设置 "+this.label +"的投影属性，等同于设置 "+this.label);
			code.ln(1," * @param "+this.shadow.getPropName()+" "+this.label);
			code.ln(1," * @return 当前对象");
			code.ln(1,"*/");

			code.ln(1,"@Transient");
			if(this.shadow.getEnumType()==null) {
				code.ln(tabs, "public " + this.classFile.getSimpleName() + " " + shadowSetter + "(Boolean " + this.shadow.getPropName() + ") {");
				code.ln(tabs+1, "if("+this.shadow.getPropName()+"==null) {");
				code.ln(tabs+2, "this."+this.name+"=null;");
				code.ln(tabs+1, "} else {");
				String q="";
				if(this.classFile.getLogicTrue() instanceof String || this.classFile.getLogicFalse() instanceof String) {
					q="\"";
				}
				code.ln(tabs+2, "this."+this.name+"="+this.shadow.getPropName()+"?"+q+this.classFile.getLogicTrue()+q+":"+q+this.classFile.getLogicFalse()+q+";");
				code.ln(tabs+1, "}");
			} else {
				code.ln(tabs, "public " + this.classFile.getSimpleName() + " " + shadowSetter + "(" + this.shadow.getEnumType().getSimpleName() + " " + this.shadow.getPropName() + ") {");
				code.ln(tabs+1, "if("+this.shadow.getPropName()+"==null) {");
				code.ln(tabs+2, "this."+setter+"(null);");
				code.ln(tabs+1, "} else {");
				code.ln(tabs+2, "this."+setter+"("+this.shadow.getPropName()+".code());");
				code.ln(tabs+1, "}");
			}


			code.ln(tabs+1, "this."+this.shadow.getPropName()+"="+this.shadow.getPropName()+";");
			code.ln(tabs+1, "return this;");
			code.ln(tabs,"}");
		}





		if(this.catalog==Catalog.LIST) {



			 String pn=StringUtil.removeLast(this.name, "s");
			 pn=StringUtil.removeLast(pn, "List");
			 String adder="add"+setter.substring(3);

			 if(this.name.equals(pn)) {
			 	pn="entity";
				 if(this.name.equals(pn)) {
					 pn="pojo";
				 }
			 }


			 adder=StringUtil.removeLast(adder, "s");
			 adder=StringUtil.removeLast(adder, "List");

			code.ln(1,"");
			code.ln(1,"/**");
			code.ln(1," * 添加 "+this.label);
			code.ln(1," * @param "+pn+" "+this.label);
			code.ln(1," * @return 当前对象");
			code.ln(1,"*/");

			 code.ln(tabs, "public "+this.classFile.getSimpleName()+" "+adder +"("+this.getTypeName()+"... "+pn+") {");
			 code.ln(tabs+1, "if(this."+this.name+"==null) "+this.name+"=new ArrayList<>();");
			 code.ln(tabs+1, "this."+this.name+".addAll(Arrays.asList("+pn+"));");
			 code.ln(tabs+1, "return this;");

			 code.ln(tabs,"}");
			 this.classFile.addImport(ArrayList.class);
			this.classFile.addImport(Arrays.class);
		}
		else if(this.catalog==Catalog.MAP) {
			 String pn=StringUtil.removeLast(this.name, "s");
			 pn=StringUtil.removeLast(pn, "Map");
			 String putter="put"+setter.substring(3);

			 putter=StringUtil.removeLast(putter, "s");
			 putter=StringUtil.removeLast(putter, "Map");

			code.ln(1,"");
			code.ln(1,"/**");
			code.ln(1," * 添加 "+this.label);
			code.ln(1," * @param key 键");
			code.ln(1," * @param "+pn+" "+this.label);
			code.ln(1," * @return 当前对象");
			code.ln(1,"*/");

			 code.ln(tabs, "public "+this.classFile.getSimpleName()+" "+putter +"("+this.keyType.getSimpleName()+" key,"+this.type.getSimpleName()+" "+pn+") {");
			 code.ln(tabs+1, "if(this."+this.name+"==null) this."+this.name+"=new HashMap<>();");
			 code.ln(tabs+1, "this."+this.name+".put(key ,"+pn+");");
			 code.ln(tabs+1, "return this;");
			 code.ln(tabs,"}");
			 this.classFile.addImport(HashMap.class);
		}

		return code;
	}

	public String makeAssignmentCode(String from,String to) {
		CodeBuilder code=new CodeBuilder();
		String getter=nameConvertor.getGetMethodName(this.name, DBDataType.OBJECT);
		if(this.catalog==Catalog.SIMPLE) {
			boolean isBoolean=DataParser.isBooleanType(this.type);
			if(isBoolean) {
				getter=nameConvertor.getGetMethodName(this.name,DBDataType.BOOL);
			}
		}
		String setter=nameConvertor.getSetMethodName(this.name, DBDataType.OBJECT);
		if(this.catalog==Catalog.SIMPLE) {
			boolean isBoolean=DataParser.isBooleanType(this.type);
			if(isBoolean) {
				setter=nameConvertor.getSetMethodName(this.name,DBDataType.BOOL);
			}
		}
		return to+"."+setter+"("+from+"."+getter+"());";
	}

	public CodeBuilder getSetterCode4Proxy(int tabs,PojoMetaClassFile file) {
		CodeBuilder code=new CodeBuilder();

		code.ln(2,"");
		code.ln(2,"/**");
		code.ln(2," * 设置 "+this.label);
		code.ln(2," * @param "+this.name+" "+this.label);
		code.ln(2," * @return 当前对象");
		code.ln(2,"*/");


		String getter=nameConvertor.getGetMethodName(this.name, DBDataType.OBJECT);
		if(this.catalog==Catalog.SIMPLE) {
			boolean isBoolean=DataParser.isBooleanType(this.type);
			if(isBoolean) {
				getter=nameConvertor.getGetMethodName(this.name,DBDataType.BOOL);
			}
		}

		file.addImport(this.classFile.getFullName());

		String setter=nameConvertor.getSetMethodName(this.name, DBDataType.OBJECT);
		if(this.catalog==Catalog.SIMPLE) {
			boolean isBoolean=DataParser.isBooleanType(this.type);
			if(isBoolean) {
				setter=nameConvertor.getSetMethodName(this.name,DBDataType.BOOL);
			}
			code.ln(tabs, "public "+this.classFile.getSimpleName()+" "+setter +"("+this.getTypeName4Proxy(file)+" "+this.name+") {");
		} else if(this.catalog==Catalog.LIST) {
			code.ln(tabs, "public "+this.classFile.getSimpleName()+" "+setter +"(List<"+this.getTypeName4Proxy(file)+"> "+this.name+") {");
			file.addImport(List.class);
		} else if(this.catalog==Catalog.MAP) {
			code.ln(tabs, "public "+this.classFile.getSimpleName()+" "+setter +"(Map<"+this.keyType.getSimpleName()+","+this.type.getSimpleName()+"> "+this.name+") {");
			file.addImport(Map.class);
		}
		code.ln(3,"super.change("+this.getNameConstants()+",super."+getter+"(),"+this.name+");");
		code.ln(3,"super."+setter+"("+this.name+");");
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

		String sign=StringUtil.join(new Object[] {catalog.name(),name,(type==null?typeFile.getFullName():type.getName()),
				keyType==null?"":keyType.getName(),label,note,isPK,isAutoIncrease,nullable});
		return MD5Util.encrypt32(sign);

	}

	public Catalog catalog() {
		return catalog;
	}

	public String catalogName() {
		return catalog.name();
	}

	public String name() {
		return name;
	}

	public Class type() {
		return type;
	}

	public Class keyType() {
		return keyType;
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
		if(this.catalog!=Catalog.SIMPLE) {
			m+=" , 集合类型: "+this.catalog.name();
		}
		m+=" , 类型: "+this.getTypeFullName();
		return m;
	}

	public PojoClassFile.Shadow getShadow() {
		return shadow;
	}

	public void setShadow(PojoClassFile.Shadow shadow) {
		this.shadow = shadow;
	}

	public boolean isSimple() {
		return  this.catalog()==Catalog.SIMPLE;
	}


}
