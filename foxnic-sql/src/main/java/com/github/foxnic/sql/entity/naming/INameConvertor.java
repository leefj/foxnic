package com.github.foxnic.sql.entity.naming;

import com.github.foxnic.sql.meta.DBDataType;

/**
 * 
 * @author fangjieli
 *
 */
public interface INameConvertor {
	public String getPackageName(String tableName);
	public String toClassNameStyle(String name);
	public String getClassName(String tableName,int startPartIndex);
	public String getPropertyName(String columnName);
	public String getGetMethodName(String columnName,DBDataType cata);
	public String getSetMethodName(String columnName,DBDataType cata);
	public String getRcdGetMethodName(DBDataType categery);
	public String getJoinId(String tableName,int startPartIndex,String[] fieldsA,String[] fieldsB,String[] relarionFields);
	public String getJoinId(String entityClassName,String[] fieldsA,String[] fieldsB,String[] relarionFields);

}
