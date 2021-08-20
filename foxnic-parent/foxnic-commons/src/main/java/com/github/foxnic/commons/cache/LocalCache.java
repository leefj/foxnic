package com.github.foxnic.commons.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.foxnic.commons.log.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 一个简单的本地内存缓存，基于 caffeine 缓存扩展
 * */
public class LocalCache<K,V> extends Cache<K,V> {
 
	private class LocalElement<V> {
		private V value = null;
		private long createTime;
		private long expireTime;
		
		public LocalElement(V value,long liveMillis) {
			this.value=value;
			this.createTime=System.currentTimeMillis();
			if(liveMillis>0) {
				this.expireTime = this.createTime + liveMillis;
			} else {
				this.expireTime=-1;
			}

		}

		protected V getValue() {
			//过期
			if(this.expireTime>0 && this.expireTime<System.currentTimeMillis()) {
				 return null;
			}
			return value;
		}

		protected void setValue(V value) {
			this.value = value;
		}



	}
	
 
	private com.github.benmanes.caffeine.cache.Cache<K,LocalElement<V>> cache=null;
	private Set<K> keys=null;
	private int limit;
	private int expire=0;
	private ExpireType expireType=ExpireType.LIVE;
	
	/**
	 * 默认超时时间
	 * @return long
	 * */
	public long getExpire() {
		return expire;
	}
 
	/**
	 * 默认超时类型
	 * @return ExpireType
	 * */
	public ExpireType getExpireType() {
		return expireType;
	}
 
	/**
	 * 获取元素上限
	 * @return 上限
	 * */
	public int getLimit() {
		return limit;
	}

	/**
	 * Instantiates a new local cache.<br>
	 * 永不超时，元素个数无限制
	 */
	public LocalCache() {
		this(-1,ExpireType.LIVE,0);
		
	}
	
	/**
	 * @param expire 默认超时时间，单位毫秒，小于0 时无超时
	 * */
	public LocalCache(int expire) {
		this(expire,ExpireType.LIVE,0);
	}
	
	/**
	 * @param expire 默认超时时间，单位毫秒，小于0 时无超时
	 * @param expireType 超时类型
	 * */
	public LocalCache(int expire,ExpireType expireType) {
		this(expire,expireType,0);
	}
	
 
	
	/**
	 * @param expire 默认超时时间，单位毫秒，小于0 时无超时
	 * @param limit 元素上限，不指定时，无上限
	 * @param expireType 超时类型
	 * */
	public LocalCache(int expire,ExpireType expireType,int limit) {
 
		this.limit = limit;
		this.expire=expire;
		this.expireType=expireType;
		
		
		
		Caffeine<Object,Object>  builder=Caffeine.newBuilder();

		if(expireType==ExpireType.LIVE  && expire>0) {
			builder.expireAfterWrite(expire, TimeUnit.MILLISECONDS);
		}
		
		if(expireType==ExpireType.IDLE && expire>0) {
			builder.expireAfterWrite(expire, TimeUnit.MILLISECONDS);
			builder.expireAfterAccess(expire, TimeUnit.MILLISECONDS);
		}
 
		if(limit>0) {
			builder.maximumSize(limit);
		}
		//
		cache =  builder.build();
		keys = new HashSet<>();
	}
 
	/**
	 * 将数据放入缓存
	 * @param key	键
	 * @param value		值
	 * */
	@Override
	public void put(K key,V value)
	{
		cache.put(key, new LocalElement<V>(value,-1));
		locks.remove(key);
		keys.add(key);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	/**
	 * 将数据放入缓存
	 * @param key	键
	 * @param value		值
	 * @param expire 超时的毫秒数
	 * */
	@Override
	public void put(K key,V value,int expire)
	{
		cache.put(key, new LocalElement<V>(value,expire));
		locks.remove(key);
		keys.add(key);
	}
	
	private ConcurrentHashMap<K,Boolean> locks=new ConcurrentHashMap<K,Boolean>();
	
	/**
	 * 标记当前key正在取值，其它使用该key取值线程将被暂停，知道这个key调用put函数设置值
	 * @param key 	缓存键
	 * */
	public void startFetch(K key)
	{
		//System.out.println("set fetching lock "+key);
		locks.put(key, true);
	}
	
	public V getOrWait(K key,int timeout)
	{
		Boolean lock=locks.get(key);
		if(lock==null) {
			lock=false;
		}
		
		if(!lock) {
			return this.get(key);
		}
		//
		V value = this.get(key);
		long t0=System.currentTimeMillis();
		long tw=t0;
		while( lock && value==null ) {
			
			try {
				Thread.sleep(32);
			} catch (InterruptedException e) {}
			
			lock=locks.get(key);
			if(lock==null) {
				lock=false;
			}
			value = this.get(key);
			
			tw = System.currentTimeMillis() - t0;
			if(tw>timeout) {
				Logger.warn("Data Fetching Timeout , key = "+key);
				break;
			}
			
//			System.out.println(tw+"\nwait fetching("+tw+") "+lock);
//			System.out.println(tw+"\nwait fetching("+tw+") "+value);
//			System.out.println("waiting "+tw);
			
			if(!lock ) {
				return value;
			}
 	
		}
		return value;
	}
 
	/**
	 * 获取值
	 * @param key	键
	 * @return 值
	 * */
	@Override
	public V get(K key)
	{
		if(key==null) return null;
		LocalElement<V> e = cache.getIfPresent(key);
		if(e==null) {
			return null;
		}
		return e.getValue();
	}
 
	/**
	 * 获取值，如果值存在就返回值
	 * @param key	键
	 * @param generator 数据生成函数
	 * @return 值
	 * */
	@Override
	public V get(K key,Function<K, V> generator)
	{
		if(this.exists(key)) {
			return this.get(key);
		}
		V value=generator.apply(key);
		this.put(key, value);
		return value;
	}
	
	
	/**
	 * 大小
	 * @return 缓存大小
	 * */
	public long size()
	{
		cache.cleanUp();
		return cache.estimatedSize();
	}
	
	/**
	 * 移除全部
	 * */
	@Override
	public void clear() {
		cache.invalidateAll();
		keys.clear();
	}
	
	/**
	 * 按key移除
	* @param key 键
	 * @return 值
	 * */
	@Override
	public V remove(K key) {
		V value=this.get(key);
		this.cache.invalidate(key);
		this.keys.remove(key);
		return value;
	}


	@Override
	public void removeKeyStarts(String keyPrefix) {
		Set<String> keys=this.keys(keyPrefix);
		for (String key : keys) {
			this.remove((K)key);
		}
	}

	/**
	 * 是否存在key，无论值是否为null，只要key存在返回true
	 * @param key 键
	 * @return 是否存在
	 * */
	@Override
	public boolean exists(K key) {
		return this.cache.getIfPresent(key)!=null;
	}

	/**
	 * 获得全部
	 * @param keys 键值集合
	 * @return 缓存数据,Map
	 * */
	@Override
	public Map<K, V> getAll(Set<? extends K> keys) {
		Map<K,LocalElement<V>> eMap=this.cache.asMap();
		Map<K,V> map=new HashMap<>();
		for (Entry<K, LocalElement<V>> entry : eMap.entrySet()) {
			map.put(entry.getKey(),entry.getValue()==null?null:entry.getValue().getValue());
		}
		return map;
	}

	/**
	 * 加入集合
	 * @param map 键值集合
	 * */
	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		Map<K,LocalElement<V>> eMap=new HashMap<K,LocalElement<V>>();
		for (Entry<? extends K,? extends V> entry : map.entrySet()) {
			eMap.put(entry.getKey(), new LocalElement<V>(entry.getValue(),-1));
		}
		this.cache.putAll(eMap);
		this.locks.clear();
		this.keys.addAll(map.keySet());
	}

 
	/**
	 * 移除集合
	 * @param keys 键集合
	 * */
	@Override
	public void removeAll(Set<? extends K> keys) {
		this.cache.invalidateAll(keys);
		this.keys.removeAll(keys);
	}

	@Override
	public Set<K> keys() {
		return this.keys;
	}

	@Override
	public Set<String> keys(String prefix) {

		Set<String> parts=new HashSet<>();

		for (K key : keys) {
			if(!(key instanceof  String)) {
				throw new RuntimeException("不支持");
			}
			if(((String)key).startsWith(prefix)) {
				parts.add((String) key);
			}
		}
		return parts;
	}

	@Override
	public Map<K,V> values() {
		Map<K,LocalElement<V>> els=this.cache.getAllPresent(this.keys);
		Map<K,V> map=new HashMap<>();
		for (Entry<K,LocalElement<V>> e : els.entrySet()) {
			map.put(e.getKey(), e.getValue().getValue());
		}
		return map;
	}
	
	
	

}
