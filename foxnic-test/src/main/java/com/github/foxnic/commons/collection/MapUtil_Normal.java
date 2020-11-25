package com.github.foxnic.commons.collection;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.support.pojo.Demo;

@SuppressWarnings("rawtypes")
public class MapUtil_Normal {
	
	 
	@Test
	 public void test_toMap() {
		 Map m=MapUtil.asMap(1,"A",2,"B",3);
		 assertTrue("A".equals(m.get(1)));
		 assertTrue("B".equals(m.get(2)));
		 assertTrue(null==(m.get(3)));
	}
	
	@Test
	 public void test_fillMap() {
		HashMap<Integer, String> map=new HashMap<Integer, String>();
		map.put(1,"Z");
		
		 Map m=MapUtil.fillMap(map,1,"A",2,"B",3);
		 assertTrue("A".equals(m.get(1)));
		 assertTrue("B".equals(m.get(2)));
		 assertTrue(null==(m.get(3)));
	}
	
	
	@Test
	 public void test_toStringMap() {
		 Map m=MapUtil.asStringKeyMap("1","A","2","B","3");
		 assertTrue("A".equals(m.get("1")));
		 assertTrue("B".equals(m.get("2")));
		 assertTrue(null==(m.get("3")));
	}
	
	@Test
	 public void test_fillStringMap() {
		HashMap<String, Object> map=new HashMap<String, Object>();
		map.put("1","Z");
		 Map m=MapUtil.fillStringKeyMap(map,"1","A","2","B","3");
		 assertTrue("A".equals(m.get("1")));
		 assertTrue("B".equals(m.get("2")));
		 assertTrue(null==(m.get("3")));
	}
	
	
	@Test
	 public void test_fillStringCaseSMap() {
 
		 Map m=MapUtil.asLowerKeyMap("A","1","B","2","c","3");
		 assertTrue("1".equals(m.get("a")));
		 assertTrue("2".equals(m.get("b")));
		 assertTrue(null!=(m.get("c")));
		 
		 Map m2=MapUtil.asUpperKeyMap("A","1","B","2","c","3");
		 assertTrue("1".equals(m2.get("A")));
		 assertTrue("2".equals(m2.get("B")));
		 assertTrue(null!=(m2.get("C")));
	}
	
	
	@Test
	 public void test_toJSON() {
		 Map m=MapUtil.asJSONObject("1","A","2","B","3");
		 assertTrue("A".equals(m.get("1")));
		 assertTrue("B".equals(m.get("2")));
		 assertTrue(null==(m.get("3")));
	}
	
 
	@Test
	 public void test_path() {
		 Map m=MapUtil.asJSONObject("1","A","2","B","3");
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
		 
		 Object v = MapUtil.getValue(m, "m1.2", String.class);
		 assertTrue("B".equals( v  ));
		 
		 v = MapUtil.getValue(m, "m1.x", String.class);
		 assertTrue(null==v);
		 
		 v = MapUtil.getValue(m, "ns.1", Integer.class);
		 assertTrue(v.equals(5));
		 
		 v = MapUtil.getValue(m, "ls.1", Integer.class);
		 assertTrue(v.equals(5));
		 
		 v = MapUtil.getValue(m, "demo.familyName", String.class);
		 assertTrue(v.equals("lee"));
		 
		 v = MapUtil.getValue(m, "jsonArr.1", Integer.class);
		 assertTrue(v.equals(8));
		 
		 v = MapUtil.getValue(m, "jsonObject.a.ab", Integer.class);
		 assertTrue(v.equals(12));
		 
		 v = MapUtil.getValue(m, "jsonObject.b.0.x", Integer.class);
		 assertTrue(v.equals(999));
		 
	}
	
	
}
