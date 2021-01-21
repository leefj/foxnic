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
	ORACLE("PLSQL","oracle"),
	/**
	 * DB2 数据库
	 * */
	DB2("DB2","db2"),
	/**
	 * MySQL 数据库
	 * */
	MYSQL("MySQL","mysql"),
	/**
	 * SQLServer 数据库
	 * */
	SQLSVR("TSQL","sqlserver"),
	/**
	 * SQLite 数据库
	 * */
	SQLLIT("SQLite","sqlite"),
	/**
	 * PostgreSQL 数据库
	 * */
	PG("PSQL","postgresql");
 
	
 
	public SQLDialect getSQLDialect() {
		if(dialect==null) {
			dialect=SQLDialect.valueOf(dialectName);
		}
		return this.dialect;
	}
	
	
	private String druidDbType;
	 
	public String getDruidDbType() {
		return this.druidDbType;
	}

	private String dialectName;
	private SQLDialect dialect;
 
	private DBType(String dialectName,String druidDbType) {
		this.dialectName=dialectName;
		this.druidDbType=druidDbType;
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
