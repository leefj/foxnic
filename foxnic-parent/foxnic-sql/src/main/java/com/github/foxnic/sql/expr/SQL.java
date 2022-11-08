package com.github.foxnic.sql.expr;

import com.github.foxnic.sql.dialect.SQLDialect;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fangjieli
 *
 */
public interface SQL extends Serializable
{

	public static final String PNAME_PREFIX = "PARAM";

	/**
	 * 是否忽略引号
	 * @param ignor 是否忽略
	 * @return SubSQL
	 * */
	SubSQL setIgnorColon(boolean ignor);

	/**
	 * 获得可直接执行的字符串SQL语句
	 * @return SQL语句
	 * */
	String getSQL();

	/**
	 * 用不同的方言输出SQL
	 * @param dialect SQL方言
	 * @return SQL语句
	 * */
	String getSQL(SQLDialect dialect);

	/**
	 * 忽的带问号占位符的语句
	 * @return SQL语句
	 * */
	String getListParameterSQL();

	/**
	 * 数组类型参数清单
	 * @return 参数清单
	 * */
	Object[] getListParameters();


	/**
	 * 获得带命名占位符的语句
	 * @return SQL语句
	 * */
	String getNamedParameterSQL();

	/**
	 * 获得Map类型参数
	 * @return 参数集合
	 * */
	Map<String, Object> getNamedParameters();

	/**
	 * 是否空语句
	 * @return 逻辑值
	 * */
	boolean isEmpty();

	/**
	 * 是否所有参数为空
	 * @return 逻辑值
	 * */
	boolean isAllParamsEmpty();


	/**
	 * top获得最顶层的语句
	 * @return SQL语句对象
	 * */
	SQL top();

	/**
	 * 获得上级子句
	 * @return 上级子句
	 * */
	SQL parent();

	/**
	 * 设置上级子句
	 * @param sql 上级子句
	 * */
	void setParent(SQL sql);


	void beginParamNameSQL();

	void endParamNameSQL();

	String getNextParamName(boolean withColon);

	/**
	 * 设置方言
	 * @param dialect 方言
	 * */
	SQL setSQLDialect(SQLDialect dialect);
	/**
	 * 获得方言
	 * @return 方言
	 * */
	SQLDialect getSQLDialect();

	SQL clone();

	/**
	 * 把字符串连接起来成为一个SQL，每个元素为一行
	 * @param sqls 字符串语句分段
	 * @return 拼接好的语句
	 * */
	public static String joinSQLs(String... sqls) {
		StringBuilder buf = new StringBuilder();
		for (String str : sqls) {
			buf.append(str + " \n");
		}
		return buf.toString();
	}



	/**
	 * 把字符串连接起来成为一个SQL，每个元素为一行
	 * @param sqls 字符串语句分段
	 * @return 拼接好的语句
	 * */
	public static String joinSQLs(List<String> sqls) {
		 return joinSQLs(sqls.toArray(new String[sqls.size()]));
	}

}
