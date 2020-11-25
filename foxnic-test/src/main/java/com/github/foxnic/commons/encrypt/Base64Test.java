package com.github.foxnic.commons.encrypt;

import static org.junit.Assert.assertTrue;

import java.util.Base64;

import org.junit.Test;

public class Base64Test {

	@Test
	public void test() {
		// TODO Auto-generated method stub
		String a=Base64Util.encodeToString("111");
		//String a=new String(Base64.getDecoder().decode("111"));
		
		//String b=jodd.util.Base64.decodeToString("111");
		String b=Base64Util.decode(a);
		assertTrue(b.equals("111"));
	}
	
}
