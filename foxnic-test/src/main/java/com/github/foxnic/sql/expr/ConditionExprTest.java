package com.github.foxnic.sql.expr;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class ConditionExprTest {

	@Test
	public void ce_normal() {
		
		
		
		//构建CE，CE 继承自 ConditionExpression
		ConditionExpr ce=new ConditionExpr("a=?",12);
		
//		ce.startWithSpace();
//		System.out.println("|"+ce.getSQL()+"|");
//		if(System.nanoTime()>0) return;
//		assertTrue(" a= 12".equals(ce.getSQL()));
		
		//输出(默认以and开头) ： AND  a= 12 
		System.out.println(ce);
		assertTrue("AND a= 12".equals(ce.getSQL()));
		assertTrue("AND a= ?".equals(ce.getListParameterSQL()));
		assertTrue("AND a= :PARAM_1".equals(ce.getNamedParameterSQL()));
		//输出以where开头的条件表达式
		ce.startWithWhere();
		System.out.println(ce);
		assertTrue("WHERE a= 12".equals(ce.getSQL()));
		assertTrue("WHERE a= ?".equals(ce.getListParameterSQL()));
		assertTrue("WHERE a= :PARAM_1".equals(ce.getNamedParameterSQL()));
		
		//输出以空格开头的条件表达式
		ce.startWithSpace();
		System.out.println(ce);
		assertTrue(" a= 12".equals(ce.getSQL()));
		assertTrue(" a= ?".equals(ce.getListParameterSQL()));
		assertTrue(" a= :PARAM_1".equals(ce.getNamedParameterSQL()));
		
		
		//增加条件
		ce.and("b=? ", 90);
		//输出   a= 12  AND b= 90 
		System.out.println(ce);
		assertTrue(" a= 12 AND b= 90".equals(ce.getSQL()));
		assertTrue(" a= ? AND b= ?".equals(ce.getListParameterSQL()));
		assertTrue(" a= :PARAM_1 AND b= :PARAM_2".equals(ce.getNamedParameterSQL()));
		
		//andIf内部判断是否为空，如果空则不连入
		String  c = null;
		ce.andIf("c=?",c);
		
		assertTrue(" a= 12 AND b= 90".equals(ce.getSQL()));
		assertTrue(" a= ? AND b= ?".equals(ce.getListParameterSQL()));
		assertTrue(" a= :PARAM_1 AND b= :PARAM_2".equals(ce.getNamedParameterSQL()));
 
	}
	
	@Test
	public void test_space() {
		
		Expr se1=new Expr("SELECT * FROM( select code from test_news_tity ) PAGED_QUERY LIMIT ?,? ",9,9);
		System.out.println(se1.getSQL());
		System.out.println(se1.getNamedParameterSQL());
		Expr se=new Expr("menu_id=? and ROLE_ID=?", 9, 88);
		//
		System.out.println(se.getSQL());
		System.out.println(se.getListParameterSQL());
		System.out.println(se.getNamedParameterSQL());
		assertTrue(se.getSQL().indexOf("  ")==-1);
		//
		ConditionExpr ce = new ConditionExpr("a=?", 1);
		ce.and("b=?", 90);
		System.out.println(ce.getSQL());
		System.out.println(ce.getListParameterSQL());
		System.out.println(ce.getNamedParameterSQL());
		assertTrue(ce.getSQL().indexOf("  ")==-1);
		//
		Delete delete = new Delete("sys_role_menu");
		delete.where().and("menu_id=? and ROLE_ID=?", 9, 88).and("K=?",1);
		System.out.println(delete.getSQL());
		System.out.println(delete.getListParameterSQL());
		System.out.println(delete.getNamedParameterSQL());
		assertTrue(delete.getSQL().indexOf("  ")==-1);
		//
		Update update = new Update("sys_role_menu");
		update.set("a", 0).set("b", "zx");
		update.where().and("menu_id=?", 9).and("ROLE_ID=?",10);
		System.out.println(update.getSQL());
		System.out.println(update.getListParameterSQL());
		System.out.println(update.getNamedParameterSQL());
		assertTrue(update.getSQL().indexOf("  ")==-1);
	}
	
	@Test
	public void test_in() {
		
		System.err.println("=================== TEST IN ============================");
		
		// 构建IN语句
		List<String> items = new ArrayList<>();

		items.add("001");
		items.add("A");
		items.add("B");
//		items.add("112");

		In in = new In("x", items);
		
		in.addItem("112");
 

		System.out.println(in.getSQL());
		System.out.println(in.getListParameterSQL());
		System.out.println(in.getNamedParameterSQL());
		
		assertTrue("x IN ( '001' , 'A' , 'B' , '112' )".equals(in.getSQL()));
		assertTrue("x IN ( :PARAM_1 , :PARAM_2 , :PARAM_3 , :PARAM_4 )".equals(in.getNamedParameterSQL()));
		assertTrue("x IN ( ? , ? , ? , ? )".equals(in.getListParameterSQL()));
		 

 
		// 构建Where，Where 继承自 ConditionExpression
		Where wh = new Where("s=?", 90);
		wh.or("name like ?","%leefj%");
		wh.and(in);
		
		System.out.println(wh.getSQL());
		System.out.println(wh.getListParameterSQL());
		System.out.println(wh.getNamedParameterSQL());
		
		System.out.println(JSON.toJSONString(wh.getListParameters()));
		System.out.println(JSON.toJSONString(wh.getNamedParameters()));
		
		assertTrue("WHERE s= 90 OR name like '%leefj%' AND x IN ( '001' , 'A' , 'B' , '112' )".equals(wh.getSQL()));
		assertTrue("WHERE s= ? OR name like ? AND x IN ( ? , ? , ? , ? )".equals(wh.getListParameterSQL()));
		System.out.println(wh.getNamedParameterSQL());
		assertTrue("WHERE s= :PARAM_1 OR name like :PARAM_2 AND x IN ( :PARAM_3 , :PARAM_4 , :PARAM_5 , :PARAM_6 )".equals(wh.getNamedParameterSQL()));
 
		assertTrue("[90,\"%leefj%\",\"001\",\"A\",\"B\",\"112\"]".equals(JSON.toJSONString(wh.getListParameters())));
		assertTrue("{\"PARAM_1\":90,\"PARAM_6\":\"112\",\"PARAM_5\":\"B\",\"PARAM_4\":\"A\",\"PARAM_3\":\"001\",\"PARAM_2\":\"%leefj%\"}".equals(JSON.toJSONString(wh.getNamedParameters())));
	}
	
	
	
	@Test
	public void test_in_2() {
		
		System.err.println("=================== TEST IN 2============================");
		
		// 构建IN语句
		List<Object> items = new ArrayList<>();

		items.add(new Object[] {"A",1});
		items.add(new Object[] {"B",2});
//		items.add(new Object[] {"C",3});
//		items.add(new Object[] {"D",4});

		In in = new In(new String[] {"f1","f2"}, items);

		in.addItem("C",3);
		in.addItem(new Object[] {"D",4});
		
		System.out.println(in.getSQL());
		System.out.println(in.getListParameterSQL());
		System.out.println(in.getNamedParameterSQL());
		
		assertTrue("( f1 , f2 ) IN ( ( 'A' , 1 ) , ( 'B' , 2 ) , ( 'C' , 3 ) , ( 'D' , 4 ) )".equals(in.getSQL()));
		assertTrue("( f1 , f2 ) IN ( ( :PARAM_1 , :PARAM_2 ) , ( :PARAM_3 , :PARAM_4 ) , ( :PARAM_5 , :PARAM_6 ) , ( :PARAM_7 , :PARAM_8 ) )".equals(in.getNamedParameterSQL()));
		assertTrue("( f1 , f2 ) IN ( ( ? , ? ) , ( ? , ? ) , ( ? , ? ) , ( ? , ? ) )".equals(in.getListParameterSQL()));
		 

 
		// 构建Where，Where 继承自 ConditionExpression
		Where wh = new Where("s=?", 90);
		wh.or("name like ?","%leefj%");
		wh.and(in);
		
		System.out.println(wh.getSQL());
		System.out.println(wh.getListParameterSQL());
		System.out.println(wh.getNamedParameterSQL());
		
		System.out.println(JSON.toJSONString(wh.getListParameters()));
		System.out.println(JSON.toJSONString(wh.getNamedParameters()));
		
		assertTrue("WHERE s= 90 OR name like '%leefj%' AND ( f1 , f2 ) IN ( ( 'A' , 1 ) , ( 'B' , 2 ) , ( 'C' , 3 ) , ( 'D' , 4 ) )".equals(wh.getSQL()));
		assertTrue("WHERE s= ? OR name like ? AND ( f1 , f2 ) IN ( ( ? , ? ) , ( ? , ? ) , ( ? , ? ) , ( ? , ? ) )".equals(wh.getListParameterSQL()));
		assertTrue("WHERE s= :PARAM_1 OR name like :PARAM_2 AND ( f1 , f2 ) IN ( ( :PARAM_3 , :PARAM_4 ) , ( :PARAM_5 , :PARAM_6 ) , ( :PARAM_7 , :PARAM_8 ) , ( :PARAM_9 , :PARAM_10 ) )".equals(wh.getNamedParameterSQL()));
 
		assertTrue("[90,\"%leefj%\",\"A\",1,\"B\",2,\"C\",3,\"D\",4]".equals(JSON.toJSONString(wh.getListParameters())));
		assertTrue("{\"PARAM_1\":90,\"PARAM_9\":\"D\",\"PARAM_8\":3,\"PARAM_10\":4,\"PARAM_7\":\"C\",\"PARAM_6\":2,\"PARAM_5\":\"B\",\"PARAM_4\":1,\"PARAM_3\":\"A\",\"PARAM_2\":\"%leefj%\"}".equals(JSON.toJSONString(wh.getNamedParameters())));
	}
	
//	@Test
//	public void test_ignor1() {
//		
//		CE e1=new CE("f0=?",11);
//		
//		e1.ignore(Ignore.BLANK);
//		e1.andIf("f1=?", "");
//		e1.andIf("f2=?", " ");
//		
//		e1.ignore(Ignore.NULL_STRING);
//		e1.andIf("f3=?", "null");
//		
//		System.out.println("ignor-1 result "+e1.getSQL());
//		
//		
//		assertTrue(!e1.getSQL().contains("f1"));
//		assertTrue(!e1.getSQL().contains("f2"));
// 
//	}
	
//	@Test
//	public void test_ignor2() {
//		
//		CE e1=new CE("f0=?",9);
//		 
//		
//		e1.andIf("f1=?", "");
//		e1.andIf("f2=?", " ");
//		
//		System.out.println("ignor-2 result "+e1.getSQL());
//		
//		assertTrue(e1.getSQL().contains("f1"));
//		assertTrue(e1.getSQL().contains("f2"));
// 
//	}
	
	
	
	
//	@Test
//	public void test_ignor4() {
//		
//		CE e1=new CE("f0=?",11);
//		
//		e1.ignore(Ignore.NP);
//		e1.andIf("f1=?", "null%");
//		e1.andIf("f2=?", "null%%%%");
//		
//		e1.ignore(Ignore.NULL_PERCENT);
//		e1.andIf("f3=?", "null%");
//		e1.andIf("f4=?", "null%%%%");
//		
//		e1.ignore(Ignore.NULL_PERCENT,Ignore.NP);
//		e1.andIf("f5=?", "null%");
//		e1.andIf("f6=?", "null%%%%");
//		
//		System.out.println("ignor-1 result "+e1.getSQL());
//		
//		
//		assertTrue(!e1.getSQL().contains("f1"));
//		assertTrue(!e1.getSQL().contains("f2"));
//		assertTrue(!e1.getSQL().contains("f3"));
//		assertTrue(!e1.getSQL().contains("f4"));
//		assertTrue(!e1.getSQL().contains("f5"));
//		assertTrue(!e1.getSQL().contains("f6"));
// 
//	}
//	
//	
//	@Test
//	public void test_ignor5() {
//		
//	CE ce=new CE("sys_id=?","DMS");
//	ce.ignore(Ignore.BLANK,Ignore.NP,Ignore.P);
//	ce.andIf("process_id=?","");
//	ce.andIf("template_form_id like ?","%"+null+"%");
//	
//	System.out.println(ce.getSQL());
//	
//	assertTrue(!ce.getSQL().contains("process_id"));
//	assertTrue(!ce.getSQL().contains("template_form_id"));
//	}
	

}
