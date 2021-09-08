package com.github.foxnic.generator.builder.view.field.config;

import com.github.foxnic.sql.meta.DBField;

public class LogicFieldConfig extends FieldConfig {
 
	private String labelOn;
	private Object valueOn;
	
	private String labelOff;
	private Object valueOff;
	
	
	public LogicFieldConfig(DBField field) {
		super(field);
	}

	/**
	 * 设置开启状(逻辑真)态下的标签与值
	 * */
	public LogicFieldConfig on(String label,Object value) {
		labelOn=label;
		valueOn=value;
		return this;
	}

	/**
	 * 设置关闭(逻辑假)状态下的标签与值
	 * */
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

	public boolean getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(boolean defaultValue) {
		this.defaultValue = defaultValue;
	}

	private boolean defaultValue=false;
	
}
