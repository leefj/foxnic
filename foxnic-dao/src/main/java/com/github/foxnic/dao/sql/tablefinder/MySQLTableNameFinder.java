package com.github.foxnic.dao.sql.tablefinder;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;

/**
 * @author fangjieli
 * */
public class MySQLTableNameFinder extends MySqlOutputVisitor implements ITableNameFinder {

	private List<String> tables=new ArrayList<String>();
	
	@Override
	public List<String> getAllTables() {
		return tables;
	}

	public MySQLTableNameFinder(Appendable appender) {
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
