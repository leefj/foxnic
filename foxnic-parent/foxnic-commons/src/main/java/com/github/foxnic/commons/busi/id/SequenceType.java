package com.github.foxnic.commons.busi.id;

import com.github.foxnic.api.constant.CodeTextEnum;

/**
 * @author 李方捷
 * */
public enum SequenceType implements CodeTextEnum {
	/**
	 * 序号自增
	 * */
	AI("自增",null),
	/**
	 * 每日自增  如 20180208006
	 * */
	DAI("每日自增","yyyyMMdd"),

	/**
	 * 一年的第几周，
	 * 可以有不同的定义，具体业务系统可以有不同的解释与实现
	 * */
	WAI_1("每周自增","yyyyww"),
	/**
	 * 一年的第几周，
	 * 可以有不同的定义，具体业务系统可以有不同的解释与实现
	 * */
	WAI_2("每周自增","yyyyww"),
	/**
	 * 每月自增  如 201801
	 * */
	MAI("每月自增","yyyyMM"),
	/**
	 * 年自增 如 201901
	 * */
	YAI("每年自增","yyyy");

    private String text=null;

	public String dateTagFormat() {
		return dateTagFormat;
	}

	private String dateTagFormat=null;
    /**
	 * @return the text
	 */
	public String text() {
		return text;
	}

	private SequenceType(String text,String dateTagFormat)
    {
    	this.dateTagFormat=dateTagFormat;
        this.text=text;
    }

    @Override
	public String toString()
    {
        return this.name();
    }

	@Override
	public String code() {
		return this.name();
	}

	@Override
	public String description() {
		return CodeTextEnum.super.description();
	}
}
