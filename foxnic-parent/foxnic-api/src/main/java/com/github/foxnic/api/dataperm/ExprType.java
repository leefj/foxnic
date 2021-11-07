package com.github.foxnic.api.dataperm;

import com.github.foxnic.api.constant.CodeTextEnum;

public enum ExprType implements CodeTextEnum {

	nvl("空","is null",0,0,true),
	notnvl("非空","is not null",0,0,true),
	eq("相等","=",1,1,true),
	neq("不等","!=",1,1,true),
	gt("大于",">",1,1,true),
	gteq("大于等于",">=",1,1,true),
	lt("小于","<",1,1,true),
	lteq("小于等于","<=",1,1,true),
	//
	like("相似","like '%word%'",1,1,false),
	like_left("相似","like 'word%'",1,1,false),
	like_right("相似","like '%word'",1,1,false),
	like_not("相似","not like '%word%'",1,1,false),
	like_left_not("相似","not like 'word%'",1,1,false),
	like_right_not("相似","not like '%word'",1,1,false),
	//
	btw("范围","between...and...",2,2,false),
	in("列表内","in",1,64,false),
	in_not("列表外","not in",1,64,false),
	;

	/**
	 * 操作符号
	 * */
	private String operator;

	private String text;



	private boolean simple=false;

	/**
	 * 最大变量数
	 * */
	private int maxVars;
	/**
	 * 最小变量数
	 * */
	private int minVars;


	private ExprType(String text,String operator,int minVars,int maxVars,boolean simple)  {
		this.text=text;
		this.operator=operator;
		this.minVars=minVars;
		this.maxVars=maxVars;
		this.simple=simple;
	}

	public String code() {
		return this.name();
	}

	public String text() {
		return text+"("+this.operator+")";
	}
	public boolean simple() {
		return simple;
	}
	public static ExprType parseByCode(String code) {
		for (ExprType value : ExprType.values()) {
			if(value.code().equals(code)) return value;
		}
		return null;
	}

	public String operator() {
		return operator;
	}



	public int maxVars() {
		return maxVars;
	}

	public int minVars() {
		return minVars;
	}

}
