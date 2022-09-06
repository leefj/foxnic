package com.github.foxnic.generator.builder.model;


import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.relation.PropertyRoute;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.sql.meta.DBTable;

import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.*;

public class PoClassFile extends PojoClassFile {

	private DBTable table;

	private List<PropertyRoute> propsJoin;

	private PojoProperty idProperty;


	public PoClassFile(ModuleContext context, MavenProject project, String packageName, DBTable table, String tablePrefix) {
		super(context,project, packageName, nameConvertor.getClassName(table.name().substring(tablePrefix.length()),0));
		this.table=table;
		this.setSuperType(Entity.class);
		//属性清单
		DBTableMeta tm=context.getTableMeta();
		for (DBColumnMeta f : tm.getColumns()) {
			PojoProperty prop=PojoProperty.simple(f.getDBDataType().getType(), f.getColumnVarName(), f.getLabel(), f.getDetail());
			prop.setPK(f.isPK());
			prop.setFromTable(true);
			prop.setAutoIncrease(f.isAutoIncrease());
			prop.setNullable(f.isNullable());
			prop.setClassFile(this);
			this.addProperty(prop);
			if(idProperty==null && prop.isPK()) {
				idProperty=prop;
			}
		}

		DBColumnMeta deletedColumn=tm.getColumn(context.getDAO().getDBTreaty().getDeletedField());
		if(deletedColumn!=null) {
			this.shadowBoolean(deletedColumn.getColumn());
		}

		this.setSuperType(Entity.class);

	}

	@Override
	protected void buildClassStartPart() {

		code.ln("@Table(name = \""+this.table.name()+"\")");
		super.buildClassStartPart();

		code.ln("");
		code.ln(1,"public static final DBTable TABLE ="+table.getClass().getSimpleName()+".$TABLE;");

		this.addImport(Table.class);
		this.addImport(DBTable.class);
		this.addImport(table.getClass().getName().replace('$', '.'));
	}


	@Override
	protected void buildOthers() {

		this.addImport(Transient.class);

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

	public void setPropsJoin(List<PropertyRoute> propsJoin) {
		this.propsJoin = propsJoin;
		if(this.propsJoin!=null) {
			for (PropertyRoute pr : propsJoin) {
				PojoProperty prop=null;
				if(pr.isList()) {
					prop=PojoProperty.list(pr.getType(), pr.getProperty(), pr.getLabel(), pr.getDetail());
					this.addImport(List.class);
				} else {
					prop=PojoProperty.simple(pr.getType(), pr.getProperty(), pr.getLabel(), pr.getDetail());
				}
				prop.setPK(false);
				prop.setAutoIncrease(false);
				prop.setNullable(true);
				this.addProperty(prop);
			}
		}
	}

	/**
	 * 联合主键，则取第一个
	 * */
	public PojoProperty getIdProperty() {
		return idProperty;
	}





}
