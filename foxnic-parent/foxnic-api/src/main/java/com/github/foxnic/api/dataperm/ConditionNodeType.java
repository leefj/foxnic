package com.github.foxnic.api.dataperm;

import com.github.foxnic.api.constant.CodeTextEnum;

public enum ConditionNodeType implements CodeTextEnum {

	group("逻辑组"),expr("表达式");

	private String text;
	private ConditionNodeType(String text)  {
		this.text=text;
	}

	public String code() {
		return this.name();
	}

	public String text() {
		return text;
	}

	public static ConditionNodeType parseByCode(String code) {
		for (ConditionNodeType value : ConditionNodeType.values()) {
			if(value.code().equals(code)) return value;
		}
		return null;
	}

}
