package com.github.foxnic.generator.builder.view.field.config;

public class RadioBoxConfig {
	private String dict;
	private Class enumType;
	
	public String getDict() {
		return dict;
	}
	public void dict(String dict) {
		this.dict = dict;
	}
	public String getEnumTypeName() {
		return enumType.getName();
	}
	public void enumType(Class enumType) {
		this.enumType = enumType;
	}
}
