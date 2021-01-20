package com.github.foxnic.dao;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.foxnic.dao.base.TableDataTest;
import com.github.foxnic.dao.clob.DAO_Clob;
import com.github.foxnic.dao.create.DAO_Quick_Connection;
import com.github.foxnic.dao.data.DataSet_Normal;
import com.github.foxnic.dao.data.ExcelWriterBasic;
import com.github.foxnic.dao.data.RcdSetFilter;
import com.github.foxnic.dao.data.RcdSetGetCollections;
import com.github.foxnic.dao.data.RcdSetJsonField;
import com.github.foxnic.dao.data.RcdSetSort;
import com.github.foxnic.dao.data.RcdSetVersion;
import com.github.foxnic.dao.entity.DAO_Entity_JPA;
import com.github.foxnic.dao.entity.DAO_Entity_MyBatis;
import com.github.foxnic.dao.entity.DAO_Entity_Pure;
import com.github.foxnic.dao.entity.DAO_QueryEntity_JPA;
import com.github.foxnic.dao.entity.DAO_QueryEntity_MyBatis;
import com.github.foxnic.dao.entity.DAO_QueryEntity_Pure;
import com.github.foxnic.dao.exec.DAO_BatchExec;
import com.github.foxnic.dao.exec.DAO_MultiExec;
import com.github.foxnic.dao.exec.DAO_Transaction;
import com.github.foxnic.dao.meta.DAO_Meta;
import com.github.foxnic.dao.query.DAO_Page;
import com.github.foxnic.dao.query.DAO_QueryMeta;
import com.github.foxnic.dao.query.DAO_QueryObject;
import com.github.foxnic.dao.query.DAO_QueryValue;

@RunWith(Suite.class)
@SuiteClasses({
	
	//
	DAO_Quick_Connection.class,
	//
	DAO_BatchExec.class,
	DAO_Clob.class,
	DAO_QueryObject.class,
	DAO_QueryValue.class,
	//
	DAO_Entity_Pure.class,
	DAO_Entity_MyBatis.class,
	DAO_Entity_JPA.class,
	//
	DAO_QueryEntity_MyBatis.class,
	DAO_QueryEntity_Pure.class,
	DAO_QueryEntity_JPA.class,
	//
	DAO_Page.class,
	DAO_Meta.class,
	DAO_QueryMeta.class,
	DAO_MultiExec.class,
	DAO_Transaction.class,
	DAO_Quick_Connection.class,
	//
	RcdSetFilter.class,
	RcdSetSort.class,
	RcdSetVersion.class,
	ExcelWriterBasic.class,
	DataSet_Normal.class,
	RcdSetGetCollections.class,
	RcdSetJsonField.class
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
