package com.github.foxnic.sql.dialect;

import com.github.foxnic.sql.dialect.processor.*;
import com.github.foxnic.sql.meta.DBType;

/**
 * SQL方言
 * @author fangjieli
 * */
public enum SQLDialect {

	/**
	 * PLSQL
	 * */
	PLSQL(DBType.ORACLE,new PLSQLDialectProcessor()),
	/**
	 * TSQL
	 * */
	TSQL(DBType.SQLSVR,new TSQLDialectProcessor()),
	/**
	 * SQLite
	 * */
	SQLite(DBType.SQLITE,new SQLiteDialectProcessor()),
	/**
	 * MySQL
	 * */
	MySQL(DBType.MYSQL,new MySQLDialectProcessor()),

	/**
	 * DB2
	 * */
	DB2(DBType.DB2,new Db2SQLDialectProcessor()),

	/**
	 * PG
	 * */
	PSQL(DBType.PG,new PSQLDialectProcessor()),

	/**
	 * DM
	 * */
	DMSQL(DBType.DM,new DMSQLDialectProcessor());

	private DBType dbType=null;

	public DBType getDBType() {
		return dbType;
	}

	private SQLDialectProcessor processor= null;

	public SQLDialectProcessor getDialectProcessor() {
		return processor;
	}

	private SQLDialect(DBType dbType,SQLDialectProcessor processor)
	{
		this.dbType=dbType;
		this.processor=processor;
	}

}
