package com.github.foxnic.generator.builder.model;

import com.github.foxnic.api.model.CompositeParameter;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.sql.data.ExprRcd;

import javax.persistence.Transient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoClassFile extends PojoClassFile {

	public VoClassFile(PoClassFile poClassFile) {
		super(poClassFile.context,poClassFile.getProject(), poClassFile.getPackageName(), poClassFile.getSimpleName()+"VO");
		this.setSuperTypeFile(poClassFile);

		this.setTitle(this.getContext().getTopic()+"VO类型");
		this.setDesc(this.getContext().getTopic()+" , 数据表 "+this.getContext().getTableMeta().getTableName()+" 的通用VO类型");


	}

	@Override
	protected void buildClassStartPart() {

		super.buildClassStartPart();

	}

	@Override
	protected void buildOthers() {


		this.code.ln(1,"@Transient");
		this.code.ln(1,"private transient CompositeParameter $compositeParameter;");

		this.code.ln(1,"/**");
		this.code.ln(1," * 获得解析后的复合查询参数");
		this.code.ln(1," */");
		this.code.ln(1,"@Transient");
		this.code.ln(1,"public CompositeParameter getCompositeParameter() {");
		this.code.ln(2,"if($compositeParameter!=null) return  $compositeParameter;");
		//this.code.ln(2,"if(!\"$composite\".equals(this.getSearchField())) return null;");
		this.code.ln(2,"$compositeParameter=new CompositeParameter(this.getSearchValue(),BeanUtil.toMap(this));");
		this.code.ln(2,"return  $compositeParameter;");
		this.code.ln(1,"}");
		this.addImport(CompositeParameter.class);
		this.addImport(Transient.class);
		this.addImport(BeanUtil.class);
		this.addImport(EntityContext.class);



		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将自己转换成指定类型的PO");
		code.ln(1," * @param poType  PO类型");
		code.ln(1," * @return "+this.getSimpleName()+" , 转换好的 "+this.getSimpleName()+" 对象");
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
		code.ln(1," * @return "+this.getSimpleName()+" , 转换好的 PoJo 对象");
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




		Map<String,PojoProperty> otherProps=new HashMap<>();
		Map<String,PojoProperty> dbProps=new HashMap<>();
		List<PojoProperty> props= this.getSuperProperties();
		for (PojoProperty prop : props) {
			if(prop.isFromTable()) {
				dbProps.put(prop.name(), prop);
			} else {
				otherProps.put(prop.name(), prop);
			}
		}
		props= this.getProperties();
		for (PojoProperty prop : props) {
			if(prop.isFromTable()) {
				dbProps.put(prop.name(), prop);
			} else {
				otherProps.put(prop.name(), prop);
			}
		}


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
		code.ln(1,"public "+this.getSimpleName()+" clone(boolean deep) {");
		code.ln(2,"return EntityContext.clone("+this.getSimpleName()+".class,this,deep);");
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
		code.ln(2,this.getSimpleName()+" vo = create();");
		code.ln(2,"EntityContext.copyProperties(vo,"+prop+"Map);");
		code.ln(2,"vo.clearModifies();");
		code.ln(2,"return vo;");
		code.ln(1,"}");


		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 将 Pojo 转换成 "+this.getSimpleName());
		code.ln(1," * @param pojo 包含实体信息的 Pojo 对象");
		code.ln(1," * @return "+this.getSimpleName()+" , 转换好的的 "+context.getPoClassFile().getSimpleName()+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+this.getSimpleName()+" createFrom(Object pojo) {");
		code.ln(2,"if(pojo==null) return null;");
		code.ln(2,this.getSimpleName()+" vo = create();");
		code.ln(2,"EntityContext.copyProperties(vo,pojo);");
		code.ln(2,"vo.clearModifies();");
		code.ln(2,"return vo;");
		code.ln(1,"}");
		this.addImport(Map.class);


		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 创建一个 "+this.getSimpleName()+"，等同于 new");
		code.ln(1," * @return "+this.getSimpleName()+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+this.getSimpleName()+" create() {");
		code.ln(2,"return new "+this.metaClassFile.getFullName()+".$$proxy$$();");
		code.ln(1,"}");


		// read 方法
		this.addImport(metaClassFile.getFullName());
		this.addImport(DataParser.class);
		code.ln("");
		code.ln(1, "/**");
		code.ln(1, " * 从 Map 读取");
		code.ln(1, " * @param map 记录数据");
		code.ln(1, " * @param cast 是否用 DataParser 进行类型转换");
		code.ln(1, " * @return  是否读取成功");
		code.ln(1, "*/");
		code.ln(1, "public boolean read(Map<String, Object> map,boolean cast) {");
		code.ln(2, "if(map==null) return false;");
		code.ln(2, "if(cast) {");
		for (PojoProperty p : dbProps.values()) {
			//code.ln(3, "this." + p.name() + " = "+ "DataParser.parse("+p.getTypeName()+".class, map.get(" + metaClassFile.getSimpleName()+ "." + p.getNameConstants()+ "))"+";");
			code.ln(3,p.makeAssignmentSetCode("this","DataParser.parse("+p.getTypeName()+".class, map.get(" + metaClassFile.getSimpleName()+ "." + p.getNameConstants()+ "))"));
		}
		code.ln(3, "// others");
		for (PojoProperty p : otherProps.values()) {
			if (p.isSimple()) {
				//code.ln(3, "this." + p.name() + " =  DataParser.parse("+p.getTypeName()+".class, map.get(" + metaClassFile.getSimpleName()+ "." + p.getNameConstants()+ "));");
				code.ln(3,p.makeAssignmentSetCode("this","DataParser.parse("+p.getTypeName()+".class, map.get(" + metaClassFile.getSimpleName()+ "." + p.getNameConstants()+ "))"));
			}
		}
		code.ln(3, "return true;");
		code.ln(2, "} else {");
		code.ln(3, "try {");
		for (PojoProperty p : dbProps.values()) {
			this.addImport(p.getTypeFullName());
			//code.ln(4, "this." + p.name() + " = "+" (" + p.getTypeName() + ")map.get(" + metaClassFile.getSimpleName()+ "." + p.getNameConstants()+ ")"+";");
			code.ln(4,p.makeAssignmentSetCode("this"," (" + p.getTypeName() + ")map.get(" + metaClassFile.getSimpleName()+ "." + p.getNameConstants()+ ")"));
		}
		code.ln(4, "// others");
		for (PojoProperty p : otherProps.values()) {
			if (p.isSimple()) {
				this.addImport(p.getTypeFullName());
				//code.ln(4, "this." + p.name() + " = map.get("+ metaClassFile.getSimpleName()+ "." + p.getNameConstants()+");");
				code.ln(4,p.makeAssignmentSetCode("this"," (" + p.getTypeName() + ")map.get(" + metaClassFile.getSimpleName()+ "." + p.getNameConstants()+ ")"));
			}
		}
		code.ln(4, "return true;");
		code.ln(3, "} catch (Exception e) {");
		code.ln(4,"return false;");
		code.ln(3,"}");
		code.ln(2,"}");
		code.ln(1,"}");


		// read 方法
		this.addImport(metaClassFile.getFullName());
		this.addImport(DataParser.class);
		this.addImport(ExprRcd.class);
		code.ln("");
		code.ln(1, "/**");
		code.ln(1, " * 从 Map 读取");
		code.ln(1, " * @param r 记录数据");
		code.ln(1, " * @param cast 是否用 DataParser 进行类型转换");
		code.ln(1, " * @return  是否读取成功");
		code.ln(1, "*/");
		code.ln(1, "public boolean read(ExprRcd r,boolean cast) {");
		code.ln(2, "if(r==null) return false;");
		code.ln(2, "if(cast) {");
		for (PojoProperty p : dbProps.values()) {
			//code.ln(3, "this." + p.name() + " =  DataParser.parse("+p.getTypeName()+".class, r.getValue(" + metaClassFile.getSimpleName()+ "." + p.getNameConstants()+ "));");
			code.ln(3,p.makeAssignmentSetCode("this","DataParser.parse("+p.getTypeName()+".class, r.getValue(" + metaClassFile.getSimpleName()+ "." + p.getNameConstants()+ "))"));
		}
//		记录里面只会有常规字段
//		code.ln(3, "// others");
//		for (PojoProperty p : otherProps.values()) {
//			if (p.isSimple()) {
//				//code.ln(3, "this." + p.name() + " =  DataParser.parse("+p.getTypeName()+".class, r.getValue(" + metaClassFile.getSimpleName()+ "." + p.getNameConstants()+ "));");
//				code.ln(3,p.makeAssignmentSetCode("this","DataParser.parse("+p.getTypeName()+".class, r.getValue(" + metaClassFile.getSimpleName()+ "." + p.getNameConstants()+ "))"));
//			}
//		}
		code.ln(3, "return true;");
		code.ln(2, "} else {");
		code.ln(3, "try {");
		for (PojoProperty p : dbProps.values()) {
			//code.ln(4, "this." + p.name() + " = (" + p.getTypeName() + ")r.getValue(" + metaClassFile.getSimpleName()+ "." + p.getNameConstants()+ ");");
			code.ln(4,p.makeAssignmentSetCode("this"," (" + p.getTypeName() + ")r.getValue(" + metaClassFile.getSimpleName()+ "." + p.getNameConstants()+ ")"));
		}
//		记录里面只会有常规字段
//		code.ln(4, "// others");
//		for (PojoProperty p : otherProps.values()) {
//			if (p.isSimple()) {
//				//code.ln(4, "this." + p.name() + " = r.getValue("+ metaClassFile.getSimpleName()+ "." + p.getNameConstants()+");");
//				code.ln(4,p.makeAssignmentSetCode("this"," (" + p.getTypeName() + ")r.getValue(" + metaClassFile.getSimpleName()+ "." + p.getNameConstants()+ ")"));
//
//			}
//		}
		code.ln(4, "return true;");
		code.ln(3, "} catch (Exception e) {");
		code.ln(4,"return false;");
		code.ln(3,"}");
		code.ln(2,"}");
		code.ln(1,"}");



	}

	private PojoProperty idsProperty;

	public void setIdsPropertyName(PojoProperty p) {
		idsProperty=p;
	}

	public PojoProperty getIdsProperty() {
		return idsProperty;
	}







}
