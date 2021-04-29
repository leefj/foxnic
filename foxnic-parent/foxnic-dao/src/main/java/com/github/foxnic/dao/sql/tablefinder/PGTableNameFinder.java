package com.github.foxnic.dao.sql.tablefinder;

import com.alibaba.druid.sql.dialect.postgresql.visitor.PGSchemaStatVisitor;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;

/**
 * @author fangjieli
 * */
public class PGTableNameFinder extends ITableNameFinder {
	@Override
	protected SchemaStatVisitor getSchemaStatVisitor() {
		return new PGSchemaStatVisitor();
	}
 
}
