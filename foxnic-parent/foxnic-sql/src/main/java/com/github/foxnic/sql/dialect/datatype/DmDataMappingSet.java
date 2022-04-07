package com.github.foxnic.sql.dialect.datatype;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.meta.DBType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DmDataMappingSet extends DataTypeMappingSet {

	private static class DmNumberMapping extends DataTypeMapping {

		public DmNumberMapping(String dbTypeName, DBDataType dbDataType, Integer sampleDataLength, Integer sampleNumScale)
		{
			super(DBType.DM,dbTypeName, null,dbDataType, sampleDataLength, sampleNumScale);
		}

		@Override
		public DBDataType getDbDataType(String table,String column,Integer precision, Integer scale) {
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

		@Override
		public String getJdbcTypeName(Integer precision, Integer scale) {
			if(scale!=null && scale>0) {
				return "DOUBLE";
			}
			if(precision==null)  return "BIGINT";
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

	public DmDataMappingSet()
	{
		DBType dbType=DBType.DM;
		//
		DataTypeMapping[] dataTypeMappings= {

				// 精确数值数据类型
				new DmNumberMapping("NUMERIC",null,8,2),
				new DataTypeMapping(dbType,"DECIMAL","DECIMAL",DBDataType.DECIMAL,8,null),
				new DataTypeMapping(dbType,"DEC","DECIMAL",DBDataType.DECIMAL,8,null),
				new DmNumberMapping("NUMBER",null,8,2),
				new DataTypeMapping(dbType,"INTEGER","INTEGER",DBDataType.INTEGER,6,null),
				new DataTypeMapping(dbType,"INT","INTEGER",DBDataType.INTEGER,6,null),
				new DataTypeMapping(dbType,"BIGINT","BIGINT",DBDataType.LONG,6,null),
				new DataTypeMapping(dbType,"TINYINT","TINYINT",DBDataType.INTEGER,6,null),
				new DataTypeMapping(dbType,"SMALLINT","SMALLINT",DBDataType.INTEGER,6,null),
				new DataTypeMapping(dbType,"BYTE","BYTE",DBDataType.INTEGER,6,null),
				// BIT
				new DataTypeMapping(dbType,"BIT","BIT",DBDataType.BYTES,8,null),
				// 近似数值类型
				new DataTypeMapping(dbType,"FLOAT","FLOAT",DBDataType.FLOAT,4,null),
				new DataTypeMapping(dbType,"DOUBLE","DOUBLE",DBDataType.DOUBLE,6,2),
				new DataTypeMapping(dbType,"DOUBLE PRECISION","DOUBLE",DBDataType.DOUBLE,6,2),
				new DataTypeMapping(dbType,"REAL","DOUBLE",DBDataType.DOUBLE,6,2),
				//
				new DataTypeMapping(dbType,"VARCHAR2","VARCHAR",DBDataType.STRING,8,null),
				new DataTypeMapping(dbType,"VARCHAR","VARCHAR",DBDataType.STRING,8,null),
				new DataTypeMapping(dbType,"CHAR","CHAR",DBDataType.STRING,8,null),
				//
				new DataTypeMapping(dbType,"DATE","DATE",DBDataType.DATE,null,null),
				new DataTypeMapping(dbType,"DATETIME","DATETIME",DBDataType.DATE,null,null),
				new DataTypeMapping(dbType,"TIME","TIME",DBDataType.DATE,null,null),
				new DataTypeMapping(dbType,"TIMESTAMP","TIMESTAMP",DBDataType.TIMESTAME,6,null),
				//
				new DataTypeMapping(dbType,"TEXT","TEXT",DBDataType.STRING,null,null),
				new DataTypeMapping(dbType,"CLOB","CLOB",DBDataType.STRING,null,null),
				new DataTypeMapping(dbType,"BLOB","BLOB",DBDataType.BLOB,null,null)


		};
		//
		this.addDataTypeMapping(dataTypeMappings);
	}







	public static void main(String[] args) {

		DmDataMappingSet mSet=new DmDataMappingSet();
		Collection<DataTypeMapping> typesRaw=mSet.getAll();

		List<DataTypeMapping> types=new ArrayList<>();
		for (DataTypeMapping dataType : typesRaw) {
			if(dataType.getDbTypeName().equalsIgnoreCase("NUMBER")) {
				for (int i = 1; i < 32; i++) {
					DataTypeMapping n0=new DmNumberMapping("NUMBER",null,i,0);
					types.add(n0);
					DataTypeMapping n2=new DmNumberMapping("NUMBER",null,i,i%6+1);
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
