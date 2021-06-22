package com.github.foxnic.generator.builder.view.field.config;

import com.github.foxnic.api.constant.CodeTextEnum;

public class RadioBoxConfig {
	private String dict;
	private Class<? extends CodeTextEnum> enumType;
	
	public String getDict() {
		return dict;
	}
	/**
	 * 设置字典代码
	 * */
	public void dict(String dict) {
		this.dict = dict;
		this.enumType = null;
	}

	public String getEnumTypeName() {
		return enumType.getName();
	}

	/**
	 * 设置 CodeTextEnum 类型的枚举
	 * */
	public void enumType(Class<? extends CodeTextEnum> enumType) {
		this.enumType = enumType;
		this.dict = null;
	}
}
