package com.github.foxnic.commons.cache;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import com.alibaba.fastjson.JSONArray;
import com.github.foxnic.commons.encrypt.MD5Util;

/**
 * 缓存基类
 * */
public abstract class Cache<K, V> {

	 
	
	/**
	 * 生成字符串的Key
	 * @param ps           参数清单
	 * @return key
	 */
	public static String gerKey(Object... ps) {
		JSONArray array = new JSONArray();
		for (Object p : ps) {
			array.add(p);
		}
		String key=array.toJSONString();
		if(key.length()>256) {
			key=MD5Util.encrypt16(array.toJSONString());
		} else {
			key=key.replace(':','：');
		}
		return key;
	}
 

	private String name = MD5Util.encrypt16(UUID.randomUUID().toString());

	/**
	 * 获得缓存名称
	 * 
	 * @return 名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置缓存名称
	 * 
	 * @param name 名称
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 获取缓存值
	 * @param key 键
	 * @return 值
	 * */
	public abstract V get(K key);

	/**
	 * 获取值，如果值存在就返回值
	 * @param key	键
	 * @param generator 数据生成函数
	 * @return 值
	 * */
	public abstract V get(K key, Function<K, V> generator);

	/**
	 * 获得全部
	 * @param keys 键值集合
	 * @return 缓存数据,Map
	 * */
	public abstract Map<K, V> getAll(Set<? extends K> keys);

	/**
	 * 将数据放入缓存
	 * @param key	键
	 * @param value		值
	 * */
	public abstract void put(K key, V value);

	/**
	 * 加入集合
	 * @param map 键值集合
	 * */
	public abstract void putAll(Map<? extends K, ? extends V> map);

	/**
	 * 按key移除
	* @param key 键
	 * @return 值
	 * */
	public abstract V remove(K key);

	/**
	 * 移除集合
	 * @param keys 键集合
	 * */
	public abstract void removeAll(Set<? extends K> keys);

	/**
	 * 是否存在key，无论值是否为null，只要key存在返回true
	 * @param key 键
	 * @return 是否存在
	 * */
	public abstract boolean exists(K key);
	
	/**
	 * 移除全部
	 * */
	public abstract void clear();
	
	/**
	 * 获得所有的key
	 * */
	public abstract Set<K> keys();
	
	/**
	 * 获得所有的value值
	 * */
	public abstract Map<K,V> values();

}
