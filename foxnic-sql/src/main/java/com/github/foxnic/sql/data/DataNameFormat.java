package com.github.foxnic.sql.data;

/**
 * @author 李方捷
 * 数列名序列化时，使用大小写转换方式，或不转换，见 AbstractSet.DATA_NAME_CASE_TYPE
 * */
public enum DataNameFormat {
	/**
	 * 不转换,完全和数据库一致
	 */
	NONE,
	/**
	 * 完全和数据库一致，整体转小写
	 */
	DB_LOWER_CASE,
	/**
	 * 完全和数据库一致，整体转大写
	 */
	DB_UPPER_CASE,
	/**
	 * POJO属性方式命名
	 * */
	POJO_PROPERTY;
}
