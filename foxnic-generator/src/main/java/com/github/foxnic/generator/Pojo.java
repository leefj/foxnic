package com.github.foxnic.generator;

import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.lang.StringUtil;

public class Pojo {
	
	public static enum PojoType {
		VO,BO,DO,DTO;
	}
	
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
	
	private String parentPackage;
	
	private List<Property> properties=new ArrayList<>();
 
	private PojoType type;
	
	public Pojo(PojoType type) {
		this.type=type;
	}

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

	public List<Property> getProperties() {
		return properties;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		if(!name.endsWith(this.getType().name())) {
			while(true) {
				char c=name.charAt(name.length()-1);
				if(Character.isUpperCase(c)) {
					name=name.substring(0,name.length()-1);
				} else {
					break;
				}
			}
			this.className=name+this.getType().name();
		} else {
			this.className=name;
		}
	}

	public PojoType getType() {
		return type;
	}

	public void setType(PojoType type) {
		this.type = type;
	}

	private String pkg=null;
	
	void bind(String name,String parentPackage) {
		if(name!=null) {
			this.setName(name);
		}
		this.parentPackage=parentPackage;
		this.pkg=this.parentPackage+"."+this.getType().name().toLowerCase();
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
		return this.pkg;
	}
	
	
	
}
