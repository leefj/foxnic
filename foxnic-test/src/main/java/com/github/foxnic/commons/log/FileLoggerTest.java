package com.github.foxnic.commons.log;

import org.junit.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.io.FileUtil;

public class FileLoggerTest {

	@Test
	public void test_json() {
		FileLogger lg=new FileLogger(FileUtil.createTempFile("log", "1122976"));
		lg.info("hahah");
		lg.info("a","hahah-1");
		lg.info("b","hahah-2");
	}
	
}
