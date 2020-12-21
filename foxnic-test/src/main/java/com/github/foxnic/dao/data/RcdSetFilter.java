package com.github.foxnic.dao.data;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.foxnic.commons.bean.FilterOperator;
import com.github.foxnic.dao.base.TableDataTest;

/**
 * 针对各种值类型测测试
 */
public class RcdSetFilter extends TableDataTest {
	@Test
	public void test_changeColumnType() {
		//初始化数据
		for (int i = 1; i <= 10; i++) {
			int z=dao.insert(normalTable).set("read_times", i%7==0?7:(56+70*Math.random())).set("price",i%5==0?8.8:(50+Math.random()*20)).set("id", i).execute();
			assertTrue(z==1);
		}
		
		RcdSet rs=dao.query("select * from "+normalTable);
		rs.changeColumnType("price", Integer.class);
		rs.filter("price", 8);
		
		
	}
	
	@Test
	public void test_sort() {
		//初始化数据
		for (int i = 1; i <= 100; i++) {
			int z=dao.insert(normalTable).set("read_times", i%7==0?7:(56+70*Math.random())).set("price",i%5==0?8.8:(50+Math.random()*20)).set("id", i).execute();
			assertTrue(z==1);
		}
		
		RcdSet rs=dao.query("select * from "+normalTable);
		assertTrue(rs.size()==100);
		
		//过滤值7
		rs.changeColumnType("read_times", Integer.class);
		RcdSet rsReadTimes7=rs.filter("read_times", 7);
		assertTrue(rsReadTimes7.size()>0);
		for (Rcd r : rsReadTimes7) {
			assertTrue(r.getInteger("read_times")==7);
		}
		
		//过滤值7
		rs.changeColumnType("read_times", Integer.class);
		RcdSet rsReadTimes7n=rs.filter("read_times", 7,FilterOperator.EQUALS.reverse());
		assertTrue(rsReadTimes7n.size()>0);
		for (Rcd r : rsReadTimes7n) {
			assertTrue(r.getInteger("read_times")!=7);
		}
		 
		
	}

}
