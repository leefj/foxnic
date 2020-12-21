package com.github.foxnic.dao.data;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.foxnic.dao.base.TableDataTest;

/**
 * 数据变更版本以及ID修改测试
 * */
public class RcdSetVersion extends TableDataTest {

	@Test
	public void test_a()
	{
		RcdSet rs0=dao.query("select * from "+normalTable);
		assertTrue(rs0.size()==0);
		//初始化数据
		dao.setPrintSQL(true);
		for (int i = 1; i <= 100; i++) {
			System.out.println(i);
			int z=dao.insert(normalTable).set("read_times", i%7==0?7:(56+70*Math.random())).set("price",i%5==0?8.8:(50+Math.random()*20)).set("id", i).execute();
			assertTrue(z==1);
		}
		
		RcdSet rs=dao.query("select * from "+normalTable);
		
		for (Rcd r : rs) {
			r.set("read_times", 9);
			
			if(r.getInteger("ID")==99)
			{
				r.set("ID", 199);
			}
			r.update(SaveMode.DIRTY_FIELDS);
		}

		int a1=dao.queryInteger("select count(1) from "+normalTable+" where read_times=?",9);
		assertTrue(a1==100);
		
		int a2=dao.queryInteger("select count(1) from "+normalTable+" where id=?",199);
		assertTrue(a2==1);
		
		
		
	}
	
}
