package com.github.foxnic.commons.bean;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;

/**
 * 常规功能测试
 * */
public class BeanUtil_Normal {
	
	@Test
	public void test_duplicateProp0() throws Exception {
		 
		MyBean bean=new MyBean();
		bean.setName("A");
		bean.setNicName("N");
//		String name=(String)BeanUtil.getFieldValue(bean, "name");
		String nicName=(String)BeanUtil.getFieldValue(bean, "nic_name");
		System.out.println();
	}
	
//	@Test
	public void test_duplicateProp() throws Exception {
		Map<String, Object> map=new HashMap<>();
		map.put("id","9090");
		map.put("name","haha name");
		map.put("mic_name","haha nic");
		map.put("birthday","2019-09-08");
		MyBean bean=BeanUtil.toJavaObject(map, MyBean.class);
		assertTrue("9090".equals(bean.getId()));
		assertTrue("haha name".equals(bean.getName()));
		assertTrue("haha nic".equals(bean.getNicName()));
		DateTime d=new DateTime(bean.getBirthday());
		assertTrue(d.getYear()==2019);
		System.out.println();
	}
	
	
	@Test
	public void test_toJavaObject() throws Exception {
		Map<String, Object> map=new HashMap<>();
		map.put("id","9090");
		map.put("name","haha");
		map.put("birthday","2019-09-08");
		MyBean bean=BeanUtil.toJavaObject(map, MyBean.class);
		assertTrue("9090".equals(bean.getId()));
		assertTrue("haha".equals(bean.getName()));
		DateTime d=new DateTime(bean.getBirthday());
		assertTrue(d.getYear()==2019);
	}
	
	@Test
	public void test_getValueArray() throws Exception {
		List<MyBean> beans=new ArrayList<MyBean>();
		for (int i = 0; i < 100; i++) {
			MyBean bean=new MyBean();
			beans.add(bean);
			bean.setAge(i);
		}
		
		Integer[] arr=BeanUtil.getFieldValueArray(beans, "age", Integer.class);
		for (int i = 0; i < 100; i++) {
			System.out.println(arr[i]+","+i);
			assertTrue(arr[i]==i);
		}
	}
	
	@Test
	public void test_getValueList() throws Exception {
		List<MyBean> beans=new ArrayList<MyBean>();
		for (int i = 0; i < 100; i++) {
			MyBean bean=new MyBean();
			beans.add(bean);
			bean.setAge(i);
		}
		
		List<Integer> arr=BeanUtil.getFieldValueList(beans, "age", Integer.class);
		for (int i = 0; i < 100; i++) {
			System.out.println(arr.get(i)+","+i);
			assertTrue(arr.get(i)==i);
		}
	}
	
	@Test
	public void test_toMap() throws Exception {
		
		List<MyBean> list=new ArrayList<MyBean>();
		for (int i = 0; i < 100; i++) {
			MyBean bean=new MyBean();
			list.add(bean);
			bean.setId("ic-"+i);
			bean.setAge(i%3);
		}
		
		
		Map<Integer,MyBean> map= BeanUtil.toMap(list, "age");
		assertTrue(map.get(0).getAge()==0);
		assertTrue(map.get(1).getAge()==1);
		assertTrue(map.get(2).getAge()==2);
		
		MyBean bean=new MyBean();
		bean.setId("kk");
		bean.setAge(99);
		bean.setzId(4);
		
		Map<String,Object> mmp=BeanUtil.toMap(bean);
		
		assertTrue("kk".equals(mmp.get("id")));
		assertTrue(mmp.get("age").equals(99));
		assertTrue(mmp.get("zId").equals(4L));
 
	}
	
	
	@Test
	public void test_toMap2() throws Exception {
		
		List<MyBean> list=new ArrayList<MyBean>();
		for (int i = 0; i < 100; i++) {
			MyBean bean=new MyBean();
			list.add(bean);
			bean.setId("ic-"+i);
			bean.setAge(i%3);
		}
		
		
		Map<String,Integer> map= BeanUtil.toMap(list, "id",String.class,"age",Integer.class);
		assertTrue(map.size()==list.size());
		System.out.println();
 
	}
	
	
	@Test
	public void test_toMapMF() throws Exception {
		
		List<MyBean> list=new ArrayList<MyBean>();
		for (int i = 0; i < 100; i++) {
			MyBean bean=new MyBean();
			list.add(bean);
			bean.setId("ic-"+i);
			bean.setAge(i%3);
			bean.setName("N-"+(i%2));
		}
		
		Map<String,MyBean> map= BeanUtil.toMap(list, "age","name");
		assertTrue(map.size()==6);
	}
	
	
	@Test
	public void test_groupAsMap() throws Exception {
		
		List<MyBean> list=new ArrayList<MyBean>();
		for (int i = 0; i < 100; i++) {
			MyBean bean=new MyBean();
			list.add(bean);
			bean.setId("ic-"+i);
			bean.setAge(i%3);
			bean.setName("N-"+(i%6));
		}

		Map<Integer,List<MyBean>> map= BeanUtil.groupAsMap(list, "age");
		assertTrue(map.get(0).size()==34);
		assertTrue(map.get(1).size()==33);
		assertTrue(map.get(2).size()==33);
		
	}
	
	
	@Test
	public void test_groupAsMapMF() throws Exception {
		
		List<MyBean> list=new ArrayList<MyBean>();
		for (int i = 0; i < 100; i++) {
			MyBean bean=new MyBean();
			list.add(bean);
			bean.setId("ic-"+i);
			bean.setAge(i%3);
			bean.setName("N-"+(i%2));
		}

		Map<String,List<MyBean>> map= BeanUtil.groupAsMap(list, "age","name");
		System.out.println(map);
		assertTrue(map.size()==6);
		for (String key : map.keySet()) {
			assertTrue(map.get(key).size()>10);
		}
		
	}
	
	@Test
	public void test_clear() throws Exception {
		//TODO 待实现
		MyBean bean=new MyBean();
		bean.setId("x9011");
		bean.setzId(89);
		bean.setAge(7);
		bean.setCreateTime(new Date());
		BeanUtil.clearValues(bean);
		
		assertTrue(bean.getId()==null);
		assertTrue(bean.getCreateTime()==null);
		assertTrue(bean.getzId()==0);
		assertTrue(bean.getAge()==0);
	}
	
	@Test
	public void test_value() throws Exception {
		//TODO 待实现
		MyBean bean=new MyBean();
		bean.setId("9011");
		bean.setzId(89);
		bean.setAge(7);
		bean.setCreateTime(new Date());
		Exception e=new Exception();
		bean.setData(e);
		Integer r=BeanUtil.getFieldValue(bean, "id",Integer.class);
		assertTrue(r==9011);
		Exception t1=BeanUtil.getFieldValue(bean, "data",Exception.class);
		Throwable t2=BeanUtil.getFieldValue(bean, "data",Throwable.class);
		assertTrue(t1==e);
		assertTrue(t2==e);
		
		Object A1=BeanUtil.getStaticFieldValue(MyBean.class, "STATIC_VALUE_1");
		Object A2=BeanUtil.getStaticFieldValue(MyBean.class, "STATIC_VALUE_2");
		assertTrue("STATIC_VALUE_1".equals(A1));
		assertTrue("STATIC_VALUE_2".equals(A2));
		
	}
}


