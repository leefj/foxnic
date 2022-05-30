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
	ORACLE("PLSQL","oracle","com.github.foxnic.dao.spring.OracleDAO","jdbc:oracle:",new String[] {"oracle.jdbc.driver.OracleDriver"}),
	/**
	 * DB2 数据库
	 * */
	DB2("DB2","db2","com.github.foxnic.dao.spring.Db2DAO","jdbc:db2:",new String[] {"com.ibm.db2.jcc.DB2Driver"}),
	/**
	 * MySQL 数据库
	 * */
	MYSQL("MySQL","mysql","com.github.foxnic.dao.spring.MySqlDAO","jdbc:mysql:",new String[] {"com.mysql.cj.jdbc.Driver"}),
	/**
	 * SQLServer 数据库
	 * */
	SQLSVR("TSQL","sqlserver","com.github.foxnic.dao.spring.SQLServerDAO","-",new String[] {}),
	/**
	 * SQLite 数据库
	 * */
	SQLITE("SQLite","sqlite","com.github.foxnic.dao.spring.SQLiteDAO","-",new String[] {}),
	/**
	 * PostgreSQL 数据库
	 * */
	PG("PSQL","postgresql","com.github.foxnic.dao.spring.PostgresDAO","jdbc:postgresql:",new String[] {"org.postgresql.Driver"}),

	/**
	 * 国产戴梦数据库 DM
	 * */
	DM("DMSQL","dm","com.github.foxnic.dao.spring.DmDAO","jdbc:dm:",new String[] {"dm.jdbc.driver.DmDriver"});




	public SQLDialect getSQLDialect() {
		if(dialect==null) {
			dialect=SQLDialect.valueOf(dialectName);
		}
		return this.dialect;
	}

	private String[] drivers;
	private String daoType;
	private String druidDbType;
	private String dialectName;
	private SQLDialect dialect;
	private String urlPrefix;

	public String getDruidDbType() {
		return this.druidDbType;
	}

	private DBType(String dialectName,String druidDbType,String daoType,String urlPrefix,String[] drivers) {
		this.dialectName=dialectName;
		this.druidDbType=druidDbType;
		this.daoType=daoType;
		this.urlPrefix=urlPrefix;
		this.drivers=drivers;
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



	/**
	 * 从驱动类型识别数据库类型
	 * */
	public static DBType parseFromDriver(String driver) {
		for (DBType dbType : DBType.values()) {
			for (String dir : dbType.drivers) {
				if(dir.equals(driver)) {
					return dbType;
				}
			}
		}
		return null;
	}

	/**
	 * 从连接字符串识别数据库类型
	 * */
	public static DBType parseFromURL(String url) {
		url=url.toLowerCase().trim();
		for (DBType dbType : DBType.values()) {
			 if(url.startsWith(dbType.urlPrefix)) {
				 return dbType;
			 }
		}
		return null;
	}


	public String getDAOType() {
		return daoType;
	}


}
