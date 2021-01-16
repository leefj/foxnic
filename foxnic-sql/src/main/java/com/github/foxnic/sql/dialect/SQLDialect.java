package com.github.foxnic.sql.dialect;

import com.github.foxnic.sql.dialect.processor.Db2SQLDialectProcessor;
import com.github.foxnic.sql.dialect.processor.MySQLDialectProcessor;
import com.github.foxnic.sql.dialect.processor.PLSQLDialectProcessor;
import com.github.foxnic.sql.dialect.processor.SQLDialectProcessor;
import com.github.foxnic.sql.dialect.processor.SQLiteDialectProcessor;
import com.github.foxnic.sql.dialect.processor.TSQLDialectProcessor;
import com.github.foxnic.sql.meta.DBType;

/**
 * SQL方言
 * @author fangjieli
 * */
public enum SQLDialect {
	
	/**
	 * PLSQL
	 * */
//	PLSQL(DBType.ORACLE,JdbcConstants.ORACLE,new PLSQLDialectProcessor()),
	PLSQL(DBType.ORACLE,new PLSQLDialectProcessor()),
	/**
	 * TSQL
	 * */
//	TSQL(DBType.SQLSVR,JdbcConstants.SQL_SERVER,new TSQLDialectProcessor()),
	TSQL(DBType.SQLSVR,new TSQLDialectProcessor()),
	/**
	 * SQLite
	 * */
//	SQLite(DBType.SQLLIT,JdbcConstants.SQLITE,new SQLiteDialectProcessor()),
	SQLite(DBType.SQLLIT,new SQLiteDialectProcessor()),
	/**
	 * MySQL
	 * */
//	MySQL(DBType.MYSQL,JdbcConstants.MYSQL,new MySQLDialectProcessor()),
	MySQL(DBType.MYSQL,new MySQLDialectProcessor()),
	
	/**
	 * DB2
	 * */
//	DB2(DBType.DB2,JdbcConstants.DB2,new Db2SQLDialectProcessor());
	DB2(DBType.DB2,new Db2SQLDialectProcessor()),

	PSQL(DBType.POSTGRES,new PLSQLDialectProcessor());

	private DBType dbType=null;
	
	public DBType getDbType() {
		return dbType;
	}

//	private String druidType=null;
	
//	public String getDruidType() {
//		return druidType;
//	}
	
	private SQLDialectProcessor processor= null;

	public SQLDialectProcessor getDialectProcessor() {
		return processor;
	}

//	private SQLDialect(DBType dbType,String druidType,SQLDialectProcessor processor)
	private SQLDialect(DBType dbType,SQLDialectProcessor processor)
	{
		this.dbType=dbType;
//		this.druidType=druidType;
		this.processor=processor;
	}
	
}
