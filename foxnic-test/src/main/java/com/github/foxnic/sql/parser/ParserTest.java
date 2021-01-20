package com.github.foxnic.sql.parser;

import org.junit.Test;

import com.github.foxnic.dao.sql.SQLParser;
import com.github.foxnic.sql.dialect.SQLDialect;

public class ParserTest {
	
	@Test
	public void testname() throws Exception {
		SQLParser.parseIndexCreationStatement("CREATE UNIQUE INDEX test01_pkey ON foxnic_test.test01 USING btree (idx, owner_id, type)",SQLDialect.PSQL);
	}

}
