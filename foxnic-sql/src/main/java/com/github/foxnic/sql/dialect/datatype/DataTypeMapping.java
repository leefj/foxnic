package com.github.foxnic.sql.dialect.datatype;

import com.github.foxnic.sql.meta.DBDataType;

public class DataTypeMapping {

	
	public DataTypeMapping(String dbTypeName,DBDataType dbDataType,Integer sampleDataLength,Integer sampleNumScale)
	{
		this.dbDataType=dbDataType;
		this.dbTypeName=dbTypeName;
		this.sampleDataLength=sampleDataLength;
		this.sampleNumScale=sampleNumScale;
	}
	
	private Integer sampleDataLength = null;
 
	private Integer sampleNumScale = null;
 
	private String dbTypeName=null;
	
	private DBDataType dbDataType=null;
	
	public String getDbTypeName() {
		return dbTypeName;
	}
	
	public DBDataType getDbDataType(Integer precision , Integer scale) {
		return dbDataType;
	}
	
	public Integer getSampleDataLength() {
		return sampleDataLength;
	}
	public Integer getSampleNumScale() {
		return sampleNumScale;
	}

	
	
}
