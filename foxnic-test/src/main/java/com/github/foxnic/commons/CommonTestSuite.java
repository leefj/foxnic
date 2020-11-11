package com.github.foxnic.commons;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.foxnic.commons.lang.ArrayUtil_Normal;
import com.github.foxnic.commons.lang.DateUtilTest;
import com.github.foxnic.commons.xml.XMLTest;

@RunWith(Suite.class)
@SuiteClasses({
 
		// lang
		ArrayUtil_Normal.class, ArrayUtil_Normal.class, DateUtilTest.class,
		//xml
		XMLTest.class
		 

})
public class CommonTestSuite {

	@BeforeClass
	public static void begin() {

	}

	@AfterClass
	public static void end() {

	}
}
