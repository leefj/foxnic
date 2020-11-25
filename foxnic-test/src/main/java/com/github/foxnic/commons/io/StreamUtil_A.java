package com.github.foxnic.commons.io;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;

public class StreamUtil_A {

	private FileInputStream getInputStream() throws Exception {
		File file= FileUtil.resolveByClass(this.getClass(), "test.txt");
		FileInputStream is=FileUtil.getInputStream(file);
		return is;
//		return PathTool.getDir(this.getClass()).resolve("test.txt").getInputStream();
	}
	
	private String getText() throws Exception {
		File file= FileUtil.resolveByClass(this.getClass(), "test.txt");
		return FileUtil.readText(file);
		//return PathTool.getDir(this.getClass()).resolve("test.txt").getStringContent();
	}
	
	
	@Test
	public void text_outstream_string() throws Exception {
		FileInputStream in=this.getInputStream();
		String str=StreamUtil.input2string(in, "UTF-8");
		assertTrue(str.equals(getText()));
	}
	
}
