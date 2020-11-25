package com.github.foxnic.commons.log;

import org.junit.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class FileLoggerTest {

	@Test
	public void test_json() {
		FileLogger lg=new FileLogger("c:\\aaa.txt");
		lg.info("hahah");
		lg.info("a","hahah-1");
		lg.info("b","hahah-2");
	}
	
}
