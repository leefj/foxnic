package com.github.foxnic.sql.meta;

import java.util.HashMap;
import java.util.Map;

import com.github.foxnic.sql.dialect.SQLDialect;

/**
 * 数据库类型
 * @author fangjieli
 *
 */
public enum DBType {
	/**
	 * Oracle 数据库
	 * */
	ORACLE,
	/**
	 * DB2 数据库
	 * */
	DB2,
	/**
	 * MySQL 数据库
	 * */
	MYSQL,
	/**
	 * SQLServer 数据库
	 * */
	SQLSVR,
	/**
	 * SQLite 数据库
	 * */
	SQLLIT,
	/**
	 * PostgreSQL 数据库
	 * */
	PG;
 
	
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
		case PG:
			dialect=SQLDialect.PSQL;
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
