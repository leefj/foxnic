package com.github.foxnic.commons.lang;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ObjectUtilTest {

	@Test
	public void test_parse()
	{
		assertTrue(!ObjectUtil.equals(4, 5));
		assertTrue(!ObjectUtil.equals(1, null));
		assertTrue(!ObjectUtil.equals(null, 5));
		assertTrue(ObjectUtil.equals(5, 5));
		assertTrue(ObjectUtil.equals(null, null));
	}
	
}
