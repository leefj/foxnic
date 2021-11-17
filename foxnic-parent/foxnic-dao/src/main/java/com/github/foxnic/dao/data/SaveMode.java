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
	 * 保存所有脏字段，被设置值，且设置前后值改变的字段
	 * */
	DIRTY_FIELDS,

	/**
	 * 保存所有非空字段 和 脏字段，(被设置值，且设置前后值生改变的字段)
	 * */
	DIRTY_OR_NOT_NULL_FIELDS,
	/**
	 * 保存所有设置过值的字段，无论设置前后的值是否发生改变
	 * */
	BESET_FIELDS;


}
