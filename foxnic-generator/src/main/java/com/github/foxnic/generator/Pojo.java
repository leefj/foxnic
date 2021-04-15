package com.github.foxnic.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.entity.Entity;

public class Pojo {
	
	private static BeanNameUtil beanNameUtil=new BeanNameUtil();
	
	public static class Property {
		private String name=null;
		private Class type=null;
		private Class keyType=null;
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
			return beanNameUtil.depart(name).toUpperCase();
		}

		public Class getType() {
			if(type!=null) {
				return type;
			}
			if("list".equals(this.cata)) {
				return List.class;
			}
			else if("map".equals(this.cata)) {
				return Map.class;
			}
			return null;
			
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

		public void setMapKeyType(Class keyType) {
			this.keyType=keyType;
		}

		public Class getMapKeyType() {
			return keyType;
		}

		
 
	}
	
	
	private String name=null;
	private String className=null;
	private String doc;
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
	
	public String getDoc() {
		return doc;
	}

	public void setDoc(String doc) {
		this.doc = doc;
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
	
	
	public void addMapProperty(String name, Class keyType, Class valueType, String label,String note) {
		Property p=new Property(name, valueType, label, note);
		p.setCata("map");
		p.setMapKeyType(keyType);
		properties.add(p);
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

	public String getSignature() {
		List<String> list=new ArrayList<>();
		
		list.add("pojo:");
		//
		list.add(this.name);
		list.add(this.className);
		list.add(doc);
		list.add(this.metaPkgName);
		list.add(this.pkgName);
		list.add(this.superClass);
		list.add(this.templateSQL);
		//
		list.add("props:");
		for (Property p : this.properties) {
			list.add(p.cata);
			list.add(p.label);
			list.add(p.name);
			list.add(p.note);
			list.add(p.typeName);
			list.add(p.getType().getName());
		}
		String str=StringUtil.join(list,"|");
		return MD5Util.encrypt32(str);
	}

	
	
	
	
}
