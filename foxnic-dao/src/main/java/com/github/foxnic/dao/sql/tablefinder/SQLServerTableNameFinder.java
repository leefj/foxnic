package com.github.foxnic.dao.sql.tablefinder;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerOutputVisitor;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;

/**
 * @author fangjieli
 * */
public class SQLServerTableNameFinder  extends ITableNameFinder {
	@Override
	protected SchemaStatVisitor getSchemaStatVisitor() {
		return new SQLServerSchemaStatVisitor();
	}
 
}
