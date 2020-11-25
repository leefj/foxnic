package com.github.foxnic.commons.cache;

import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.function.Function;

import org.junit.Test;

import com.github.foxnic.commons.collection.MapUtil;
import com.github.foxnic.commons.concurrent.ThreadUtil;
import com.github.foxnic.commons.lang.ArrayUtil;
 

public class LocalCacheTest {
	
	int z=0;
	
	@Test
	public void test_normal()
	{
		int a=8,b=9;
		String c="XXXX";
		double d=9.8;
		String e="A";
		
		String key=Cache.gerKey("test",a,b,c,d,e);
 
		Function<String,Long> dg=new Function<String, Long>() {
			@Override
			public Long apply(String key) {
				z++;
				Double r= (a+b)+c.length()*d+e.charAt(0);
				return r.longValue();
			}
		};
		
		final LocalCache<String,Long> cache=new LocalCache<String,Long>(10000,ExpireType.LIVE);
		assertTrue(z==0);
		Long r1=cache.get(key, dg);
		assertTrue(z==1);
		Long r2=cache.get(key, dg);
		assertTrue(z==1);
		assertTrue(r1.longValue()==r2.longValue());
		
		Map<String,Long> map=MapUtil.asMap("Z1",9L);
		cache.putAll(map);
		assertTrue(cache.get("Z1").equals(9L));
		
		Map<String,Long> mm=cache.getAll(ArrayUtil.asSet("A","Z1"));
		assertTrue(mm.size()==2);
		assertTrue(mm.get("Z1").equals(9L));
		
	}
	
	@Test
	public void test_element_expire_live()
	{
		final LocalCache<String,Long> cache=new LocalCache<String,Long>(1000,ExpireType.LIVE);
		cache.put("A", 15L);
		ThreadUtil.sleep(500);
		assertTrue(15==cache.get("A"));
		ThreadUtil.sleep(2000);
		assertTrue(null==cache.get("A"));
	}
	
	
	@Test
	public void test_element_expire_idle()
	{
		final LocalCache<String,Long> cache=new LocalCache<String,Long>(1000,ExpireType.IDLE);
		cache.put("A", 15L);
		ThreadUtil.sleep(600);
		assertTrue(15==cache.get("A"));
		
		ThreadUtil.sleep(600);
		assertTrue(null==cache.get("A"));
		
		ThreadUtil.sleep(1500);
		assertTrue(null==cache.get("A"));
	}
	
	
	
	@Test
	public void test_elements_count()
	{
		final LocalCache<String,Integer> cache=new LocalCache<String,Integer>(1000000,ExpireType.LIVE,10);
		for (int i = 0; i < 20; i++) {
			cache.put("K-"+i, i);
			ThreadUtil.sleep(1,5);
			System.out.println("add");
		}
		System.out.println(cache.size());
		assertTrue(cache.size()<20);
		ThreadUtil.sleep(1500);
		System.out.println(cache.size());
		assertTrue(cache.size()<20 && cache.size()>8);
 
	}
	
	
	
	private Object fetchingValue1=null;
	private Object fetchingValue2=null;
	@Test
	public void fetching_lock()
	{
		String key="AAZZ";
		final LocalCache<String,Long> cache=new LocalCache<String,Long>(1000,ExpireType.LIVE);
		
		new Thread() {
			public void run() {
				cache.startFetch(key);
				ThreadUtil.sleep(2000);
				cache.put(key,284L);
			};
		}.start();
		
		ThreadUtil.sleep(128);
		
		new Thread() {
			public void run() {
				//设置为未超时
				fetchingValue1=cache.getOrWait(key, 3000);
			};
		}.start();
		
		new Thread() {
			public void run() {
				//设置为超时
				fetchingValue2=cache.getOrWait(key, 1000);
			};
		}.start();
		
		ThreadUtil.sleep(4000);
		
		 assertTrue(fetchingValue1.equals(284L));
		 assertTrue(fetchingValue2==null);
	}
	
	
	public static void main(String[] args) {
		(new LocalCacheTest()).fetching_lock();
	}

}
