package com.github.foxnic.commons.lang;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringUtilTest {

	@Test
	public void getPartTest1()
	{
		String content="sys_user_role";
		
		String role=StringUtil.getLastPart(content, "_");
		assertTrue("role".equals(role));
		
		String sys=StringUtil.getFirstPart(content, "_");
		assertTrue("sys".equals(sys));
		
		String user=StringUtil.getPart(content, "_",1);
		assertTrue("user".equals(user));
		
		user=StringUtil.getLastPart(content, "_",1);
		assertTrue("user".equals(user));
	}
	
	
	@Test
	public void getPartTest2()
	{
		String content="com.github.foxnic.commons.lang";
		
		String lang=StringUtil.getLastPart(content, ".");
		assertTrue("lang".equals(lang));
		
		String com=StringUtil.getFirstPart(content, ".");
		assertTrue("com".equals(com));
		
		String foxnic=StringUtil.getPart(content, ".",2);
		assertTrue("foxnic".equals(foxnic));
		
		foxnic=StringUtil.getLastPart(content, ".",2);
		assertTrue("foxnic".equals(foxnic));
	}
	
}
