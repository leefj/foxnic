package com.github.foxnic.dao.entity;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.github.foxnic.dao.base.TableDataTest;
import com.github.foxnic.dao.base.pojo.News;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.sql.expr.ConditionExpr;

/**
 * 针对各种值类型测测试
 */
public class DAO_QueryEntity_Pure extends TableDataTest {

	@Test
	public void test_querySingleEntity() {
		News news = new News();
		news.setCode("AA");
		news.setTitle("测试标题");
		long id = 10L;

		news.setId(id);

		this.dao.insertEntity(news, normalTable);

		News sample = new News();
		sample.setId(id);
		News news2 = this.dao.queryEntity(sample, normalTable);
		
		News news3 = this.dao.queryEntity(News.class, normalTable,id);
		
		 
		
		assertTrue(news2!=null);
		assertTrue(news3!=null);
		
		assertTrue(news2.getTitle().equals(news.getTitle()));
		assertTrue(news3.getTitle().equals(news.getTitle()));

	}
	
	@Test
	public void test_QueryEntities() {

		for (int i = 1; i < 88; i++) {
			News news = new News();
			news.setCode("AA-"+i);
			news.setTitle("测试标题");
			long id = i;

			news.setId(id);

			this.dao.insertEntity(news, normalTable);
		}
		
		News sample = new News();
		sample.setTitle("测试标题");
		
		List<News> list=this.dao.queryEntities(sample,normalTable);
		
		assertTrue(list!=null && list.size()==87);
 
		PagedList<News> plist=this.dao.queryPagedEntities(sample,normalTable,10,3);
		assertTrue(plist.getTotalRowCount()==87);
		
		for (News n : plist) {
			System.out.println(JSON.toJSONString(n));
		}
		
		assertTrue(plist.size()==10);
		
		
		List<News> list2=this.dao.queryEntities(News.class,normalTable,new ConditionExpr("title like ? and id > ?","%测试%",18));
		assertTrue(list2.size()==69);
 
		PagedList<News> plist1=this.dao.queryPagedEntities(News.class,normalTable,10,3,"title like ? and id > ?","%测试%",18);
		assertTrue(plist1.getTotalRowCount()==69);
		
		
	}

}
