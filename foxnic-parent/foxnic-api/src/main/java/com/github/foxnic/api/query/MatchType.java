package com.github.foxnic.api.query;

import com.github.foxnic.api.constant.CodeTextEnum;

/**
 * 查询的匹配模式
 * */
public enum MatchType implements CodeTextEnum {

	/**
	 * 日期匹配
	 * */
	day("日期"),auto("自动");

	private String text;
	private MatchType(String text)  {
		this.text=text;
	}

	public String code() {
		return this.name();
	}

	public String text() {
		return text;
	}

	public static MatchType parseByCode(String code) {
		for (MatchType value : MatchType.values()) {
			if(value.code().equals(code)) return value;
		}
		return null;
	}

}
