package com.github.foxnic.generator;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class Pojo {
	
	private static BeanNameUtil beanNameUtil=new BeanNameUtil();
	
	public static class Property {
		private String name=null;
		private Class type=null;
		private String typeName=null;
		private String label=null;
		private String note=null;
		private String cata="default";  //default 默认 ; list  列表
		public Property(String name,Class type,String label,String note) {
			this.name=name;
			this.type=type;
			this.label=label;
			this.note=note;
		}

		public String getName() {
			return name;
		}
		
		public String getNameConst() {
			return "PROP_"+beanNameUtil.depart(name).toUpperCase();
		}

		public Class getType() {
			return type;
		}

		public String getLabel() {
			return label;
		}

		public String getNote() {
			return note;
		}
		
		public boolean hasNote() {
			return !StringUtil.isBlank(this.note);
		}

		public String getCata() {
			return cata;
		}

		public void setCata(String cata) {
			this.cata = cata;
		}

		public String getTypeName() {
			return typeName;
		}

		public void setTypeName(String typeName) {
			this.typeName = typeName;
		}
 
	}
	
	
	private String name=null;
	private String className=null;
	/**
	 * 继承的父类
	 * */
	private String superClass=null;
	
	private String templateSQL;
	
	private List<Property> properties=new ArrayList<>();
	 

	public Pojo() { }

	public String getSuperClass() {
		return superClass;
	}
	
	public String getMetaSuperClass() {
		if(StringUtil.isBlank(superClass)) return null;
		//如果继承Entity,则Meta类无需继承
		if(Entity.class.getName().equals(this.superClass)) {
			return null;
		}
		return superClass+"Meta";
	}
	
	/**
	 * @param superClass  默认值 null ，此时继承子PO；若指定空字符串则不进行任何继承
	 * */
	public void setSuperClass(String superClass) {
		if(superClass!=null && StringUtil.isBlank(superClass)) {
			superClass= Entity.class.getName();
		}
		this.superClass = superClass;
	}

	public Pojo addProperty(String name,Class type,String label,String note) {
		properties.add(new Property(name, type, label, note));
		return this;
	}
	
	public Pojo addListProperty(String name,Class componentType,String label,String note) {
		Property p=new Property(name, componentType, label, note);
		p.setCata("list");
		properties.add(p);
		return this;
	}
	
	public Pojo addListProperty(String name,String componentType,String label,String note) {
		Property p=new Property(name, null, label, note);
		p.setTypeName(componentType);
		p.setCata("list");
		properties.add(p);
		return this;
	}
	
	/**
	 * 覆盖原有的配置
	 * */
	public Pojo setProperty(String name,Class type,String label,String note) {
		for (Property property : properties) {
			if(name.equals(property.getName())) {
				property.type=type;
				property.label=label;
				property.note=note;
			}
		}
		return this;
	}

	public List<Property> getProperties() {
		return properties;
	}

	public String getName() {
		return name;
	}
	
	public String getMetaName() {
		return name+"Meta";
	}

	public void setName(String name) {
		this.name = name;
		this.className=name;
	}


	private String pkgName=null;
	private String metaPkgName=null;
	
	void bind(String name,String pkgName) {
		if(name!=null) {
			this.setName(name);
		}
		this.pkgName=pkgName;
		this.metaPkgName=pkgName+".meta";
	}
	
	
	public String getClassName() {
		return className;
	}
	
	public String getFullName() {
		return this.getPackage()+"."+this.getClassName();
	}
	
	public String getMetaFullName() {
		return this.getMetaPackage()+"."+this.getMetaName();
	}
	
	public String getVarName() {
		return this.className.substring(0,1).toLowerCase()+this.className.substring(1);
	}
	
	public String getPackage() {
		return this.pkgName;
	}
	
	public String getMetaPackage() {
		return this.metaPkgName;
	}

	public String getTemplateSQL() {
		return templateSQL;
	}

	public void setTemplateSQL(String templateSQL) {
		this.templateSQL = templateSQL;
	}
	
	
	
}
