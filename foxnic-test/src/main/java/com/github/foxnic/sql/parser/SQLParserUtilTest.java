package com.github.foxnic.sql.parser;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.alibaba.druid.DbType;
import com.github.foxnic.dao.sql.SQLParser;

public class SQLParserUtilTest {

	String sql = "select a.*,(select x from sys_pp s,sys_uc u where u.id=p.id) from org_system a left join org_user b on a.id=b.id  where code=   ? "
			+ " and (select y from usr_xxx where id=9) order by (select dd from usr_pp) desc";
	
	@Test
	public void test_oracle() {

		DbType dbType = DbType.oracle;
		List<String> tables = SQLParser.getAllTables(sql, dbType);
		assertTrue(tables.size()==6);
		assertTrue(tables.contains("org_system"));
		assertTrue(tables.contains("org_user"));
		
		sql="select DEMO_SEQ.nextVal from dualx";
		tables = SQLParser.getAllTables(sql, dbType);
		assertTrue(tables.size()==1); 
		assertTrue("dualx".equals(tables.get(0)));
		
		sql="select 1";
		tables = SQLParser.getAllTables(sql, dbType);
		assertTrue(tables.size()==0); 
	 
		
		sql="select 1 from dual";
		tables = SQLParser.getAllTables(sql, dbType);
		assertTrue(tables.size()==0);
		//assertTrue("dual".equals(tables.get(0)));
		
		sql="select DEMO_SEQ.nextVal from dual";
		tables = SQLParser.getAllTables(sql, dbType);
		assertTrue(tables.size()==0); 
		//assertTrue("dual".equals(tables.get(0)));
		
	}
	
	@Test
	public void test_mysql() {

		DbType dbType = DbType.mysql;
		List<String> tables = SQLParser.getAllTables(sql, dbType);
		assertTrue(tables.size()==6);
		assertTrue(tables.contains("org_system"));
		assertTrue(tables.contains("org_user"));
	}
	
	@Test
	public void test_sqlserver() {

		DbType dbType = DbType.sqlserver;
		List<String> tables = SQLParser.getAllTables(sql, dbType);
		assertTrue(tables.size()==6);
		assertTrue(tables.contains("org_system"));
		assertTrue(tables.contains("org_user"));
	}
	
	@Test
	public void test_sqlite() {

		DbType dbType = DbType.sqlite;
		List<String> tables = SQLParser.getAllTables(sql, dbType);
		assertTrue(tables.size()==6);
		assertTrue(tables.contains("org_system"));
		assertTrue(tables.contains("org_user"));
	}
	
	@Test
	public void test_postgres() {

		DbType dbType = DbType.postgresql;
		List<String> tables = SQLParser.getAllTables(sql, dbType);
		assertTrue(tables.size()==6);
		assertTrue(tables.contains("org_system"));
		assertTrue(tables.contains("org_user"));
	}
	
	@Test
	public void test_db2() {

		DbType dbType = DbType.db2;
		List<String> tables = SQLParser.getAllTables(sql, dbType);
		assertTrue(tables.size()==6);
		assertTrue(tables.contains("org_system"));
		assertTrue(tables.contains("org_user"));
		
	}
	
 

}
