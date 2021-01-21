package com.github.foxnic.dao.sql.tablefinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.github.foxnic.commons.lang.StringUtil;

/**
 * @author fangjieli
 * */
public abstract class ITableNameFinder  {

	public List<String> getAllTables(SQLStatement stmt) {
		List<String> tableNames=new ArrayList<String>();
		SchemaStatVisitor visitor = getSchemaStatVisitor();
		stmt.accept(visitor);
		Map<TableStat.Name, TableStat> tables = visitor.getTables();
		Set<TableStat.Name> tableNameSet = tables.keySet();
		for (TableStat.Name name : tableNameSet) {
			String tableName = name.getName();
			if (StringUtil.hasContent(tableName)) {
				tableNames.add(tableName);
			}
		}
		return tableNames;
	}
	
	protected abstract SchemaStatVisitor getSchemaStatVisitor();
	
}
