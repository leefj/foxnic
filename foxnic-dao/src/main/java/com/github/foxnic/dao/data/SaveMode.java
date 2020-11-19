package com.github.foxnic.dao.data;

/**
 * 实体保存方式
 * @author fangjieli
 *
 */
public enum SaveMode {
	/**
	 * 保存所有字段，无论是否为空值
	 * */
	ALL_FIELDS, 
	/**
	 * 保存所有非空字段
	 * */
	NOT_NULL_FIELDS, 
	/**
	 * 保存所有脏字段
	 * */
	DIRTY_FIELDS;
}
