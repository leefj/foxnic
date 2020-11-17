package com.github.foxnic.sql.dialect.datatype;

import java.util.ArrayList;
import java.util.Collection;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.meta.DBDataType;

public class MySQLDataTypeMappingSet extends DataTypeMappingSet {
	
	public MySQLDataTypeMappingSet()
	{
		DataTypeMapping[] dataTypeMappings = {
				//BIT
				new DataTypeMapping("BIT",DBDataType.BYTES,8,null),
				//整形
				new DataTypeMapping("TINYINT",DBDataType.INTEGER,6,null),
				new DataTypeMapping("SMALLINT",DBDataType.INTEGER,6,null),
				new DataTypeMapping("MEDIUMINT",DBDataType.INTEGER,6,null),
				new DataTypeMapping("INT",DBDataType.INTEGER,6,null),
				new DataTypeMapping("BIGINT",DBDataType.LONG,6,null),	
				//小数
				new DataTypeMapping("FLOAT",DBDataType.FLOAT,6,2),
				new DataTypeMapping("DOUBLE",DBDataType.DOUBLE,6,2),
				new DataTypeMapping("DECIMAL",DBDataType.DECIMAL,6,2),
				//字符串
				new DataTypeMapping("CHAR",DBDataType.STRING,64,null),
				new DataTypeMapping("VARCHAR",DBDataType.STRING,64,null),
				new DataTypeMapping("TINYTEXT",DBDataType.STRING,null,null),
				new DataTypeMapping("TEXT",DBDataType.STRING,null,null),
				new DataTypeMapping("MEDIUMTEXT",DBDataType.STRING,null,null),
				new DataTypeMapping("LONGTEXT",DBDataType.STRING,null,null),
				new DataTypeMapping("ENUM",DBDataType.STRING,null,null),
				new DataTypeMapping("SET",DBDataType.STRING,null,null),
				//日期时间
				new DataTypeMapping("TIME",DBDataType.TIME,null,null),
				new DataTypeMapping("DATE",DBDataType.DATE,null,null),
				new DataTypeMapping("DATETIME",DBDataType.DATE,null,null),
				new DataTypeMapping("TIMESTAMP",DBDataType.TIMESTAME,null,null),
				new DataTypeMapping("YEAR",DBDataType.INTEGER,null,null),
				//
				new DataTypeMapping("BLOB",DBDataType.BLOB,null,null),
				new DataTypeMapping("LONGBLOB",DBDataType.BLOB,null,null),
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
