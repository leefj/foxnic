package com.github.foxnic.dao.query;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.foxnic.dao.base.TableDataTest;
import com.github.foxnic.dao.data.RcdSet;

/**
 * 分页测试
 */
public class DAO_Page extends TableDataTest {

	@Test
	public void test_page() {
		
		int c=dao.queryInteger("select count(1) from "+normalTable);
		assertTrue(c==0);
		
		//初始化数据
		for (int i = 1; i <= 100; i++) {
			int z=dao.insert(normalTable).set("read_times", i%7==0?7:(56+70*Math.random())).set("price",i%5==0?8.8:(50+Math.random()*20)).set("id", i).execute();
			assertTrue(z==1);
		}
		
		 
		RcdSet p0=dao.query("select * from "+normalTable);
		assertTrue(p0.size()==100);
		//
		RcdSet p1=dao.queryPage("select * from "+normalTable+" order by id asc", 10, 1);
		
		assertTrue(p1.size()==10);
		if(p1.getRcd(0).getInteger("ID")!=1) {
			System.out.println();
		}
		System.out.println(p1.getRcd(0).getInteger("ID"));
		assertTrue(p1.getRcd(0).getInteger("ID")==1);
		assertTrue(p1.getRcd(p1.size()-1).getInteger("ID")==10);
		
		//
		RcdSet p2=dao.queryPage("select * from "+normalTable+" order by id asc", 10, 2);
		assertTrue(p2.size()==10);
		assertTrue(p2.getRcd(0).getInteger("ID")==11);
		assertTrue(p2.getRcd(p1.size()-1).getInteger("ID")==20);
		
		
		//
		RcdSet p3=dao.queryPage("select * from "+normalTable+" order by price", 6, 7);
		assertTrue(p3.size()==6);
		
		RcdSet p4=dao.queryPage("select * from "+normalTable+" where id>? order by id asc", 10, 1,10);
		assertTrue(p4.getRcd(0).getInteger("ID")==11);
		assertTrue(p4.getRcd(p4.size()-1).getInteger("ID")==20);
		assertTrue(p4.size()==10);
	 
		
		
	}

}
