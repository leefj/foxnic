package com.github.foxnic;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.foxnic.commons.CommonTestSuite;
import com.github.foxnic.dao.base.TableDataTest;
import com.github.foxnic.dao.meta.DAO_Meta;
import com.github.foxnic.sql.SQLTestSuite;

@RunWith(Suite.class)
@SuiteClasses({ 
	CommonTestSuite.class,
	SQLTestSuite.class,
	DAO_Meta.class
})
public class TestAll {

	@BeforeClass
	public static void begin() {
		TableDataTest.prepareIf();
	}

	@AfterClass
	public static void end() {
		TableDataTest.cleanUpIf();
	}
}
