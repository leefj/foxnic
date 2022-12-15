package com.github.foxnic.sql.dialect.datatype;

import java.util.ArrayList;
import java.util.Collection;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.meta.DBType;

public class DB2DataMappingSet extends DataTypeMappingSet {

	public DB2DataMappingSet()
	{
		DBType dbType=DBType.DB2;
		//
		DataTypeMapping[] dataTypeMappings= {


				//整形
				new DataTypeMapping(dbType,"SMALLINT","SMALLINT",DBDataType.INTEGER,null,null),
				new DataTypeMapping(dbType,"INTEGER","INTEGER",DBDataType.INTEGER,null,null),
				new DataTypeMapping(dbType,"BIGINT","BIGINT",DBDataType.LONG,null,null),
				//小数
				new DataTypeMapping(dbType,"DOUBLE","DOUBLE",DBDataType.DOUBLE,null,null),
				new DataTypeMapping(dbType,"REAL","FLOAT",DBDataType.FLOAT,null,null),
				new DataTypeMapping(dbType,"DECIMAL","DECIMAL",DBDataType.DECIMAL,6,2),
				//字符串
				new DataTypeMapping(dbType,"GRAPHIC","VARCHAR",DBDataType.STRING,64,null),
				new DataTypeMapping(dbType,"VARGRAPHIC","VARCHAR",DBDataType.STRING,64,null),
				new DataTypeMapping(dbType,"CHAR","CHAR",DBDataType.STRING,64,null),
				new DataTypeMapping(dbType,"VARCHAR","VARCHAR",DBDataType.STRING,64,null),
				//日期时间
				new DataTypeMapping(dbType,"TIME","TIME",DBDataType.TIME,null,null),
				new DataTypeMapping(dbType,"DATE","DATE",DBDataType.DATE,null,null),
				new DataTypeMapping(dbType,"TIMESTMP","TIMESTMP",DBDataType.TIMESTAMP,6,null),
				//
				new DataTypeMapping(dbType,"BLOB","BLOB",DBDataType.BLOB,null,null),
				new DataTypeMapping(dbType,"CLOB","CLOB",DBDataType.STRING,null,null)
		};
		//
		this.addDataTypeMapping(dataTypeMappings);
	}



	public static void main(String[] args) {

		DB2DataMappingSet mSet = new DB2DataMappingSet();
		Collection<DataTypeMapping> types = mSet.getAll();

		ArrayList<String> columns = new ArrayList<>();
		ArrayList<String> comments = new ArrayList<>();

		for (DataTypeMapping dataType : types) {
			String cn = dataType.getDbTypeName() + "_value";
			if (dataType.getSampleDataLength() != null) {
				cn += "_l" + dataType.getSampleDataLength();
			}

			if (dataType.getSampleNumScale() != null) {
				cn += "_s" + dataType.getSampleNumScale();
			}

			comments.add("COMMENT ON COLUMN tity_all_type." + cn + " IS '" + dataType.getDbTypeName() + "类型字段'");

			cn += " " + dataType.getDbTypeName();

			if (dataType.getSampleDataLength() != null || dataType.getSampleNumScale() != null) {
				cn += "(";
				if (dataType.getSampleDataLength() != null) {
					cn += dataType.getSampleDataLength();
				}
				if (dataType.getSampleNumScale() != null) {
					cn += "," + dataType.getSampleNumScale();
				}
				cn += ")";
			}

			columns.add(cn);

		}

		String cr = "CREATE TABLE tity_all_type (\n";
		cr += "ID INTEGER NOT NULL\n,";
		cr += StringUtil.join(columns, ",\n");
		cr += "\n)";
		System.out.println(cr);

		for (String string : comments) {
			System.out.println(string);
		}

	}

}
