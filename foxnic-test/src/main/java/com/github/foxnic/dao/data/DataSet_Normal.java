package com.github.foxnic.dao.data;

import com.github.foxnic.dao.base.TableDataTest;

/**
 * 针对各种值类型测测试
 */
public class DataSet_Normal extends TableDataTest {

//	@Test
//	public void test_dataset() {
//		//初始化数据
//		for (int i = 1; i <= 100; i++) {
//			int z=dao.insert(normalTable).set("read_times", i%7==0?null:(56+70*Math.random())).set("price",i%5==0?null:(50+Math.random()*20)).set("id", i).execute();
//			assertTrue(z==1);
//		}
//		
//		
//		DataSet ds=dao.queryDataSet("select * from "+normalTable,false);
//		
//		Double[] price1=ds.getDoubles("price");
//		assertTrue(price1.length==100);
//		assertTrue(price1[0]!=null && price1[0]>0);
//		
//		Integer[] id1=ds.getIntegers("id");
//		
//		assertTrue(id1.length==100);
//		assertTrue(id1[0]!=null && id1[0]>0);
//		
//		
//		ds=dao.queryDataSet("select * from "+normalTable,true);
//		
//		double[] price2=ds.getPrimitiveDoubles("price");
//		assertTrue(price2.length==100);
//		assertTrue(price1[0]>0);
//		
//		int[] id2=ds.getPrimitiveInts("id");
//		
//		assertTrue(id2.length==100);
//		assertTrue( id2[0]>0);
//		
//		 
//	}
	
	 

}
