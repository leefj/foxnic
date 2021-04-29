package com.github.foxnic.dao.sql.tablefinder;

import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;

/**
 * @author fangjieli
 * */
public class SQLiteTableNameFinder extends ITableNameFinder {
	@Override
	protected SchemaStatVisitor getSchemaStatVisitor() {
		return new SQLServerSchemaStatVisitor();
	}
 
}
