package com.github.foxnic.commons.busi.id;

/**
 * @author 李方捷
 * */
public enum SequenceType {
	/**
	 * 序号自增
	 * */
	AI("自增"), 
	/**
	 * 每日自增  如 20180208006
	 * */
	DAI("每日自增"), 
	
	/**
	 * 一年的第几周，
	 * 可以有不同的定义，具体业务系统可以有不同的解释与实现
	 * */
	WAI_1("每周自增"), 
	/**
	 * 一年的第几周，
	 * 可以有不同的定义，具体业务系统可以有不同的解释与实现
	 * */
	WAI_2("每周自增"),
	/**
	 * 每月自增  如 201801
	 * */
	MAI("每月自增"), 
	/**
	 * 年自增 如 201901
	 * */
	YAI("每年自增");
    private String text=null;
    /**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	private SequenceType(String text)
    {
        this.text=text;
    }
    @Override
	public String toString()
    {
        return text;
    }
    
}
