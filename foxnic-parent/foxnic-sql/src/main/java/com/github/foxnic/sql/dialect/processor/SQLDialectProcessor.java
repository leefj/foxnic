package com.github.foxnic.sql.dialect.processor;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.dialect.datatype.DataTypeMappingSet;

import java.util.Date;
import java.util.HashMap;

/**
 * 方言处理器
 * @author lifangjie
 * */
public abstract class SQLDialectProcessor {

	private static HashMap<SQLDialect, SQLDialectProcessor>  DIALECT_PROCESSOR_MAP=null;

	private static synchronized void initIf() {
		if(DIALECT_PROCESSOR_MAP!=null) return;
		DIALECT_PROCESSOR_MAP = new HashMap<SQLDialect, SQLDialectProcessor>();
	}


	public static SQLDialectProcessor getDialectProcessor(SQLDialect dialect) {
		initIf();
		return DIALECT_PROCESSOR_MAP.get(dialect);
	}


	/**
	 * 移除 name 两侧的引号字符
	 * @param identity  SQL标识
	 * @param quotation 引号字符，可以是单引号、双引号等
	 * @return 两侧无引号字符的 identity
	 * */
	public String removeQuotations(String identity,char...quotation)
	{
		for (char c : quotation) {
			identity=StringUtil.removeFirst(identity, c+"");
			identity=StringUtil.removeLast(identity, c+"");
		}
		return identity;
	}

	/**
	 * 给数据库内的标识符加引号(按方言类型，包括单引号、双引号，反单引号等)
	 * @param identity  SQL标识
	 * @return 两侧有引号字符的 identity
	 * */
	public abstract String quotes(String identity);


	/**
	 * 简化表名，去除某些引号，或反单引号
	 * @param table 表名
	 * @return table
	 * */
	public String simplifyTableName(String table)
	{
		return removeQuotations(table,'`','"');
	}

	/**
	 * 简化表名，去除某些引号，或反单引号
	 * @param field 列名
	 * @return field
	 * */
	public String simplifyFieldName(String field)
	{
		return removeQuotations(field,'`','"');
	}

//	/**
//	 * 获得当前时间的表达式
//	 * @return SQLExpr
//	 * */
//	public abstract SQLExpr getNowExpr();
//
//	/**
//	 * 获得当前时间戳的表达式
//	 * @return SQLExpr
//	 * */
//	public abstract SQLExpr getNowTimestampExpr();

	/**
	 * 字符串内某些特殊字符转义，如单引号
	 * @param chars 字符的串
	 * @return SQLExpr
	 * */
	public abstract String castCharInjection(String chars);

	/**
	 * 获得日期函数转换的语句
	 * @param date 日期
	 * @return SQLExpr
	 * */
	public abstract String getToDateTimeSQL(Date date);


	/**
	 * 获得数据库类型映射关系
	 * @return DataTypeMappingSet
	 * */
	public abstract DataTypeMappingSet getDataTypeMappingSet();




}
