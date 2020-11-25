package com.github.foxnic.commons.cache;

import static org.junit.Assert.assertTrue;

import java.util.function.Function;

import org.junit.Test;

import com.github.foxnic.commons.concurrent.ThreadUtil;

public class DoubleCacheTest {
	
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
		
		final LocalCache<String,Long> local=new LocalCache<String,Long>(1000,ExpireType.LIVE);
		final LocalCache<String,Long> remote=new LocalCache<String,Long>(2000,ExpireType.LIVE);
		
		DoubleCache<String,Long> cache=new DoubleCache<String, Long>("demo",local, remote);
 
		assertTrue(z==0);
		Long r1=cache.get(key, dg);
		assertTrue(z==1);
		Long r2=cache.get(key, dg);
		assertTrue(z==1);
		assertTrue(r1.longValue()==r2.longValue());
		
		
		
	}
	
	@Test
	public void test_element_expire_live()
	{
		final LocalCache<String,Long> local=new LocalCache<String,Long>(1000,ExpireType.LIVE);
		final LocalCache<String,Long> remote=new LocalCache<String,Long>(2000,ExpireType.LIVE);
		
		DoubleCache<String,Long> cache=new DoubleCache<String, Long>("demo",local, remote);
		
		cache.put("A", 15L);
		ThreadUtil.sleep(500);
		assertTrue(15==cache.get("A"));
		ThreadUtil.sleep(1200);
		assertTrue(15==cache.get("A"));
		
		ThreadUtil.sleep(2500);
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
//	
//	
//	
//	@Test
//	public void test_elements_count()
//	{
//		final LocalCache<String,Integer> cache=new LocalCache<String,Integer>(1000000,ExpireType.LIVE,10);
//		for (int i = 0; i < 20; i++) {
//			cache.put("K-"+i, i);
//			ThreadUtil.sleep(1,5);
//			System.out.println("add");
//		}
//		System.out.println(cache.size());
//		assertTrue(cache.size()<20);
//		ThreadUtil.sleep(1500);
//		System.out.println(cache.size());
//		assertTrue(cache.size()<20 && cache.size()>8);
// 
//	}
//	
//	public static void main(String[] args) {
//		(new LocalCacheTest()).test_elements_count();
//	}

}
