package com.github.foxnic.generator.builder.model;

import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.dao.entity.FieldsBuilder;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;
import com.github.foxnic.sql.meta.DBField;
import io.swagger.annotations.ApiModel;

import javax.persistence.Transient;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PojoClassFile extends ModelClassFile {

	public static final DefaultNameConvertor nameConvertor=new DefaultNameConvertor(false);

	protected List<PojoProperty> properties=new ArrayList<>();

	private String doc;

	protected PojoMetaClassFile metaClassFile;

	public void setMetaClassFile(PojoMetaClassFile metaClassFile) {
		this.metaClassFile = metaClassFile;
	}

    public PojoProperty getProperty(DBColumnMeta col) {
		for (PojoProperty property : properties) {
			if(col.getColumnVarName().equals(property.name())) {
				return property;
			}
		}
		return null;
    }

	public PojoProperty getProperty(String prop) {
		for (PojoProperty property : properties) {
			if(prop.equals(property.name())) {
				return property;
			}
		}
		return null;
	}

	public List<PojoProperty> addSimpleProperties(FieldsBuilder fields) {
		List<PojoProperty> properties=new ArrayList<>();
		List<DBColumnMeta> columnMetas=fields.getColumns();
		for (DBColumnMeta cm : columnMetas) {
			PojoProperty property=this.addSimpleProperty(cm.getDBDataType().getType(),cm.getColumnVarName(),cm.getLabel(),cm.getComment());
			property.setFromTable(true);
		}
		return properties;
	}

	public boolean isExtendsEntity() {
		Class superType=null;
		JavaClassFile superTypeFile=null;
		PojoClassFile classFile=this;
		while (true) {
			superType=classFile.getSuperType();
			superTypeFile=classFile.getSuperTypeFile();
			if(superType==null && superTypeFile==null) {
				return false;
			}
			if(superType!=null) {
				if(ReflectUtil.isSubType(Entity.class,superType)) {
					return true;
				}
			}
			if(superTypeFile!=null) {
				classFile=(PojoClassFile)superTypeFile;
			}
		}
	}


	public static  class  Shadow {

		String field;
		String table;
		Class<? extends CodeTextEnum> enumType;
		String propName;

		public Shadow (DBField field, Class<? extends CodeTextEnum> enumType, String propName) {
			this.field=field.name();
			this.table=field.table().name();
			this.enumType=enumType;
			this.propName=propName;
		}

		public Shadow (String table,String field, Class<? extends CodeTextEnum> enumType, String propName) {
			this.field=field;
			this.table=table;
			this.enumType=enumType;
			this.propName=propName;
		}

		public String getField() {
			return field;
		}

		public Class<? extends CodeTextEnum> getEnumType() {
			return enumType;
		}

		public String getPropName() {
			return propName;
		}


		public String getSign() {
			if(this.enumType==null) {
				return this.table + "," + this.field + "," + Boolean.class.getName() + "," + this.propName;
			} else {
				return this.table + "," + this.field + "," + this.enumType.getName() + "," + this.propName;
			}
		}
	}

	/**
	 * 添加一个非集合类型的简单属性
	 * @param type 类型
	 * @param name 参数名称，建议使用驼峰命名，如 orderId
	 * @param  label 属性标签
	 * @param  note  属性详细说明
	 * */
	public PojoProperty addSimpleProperty(Class type,String name,String label,String note) {
		PojoProperty property=PojoProperty.simple(type, name, label, note);
		this.addProperty(property);
		return property;
	}

	public PojoProperty addSimpleProperty(Class type,String name,String label,String note,String example) {
		PojoProperty property=PojoProperty.simple(type, name, label, note);
		property.setExample(example);
		this.addProperty(property);
		return property;
	}

	/**
	 * 添加一个List类型的属性
	 * @param type List内部元素的类型
	 * @param name 参数名称，建议使用驼峰命名，如 orderId
	 * @param  label 属性标签
	 * @param  note  属性详细说明
	 * */
	public void addListProperty(Class type,String name,String label,String note) {
		this.addProperty(PojoProperty.list(type, name, label, note));
	}

	public void addListProperty(JavaClassFile type,String name,String label,String note) {
		this.addProperty(PojoProperty.list(type, name, label, note));
	}


	public void shadow(DBField field, Class<? extends CodeTextEnum> enumType) {
		shadow(field,enumType,field.getVar()+"Enum");
	}

	public void shadow(String field, Class<? extends CodeTextEnum> enumType) {
		shadow(field,enumType,field+"Enum");
	}

	/**
	 * 设置属性投影
	 * */
	public void shadow(String field, Class<? extends CodeTextEnum> enumType, String propName) {
		for (PojoProperty property : properties) {
			if(property.name().equals(field)) {
				if(!property.isSimple()) {
					throw new IllegalArgumentException("仅支持Simple类型");
				}
				property.setShadow(new Shadow(this.getFullName(),field,enumType,propName));
				break;
			}
		}
	}

	public void shadow(DBField field, Class<? extends CodeTextEnum> enumType, String propName) {
		for (PojoProperty property : properties) {
			if(property.name().equals(field.getVar())) {
				if(!property.isSimple()) {
					throw new IllegalArgumentException("仅支持Simple类型");
				}
				property.setShadow(new Shadow(field,enumType,propName));
				break;
			}
		}
	}


	/**
	 * 设置属性投影
	 * */
	public void shadowBoolean(String field) {
		shadowBoolean(field,nameConvertor.getPropertyName(field)+"Bool");
	}
	/**
	 * 设置属性投影
	 * */
	public void shadowBoolean(String field, String propName) {
		for (PojoProperty property : properties) {
			if(property.name().equals(field)) {
				if(!property.isSimple()) {
					throw new IllegalArgumentException("仅支持Simple类型");
				}
				property.setShadow(new Shadow(this.getFullName(),field,null,propName));
				break;
			}
		}
	}

	public void shadowBoolean(DBField field) {
		shadowBoolean(field,field.getVar()+"Bool");
	}

	public void shadowBoolean(DBField field,String propName) {
		for (PojoProperty property : properties) {
			if(property.name().equals(field.getVar())) {
				if(!property.isSimple()) {
					throw new IllegalArgumentException("仅支持Simple类型");
				}
				property.setShadow(new Shadow(field,null,propName));
				break;
			}
		}
	}



	/**
	 * 添加一个Map类型的属性
	 * @param keyType Map Key 的类型
	 * @param type Map内部元素的类型
	 * @param name 参数名称，建议使用驼峰命名，如 orderId
	 * @param  label 属性标签
	 * @param  note  属性详细说明
	 * */
	public void addMapProperty(Class keyType,Class type,String name,String label,String note) {
		this.addProperty(PojoProperty.map(keyType, type, name, label, note));
	}


	public void addProperty(PojoProperty prop) {
		if(getProperty(prop.name())!=null) return;
		properties.add(prop);
		prop.setClassFile(this);
	}

	public PojoClassFile(ModuleContext context,MavenProject project, String packageName, String simpleName) {
		super(context,project, packageName, simpleName);
		this.setDoc(context.getTopic());
	}



	@Override
	protected void buildBody() {

		buildClassJavaDoc();

		buildClassStartPart();

		buildProperties();

		buildGetterAndSetter();

		buildOthers();

		buildClassEndPart();

	}



	protected void buildOthers() {

		this.addImport(Transient.class);
		if(this.isExtendsEntity()) {
			buildOthers4Entity();
		} else {
			buildOthers4Normal();
		}

	}

	private void buildOthers4Normal() {

		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 创建一个 "+this.getSimpleName()+"，等同于 new");
		code.ln(1," * @return "+this.getSimpleName()+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+this.getSimpleName()+" create() {");
		code.ln(2,"return new "+this.getSimpleName()+"();");
		code.ln(1,"}");


		String prop=context.getPoClassFile().getVar();
		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将 Map 转换成 "+this.getSimpleName());
		code.ln(1," * @param "+prop+"Map 包含实体信息的 Map 对象");
		code.ln(1," * @return "+this.getSimpleName()+" , 转换好的的 "+context.getPoClassFile().getSimpleName()+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+this.getSimpleName()+" createFrom(Map<String,Object> "+prop+"Map) {");
		code.ln(2,"if("+prop+"Map==null) return null;");
		code.ln(2,this.getSimpleName()+" po = new "+this.getSimpleName()+"();");
		code.ln(2,"BeanUtil.copy("+prop+"Map,po);");
		code.ln(2,"return po;");
		code.ln(1,"}");

		this.addImport(BeanUtil.class);
		this.addImport(Map.class);
		this.addImport(EntityContext.class);


		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将 Pojo 转换成 "+this.getSimpleName());
		code.ln(1," * @param pojo 包含实体信息的 Pojo 对象");
		code.ln(1," * @return "+this.getSimpleName()+" , 转换好的的 "+context.getPoClassFile().getSimpleName()+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+this.getSimpleName()+" createFrom(Object pojo) {");
		code.ln(2,"if(pojo==null) return null;");
		code.ln(2,this.getSimpleName()+" po = new "+this.getSimpleName()+"();");
		code.ln(2,"BeanUtil.copy(pojo,po,true);");
		code.ln(2,"return po;");
		code.ln(1,"}");


		Map<String,PojoProperty> otherProps=new HashMap<>();
		Map<String,PojoProperty> dbProps=new HashMap<>();
		List<PojoProperty> props= this.getSuperProperties();
		for (PojoProperty p : props) {
			if(p.isFromTable()) {
				dbProps.put(p.name(), p);
			} else {
				otherProps.put(p.name(), p);
			}
		}
		props= this.getProperties();
		for (PojoProperty p : props) {
			if(p.isFromTable()) {
				dbProps.put(p.name(), p);
			} else {
				otherProps.put(p.name(), p);
			}
		}

		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 复制当前对象");
		code.ln(1," * @param all 是否复制全部属性，当 false 时，仅复制来自数据表的属性");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public "+this.getSimpleName()+" duplicate(boolean all) {");
		code.ln(2,this.getSimpleName()+" inst = new "+this.getSimpleName()+"();");
		//code.ln(2,"return EntityContext.clone("+this.getSimpleName()+".class,this);");
		for (PojoProperty p : dbProps.values()) {
			code.ln(2,p.makeAssignmentCode("this","inst"));
		}

		if(!otherProps.isEmpty()) {
			code.ln(2, "// others");
			for (PojoProperty p : otherProps.values()) {
				code.ln(3, p.makeAssignmentCode("this", "inst"));
			}
		}

		code.ln(2,"return inst;");
		code.ln(1,"}");



		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 克隆当前对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public "+this.getSimpleName()+" clone() {");
		code.ln(2,"return BeanUtil.clone(this,false);");
		code.ln(1,"}");

		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 克隆当前对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public "+this.getSimpleName()+" clone(boolean deep) {");
		code.ln(2,"return BeanUtil.clone(this,deep);");
		code.ln(1,"}");

		this.addImport(Entity.class);
		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将自己转换成任意指定类型");
		code.ln(1," * @param pojoType  Pojo类型");
		code.ln(1," * @return "+this.getSimpleName()+" , 转换好的 PoJo 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public <T> T toPojo(Class<T> pojoType) {");
		code.ln(2,"if(Entity.class.isAssignableFrom(pojoType)) {");
		code.ln(3,"return (T)EntityContext.create((Class<? extends Entity>) pojoType,this);");
		code.ln(2,"}");
		code.ln(2,"try {");
		code.ln(3,"T pojo=pojoType.newInstance();");
		code.ln(3,"EntityContext.copyProperties(pojo, this);");
		code.ln(3,"return pojo;");
		code.ln(2,"} catch (Exception e) {");
		code.ln(3,"throw new RuntimeException(e);");
		code.ln(2,"}");
		code.ln(1,"}");

	}

	private void buildOthers4Entity() {


		String prop=context.getPoClassFile().getVar();
		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将 Map 转换成 "+this.getSimpleName());
		code.ln(1," * @param "+prop+"Map 包含实体信息的 Map 对象");
		code.ln(1," * @return "+this.getSimpleName()+" , 转换好的的 "+context.getPoClassFile().getSimpleName()+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+this.getSimpleName()+" createFrom(Map<String,Object> "+prop+"Map) {");
		code.ln(2,"if("+prop+"Map==null) return null;");
		code.ln(2,this.getSimpleName()+" po = create();");
		code.ln(2,"EntityContext.copyProperties(po,"+prop+"Map);");
		code.ln(2,"po.clearModifies();");
		code.ln(2,"return po;");
		code.ln(1,"}");


		this.addImport(Map.class);
		this.addImport(EntityContext.class);


		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将 Pojo 转换成 "+this.getSimpleName());
		code.ln(1," * @param pojo 包含实体信息的 Pojo 对象");
		code.ln(1," * @return "+this.getSimpleName()+" , 转换好的的 "+context.getPoClassFile().getSimpleName()+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+this.getSimpleName()+" createFrom(Object pojo) {");
		code.ln(2,"if(pojo==null) return null;");
		code.ln(2,this.getSimpleName()+" po = create();");
		code.ln(2,"EntityContext.copyProperties(po,pojo);");
		code.ln(2,"po.clearModifies();");
		code.ln(2,"return po;");
		code.ln(1,"}");


		Map<String,PojoProperty> otherProps=new HashMap<>();
		Map<String,PojoProperty> dbProps=new HashMap<>();
		List<PojoProperty> props= this.getSuperProperties();
		for (PojoProperty p : props) {
			if(p.isFromTable()) {
				dbProps.put(p.name(), p);
			} else {
				otherProps.put(p.name(), p);
			}
		}
		props= this.getProperties();
		for (PojoProperty p : props) {
			if(p.isFromTable()) {
				dbProps.put(p.name(), p);
			} else {
				otherProps.put(p.name(), p);
			}
		}

		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 复制当前对象");
		code.ln(1," * @param all 是否复制全部属性，当 false 时，仅复制来自数据表的属性");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public "+this.getSimpleName()+" duplicate(boolean all) {");
		code.ln(2,this.metaClassFile.getFullName()+".$$proxy$$"+" inst = new "+this.metaClassFile.getFullName()+".$$proxy$$();");
		//code.ln(2,"return EntityContext.clone("+this.getSimpleName()+".class,this);");
		for (PojoProperty p : dbProps.values()) {
			code.ln(2,p.makeAssignmentCode("this","inst"));
		}
		if(!otherProps.isEmpty()) {
			code.ln(2, "if(all) {");
			for (PojoProperty p : otherProps.values()) {
				code.ln(3, p.makeAssignmentCode("this", "inst"));
			}
			code.ln(2,"}");
		}
		code.ln(2,"inst.clearModifies();");
		code.ln(2,"return inst;");
		code.ln(1,"}");

		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 克隆当前对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public "+this.getSimpleName()+" clone() {");
		code.ln(2,"return duplicate(true);");
		code.ln(1,"}");

		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 克隆当前对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public "+this.getSimpleName()+" clone(boolean deep) {");
		code.ln(2,"return EntityContext.clone("+this.getSimpleName()+".class,this,deep);");
		code.ln(1,"}");

		this.addImport(Entity.class);
		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将自己转换成任意指定类型");
		code.ln(1," * @param pojoType  Pojo类型");
		code.ln(1," * @return "+this.getSimpleName()+" , 转换好的 PoJo 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public <T> T toPojo(Class<T> pojoType) {");
		code.ln(2,"if(Entity.class.isAssignableFrom(pojoType)) {");
		code.ln(3,"return (T)EntityContext.create((Class<? extends Entity>) pojoType,this);");
		code.ln(2,"}");
		code.ln(2,"try {");
		code.ln(3,"T pojo=pojoType.newInstance();");
		code.ln(3,"EntityContext.copyProperties(pojo, this);");
		code.ln(3,"return pojo;");
		code.ln(2,"} catch (Exception e) {");
		code.ln(3,"throw new RuntimeException(e);");
		code.ln(2,"}");
		code.ln(1,"}");

		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 创建一个 "+this.getSimpleName()+"，等同于 new");
		code.ln(1," * @return "+this.getSimpleName()+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+this.getSimpleName()+" create() {");
		code.ln(2,"return new "+this.metaClassFile.getFullName()+".$$proxy$$();");
		code.ln(1,"}");

	}



	protected String getApiModelSuperTypeName() {
		return this.getSuperTypeSimpleName();
	}

	protected void buildClassStartPart() {


		if(this.getContext().getSettings().isEnableSwagger()) {
			this.addImport(ApiModel.class);
			String apiModel="@ApiModel(";
			List<String> parts=new ArrayList<>();

			if(this.getDesc()!=null || this.getTitle()!=null) {
				String dstr="description = \"";
				if(this.getTitle()!=null) {
					dstr+=getTitle();
				}
				if(this.getDesc()!=null) {
					dstr+=" ; "+getDesc();
				}
				dstr+="\"";
				parts.add(dstr);
			}
			if(this.getApiModelSuperTypeName()!=null && !Entity.class.equals(this.getSuperType())) {
				parts.add("parent = "+this.getApiModelSuperTypeName()+".class");
			}
			apiModel+= StringUtil.join(parts," , ");
			apiModel+=")";
			code.ln(apiModel);
		}

		code.ln("public class "+this.getSimpleName()+(this.getSuperTypeSimpleName()==null?"":(" extends "+this.getSuperTypeSimpleName()))+" {");

		code.ln("");
		code.ln(1,"private static final long serialVersionUID = 1L;");

	}

	protected void buildClassJavaDoc() {

		//加入注释
		code.ln("/**");
		if(this.getTitle()==null) {
			code.ln(" * " + this.getDoc());
		} else {
			code.ln(" * " + this.getTitle());
		}
		if(this.getDesc()!=null) {
			code.ln(" * <p>" + this.getDesc()+"</p>");
		}
		code.ln(" * @author "+this.context.getSettings().getAuthor());
		code.ln(" * @since "+DateUtil.getFormattedTime(false));
		code.ln(" * @sign "+this.getSign());
		code.ln(" * 此文件由工具自动生成，请勿修改。若表结构或配置发生变动，请使用工具重新生成。");
		code.ln("*/");
		code.ln("");

	}

	protected void buildClassEndPart() {
		code.ln("}");
	}

	protected void buildGetterAndSetter() {
		for (PojoProperty prop : properties) {
			this.code.append(prop.getGetterCode(1));
			this.code.append(prop.getSetterCode(1));
		}
	}

	private void buildProperties() {
		for (PojoProperty prop : properties) {
			this.code.append(prop.getDefineCode(1));
		}
	}

	public String getSign() {
		String sign=this.getSuperTypeSimpleName()+"|"+this.getDoc()+"|"+this.isExtendsEntity()+"|";
		for (PojoProperty prop : properties) {
			sign+=prop.getSign()+",";
			if(prop.getShadow()!=null) {
				sign+=prop.getShadow().getSign();
			}
		}
		return MD5Util.encrypt32(sign);
	}

	@Override
	public void save(boolean override) {
		if(this.context.getSettings().isRebuildEntity()) {
			override=true;
		} else {
			override=isSignatureChanged();
		}
		super.save(override);
	}

	private Boolean isSignatureChanged=null;
	/**
	 * 判断签名是否变化
	 * */
	public boolean isSignatureChanged() {
		if(isSignatureChanged!=null) return isSignatureChanged;
		File sourceFile=this.getSourceFile();
		String sign=this.getSign();
		if(!sourceFile.exists())  return true;
		String s="";
		String str=FileUtil.readText(sourceFile);
		String[] lns=str.split("\\n");
		for (String ln : lns) {
			ln=ln.trim();
			if(ln.startsWith("* @sign ")) {
				s=ln.substring(8);
				break;
			}
		}
		isSignatureChanged = !s.equals(sign);
		return isSignatureChanged;
	}

	public List<PojoProperty> getProperties() {
		return properties;
	}

	public String getDoc() {
		return doc;
	}

	public void setDoc(String doc) {
		this.doc = doc;
	}


	/**
	 * 获得所有父类属性
	 * */
	public List<PojoProperty> getSuperProperties() {
		List<PojoProperty> properties=new ArrayList<>();
		if(this.getSuperType()!=null) {
			if(this.getSuperType().equals(Entity.class)) return properties;
			if(this.getSuperType().equals(Object.class)) return properties;

			List<Field> fields=BeanUtil.getAllFields(this.getSuperType());
			for (Field field : fields) {
				if(field==null) continue;
				if(field.getName().equals("serialVersionUID")) continue;
				PojoProperty p=null;
				if(field.getType().equals(Map.class)) {
					System.out.println();
				} else if(field.getType().equals(List.class)) {
					System.out.println();
				} else {
					p=PojoProperty.simple(field.getType(),field.getName(),field.getName(),field.getName());
				}
				p.setClassFile(this);
				properties.add(p);
			}
		} else if(this.getSuperTypeFile()!=null) {
			PojoClassFile parent=(PojoClassFile)this.getSuperTypeFile();
			properties.addAll(parent.getProperties());
			properties.addAll(parent.getSuperProperties());
		}
		return properties;
	}

}
