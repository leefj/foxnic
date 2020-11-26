package com.github.foxnic.dao;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.foxnic.dao.base.TableDataTest;
import com.github.foxnic.dao.exec.DAO_BatchExec;
import com.github.foxnic.dao.exec.DAO_MultiExec;
import com.github.foxnic.dao.exec.DAO_Transaction;
import com.github.foxnic.dao.meta.DAO_Meta;

@RunWith(Suite.class)
@SuiteClasses({
	DAO_BatchExec.class,
//	DAO_Clob.class,
//	DAO_QueryObject.class,
//	DAO_QueryValue.class,
//	DAO_Pojo.class,
//	DAO_Entity.class,
//	DAO_Page.class,
	DAO_Meta.class,
//	DAO_QueryMeta.class,
	DAO_MultiExec.class,
	DAO_Transaction.class,
//	DAO_Quick_Connection.class,
//	TestEntityConfigBuilder.class
	
})
public class DAOTestSuite {
	
	
 
	@BeforeClass
	public static void begin() {
		TableDataTest.prepareIf();
	}

	@AfterClass
	public static void end() {
		TableDataTest.cleanUpIf();
	}
}
