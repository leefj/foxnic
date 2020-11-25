package com.github.foxnic.commons;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.foxnic.commons.bean.BeanUtil_Advance;
import com.github.foxnic.commons.bean.BeanUtil_Normal;
import com.github.foxnic.commons.cache.DoubleCacheTest;
import com.github.foxnic.commons.cache.LocalCacheTest;
import com.github.foxnic.commons.collection.MapUtil_Group;
import com.github.foxnic.commons.collection.MapUtil_Normal;
import com.github.foxnic.commons.collection.TreeUtilTest;
import com.github.foxnic.commons.collection.TypedHashMap_Normal;
import com.github.foxnic.commons.concurrent.SimpleTaskManagerTest;
import com.github.foxnic.commons.concurrent.ThreadUtil_A;
import com.github.foxnic.commons.encrypt.Base64Util;
import com.github.foxnic.commons.io.FileUtilTest;
import com.github.foxnic.commons.io.StreamUtil_A;
import com.github.foxnic.commons.lang.ArrayUtil_Normal;
import com.github.foxnic.commons.lang.ColorTest;
import com.github.foxnic.commons.lang.DataParserTest;
import com.github.foxnic.commons.lang.DateUtilTest;
import com.github.foxnic.commons.lang.EnumTest;
import com.github.foxnic.commons.lang.ObjectUtilTest;
import com.github.foxnic.commons.log.FileLoggerTest;
import com.github.foxnic.commons.network.HttpClientTest;
import com.github.foxnic.commons.xml.XMLTest;

@RunWith(Suite.class)
@SuiteClasses({

		// bean
		BeanUtil_Normal.class, BeanUtil_Advance.class,
		
		// lang
		ArrayUtil_Normal.class, DataParserTest.class, DateUtilTest.class,ColorTest.class,EnumTest.class,ObjectUtilTest.class,
		// collection
		MapUtil_Normal.class, MapUtil_Group.class, TypedHashMap_Normal.class,TreeUtilTest.class,
		// encrypt
		Base64Util.class,
		// file & stream
		FileUtilTest.class, StreamUtil_A.class,
		// log
		FileLoggerTest.class,
		// xml
		XMLTest.class,
		// http
		HttpClientTest.class,
		// concurrent
		SimpleTaskManagerTest.class, ThreadUtil_A.class,
		// cache
		LocalCacheTest.class,DoubleCacheTest.class

})
public class CommonTestSuite {

	@BeforeClass
	public static void begin() {

	}

	@AfterClass
	public static void end() {

	}
}
