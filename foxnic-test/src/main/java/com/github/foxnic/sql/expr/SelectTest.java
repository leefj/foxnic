package com.github.foxnic.sql.expr;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.foxnic.sql.dialect.SQLDialect;

public class SelectTest {

	@Test
	public void test_mysql()
	{
		System.err.println("========= test_mysql ==========================");
		//创建一个Delete语句
		Select sel=new Select();
		//设置表名
		sel.from("sometable","ta")
		//选取字段
		.select("f1")
		//选取多个字段
		.selects("f2","f3","f4")
		//选取带别名的字段
		.select("f5","name")
		//绑定变量
		.select("ifnull(f5,?)", "fx",6)
		.where().and("id=?",8)
		//返回顶层
		.top()
		 //group by 与 having
		.groupBy().by("f2").bys("f3","f4").having().and("count(1)>?",407)
		//返回顶层
		.top()
		//order by
		.orderBy().asc("f1").ascNL("f2");
	 
		sel.setSQLDialect(SQLDialect.MySQL);
		
		System.out.println(sel.getSQL());
		System.out.println(sel.getListParameterSQL());
		System.out.println(sel.getNamedParameterSQL());
		
		if(sel.getSQLDialect()==SQLDialect.MySQL) {
			assertTrue("SELECT f1 , f2 , f3 , f4 , f5 name , ifnull(f5, 6 ) fx FROM sometable ta WHERE id= 8 GROUP BY f2 , f3 , f4 HAVING count(1)> 407 ORDER BY f1 ASC , isnull( f2 ) -1 asc, f2 ASC".equals(sel.getSQL()));
			assertTrue("SELECT f1 , f2 , f3 , f4 , f5 name , ifnull(f5, ? ) fx FROM sometable ta WHERE id= ? GROUP BY f2 , f3 , f4 HAVING count(1)> ? ORDER BY f1 ASC , isnull( f2 ) -1 ASC, f2 ASC".equals(sel.getListParameterSQL()));
			assertTrue("SELECT f1 , f2 , f3 , f4 , f5 name , ifnull(f5, :PARAM_1 ) fx FROM sometable ta WHERE id= :PARAM_2 GROUP BY f2 , f3 , f4 HAVING count(1)> :PARAM_3 ORDER BY f1 ASC , isnull( f2 ) -1 asc, f2 ASC".equals(sel.getNamedParameterSQL()));
		}
		
	}
	
	
	@Test
	public void test_oracle()
	{
		System.err.println("========= test_oracle ==========================");
		//创建一个Delete语句
		Select sel=new Select();
		//设置表名
		sel.from("sometable","ta")
		//选取字段
		.select("f1")
		//选取多个字段
		.selects("f2","f3","f4")
		//选取带别名的字段
		.select("f5","name")
		//绑定变量
		.select("nvl(f5,?)", "fx",6)
		.where().and("id=?",8)
		//返回顶层
		.top()
		 //group by 与 having
		.groupBy().by("f2").bys("f3","f4").having().and("count(1)>?",407)
		//返回顶层
		.top()
		//order by
		.orderBy().asc("f1").ascNL("f2");
	 
		sel.setSQLDialect(SQLDialect.PLSQL);
		
		System.out.println(sel.getSQL());
		System.out.println(sel.getListParameterSQL());
		System.out.println(sel.getNamedParameterSQL());
		
		if(sel.getSQLDialect()==SQLDialect.MySQL) {
			assertTrue("SELECT f1 , f2 , f3 , f4 , f5 name , nvl(f5, 6 ) fx FROM sometable ta WHERE id= 8 GROUP BY f2 , f3 , f4 HAVING count(1)> 407 ORDER BY f1 ASC , f2 ASC NULLS LAST".equals(sel.getSQL()));
			assertTrue("SELECT f1 , f2 , f3 , f4 , f5 name , nvl(f5, ? ) fx FROM sometable ta WHERE id= ? GROUP BY f2 , f3 , f4 HAVING count(1)> ? ORDER BY f1 ASC , f2 ASC NULLS LAST".equals(sel.getListParameterSQL()));
			assertTrue("SELECT f1 , f2 , f3 , f4 , f5 name , nvl(f5, :PARAM_1 ) fx FROM sometable ta WHERE id= :PARAM_2 GROUP BY f2 , f3 , f4 HAVING count(1)> :PARAM_3 ORDER BY f1 ASC , f2 ASC NULLS LAST".equals(sel.getNamedParameterSQL()));
		}
	}
	
//	@Test
//	public void test_select_fields()
//	{
//		SelectFields fields=new SelectFields(this.dao, this.normalTable, "ta");
//		System.out.println(fields.getSQL());
//		Select sel=new Select();
//		//设置表名
//		sel.from(normalTable,"ta");
//		sel.selects(fields);
//		
//		
//		System.out.println(sel.getSQL());
//		System.out.println(sel.getListParameterSQL());
//		System.out.println(sel.getNameParameterSQL());
//		
//		assertTrue(sel.getSQL().toUpperCase().contains("ta.newsId".toUpperCase()));
//		if(dao.getSQLDialect()==SQLDialect.MySQL) {
//			String sql=sel.getSQL();
//			assertTrue(sql.contains("publish_day"));
//			assertTrue(sql.contains("publish_day"));
//			assertTrue(sql.contains("alert_time"));
//			assertTrue(sql.contains("ta.create_by"));
//			assertTrue(sql.contains("ta.create_time"));
//			assertTrue(sql.contains("FROM test_news_tity ta"));
//			//assertTrue("SELECT t.id , t.code , t.title , t.publish_day , t.enter_time , t.newsId , t.alert_time , t.read_times , t.price , t.create_by , t.create_time , t.deleted FROM test_news_tity ta".equals(sel.getSQL()));
//		}
//	}
}
