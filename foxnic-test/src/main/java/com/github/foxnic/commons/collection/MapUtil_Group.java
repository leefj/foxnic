package com.github.foxnic.commons.collection;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.github.foxnic.commons.bean.MyBean;

public class MapUtil_Group {
	
	//TODO  测试两个Group ，两个 tomap
	
	@Test
	public void test_group()
	{
		List<Map> maps=new ArrayList<Map>();
		maps.add(MapUtil.asMap("id",1,"code","001-1","name","N-AX-1"));
		maps.add(MapUtil.asMap("id",1,"code","001-2","name","N-AX-2"));
		
		Map<Integer,List<Map>> grouped=MapUtil.groupAsMap(maps, "id",Integer.class);
		
		assertTrue(grouped.size()==1);
		List<Map> gmaps=grouped.get(1);
		
		assertTrue(gmaps.size()==2);
		
	}
	
	@Test
	public void test_tomap()
	{
		MyBean bean=new MyBean();
		bean.setAge(99);
		bean.setBirthday(new Date());
		bean.setId("X-911");
		
		//
		Map<String,Object> map=MapUtil.toMap(bean);
		assertTrue("X-911".equals(map.get("id")));
	}
	
	
}
