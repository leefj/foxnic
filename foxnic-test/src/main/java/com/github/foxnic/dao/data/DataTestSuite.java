package com.github.foxnic.dao.data;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.foxnic.dao.base.TableDataTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	RcdSetFilter.class,
	RcdSetSort.class,
	RcdSetVersion.class,
	ExcelWriterBasic.class,
	DataSet_Normal.class,
	RcdSetGetCollections.class,
	RcdSetJsonField.class
})
public class DataTestSuite {

	@BeforeClass
	public static void begin() {
		TableDataTest.prepareIf();
	}

	@AfterClass
	public static void end() {
		TableDataTest.cleanUpIf();
	}
}
