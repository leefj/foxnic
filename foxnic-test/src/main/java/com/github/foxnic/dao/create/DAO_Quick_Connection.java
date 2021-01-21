package com.github.foxnic.dao.create;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.foxnic.dao.config.Configs;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.dao.spec.DAOBuilder;
import com.github.foxnic.dao.spring.Db2DAO;
import com.github.foxnic.dao.spring.MySqlDAO;
import com.github.foxnic.dao.spring.OracleDAO;
import com.github.foxnic.dao.spring.PostgresDAO;

 /**
  * 针对各种值类型测测试
  * */
public class DAO_Quick_Connection   {
 
	
	@Test
	public void test_connect_pg() {
		
		//TEST MYSQL
		PostgresDAO mydao=new PostgresDAO();
		mydao.setDataSource(Configs.PG_DRIVER, Configs.PG_URL, Configs.PG_USER, Configs.PG_PASSWD);
		int n=mydao.queryInteger("select 66");
		assertTrue(n==66);
		
		DAOBuilder builder=new DAOBuilder();
		try {
			DAO dao=builder.datasource(mydao.getDataSource()).build();
			int nm=dao.queryInteger("select 667");
			assertTrue(nm==667);
		} catch (Exception e) {
			assertTrue(false);
		}
 
		
	}
	
	@Test
	public void test_connect_mysql() {
		
		//TEST MYSQL
		MySqlDAO mydao=new MySqlDAO();
		mydao.setDataSource(Configs.MYSQL_DRIVER, Configs.MYSQL_URL, Configs.MYSQL_USER, Configs.MYSQL_PASSWD);
		int n=mydao.queryInteger("select 66");
		assertTrue(n==66);
		
		
		DAOBuilder builder=new DAOBuilder();
		try {
			DAO dao=builder.datasource(mydao.getDataSource()).build();
			int nm=dao.queryInteger("select 667");
			assertTrue(nm==667);
		} catch (Exception e) {
			assertTrue(false);
		}
 
		
	}
	
	@Test
	public void test_connect_oracle() {
 
		//TEST ORACLE
		OracleDAO oradao=new OracleDAO();
		oradao.setDataSource(Configs.ORACLE_DRIVER, Configs.ORACLE_URL, Configs.ORACLE_USER, Configs.ORACLE_PASSWD);
		int n=oradao.queryInteger("select 66 from dual");
		assertTrue(n==66);
		
		DAOBuilder builder=new DAOBuilder();
		try {
			DAO dao=builder.datasource(oradao.getDataSource()).build();
			int nm=dao.queryInteger("select 667 from dual");
			assertTrue(nm==667);
		} catch (Exception e) {
			assertTrue(false);
		}
		
	}
	
	@Test
	public void test_connect_db2() {
 
		//TEST ORACLE
		Db2DAO oradao=new Db2DAO();
		oradao.setDataSource(Configs.DB2_DRIVER, Configs.DB2_URL, Configs.DB2_USER, Configs.DB2_PASSWD);
		int n=oradao.queryInteger("select 66 from SYSIBM.SYSDUMMY1");
		assertTrue(n==66);
		
		DAOBuilder builder=new DAOBuilder();
		try {
			DAO dao=builder.datasource(oradao.getDataSource()).build();
			int nm=dao.queryInteger("select 667 from SYSIBM.SYSDUMMY1");
			assertTrue(nm==667);
		} catch (Exception e) {
			assertTrue(false);
		}
		
	}
	
	 
	
	
	

}
