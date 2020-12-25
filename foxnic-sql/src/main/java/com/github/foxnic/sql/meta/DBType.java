package com.github.foxnic.sql.meta;

import java.util.HashMap;
import java.util.Map;

import org.checkerframework.checker.units.qual.K;

import com.github.foxnic.sql.dialect.SQLDialect;

/**
 * 数据库类型
 * @author fangjieli
 *
 */
public enum DBType {
	/**
	 * Oracle数据库
	 * */
	ORACLE,
	/**
	 * DB2数据库
	 * */
	DB2,
	/**
	 * MySQL数据库
	 * */
	MYSQL,
	/**
	 * SQLServer 数据库
	 * */
	SQLSVR,
	/**
	 * SQLite数据库
	 * */
	SQLLIT;
 
	
	public SQLDialect getSQLDialect() {
		SQLDialect dialect=null;
		switch (this) {
		case ORACLE:
			dialect=SQLDialect.PLSQL;
			break;
		case MYSQL:
			dialect=SQLDialect.MySQL;
			break;
		case SQLSVR:
			dialect=SQLDialect.TSQL;
			break;
		case SQLLIT:
			dialect=SQLDialect.SQLite;
			break;
		case DB2:
			dialect=SQLDialect.DB2;
			break;
		default:
			break;
		}
		return dialect;
	}

	private DBType() {
	}
	
	private Map<String,String> jdbcDataTypes=new HashMap<>();
 
	public void addJDBCType(String localDataType,String jdbcDataType) {
		localDataType=localDataType.toUpperCase();
		jdbcDataTypes.put(localDataType, jdbcDataType);
	}
	
	public String getJDBCType(String localDataType) {
		localDataType=localDataType.toUpperCase();
		return jdbcDataTypes.get(localDataType);
	}
	
}
