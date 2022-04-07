package com.github.foxnic.sql.dialect.datatype;

import java.util.ArrayList;
import java.util.Collection;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.meta.DBType;

public class MySQLDataTypeMappingSet extends DataTypeMappingSet {

	public MySQLDataTypeMappingSet()
	{
		DBType dbType=DBType.MYSQL;
		DataTypeMapping[] dataTypeMappings = {
				//BIT
				new DataTypeMapping(dbType,"BIT","BIT",DBDataType.BYTES,8,null),
				//整形
				new DataTypeMapping(dbType,"TINYINT","TINYINT",DBDataType.INTEGER,6,null),
				new DataTypeMapping(dbType,"SMALLINT","SMALLINT",DBDataType.INTEGER,6,null),
				new DataTypeMapping(dbType,"MEDIUMINT","INTEGER",DBDataType.INTEGER,6,null),
				new DataTypeMapping(dbType,"INT","INTEGER",DBDataType.INTEGER,6,null),
				new DataTypeMapping(dbType,"INTEGER","INTEGER",DBDataType.INTEGER,6,null),
				new DataTypeMapping(dbType,"BIGINT","BIGINT",DBDataType.LONG,6,null),
				//小数
				new DataTypeMapping(dbType,"FLOAT","FLOAT",DBDataType.FLOAT,6,2),
				new DataTypeMapping(dbType,"DOUBLE","DOUBLE",DBDataType.DOUBLE,6,2),
				new DataTypeMapping(dbType,"DECIMAL","DECIMAL",DBDataType.DECIMAL,6,2),
				//字符串
				new DataTypeMapping(dbType,"CHAR","CHAR",DBDataType.STRING,64,null),
				new DataTypeMapping(dbType,"VARCHAR","VARCHAR",DBDataType.STRING,64,null),
				new DataTypeMapping(dbType,"TINYTEXT","LONGVARCHAR",DBDataType.STRING,null,null),
				new DataTypeMapping(dbType,"TEXT","LONGVARCHAR",DBDataType.STRING,null,null),
				new DataTypeMapping(dbType,"MEDIUMTEXT","LONGVARCHAR",DBDataType.STRING,null,null),
				new DataTypeMapping(dbType,"LONGTEXT","LONGVARCHAR",DBDataType.STRING,null,null),
				new DataTypeMapping(dbType,"ENUM","VARCHAR",DBDataType.STRING,null,null),
				new DataTypeMapping(dbType,"SET","VARCHAR",DBDataType.STRING,null,null),
				//日期时间
				new DataTypeMapping(dbType,"TIME","TIME",DBDataType.TIME,null,null),
				new DataTypeMapping(dbType,"DATE","DATE",DBDataType.DATE,null,null),
				new DataTypeMapping(dbType,"DATETIME","TIMESTAMP",DBDataType.DATE,null,null),
				new DataTypeMapping(dbType,"TIMESTAMP","TIMESTAMP",DBDataType.TIMESTAME,null,null),
				new DataTypeMapping(dbType,"YEAR","INTEGER",DBDataType.INTEGER,null,null),
				//
				new DataTypeMapping(dbType,"BLOB","BLOB",DBDataType.BLOB,null,null),
				new DataTypeMapping(dbType,"LONGBLOB","BLOB",DBDataType.BLOB,null,null),
		};
		//
		this.addDataTypeMapping(dataTypeMappings);
	}



	public static void main(String[] args) {

		MySQLDataTypeMappingSet mSet=new MySQLDataTypeMappingSet();
		Collection<DataTypeMapping> types=mSet.getAll();

		ArrayList<String> columns=new ArrayList<>();



		for (DataTypeMapping dataType : types) {
			String cn=dataType.getDbTypeName()+"_value";
			if(dataType.getSampleDataLength()!=null) {
				cn+="_l"+dataType.getSampleDataLength();
			}

			if(dataType.getSampleNumScale()!=null) {
				cn+="_s"+dataType.getSampleNumScale();
			}

			cn+=" "+dataType.getDbTypeName();

			if(dataType.getDbTypeName().equalsIgnoreCase("SET") || dataType.getDbTypeName().equalsIgnoreCase("ENUM")) {
				cn+="('A','B','C','D')";
			} else {
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
			}

			cn+=" "+"COMMENT '"+dataType.getDbTypeName()+"类型字段'";

			columns.add(cn);

		}

		String cr="CREATE TABLE tity_all_type (\n" ;
		cr+="ID int(11) NOT NULL COMMENT 'ID',";
		cr+=StringUtil.join(columns,",\n");
		cr+="\n,  PRIMARY KEY (ID) USING BTREE\n";
		cr+=") ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT = '类型测试表'";
		System.out.println(cr);

	}

}
