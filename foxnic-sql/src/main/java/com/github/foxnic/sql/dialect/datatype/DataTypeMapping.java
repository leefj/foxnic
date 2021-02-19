package com.github.foxnic.sql.dialect.datatype;

import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.meta.DBType;

public class DataTypeMapping {

	private DBType dbType;
	
	public DataTypeMapping(DBType dbType, String dbTypeName,String jdbcTypeName,DBDataType dbDataType,Integer sampleDataLength,Integer sampleNumScale)
	{
		this.dbDataType=dbDataType;
		this.dbTypeName=dbTypeName;
		this.jdbcTypeName=jdbcTypeName;
		this.sampleDataLength=sampleDataLength;
		this.sampleNumScale=sampleNumScale;
		this.dbType=dbType;
		
		//
		this.dbType.addJDBCType(dbTypeName, jdbcTypeName);
		
	}
	
	private Integer sampleDataLength = null;
 
	private Integer sampleNumScale = null;
 
	private String dbTypeName=null;
	
	private String jdbcTypeName=null;
	
	private DBDataType dbDataType=null;
	
	public String getDbTypeName() {
		return dbTypeName;
	}
	
	public DBDataType getDbDataType(String table,String column,Integer precision , Integer scale) {
		return dbDataType;
	}
	
	public Integer getSampleDataLength() {
		return sampleDataLength;
	}
	public Integer getSampleNumScale() {
		return sampleNumScale;
	}

	public String getJdbcTypeName(Integer precision, Integer scale) {
		return jdbcTypeName;
	}

	
	
}
