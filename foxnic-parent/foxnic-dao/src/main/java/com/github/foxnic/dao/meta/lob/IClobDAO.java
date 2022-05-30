package com.github.foxnic.dao.meta.lob;

import java.sql.SQLException;
import java.util.HashMap;

import com.github.foxnic.sql.expr.Where;

/**
 * @author 李方捷
 * */
public interface IClobDAO {

	/**
	 * 插入数据
	 * @param table 表名
	 * @param clobField clob字段名
	 * @param content 内容
	 * @param otherfields 其它需要一起更新的字段
	 * @param pkFields 主键字段名
	 * @exception SQLException 异常
	 * */
	public void insert(String table, String clobField,String content,HashMap<String,Object> otherfields,String[] pkFields) throws SQLException;
	/**
	 * 更新数据
	 * @param table 表名
	 * @param field clob字段名
	 * @param where 更新条件
	 * @param content 内容
	 * @exception SQLException 异常
	 * */
	public void update(String table, String field, Where where, String content) throws SQLException;
	/**
	 * 获得文本内容
	 * @param table 表名
	 * @param field clob字段名
	 * @param where 查询条件
	 * @return 文本内容
	 * */
	public String getText(String table, String field, Where where);
}
