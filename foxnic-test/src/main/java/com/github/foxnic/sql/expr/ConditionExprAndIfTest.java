package com.github.foxnic.sql.expr;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import com.github.foxnic.commons.lang.StringUtil;

public class ConditionExprAndIfTest {
	
	@Test
	public void ce_normal() {
		
		ConditionExpr ce=new ConditionExpr();
		ce.andEquals("a", 9);
		ce.startWithWhere();
		
		Where wh=new Where();
		wh.andEquals("a", 9);
		
		System.out.println(ce.getSQL());
		System.out.println(wh.getSQL());
		
		assertTrue(wh.getSQL().contains(ce.getSQL()));
 
	}
	
	@Test
	public void ce_2() {
		Where wh1=new Where();
		wh1.andIf("s like ?",null);
		System.out.println(wh1.getSQL());
		
		Where wh2=new Where();
		wh2.and("s like ?",null);
		System.out.println(wh2.getSQL());
		
		Where wh3=new Where();
		wh3.andIf("s like ?","%"+null);
		System.out.println(wh3.getSQL());
	}
	
	
	@Test
	public void ce_3() {
		Where wh1=new Where();
		Date d=null;
		wh1.andIf("s >= ?",d);
		System.out.println(wh1.getSQL());
		assertTrue(StringUtil.isBlank(wh1.getSQL()));
		
		 
	}
	
	
	@Test
	public void ce_4() {
		
		ConditionExpr ce=new ConditionExpr("f1=?","AUX");
		
		ce.ignore("YG").andIf("f2 = ?", "YG");
		
		In in =new In("f3", 1,6,8);
		ce.ignore(1,6,8).andIf(in);
		
		In in2 =new In("f4", 1,6,9);
		ce.ignore(1,6,8).andIf(in2);
		
		In in3 =new In("f5", 2,6,9);
		ce.andIf(in3,9,2,6);
		
		ce.ignore(9,"Z").andIf(new Expr("f6=? and f7=?",9,"Z"));
		
		ce.ignore(9,"A").andIf(new Expr("f8=? and f9=?",9,"Z"));
		
		ce.andIf(new Expr("fm1=? and fm2=?",9,"Z"),9,"Z");
		
		ce.andIf(new Expr("fm3=? and fm4=?",9,"Z"),9,"X");
 
		
		System.out.println(ce.getSQL());
		
		assertTrue(!ce.getSQL().contains("f2"));
		assertTrue(!ce.getSQL().contains("f3"));
		assertTrue(ce.getSQL().contains("f4"));
		assertTrue(!ce.getSQL().contains("f5"));
		assertTrue(!ce.getSQL().contains("f6"));
		assertTrue(!ce.getSQL().contains("f7"));
		
		assertTrue(ce.getSQL().contains("f8"));
		assertTrue(ce.getSQL().contains("f9"));
		
		assertTrue(!ce.getSQL().contains("fm1"));
		assertTrue(!ce.getSQL().contains("fm2"));
		
		assertTrue(ce.getSQL().contains("fm3"));
		assertTrue(ce.getSQL().contains("fm4"));
		
		//10次的性能测试
		long t0=System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			ce.getSQL();
			ce.getListParameterSQL();
			ce.getNamedParameterSQL();
			ce.getNamedParameters();
			ce.getListParameters();
		}
		System.out.println(System.currentTimeMillis()-t0);

	}

}
