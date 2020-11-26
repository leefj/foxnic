package com.github.foxnic.dao.query;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.foxnic.commons.busi.id.IDGenerator;
import com.github.foxnic.dao.base.TableDataTest;
import com.github.foxnic.sql.parameter.MapParamBuilder;

/**
 * 针对Object类型，查询测试
 * */
public class DAO_QueryObject  extends TableDataTest{
 
	@Test
	public void test_null() {
		//单列情况
		Object value=dao.queryObject("select code from "+normalTable);
		assertTrue(value==null);
		//多列情况
		value=dao.queryObject("select code,'AK' name from "+normalTable);
		assertTrue(value==null);
		
	}
	
	@Test
	public void test_single_row() {
		 
		String code=IDGenerator.getSUID();
		String content="leefj";
		int i=dao.insert(normalTable).set("code", code).set("title", content).set("read_times", 56).setIf("id", 10).execute();
		assertTrue(i==1);
		//单列情况
		Object value=dao.queryObject("select code from "+normalTable+" where code=?",code);
		assertTrue(code.equals(value));
		//多列情况
		value=dao.queryObject("select code,title from "+normalTable+" where code=:code",MapParamBuilder.create("code",code));
		assertTrue(code.equals(value));
		
	}
	
	@Test
	public void test_two_row() {
		 
		String code1=IDGenerator.getSUID();
		String code2=IDGenerator.getSUID();
		String content="leefj";
		int i=dao.insert(normalTable).set("code", code1).set("title", content+"-1").setIf("id", 10).execute();
		assertTrue(i==1);
		i=dao.insert(normalTable).set("code", code2).set("title", content+"-2").set("id",11).execute();
		assertTrue(i==1);
		
		int count=dao.queryInteger("select count(1) from "+normalTable);
		assertTrue(count==2);
		//返回多行时单列情况,
		Object value=dao.queryObject("select id from "+normalTable);
		assertTrue(value!=null);
		//返回多行时多列情况
		value=dao.queryObject("select code,title from "+normalTable+" where code=:code",MapParamBuilder.create("code",code1));
		assertTrue(code1.equals(value));
		
	}

}
