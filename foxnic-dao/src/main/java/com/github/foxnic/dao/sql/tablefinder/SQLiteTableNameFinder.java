package com.github.foxnic.dao.sql.tablefinder;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleOutputVisitor;

/**
 * @author fangjieli
 * */
public class SQLiteTableNameFinder extends OracleOutputVisitor implements ITableNameFinder {

	private List<String> tables=new ArrayList<String>();
	
	@Override
	public List<String> getAllTables() {
		return tables;
	}

	public SQLiteTableNameFinder(Appendable appender) {
		super(appender);
	}

	@Override
	public boolean visit(SQLExprTableSource x) {
		SQLName table = (SQLName) x.getExpr();
		String tableName = table.getSimpleName();
		tables.add(tableName.toLowerCase());
		return true;
	}
	
}
