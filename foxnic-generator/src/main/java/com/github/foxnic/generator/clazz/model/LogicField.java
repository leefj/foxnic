package com.github.foxnic.generator.clazz.model;

import com.github.foxnic.sql.meta.DBField;

public class LogicField {

	private DBField field;
	
	private String labelOn;
	private Object valueOn;
	
	private String labelOff;
	private Object valueOff;
	
	
	public LogicField(DBField fieldValid) {
		this.field=fieldValid;
	}

	public LogicField on(String label,Object value) {
		labelOn=label;
		valueOn=value;
		return this;
	}
	
	public LogicField off(String label,Object value) {
		labelOff=label;
		valueOff=value;
		return this;
	}

	public DBField getField() {
		return field;
	}

	public String getLabelOn() {
		return labelOn;
	}

	public Object getValueOn() {
		return valueOn;
	}

	public String getLabelOff() {
		return labelOff;
	}

	public Object getValueOff() {
		return valueOff;
	}
	
}
