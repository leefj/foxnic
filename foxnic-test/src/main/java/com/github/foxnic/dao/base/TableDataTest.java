package com.github.foxnic.dao.base;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.config.Configs;
import com.github.foxnic.dao.config.TestDAO;
import com.github.foxnic.dao.spec.DAO;

public class TableDataTest {

	protected static DAO DAO_4_TEST = null;
	protected static String NORMAL_TABLE_4_TEST = null;
	protected static String PK_TABLE_4_TEST = null;
	protected static String CLOB_TABLE_4_TEST = null;
	protected static String ALL_TYPE_TABLE = null;
	protected static  String STARTUP_CLASS=null;

	protected DAO dao = null;
	protected String normalTable = null;
	protected String pkTable = null;
	protected String clobTable = null;
	
	/**
	 * 测试前的准备
	 */
	public static void prepareIf() {
		
		
		
		if (DAO_4_TEST != null) return;
		
		StackTraceElement[] els=(new Throwable()).getStackTrace();
		STARTUP_CLASS=els[1].getClassName();
		
		Logger.info("prepare for  test ...");
		 
		
		
		DAO_4_TEST = Configs.getDAO();
		TestDAO td = (TestDAO) DAO_4_TEST;
		NORMAL_TABLE_4_TEST = "test_news_tity";
		PK_TABLE_4_TEST = "test_relation_tity";
		CLOB_TABLE_4_TEST="test_content";
		ALL_TYPE_TABLE="test_all_type";
		System.err.println("table = " + NORMAL_TABLE_4_TEST);
		td.setTableName(NORMAL_TABLE_4_TEST,PK_TABLE_4_TEST,CLOB_TABLE_4_TEST,ALL_TYPE_TABLE);
		td.createTables();
		Logger.info("prepare reday");
	}

	/**
	 * 完成测试后的清理
	 */
	public static void cleanUpIf() {
		
		StackTraceElement[] els=(new Throwable()).getStackTrace();
		String startupClass=els[1].getClassName();
		if(!startupClass.equals(STARTUP_CLASS)) return;
		
		TestDAO td = (TestDAO) DAO_4_TEST;
		td.dropTables();
	}

	@Before
	public void before() throws Exception {
		prepareIf();
		dao = DAO_4_TEST;
		normalTable = NORMAL_TABLE_4_TEST;
		pkTable = PK_TABLE_4_TEST;
		clobTable=CLOB_TABLE_4_TEST;
		if(dao.isTableExists(normalTable)) {
			dao.execute("delete from " + normalTable);
		}
		if(dao.isTableExists(pkTable)) {
			dao.execute("delete from " + pkTable);
		}
		if(dao.isTableExists(pkTable)) {
			dao.execute("delete from " + pkTable);
		}
	}

	@After
	public void after() throws Exception {

	}
	
	@AfterClass
    public static  void afterRun(){
		cleanUpIf();
    }
 


}
