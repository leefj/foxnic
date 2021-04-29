package com.github.foxnic.dao.sql.tablefinder;

import com.alibaba.druid.sql.dialect.db2.visitor.DB2SchemaStatVisitor;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;

/**
 * @author fangjieli
 * */
public class DB2TableNameFinder extends ITableNameFinder {
	@Override
	protected SchemaStatVisitor getSchemaStatVisitor() {
		return new DB2SchemaStatVisitor();
	}
 
}
 
