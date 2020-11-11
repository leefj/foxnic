package com.github.foxnic.commons.io;

import java.io.File;

import org.junit.Test;

public class FileUtilTest {

	@Test
	public void xPathTest()
	{
//		File file=PathTool.getDir(this.getClass()).resolve("pom-ns.xml").file();
		File dir=FileUtil.resolveByInvoke();
		System.out.println();
	}
}
