package com.github.foxnic.commons.collection;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.support.pojo.Demo;

public class TypedHashMap_Normal {
	
	@Test
	 public void test_toMap() {
		 Map m=TypedHashMap.asMap(1,"A",2,"B",3);
		 assertTrue("A".equals(m.get(1)));
		 assertTrue("B".equals(m.get(2)));
		 assertTrue(null==(m.get(3)));
		 
		 TypedHashMap m2=TypedHashMap.asMap("A","02","B","03","C",9,"Good","303");
		 assertTrue(m2.getInt("A")==2);
		 assertTrue(m2.getInt("a")==2);
		 
		 assertTrue(m2.getInt("GOOD")==303);
		 assertTrue(m2.getInt("Good")==303);
		 assertTrue(m2.getInt("good")==303);
	}
	
	@Test
	 public void test_int() {
		TypedHashMap m=new TypedHashMap<>();
		m.put("a","[1,5,'6']");
		m.put("b","1,4,6,'8'");
		
		Integer[] a=m.getIntegerArray("a");
		Integer[] b=m.getIntegerArray("b");
		
		assertTrue(a.length==3);
		assertTrue(a[0]==1);
		assertTrue(a[2]==6);
		
		assertTrue(b.length==4);
		assertTrue(b[0]==1);
		assertTrue(b[3]==8);
		System.out.println();
	}
	
	@Test
	 public void test_array() {
		TypedHashMap m=new TypedHashMap<>();
		m.put("a","[1,5,'6']");
		m.put("b","1,4,6,'8'");
		
		JSONArray json1=m.getJSONArray("a");
		assertTrue(json1.size()==3);
		assertTrue(6==json1.getInteger(2));
		
		
		
		String[] arr1=m.getStringArray("b");
		assertTrue(arr1.length==4);
		assertTrue("6".equals(arr1[2]));
		System.out.println();
		
		
		JSONArray json2=m.getJSONArray("b");
		assertTrue(json2.size()==4);
		assertTrue(6==json2.getInteger(2));
		
		
		String[] arr2=m.getStringArray("a");
		assertTrue(arr2.length==3);
		assertTrue("6".equals(arr2[2]));
		System.out.println();
		
	 }
	
	
	@Test
	 public void test_path() {
		 TypedHashMap m=new TypedHashMap<>();
		 
		 Map mx=MapUtil.fillMap(m,"1","A","2","B","3");
		 Map m1=MapUtil.asJSONObject("1","A","2","B","3");
		 
		 m.put("m1", m1);
		 
		 Integer[] aa= {7,5,6,7};
		 
		 m.put("ns", aa);
		 
		 List<Integer> bb=(List)Arrays.asList(aa);
		 m.put("ls", bb);
		 
		 Demo demo=new Demo();
		 demo.setFamilyName("lee");
		 m.put("demo", demo);
		 
		 JSONArray jsonArr=JSONArray.parseArray("[4,8]");
		 m.put("jsonArr", jsonArr);
		 
		 JSONObject jsonObject=JSONObject.parseObject("{a:{ab:12},b:[{x:'999'}]}");
		 m.put("jsonObject", jsonObject);
		 
		 
		 assertTrue("A".equals(m.get("1")));
		 assertTrue("B".equals(m.get("2")));
		 assertTrue(null==(m.get("3")));
		 
		 Object v = m.get("m1.2") ;// MapUtil.getValue(m, "m1.2", String.class);
		 assertTrue("B".equals( v  ));
		 
		 v = m.get("m1.x", String.class);//  MapUtil.getValue(m, "m1.x", String.class);
		 assertTrue(null==v);
		 
		 v = m.get("ns.1", Integer.class);// MapUtil.getValue(m, "ns.1", Integer.class);
		 assertTrue(v.equals(5));
		 
		 v = m.get("ls.1", Integer.class);// MapUtil.getValue(m, "ls.1", Integer.class);
		 assertTrue(v.equals(5));
		 
		 v = m.get("demo.familyName", String.class);// MapUtil.getValue(m, "demo.familyName", String.class);
		 assertTrue(v.equals("lee"));
		 
		 
		 v = m.get("jsonArr.1", Integer.class);// MapUtil.getValue(m, "jsonArr.1", Integer.class);
		 assertTrue(v.equals(8));
		 
		 v = m.get("jsonObject.a.ab", Integer.class);//MapUtil.getValue(m, "jsonObject.a.ab", Integer.class);
		 assertTrue(v.equals(12));
		 
		 v = m.get("jsonObject.b.0.x", Integer.class);// MapUtil.getValue(m, "jsonObject.b.0.x", Integer.class);
		 assertTrue(v.equals(999));
		 
	}

}
