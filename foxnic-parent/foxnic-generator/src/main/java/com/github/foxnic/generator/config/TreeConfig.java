package com.github.foxnic.generator.config;

import com.github.foxnic.sql.meta.DBField;

public class TreeConfig {
 
	private Object rootId=null;
	private DBField idField;
	private DBField nameField;
	private DBField parentIdField;
	private String dimension;
	
	public DBField getIdField() {
		return idField;
	}
	
	public void setIdField(DBField idField) {
		this.idField = idField;
	}
	
	public DBField getParentIdField() {
		return parentIdField;
	}
	
	public void setParentIdField(DBField parentIdField) {
		this.parentIdField = parentIdField;
	}
	
	public String getDimension() {
		return dimension;
	}
	
	public void setDimension(String dimension) {
		this.dimension = dimension;
	}
	
	public DBField getNameField() {
		return nameField;
	}
	
	public void setNameField(DBField nameField) {
		this.nameField = nameField;
	}
	
	public Object getRootId() {
		return rootId;
	}
	
	public void setRootId(Object rootId) {
		this.rootId = rootId;
	}

}
