package com.github.foxnic.dao.create;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.foxnic.dao.config.Configs;
import com.github.foxnic.dao.spring.MySqlDAO;
import com.github.foxnic.dao.spring.OracleDAO;

 /**
  * 针对各种值类型测测试
  * */
public class DAO_Quick_Connection   {
 
	
	@Test
	public void test_connect() {
		
		//TEST MYSQL
		MySqlDAO mydao=new MySqlDAO();
		mydao.setDataSource(Configs.MYSQL_DRIVER, Configs.MYSQL_URL, Configs.MYSQL_USER, Configs.MYSQL_PASSWD);
		int n=mydao.queryInteger("select 66");
		assertTrue(n==66);
		
		//TEST ORACLE
//		OracleDAO oradao=new OracleDAO();
//		oradao.setDataSource(Configs.ORACLE_DRIVER, Configs.ORACLE_URL, Configs.ORACLE_USER, Configs.ORACLE_PASSWD);
//		n=mydao.queryInteger("select 66 from dual");
//		assertTrue(n==66);
		
	}
	
	 
	
	
	

}
