package com.github.foxnic.dao.query;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.foxnic.dao.base.TableDataTest;
import com.github.foxnic.dao.data.QueryMetaData;
import com.github.foxnic.dao.data.RcdSet;

/**
 * 查询结果Meta测试
 */
public class DAO_QueryMeta extends TableDataTest {

	@Test
	public void test_meta() {
		//初始化数据
		for (int i = 1; i <= 10; i++) {
			int z=dao.insert(normalTable).set("read_times", i%7==0?7:(56+70*Math.random())).set("price",i%5==0?8.8:(50+Math.random()*20)).set("id", i).execute();
			assertTrue(z==1);
		}
		
		RcdSet rs=dao.query("select * from "+normalTable);
		
		//
		QueryMetaData qm=rs.getMetaData();
		//
		assertTrue(qm.getTableName(0).equalsIgnoreCase(normalTable));
		assertTrue(qm.getColumnCount()>2);
		assertTrue(qm.getDistinctTableNames().length==1);
		
		String tmp=qm.getColumnLabel(0);
		assertTrue(tmp!=null && tmp.length()>0);
		
//		tmp=qm.getCatalogName(0);
//		System.out.println();
	}

}
