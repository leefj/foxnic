package com.github.foxnic.sql.expr;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConditionExprAdvanceTest {

	@Test
	public void test_ignor3() {
		
		ConditionExpr e1=new ConditionExpr("f0=?",11);
		
	 
		e1.andEquals("f1", "%","%");
		e1.andEquals("f2", "","");
		
		e1.andNotEquals("f3", null);
		e1.andLike("f4", "%%%%");
		
		e1.ignore("ALL").andLike("f5", "ALL");

		e1.andNotContains("f6", "rt", "rr","rt");
		
		e1.andNotContains("f7", "rt", "rr","rct");
		
		System.out.println("ignor-1 result "+e1.getSQL());
		
 
		assertTrue(!e1.getSQL().contains("f1"));
		assertTrue(!e1.getSQL().contains("f2"));
		assertTrue(!e1.getSQL().contains("f3"));
		assertTrue(!e1.getSQL().contains("f4"));
		assertTrue(!e1.getSQL().contains("f5"));
		assertTrue(!e1.getSQL().contains("f6"));
		assertTrue(e1.getSQL().contains("f7"));
	}

}
