package com.github.foxnic.commons.io;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class FileUtilTest {

	@Test
	public void xPathTest()
	{
		File dir=FileUtil.resolveByInvoke();
		boolean in=FileUtil.isInPath(dir,"com","io");
		assertTrue(in);
	}
	
	//TODO 增加更多测试用例
	
}
