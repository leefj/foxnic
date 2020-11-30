package com.github.foxnic.dao.entity;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.foxnic.dao.base.TableDataTest;
import com.github.foxnic.dao.base.pojo.News;
import com.github.foxnic.dao.base.pojo.Relation;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.spring.MySqlDAO;

 /**
  * 针对各种值类型测测试
  * */
public class DAO_Entity_Pure  extends TableDataTest{
 
	@Test
	public void test_insert_update() {
		 News news=new News();
		 news.setCode("AA");
		 news.setTitle("测试标题");
		 if(!dao.getTableColumnMeta(normalTable, "id").isAutoIncrease()) {
			 news.setId(10L);
		 }
		 
		 //插入测试
		 boolean suc=dao.insertEntity(news, normalTable);
		 assertTrue(suc);
		 
		 if(dao.getTableColumnMeta(normalTable, "id").isAutoIncrease()) {
			 assertTrue(news.getId()!=null);
			 assertTrue(news.getId()>0);
		 }
		 
		 RcdSet rs=dao.query("select * from "+normalTable+" where code=?","AA");
		 assertTrue(rs.size()==1);
		 assertTrue("测试标题".equals(rs.getRcd(0).getString("title")));
		 
		 //转换为实体
		 News news2=rs.getRcd(0).toEntity(News.class);
		 assertTrue("测试标题".equals(news2.getTitle()));
		 assertTrue("AA".equals(news2.getCode()));
		 
		 //更新
		 news2.setNewsId("today");
		 suc=dao.updateEntity(news2, normalTable, false);
		 assertTrue(suc);
		 rs=dao.query("select * from "+normalTable+" where code=?","AA");
		 assertTrue(rs.size()==1);
		 assertTrue("测试标题".equals(rs.getRcd(0).getString("title")));
		 assertTrue("today".equals(rs.getRcd(0).getString("newsId")));
 
	}
	
	@Test
	public void test_save() {
		 News news=new News();
		 news.setCode("AA");
		 news.setTitle("测试标题");
		 if(!dao.getTableColumnMeta(normalTable, "id").isAutoIncrease()) {
			 news.setId(10L);
		 }
		 //插入测试
		 boolean suc=dao.saveEntity(news, normalTable,false);
		 assertTrue(suc);
		 assertTrue(news.getId()!=null);
		 
		 news.setTitle("T2");
		 news.setNewsId("NS");
		 news.setCreateBy("leefj");
		 suc=dao.saveEntity(news, normalTable, false);
		 assertTrue(suc);
		 
		 RcdSet rs=dao.query("select * from "+normalTable+" where code=?","AA");
		 assertTrue(rs.size()==1);
		 News newsdb=rs.getRcd(0).toEntity(News.class);
		 assertTrue("T2".equals(newsdb.getTitle()));
		 assertTrue("NS".equals(newsdb.getNewsId()));
		 assertTrue("leefj".equals(newsdb.getCreateBy()));
		 
		 
	}
	
	
	@Test
	public void test_delete_by_pk() {
		 News news=new News();
		 news.setCode("AA");
		 news.setTitle("测试标题");
		 if(!dao.getTableColumnMeta(normalTable, "id").isAutoIncrease()) {
			 news.setId(10L);
		 }
		 //插入测试
		 boolean suc=dao.insertEntity(news, normalTable);
		 assertTrue(suc);
		 if(dao instanceof MySqlDAO)
		 {
			 assertTrue(news.getId()!=null);
		 }
		 
		 //删除
		 suc=dao.deleteEntity(news, normalTable);
		 assertTrue(suc);
		 
		 RcdSet rs=dao.query("select * from "+normalTable+" where code=?","AA");
		 assertTrue(rs.size()==0);
	}
	
	
	@Test
	public void test_delete_by_sample() {
		 News news=new News();
		 news.setCode("AA");
		 news.setTitle("测试标题");
		 if(!dao.getTableColumnMeta(normalTable, "id").isAutoIncrease()) {
			 news.setId(10L);
		 }
		 //插入测试
		 boolean suc=dao.insertEntity(news, normalTable);
		 assertTrue(suc);
		 
		 //删除
		 News sample=new News();
		 sample.setCode("AA");
		 
		 int i=dao.deleteEntities(sample, normalTable);
		 assertTrue(i==1);
		 
		 RcdSet rs=dao.query("select * from "+normalTable+" where code=?","AA");
		 assertTrue(rs.size()==0);
	}
	
	
	
	@Test
	public void test_pk_table() {
		
		 Relation rel=new Relation();
		 rel.setBillId(9);
		 rel.setOwnerId(1);
		 rel.setType("shop");
		  
		 //插入测试
		 boolean suc=dao.insertEntity(rel, pkTable);
		 assertTrue(suc);
		 
		 //删除
		 suc=dao.deleteEntity(rel, pkTable);
		 assertTrue(suc);
		 
		 RcdSet rs=dao.query("select * from "+pkTable);
		 assertTrue(rs.size()==0);
		 
		
	}
 
}
