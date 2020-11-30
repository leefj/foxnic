package com.github.foxnic.sql.expr;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.alibaba.druid.DbType;
import com.github.foxnic.dao.sql.SQLParser;

public class SQLParserUtilTest {

	@Test
	public void aaa() {

		String sql = "select * from org_system a left join org_user b on a.id=b.id  where code=   ?";
		DbType dbType = DbType.oracle;
		List<String> tables = SQLParser.getAllTables(sql, dbType);
		assertTrue(tables.contains("org_system"));
		assertTrue(tables.contains("org_user"));
	}

}
