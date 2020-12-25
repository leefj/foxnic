package com.github.foxnic.commons.bean;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 常规功能测试
 * */
public class BeanUtil_Advance {
	
	
	
	@Test
	public void test_filter() throws Exception {
		List<MyBean> list=createList();
		
		//过滤获得age=为25的bean集合
		List<MyBean> listAge25=BeanUtil.filter(list, "age", 25);
		assertTrue(listAge25.size()==5);
		
		//过滤获得age>25的bean集合
		List<MyBean> listAgeGT25=BeanUtil.filter(list, "age", 25,FilterOperator.GREATER_THAN);
		assertTrue(listAgeGT25.size()>0);
		for (MyBean bean : listAgeGT25) {
			assertTrue(bean.getAge()>25);
		}

		//过滤获得weight>120.5的bean集合
		List<MyBean> listWeightGT120_5=BeanUtil.filter(list, "weight", 120.5,FilterOperator.GREATER_THAN);
		assertTrue(listWeightGT120_5.size()>0);
		for (MyBean bean : listWeightGT120_5) {
			assertTrue(bean.getWeight()>120.5);
		}
		
		//name包含2
		List<MyBean> listNameContains2=BeanUtil.filter(list, "name", "2",FilterOperator.CONTAINS);
		assertTrue(listNameContains2.size()>0);
		for (MyBean bean : listNameContains2) {
			assertTrue(bean.getName().contains("2"));
		}
		
		//weight 包含 5
		List<MyBean> listWeightContains5=BeanUtil.filter(list, "weight", "5",FilterOperator.CONTAINS);
		assertTrue(listWeightContains5.size()>0);
		for (MyBean bean : listWeightContains5) {
			assertTrue((bean.getWeight()+"").contains("5"));
		}

	}
	
	@Test
	public void test_sort() throws Exception {
	 
		List<MyBean> list=createList();
		
		//正着排一下
		List<MyBean> sortedList=BeanUtil.sort(list, "integerSortIndex", true, true);
		
		assertTrue(sortedList.get(0).getIntegerSortIndex()!=null);
		assertTrue(sortedList.get(sortedList.size()-1).getIntegerSortIndex()==null);
		MyBean prev=null;
		for (MyBean bean : sortedList) {
			 if(bean.getIntegerSortIndex()==null) continue;
			 if(prev!=null) {
				 assertTrue(prev.getIntegerSortIndex()<=bean.getIntegerSortIndex());
			 }
			 prev=bean;
		}
		
		//反着排一下
		sortedList=BeanUtil.sort(list, "integerSortIndex", false, false);
		
		assertTrue(sortedList.get(0).getIntegerSortIndex()==null);
		assertTrue(sortedList.get(sortedList.size()-1).getIntegerSortIndex()!=null);
		prev=null;
		for (MyBean bean : sortedList) {
			 if(bean.getIntegerSortIndex()==null) continue;
			 if(prev!=null) {
				 assertTrue(prev.getIntegerSortIndex()>=bean.getIntegerSortIndex());
			 }
			 prev=bean;
		}
	}
	
	@Ignore
	public List<MyBean> createList()
	{
		List<MyBean> list=new ArrayList<MyBean>();
		for (int i = 0; i < 100; i++) {
			MyBean bean=new MyBean();
			bean.setId("R-"+i);
			bean.setName("N-"+i);
			bean.setAge(20+i%20);
			bean.setWeight(100+Math.random()*i);
			bean.setIntegerSortIndex(i+(int)(Math.random()*10));
			if(i%7==0) bean.setIntegerSortIndex(null);
			list.add(bean);
		}
		return list;
	}
	 
}


