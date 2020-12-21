package com.github.foxnic.dao.data;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.busi.id.IDGenerator;
import com.github.foxnic.dao.base.TableDataTest;

/**
 * 数据变更版本以及ID修改测试
 * */
public class RcdSetJsonField extends TableDataTest {

	@Test
	public void test_jsonobject()
	{
		dao.execute("delete from "+clobTable);
		RcdSet rs0=dao.query("select * from "+clobTable);
		assertTrue(rs0.size()==0);
		//初始化数据
		dao.setPrintSQL(true);
		JSONObject json=new JSONObject();
		for (int i = 1; i <= 100; i++) {
			System.out.println(i);
			 json.put("t-"+i, System.nanoTime());
			 json.put("id-"+i, IDGenerator.getSnowflakeId());
			 json.put("value-"+i, i);
			 if(i%7==0) {
				 int z1=dao.insert(clobTable).set("id","id-"+i).set("content",null).execute();
			 } else  if(i%4==0) {
				 int z2=dao.insert(clobTable).set("id","id-"+i).set("content","XX").execute();
			 } else {
				 int z=dao.insert(clobTable).set("id","id-"+i).set("content",json.toJSONString()).execute();
			 }
	 
		}
		
		RcdSet rs=dao.query("select * from "+clobTable);
		
		for (Rcd r : rs) {
			System.out.println(r.toJSONObject().toJSONString());
			String x=r.getString("content");
			JSONObject js=null;
			try {
				js = r.getJSONObject("content");
				assertTrue(js.keySet().size()>2);
			} catch (Exception e) {
				if(x==null) {
					assertTrue(true);
					continue;
				}
				if("XX".equals(x)) {
					assertTrue(true);
					System.out.println();
					continue;
				}
				assertTrue(false);
			}
			
		}
 
	}
	
	
	@Test
	public void test_jsonarray()
	{
		dao.execute("delete from "+clobTable);
		RcdSet rs0=dao.query("select * from "+clobTable);
		assertTrue(rs0.size()==0);
		//初始化数据
		dao.setPrintSQL(true);
		JSONArray arr=new JSONArray();
		for (int i = 1; i <= 100; i++) {
			System.out.println(i);
			JSONObject json=new JSONObject();
			 json.put("t-"+i, System.nanoTime());
			 json.put("id-"+i, IDGenerator.getSnowflakeId());
			 json.put("value-"+i, i);
			 arr.add(json);
			 if(i%7==0) {
				 int z1=dao.insert(clobTable).set("id","id-"+i).set("content",null).execute();
			 } else  if(i%4==0) {
				 int z2=dao.insert(clobTable).set("id","id-"+i).set("content","XX").execute();
			 } else {
				 int z=dao.insert(clobTable).set("id","id-"+i).set("content",arr.toJSONString()).execute();
			 }
		}
		
		RcdSet rs=dao.query("select * from "+clobTable);
		
		for (Rcd r : rs) {
			System.out.println(r.toJSONObject().toJSONString());
			String x=r.getString("content");
			JSONArray js=null;
			try {
				js = r.getJSONArray("content");
				assertTrue(js.size()>=1);
			} catch (Exception e) {
				if(x==null) {
					assertTrue(true);
					continue;
				}
				if("XX".equals(x)) {
					assertTrue(true);
					System.out.println();
					continue;
				}
				assertTrue(false);
			}
			
		}
 
	}
	
}
