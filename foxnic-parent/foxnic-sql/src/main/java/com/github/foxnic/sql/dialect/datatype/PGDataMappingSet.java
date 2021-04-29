package com.github.foxnic.sql.dialect.datatype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.meta.DBType;

public class PGDataMappingSet extends DataTypeMappingSet {

	private static class PGNumberMapping extends DataTypeMapping {
		
		public PGNumberMapping(String dbTypeName,DBDataType dbDataType,Integer sampleDataLength,Integer sampleNumScale)
		{
			super(DBType.PG,dbTypeName, null,dbDataType, sampleDataLength, sampleNumScale);
		}
		
		@Override
		public DBDataType getDbDataType(String table,String column,Integer precision, Integer scale) {
			if(scale!=null && scale>0) {
				return DBDataType.DOUBLE;
			}
//			if(precision==null)  return DBDataType.LONG;
			if(precision>=16) precision=precision-4;
			if(precision<=9) {
				return DBDataType.INTEGER;
			} else if(precision<=18) {
				return DBDataType.LONG;
			} else {
				return DBDataType.BIGINT;
			}
		}
		
		@Override
		public String getJdbcTypeName(Integer precision, Integer scale) {
			if(scale!=null && scale>0) {
				return "DOUBLE";
			}
//			if(precision==null)  return "BIGINT";
			if(precision>=16) precision=precision-4;
			if(precision<=9) {
				return "INTEGER";
			} else if(precision<=18) {
				return "BIGINT";
			} else {
				return "BIGINT";
			}
		}
		
	}
	
	
	private static class PGTimestampMapping extends DataTypeMapping {
		
		public PGTimestampMapping(String dbTypeName,DBDataType dbDataType,Integer sampleDataLength,Integer sampleNumScale)
		{
			super(DBType.PG,dbTypeName, null,dbDataType, sampleDataLength, sampleNumScale);
		}
		
		@Override
		public DBDataType getDbDataType(String table,String column,Integer precision, Integer scale) {
			 if(precision>0) {
				 return DBDataType.TIMESTAME;
			 } else {
				 return DBDataType.DATE;
			 }
		}
		
		@Override
		public String getJdbcTypeName(Integer precision, Integer scale) {
			 if(precision>0) {
				 return "TIMESTAME";
			 } else {
				 return "DATE";
			 }
		}
		
	}
	
	
	public PGDataMappingSet()
	{
		DBType dbType=DBType.PG;
		//
		DataTypeMapping[] dataTypeMappings= {

//				new DataTypeMapping(dbType,"BIGINT","BIGINT",DBDataType.BIGINT,8,null),
				new PGNumberMapping("BIGINT",null,8,2),
				new DataTypeMapping(dbType,"BIT","BIT",DBDataType.BYTES,8,null),
				new DataTypeMapping(dbType,"BOOLEAN","BIT",DBDataType.BOOL,8,null),
				new DataTypeMapping(dbType,"BYTEA","BINARY",DBDataType.BYTES,8,null),
				new DataTypeMapping(dbType,"CHARACTER","CHAR",DBDataType.STRING,8,null),
				new DataTypeMapping(dbType,"CIDR","VARCHAR",DBDataType.STRING,8,null),
				new DataTypeMapping(dbType,"DATE","DATE",DBDataType.DATE,8,null),
				new PGNumberMapping("NUMERIC",null,8,2),
				new DataTypeMapping(dbType,"DOUBLE","DOUBLE",DBDataType.DOUBLE,8,null),
				new DataTypeMapping(dbType,"REAL","REAL",DBDataType.FLOAT,4,null),
				new DataTypeMapping(dbType,"INET","CHAR",DBDataType.STRING,8,null),
				new DataTypeMapping(dbType,"SMALLINT","SMALLINT",DBDataType.INTEGER,8,null),
				new DataTypeMapping(dbType,"INTEGER","INTEGER",DBDataType.INTEGER,8,null),
				new DataTypeMapping(dbType,"JSON","OTHER",DBDataType.OBJECT,8,null),
				new DataTypeMapping(dbType,"JSONB","OTHER",DBDataType.OBJECT,8,null),
				new DataTypeMapping(dbType,"MACADDR","VARCHAR",DBDataType.STRING,8,null),
 
				new DataTypeMapping(dbType,"MONEY","DOUBLE",DBDataType.DOUBLE,8,null),

				new DataTypeMapping(dbType,"TEXT","VARCHAR",DBDataType.STRING,0,null),
				new DataTypeMapping(dbType,"UUID","VARCHAR",DBDataType.STRING,0,null),
				new DataTypeMapping(dbType,"BIT VARYING","BIT",DBDataType.BYTES,64,null),
				new DataTypeMapping(dbType,"CHARACTER VARYING","VARCHAR",DBDataType.STRING,64,null),

				new DataTypeMapping(dbType,"INTERVAL","OTHER",DBDataType.OBJECT,0,null),
				new DataTypeMapping(dbType,"TIME WITHOUT TIME ZONE","TIME",DBDataType.TIME,0,null),
				new DataTypeMapping(dbType,"TIME WITH TIME ZONE","TIME",DBDataType.TIME,0,null),
				
				//new DataTypeMapping(dbType,"TIMESTAMP WITHOUT TIME ZONE","TIMESTAMP",DBDataType.TIMESTAME,8,null),
				//new DataTypeMapping(dbType,"TIMESTAMP WITH TIME ZONE","TIMESTAMP",DBDataType.TIMESTAME,8,null),
				new PGTimestampMapping("TIMESTAMP WITHOUT TIME ZONE",null,8,3),
				new PGTimestampMapping("TIMESTAMP WITH TIME ZONE",null,8,3),
				
				new DataTypeMapping(dbType,"BOX","OTHER",DBDataType.OBJECT,0,null),
				new DataTypeMapping(dbType,"CIRCLE","OTHER",DBDataType.OBJECT,0,null),
				new DataTypeMapping(dbType,"LINE","OTHER",DBDataType.OBJECT,0,null),
				new DataTypeMapping(dbType,"LSEG","OTHER",DBDataType.OBJECT,0,null),
				new DataTypeMapping(dbType,"PATH","OTHER",DBDataType.OBJECT,0,null),
				new DataTypeMapping(dbType,"POINT","OTHER",DBDataType.OBJECT,0,null),
				new DataTypeMapping(dbType,"POLYGON","OTHER",DBDataType.OBJECT,0,null),
				new DataTypeMapping(dbType,"TSQUERY","OTHER",DBDataType.OBJECT,0,null),
				new DataTypeMapping(dbType,"TSVECTOR","OTHER",DBDataType.OBJECT,0,null),
				new DataTypeMapping(dbType,"TXID_SNAPSHOT","OTHER",DBDataType.OBJECT,0,null),
 
				

				
				
		}; 
		//
		this.addDataTypeMapping(dataTypeMappings);
	}
	
	
	 
	
	
	
	
	public static void main(String[] args) {
		
		PGDataMappingSet mSet=new PGDataMappingSet();
		Collection<DataTypeMapping> typesRaw=mSet.getAll();
		
		List<DataTypeMapping> types=new ArrayList<>();
		for (DataTypeMapping dataType : typesRaw) {
			if(dataType.getDbTypeName().equalsIgnoreCase("NUMBER")) {
				for (int i = 1; i < 32; i++) {
					DataTypeMapping n0=new PGNumberMapping("NUMBER",null,i,0);
					types.add(n0);
					DataTypeMapping n2=new PGNumberMapping("NUMBER",null,i,i%6+1);
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
