package com.github.foxnic.dao.data;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.foxnic.dao.base.TableDataTest;

/**
 * 针对各种值类型测测试
 */
public class RcdSetSort extends TableDataTest {

	@Test
	public void test_sort() {
		//初始化数据
		for (int i = 1; i <= 100; i++) {
			int z=dao.insert(normalTable).set("read_times", i%7==0?null:(56+70*Math.random())).set("price",i%5==0?null:(50+Math.random()*20)).set("id", i).execute();
			assertTrue(z==1);
		}
		
		RcdSet rs=dao.query("select * from "+normalTable);
		assertTrue(rs.size()==100);
	
		//基本排序测试
		rs.sort("id", false, true);
		Rcd p=null;
		for (Rcd r : rs) {
			if(p!=null) {
				assertTrue(p.getInteger("id")>r.getInteger("id"));
			}
			p=r;
		}
		
		
		//有null值的排序
		rs.sort("price", false, true);
		p=null;
		assertTrue(rs.getRcd(0).getDouble("price")!=null);
		assertTrue(rs.getRcd(rs.size()-1).getDouble("price")==null);
		for (Rcd r : rs) {
			System.out.println(r.getDouble("price"));
			if(r.getDouble("price")==null) continue;
			if(p!=null) {
				assertTrue(p.getInteger("price")>=r.getInteger("price"));
			}
			p=r;
		}
		
		//有null值的排序
		rs.sort("price", true, false);
		p=null;
		assertTrue(rs.getRcd(0).getDouble("price")==null);
		assertTrue(rs.getRcd(rs.size()-1).getDouble("price")!=null);
		for (Rcd r : rs) {
			System.out.println(r.getDouble("price"));
			if(r.getDouble("price")==null) continue;
			if(p!=null) {
				assertTrue(p.getInteger("price")<=r.getInteger("price"));
			}
			p=r;
		}
		
	}
	
	@Test
	public void test_sort_2() {
		//初始化数据
		for (int i = 1; i <= 100; i++) {
			int z=dao.insert(normalTable).set("read_times", i%7==0?null:(56+70*Math.random())).set("price",i%5==0?null:(50+Math.random()*20)).set("id", i).execute();
			assertTrue(z==1);
		}
		
		RcdSet rs=dao.query("select * from "+normalTable);
		assertTrue(rs.size()==100);
		Rcd p=null;
		
		//有null值的排序
		rs.sort("price", true, false);
		p=null;
		assertTrue(rs.getRcd(0).getDouble("price")==null);
		assertTrue(rs.getRcd(rs.size()-1).getDouble("price")!=null);
		for (Rcd r : rs) {
			System.out.println(r.getDouble("price"));
			if(r.getDouble("price")==null) continue;
			if(p!=null) {
				assertTrue(p.getInteger("price")<=r.getInteger("price"));
			}
			p=r;
		}
	}

}
