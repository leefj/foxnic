package com.github.foxnic.generator;

import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.lang.StringUtil;

public class DtoConfig {
	
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
	/**
	 * 继承的父类
	 * */
	private String superClass=null;
	
	private List<Property> properties=new ArrayList<>();
 
	public String getSuperClass() {
		return superClass;
	}
	
	/**
	 * @param superClass  默认值 null ，此时继承子PO；若指定空字符串则不进行任何继承
	 * */
	public void setSuperClass(String superClass) {
		this.superClass = superClass;
	}
	
	
	public DtoConfig addProperty(String name,Class type,String label,String note) {
		properties.add(new Property(name, type, label, note));
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
	}
	
	
	
}
