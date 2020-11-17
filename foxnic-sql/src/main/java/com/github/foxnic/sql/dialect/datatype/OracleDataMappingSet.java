package com.github.foxnic.sql.dialect.datatype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.meta.DBDataType;

public class OracleDataMappingSet extends DataTypeMappingSet {

	private static class OracleNumberMapping extends DataTypeMapping {
		
		public OracleNumberMapping(String dbTypeName,DBDataType dbDataType,Integer sampleDataLength,Integer sampleNumScale)
		{
			super(dbTypeName, dbDataType, sampleDataLength, sampleNumScale);
		}
		
		@Override
		public DBDataType getDbDataType(Integer precision, Integer scale) {
			if(scale!=null && scale>0) {
				return DBDataType.DOUBLE;
			}
			if(precision==null)  return DBDataType.LONG;
			if(precision>=16) precision=precision-4;
			if(precision<=9) {
				return DBDataType.INTEGER;
			} else if(precision<=18) {
				return DBDataType.LONG;
			} else {
				return DBDataType.BIGINT;
			}
		}
		
	}
	
	public OracleDataMappingSet()
	{
		//
		DataTypeMapping[] dataTypeMappings= {
				 
				new DataTypeMapping("CHAR",DBDataType.STRING,8,null),
				new DataTypeMapping("NCHAR",DBDataType.STRING,8,null),
				new DataTypeMapping("VARCHAR2",DBDataType.STRING,8,null),
				new DataTypeMapping("NVARCHAR2",DBDataType.STRING,8,null),
				new DataTypeMapping("LONG",DBDataType.STRING,null,null),
				new OracleNumberMapping("NUMBER",null,8,2),
				new DataTypeMapping("FLOAT",DBDataType.FLOAT,4,null),
				new DataTypeMapping("DATE",DBDataType.DATE,null,null),
				new DataTypeMapping("TIMESTAMP",DBDataType.TIMESTAME,6,null),
				new DataTypeMapping("CLOB",DBDataType.STRING,null,null),
				new DataTypeMapping("NCLOB",DBDataType.STRING,null,null),
				new DataTypeMapping("BLOB",DBDataType.BLOB,null,null),
				

		}; 
		//
		this.addDataTypeMapping(dataTypeMappings);
	}
	
	
	 
	
	
	
	
	public static void main(String[] args) {
		
		OracleDataMappingSet mSet=new OracleDataMappingSet();
		Collection<DataTypeMapping> typesRaw=mSet.getAll();
		
		List<DataTypeMapping> types=new ArrayList<>();
		for (DataTypeMapping dataType : typesRaw) {
			if(dataType.getDbTypeName().equalsIgnoreCase("NUMBER")) {
				for (int i = 1; i < 32; i++) {
					DataTypeMapping n0=new OracleNumberMapping("NUMBER",null,i,0);
					types.add(n0);
					DataTypeMapping n2=new OracleNumberMapping("NUMBER",null,i,i%6+1);
					types.add(n2);
				}
			}
			else {
				types.add(dataType);
			}
		}
		
		ArrayList<String> columns=new ArrayList<>();
		ArrayList<String> comments=new ArrayList<>();
		
		for (DataTypeMapping dataType : types) {
			String cn=dataType.getDbTypeName()+"_value";
			if(dataType.getSampleDataLength()!=null) {
				cn+="_l"+dataType.getSampleDataLength();
			}
			
			if(dataType.getSampleNumScale()!=null) {
				cn+="_s"+dataType.getSampleNumScale();
			}
			
			comments.add("COMMENT ON COLUMN tity_all_type."+cn+" IS '"+dataType.getDbTypeName()+"类型字段'");
			
			cn+=" "+dataType.getDbTypeName();
			
			 
				if(dataType.getSampleDataLength()!=null || dataType.getSampleNumScale()!=null) {
					cn+="(";
					if(dataType.getSampleDataLength()!=null) {
						cn+=dataType.getSampleDataLength();
					}
					if(dataType.getSampleNumScale()!=null) {
						cn+=","+dataType.getSampleNumScale();
					}
					cn+=")";
				}
 
			columns.add(cn);
			
		}
		
		String cr="CREATE TABLE tity_all_type (\n" ;
		cr+="ID INTEGER NOT NULL\n,";
		cr+=StringUtil.join(columns,",\n");
		cr+="\n)";
		System.out.println(cr);
		
		for (String string : comments) {
			System.out.println(string);
		}
		
	}
	
}
