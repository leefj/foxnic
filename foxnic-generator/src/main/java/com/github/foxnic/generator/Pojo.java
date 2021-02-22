package com.github.foxnic.generator;

import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.lang.StringUtil;

public class Pojo {
	
	 
	
	public static class Property {
		private String name=null;
		private Class type=null;
		private String label=null;
		private String note=null;
		
		public Property(String name,Class type,String label,String note) {
			this.name=name;
			this.type=type;
			this.label=label;
			this.note=note;
		}

		public String getName() {
			return name;
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
	
	/**
	 * @param superClass  默认值 null ，此时继承子PO；若指定空字符串则不进行任何继承
	 * */
	public void setSuperClass(String superClass) {
		this.superClass = superClass;
	}
	
	
	public Pojo addProperty(String name,Class type,String label,String note) {
		properties.add(new Property(name, type, label, note));
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

	public void setName(String name) {
		this.name = name;
		this.className=name;
	}


	private String pkgName=null;
	
	void bind(String name,String pkgName) {
		if(name!=null) {
			this.setName(name);
		}
		this.pkgName=pkgName;
	}
	
	
	public String getClassName() {
		return className;
	}
	
	public String getFullName() {
		return this.getPackage()+"."+this.getClassName();
	}
	
	public String getVarName() {
		return this.className.substring(0,1).toLowerCase()+this.className.substring(1);
	}
	
	public String getPackage() {
		return this.pkgName;
	}

	public String getTemplateSQL() {
		return templateSQL;
	}

	public void setTemplateSQL(String templateSQL) {
		this.templateSQL = templateSQL;
	}
	
	
	
}
