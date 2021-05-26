package com.github.foxnic.generator.builder.model;


import java.util.List;
import java.util.Map;

import javax.persistence.Table;
import javax.persistence.Transient;

import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.relation.PropertyRoute;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;

public class PoClassFile extends PojoClassFile {

	private DBTable table;
//	private String tablePrefix=null;
//	private String sign=null;
	
	private List<PropertyRoute> propsJoin;
 
	private PojoProperty idProperty;
	
	public PoClassFile(ModuleContext context,MavenProject project, String packageName, DBTable table, String tablePrefix) {
		super(context,project, packageName, nameConvertor.getClassName(table.name().substring(tablePrefix.length()),0));
//		this.tablePrefix=tablePrefix;
		this.table=table;
		this.setSuperType(Entity.class);
		//属性清单
		DBTableMeta tm=context.getTableMeta();
		for (DBColumnMeta f : tm.getColumns()) {
			PojoProperty prop=PojoProperty.simple(f.getDBDataType().getType(), f.getColumnVarName(), f.getLabel(), f.getDetail());
			prop.setPK(f.isPK());
			prop.setAutoIncrease(f.isAutoIncrease());
			prop.setNullable(f.isNullable());
			this.addProperty(prop);
			if(idProperty==null && prop.isPK()) {
				idProperty=prop;
			}
		}
		
		this.setSuperType(Entity.class);
		
	}
	
	@Override
	protected void buildClassStartPart() {
		
		
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
		code.ln(2,this.getSimpleName()+" po = EntityContext.create("+this.getSimpleName()+".class, "+prop+"Map);");
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
		code.ln(2,this.getSimpleName()+" po = EntityContext.create("+this.getSimpleName()+".class,pojo);");
		code.ln(2,"return po;");
		code.ln(1,"}");


		code.ln("");
		code.ln(1,"/**");
		code.ln(1," * 创建一个 "+this.getSimpleName()+"，等同于 new");
		code.ln(1," * @return "+this.getSimpleName()+" 对象");
		code.ln(1,"*/");
		code.ln(1,"@Transient");
		code.ln(1,"public static "+this.getSimpleName()+" create() {");
		code.ln(2,"return new "+this.getSimpleName()+"();");
		code.ln(1,"}");
	}

	public void setPropsJoin(List<PropertyRoute> propsJoin) {
		this.propsJoin = propsJoin;
	}

	/**
	 * 联合主键，则取第一个
	 * */
	public PojoProperty getIdProperty() {
		return idProperty;
	}

	
	
	

}
