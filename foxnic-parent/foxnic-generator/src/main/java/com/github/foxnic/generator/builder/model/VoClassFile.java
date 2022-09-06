package com.github.foxnic.generator.builder.model;

import com.github.foxnic.api.model.CompositeParameter;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;

import javax.persistence.Transient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoClassFile extends PojoClassFile {

	public VoClassFile(PoClassFile poClassFile) {
		super(poClassFile.context,poClassFile.getProject(), poClassFile.getPackageName(), poClassFile.getSimpleName()+"VO");
		this.setSuperTypeFile(poClassFile);

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




	}

	private PojoProperty idsProperty;

	public void setIdsPropertyName(PojoProperty p) {
		idsProperty=p;
	}

	public PojoProperty getIdsProperty() {
		return idsProperty;
	}







}
