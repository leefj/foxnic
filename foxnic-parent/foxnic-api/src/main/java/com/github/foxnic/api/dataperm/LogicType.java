package com.github.foxnic.api.dataperm;

import com.github.foxnic.api.constant.CodeTextEnum;

public enum LogicType implements CodeTextEnum {

	and("AND"),or("OR");

	private String text;
	private LogicType(String text)  {
		this.text=text;
	}

	public String code() {
		return this.name();
	}

	public String text() {
		return text;
	}

	public static LogicType parseByCode(String code) {
		for (LogicType value : LogicType.values()) {
			if(value.code().equals(code)) return value;
		}
		return null;
	}

}
