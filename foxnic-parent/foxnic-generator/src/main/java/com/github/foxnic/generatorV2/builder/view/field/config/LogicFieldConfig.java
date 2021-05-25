package com.github.foxnic.generatorV2.builder.view.field.config;

import com.github.foxnic.sql.meta.DBField;

public class LogicFieldConfig extends FieldConfig {
 
	private String labelOn;
	private Object valueOn;
	
	private String labelOff;
	private Object valueOff;
	
	
	public LogicFieldConfig(DBField field) {
		super(field);
	}

	public LogicFieldConfig on(String label,Object value) {
		labelOn=label;
		valueOn=value;
		return this;
	}
	
	public LogicFieldConfig off(String label,Object value) {
		labelOff=label;
		valueOff=value;
		return this;
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
