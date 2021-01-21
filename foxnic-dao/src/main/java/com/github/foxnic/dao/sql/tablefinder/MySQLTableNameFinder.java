package com.github.foxnic.dao.sql.tablefinder;

import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;

/**
 * @author fangjieli
 * */
public class MySQLTableNameFinder extends ITableNameFinder {
	@Override
	protected SchemaStatVisitor getSchemaStatVisitor() {
		return new MySqlSchemaStatVisitor();
	}
 
}
