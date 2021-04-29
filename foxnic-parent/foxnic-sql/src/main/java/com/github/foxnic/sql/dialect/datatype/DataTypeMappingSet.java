package com.github.foxnic.sql.dialect.datatype;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DataTypeMappingSet {

	/*
	 * 类型参考
	 * https://www.cnblogs.com/FlyingPuPu/p/6265353.html
	 * */
	
	private Map<String,DataTypeMapping> mappings=new HashMap<String, DataTypeMapping>();
	
	protected void addDataTypeMapping(DataTypeMapping...dataTypeMappings)
	{
		for (DataTypeMapping dataTypeMapping : dataTypeMappings) {
			mappings.put(dataTypeMapping.getDbTypeName().toUpperCase(), dataTypeMapping);
		}
	}
	
	public DataTypeMapping getDataTypeMapping(String dbTypeName) 
	{
		//DB2需要Trim一下，很诡异
		dbTypeName=dbTypeName.trim();
		int i=dbTypeName.indexOf("(");
		if(i>-1) {
			dbTypeName=dbTypeName.substring(0,i);
		}
		return mappings.get(dbTypeName.toUpperCase());
	}
	
	public Collection<DataTypeMapping> getAll()
	{
		return Collections.unmodifiableCollection(mappings.values());
	}
	
	
}
