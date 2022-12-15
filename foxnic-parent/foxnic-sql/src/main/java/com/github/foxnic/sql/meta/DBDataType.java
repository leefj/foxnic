package com.github.foxnic.sql.meta;

import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.sql.dialect.datatype.DataTypeMapping;
import com.github.foxnic.sql.dialect.datatype.DataTypeMappingSet;
import com.github.foxnic.sql.exception.ExprException;
import com.github.foxnic.sql.treaty.DBTreaty;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;



/**
 * 数据库类型 对应 JAVA类型 的关系
 * @author fangjieli
 * */
public enum DBDataType {

	/**
	 * 对象类型
	 */
	OBJECT(Object.class,false),
	/**
	 * 文本类型
	 */
	STRING(String.class,false, Types.VARCHAR,Types.CHAR,Types.LONGNVARCHAR),
	/**
	 * 日期类型
	 */
	DATE(Date.class,false,Types.DATE),
	/**
	 * 时间类型
	 */
	TIME(Time.class,false,Types.TIME),
	/**
	 * 日期类型
	 */
	TIMESTAMP(Timestamp.class,false,Types.TIMESTAMP),

	/**
	 * BYTE类型
	 */
	BYTE(Byte.class,true,Types.TINYINT),

	/**
	 * 整型类型
	 */
	SMALLINT(Short.class,true,Types.SMALLINT),

	/**
	 * 整型类型
	 */
	INTEGER(Integer.class,true,Types.INTEGER),
	/**
	 * 长整型
	 * */
	LONG(Long.class,true,Types.BIGINT),
	/**
	 * 长整型
	 * */
	BIGINT(BigInteger.class,true,Types.BIGINT),
	/**
	 * 浮点数值类型
	 */
	DECIMAL(BigDecimal.class,true,Types.DECIMAL,Types.NUMERIC),

	/**
	 * 浮点数值类型
	 */
	FLOAT(Float.class,true,Types.REAL),

	/**
	 * 浮点数值类型
	 */
	DOUBLE(Double.class,true,Types.DOUBLE),
	/**
	 * 逻辑类型
	 */
	BOOL(Boolean.class,false,Types.BIT,Types.BOOLEAN),

	CLOB(String.class,false,Types.CLOB),

	BLOB(Blob.class,false,Types.BLOB),

	ARRAY(Object[].class,false,Types.ARRAY),

	BYTES(Byte[].class,false,Types.BINARY,Types.VARBINARY,Types.LONGVARBINARY);

	private Class<?> type=null;

	/**
	 *  Java 类型
	 * */
	public Class<?> getType() {
		return type;
	}

	/**
	 * 是否数值类型，包括整数和小数
	 * @return 逻辑值
	 * */
	public boolean isDigital() {
		return digital;
	}


	private boolean digital;

	private int[] jdbcTypes=null;
	/**
	 * 获得Java类型
	 * @param type 类型
	 * @param isDigital 是否数值
	 * */
	private DBDataType(Class<?> type,boolean isDigital,int... jdbcTypes)
	{
		this.type=type;
		this.digital=isDigital;
		this.jdbcTypes=jdbcTypes;
	}

	public int[] getJDBCTypes() {
		return jdbcTypes;
	}

	public int getDefaultJDBCType() {
		if(jdbcTypes==null || jdbcTypes.length==0) return Types.OTHER;
		return jdbcTypes[0];
	}

	/**
	 * 将数据转换成具体对应类型的值
	 * @param val 值
	 * @return 转换后的值
	 * */
	public Object cast(Object val) {
		if(val==null) return null;
		if(this==STRING) {
			return DataParser.parseString(val);
		}
		else if(this==CLOB) {
			return DataParser.parseString(val);
		} else if(this==BLOB) {
			throw new ExprException("not support blob type");
		} else if(this==BYTES) {
			throw new ExprException("not support bytes type");
		} else if(this==DATE) {
			return DataParser.parseDate(val);
		} else if(this== TIMESTAMP) {
			return DataParser.parseTime(val);
		} else if(this==DECIMAL) {
			return DataParser.parseBigDecimal(val);
		} else if(this==FLOAT) {
			return DataParser.parseFloat(val);
		} else if(this==DOUBLE) {
			return DataParser.parseDouble(val);
		} else if(this==INTEGER) {
			Integer valInt=DataParser.parseInteger(val);
			return valInt;
		} else if(this==LONG) {
			Long valLong=DataParser.parseLong(val);
			if(valLong!=null) {
				return valLong;
			}
			BigInteger valBigint=DataParser.parseBigInteger(val);
			return valBigint.longValue();
		} else if(this==BIGINT) {
			return DataParser.parseBigInteger(val);
		} else if(this==BOOL) {
			return DataParser.parseBoolean(val);
		} else {
			return val;
		}
	}


	public static DBDataType parseFromType(Class<?> type) {
		if(type==null) {
			throw new RuntimeException("not support type null");
		}
		for (DBDataType dbDataType : DBDataType.values()) {
			if(dbDataType.getType().equals(type)) return dbDataType;
		}
		throw new RuntimeException("not support type "+type.getName());
	}

	/**
	 * 把数据库类型转换成具体的分类
	 * @param dbType 数据库类型
	 * @param dbTypeName 数据库数据类型
	 * @param len 长度
	 * @param scale 精度
	 * @return DBTypeCategery
	 * */
	public static DBDataType parseFromDBInfo(String table,String column,DBTreaty dbTreaty,DBType dbType,String dbTypeName,Integer len,Integer precision,Integer scale,String comment) {

		if("is_active".equals(column)) {
			System.out.println();
		}

		if(len==null) len=0;
		if(scale==null) scale=0;
		dbTypeName=dbTypeName.toLowerCase();

		boolean isLogic=dbTreaty.isLogicField(table,column, len,comment);
		if(isLogic) return DBDataType.BOOL;

		DataTypeMappingSet dataTypeMappingSet=dbType.getSQLDialect().getDialectProcessor().getDataTypeMappingSet();

		if(dataTypeMappingSet!=null) {
			DataTypeMapping mapping=dataTypeMappingSet.getDataTypeMapping(dbTypeName);
			if(mapping==null) {
				throw new ExprException("不支持的数据类型:"+dbType.name()+" , "+table+"."+column);
			}
			return mapping.getDbDataType(table,column,precision, scale);
		} else {
			throw new ExprException("不支持的数据类型:"+dbType.name()+" , "+table+"."+column);
		}
	}



}
