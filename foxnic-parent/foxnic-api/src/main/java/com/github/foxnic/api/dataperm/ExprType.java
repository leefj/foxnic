package com.github.foxnic.api.dataperm;

import com.github.foxnic.api.constant.CodeTextEnum;

public enum ExprType implements CodeTextEnum {

	custom("自定义",null,-1,-1),
	eq("相等","=",1,1),neq("不相等","!=",1,1);

	/**
	 * 操作符号
	 * */
	private String operator;

	private String text;
	/**
	 * 最大变量数
	 * */
	private int maxVars;
	/**
	 * 最小变量数
	 * */
	private int minVars;


	private ExprType(String text,String operator,int minVars,int maxVars)  {
		this.text=text;
		this.operator=operator;
		this.minVars=minVars;
		this.maxVars=maxVars;
	}

	public String code() {
		return this.name();
	}

	public String text() {
		return text;
	}

	public static ExprType parseByCode(String code) {
		for (ExprType value : ExprType.values()) {
			if(value.code().equals(code)) return value;
		}
		return null;
	}

}
