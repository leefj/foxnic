package com.github.foxnic.dao.data;

import com.github.foxnic.dao.base.TableDataTest;

public class ExcelWriterBasic extends TableDataTest {
	
//	/**
//	 * 列名表头，以SQL语句为准
//	 * */
//	@Test
//	public void test_write_normal() throws Exception {
//		//初始化数据
//		for (int i = 1; i <= 100; i++) {
//			int z=dao.insert(normalTable).set("read_times", i%7==0?7:(56+70*Math.random())).set("price",i%5==0?8.8:(50+Math.random()*20)).set("id", i).execute();
//			assertTrue(z==1);
//			TestNewsTity n=new TestNewsTity();
//			n.setId(i).setCode("Z-"+i);
//			n.setNewsid("这是新闻内容-"+i).setPublishDay(new Date()).setTitle("标题"+System.currentTimeMillis());			
//			dao.updateEntity(n, SaveMode.DIRTY_FIELDS);
//		}
//		
//		File xls=EasyFile.createTempFile("AutoHeader_", "_xx.xlsx").file();
//		System.out.println(xls.getAbsolutePath());
//		RcdSet rs=dao.query("select * from "+normalTable);
//		
//		rs.setDataNameFormat(DataNameFormat.POJO_PROPERTY);
//		
//		ExcelWriter writer=new ExcelWriter(Version.V2007);
//		writer.fillSheet(rs, "demo");
//		writer.save(xls);
//		
//		assertTrue(xls.exists());
//		assertTrue(xls.length()>0);
//	}
//	
//	/**
//	 * 尽可能的以数据库中的注释作为表头，前提是单表查询，且表名，列名可识别
//	 * */
//	@Test
//	public void test_write_comment_auto_header() throws Exception {
//		//初始化数据
//		for (int i = 1; i <= 100; i++) {
//			int z=dao.insert(normalTable).set("read_times", i%7==0?7:(56+70*Math.random())).set("price",i%5==0?8.8:(50+Math.random()*20)).set("id", i).execute();
//			assertTrue(z==1);
//			TestNewsTity n=new TestNewsTity();
//			n.setId(i).setCode("Z-"+i);
//			n.setNewsid("这是新闻内容-"+i).setPublishDay(new Date()).setTitle("标题"+System.currentTimeMillis());			
//			dao.updateEntity(n, SaveMode.DIRTY_FIELDS);
//		}
//		
//		File xls=EasyFile.createTempFile("CommentAutoHeasder_", "_xx.xlsx").file();
//		System.out.println(xls.getAbsolutePath());
//		RcdSet rs=dao.query("select * from "+normalTable);
//		
//		rs.setDataNameFormat(DataNameFormat.POJO_PROPERTY);
//		
//		//先从RcdMeta进行转换,使用字段注释的前半部分作为列标题
//		ExcelStructure es=ExcelStructure.parse(rs,true);
// 
//		ExcelWriter writer=new ExcelWriter(Version.V2007);
//		writer.fillSheet(rs, "demo",es);
//		writer.save(xls);
//		
//		assertTrue(xls.exists());
//		assertTrue(xls.length()>0);
//	}
//	
//	
//	/**
//	 * 完全自定义表头
//	 * */
//	@Test
//	public void test_write_custom_header() throws Exception {
//		//初始化数据
//		for (int i = 1; i <= 100; i++) {
//			int z=dao.insert(normalTable).set("read_times", i%7==0?7:(56+70*Math.random())).set("price",i%5==0?8.8:(50+Math.random()*20)).set("id", i).execute();
//			assertTrue(z==1);
//			TestNewsTity n=new TestNewsTity();
//			n.setId(i).setCode("Z-"+i);
//			n.setNewsid("这是新闻内容-"+i).setPublishDay(new Date()).setTitle("标题"+System.currentTimeMillis());			
//			dao.updateEntity(n, SaveMode.DIRTY_FIELDS);
//		}
//		
//		File xls=EasyFile.createTempFile("CustomHeasder_", "_xx.xlsx").file();
//		System.out.println(xls.getAbsolutePath());
//		RcdSet rs=dao.query("select * from "+normalTable);
//		
//		rs.setDataNameFormat(DataNameFormat.POJO_PROPERTY);
//		
//		//先从RcdMeta进行转换
//		ExcelStructure es=ExcelStructure.parse(rs);
//		es.getColumnByField("price").setTitle("价格");
//		es.getColumnByField("code").setTitle("代码").setBackgroundColor("#ff0000").setTextColor("#FFFFFF");
//		
//		//
//		es.setColumnTitleCharIndex("F", "新闻内容").setColumnTitleCharIndex("E", "录入时间");
//		//
//		es.setColumnTitleByField("ID", "编号");
//		
//		
//	 
//		ExcelWriter writer=new ExcelWriter(Version.V2007);
//		writer.fillSheet(rs, "demo",es);
//		writer.save(xls);
//		
//		assertTrue(xls.exists());
//		assertTrue(xls.length()>0);
//	}

}
