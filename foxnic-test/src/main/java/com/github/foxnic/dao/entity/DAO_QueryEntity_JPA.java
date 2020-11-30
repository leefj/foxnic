package com.github.foxnic.dao.entity;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.github.foxnic.dao.base.TableDataTest;
import com.github.foxnic.dao.base.jpa.NewsJPA;
import com.github.foxnic.dao.data.PagedList;
import com.github.foxnic.sql.expr.ConditionExpr;

/**
 * 针对各种值类型测测试
 */
public class DAO_QueryEntity_JPA extends TableDataTest {

	@Test
	public void test_querySingleEntity() {
		NewsJPA news = new NewsJPA();
		news.setCode("AA");
		news.setTitle("测试标题");
		long id = 10L;

		news.setId(id);

		this.dao.insertEntity(news);

		NewsJPA sample = new NewsJPA();
		sample.setId(id);
		NewsJPA news2 = this.dao.queryEntity(sample);
		
		NewsJPA news3 = this.dao.queryEntity(NewsJPA.class,id);
		
		 
		
		assertTrue(news2!=null);
		assertTrue(news3!=null);
		
		assertTrue(news2.getTitle().equals(news.getTitle()));
		assertTrue(news3.getTitle().equals(news.getTitle()));

	}
	
	@Test
	public void test_QueryEntities() {

		for (int i = 1; i < 88; i++) {
			NewsJPA news = new NewsJPA();
			news.setCode("AA-"+i);
			news.setTitle("测试标题");
			long id = i;

			news.setId(id);

			this.dao.insertEntity(news);
		}
		
		NewsJPA sample = new NewsJPA();
		sample.setTitle("测试标题");
		
		List<NewsJPA> list=this.dao.queryEntities(sample);
		
		assertTrue(list!=null && list.size()==87);
 
		PagedList<NewsJPA> plist=this.dao.queryPagedEntities(sample,10,3);
		assertTrue(plist.getTotalRowCount()==87);
		
		for (NewsJPA n : plist) {
			System.out.println(JSON.toJSONString(n));
		}
		
		assertTrue(plist.size()==10);
		
		
		List<NewsJPA> list2=this.dao.queryEntities(NewsJPA.class,new ConditionExpr("title like ? and id > ?","%测试%",18));
		assertTrue(list2.size()==69);
 
		PagedList<NewsJPA> plist1=this.dao.queryPagedEntities(NewsJPA.class,10,3,"title like ? and id > ?","%测试%",18);
		assertTrue(plist1.getTotalRowCount()==69);
		
		
	}

}
