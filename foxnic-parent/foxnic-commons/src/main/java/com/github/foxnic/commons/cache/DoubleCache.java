package com.github.foxnic.commons.cache;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.concurrent.task.SimpleTaskManager;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * 二级缓存
 * */
public class DoubleCache<K,V> extends Cache<K, V> {

	private static SimpleTaskManager taskMgr = null;

	/**
	 * 启动缓存清理任务
	 */
	private static void startLoggerTask() {
		if (taskMgr != null) {
			return;
		}
		taskMgr = new SimpleTaskManager(2, DoubleCache.class.getSimpleName());
		taskMgr.doIntervalTask(new Runnable() {
			@Override
			public void run() {
				logStatis();
			}

		}, 30000);
	}

	private static final ArrayList<DoubleCache> CACHES=new ArrayList<DoubleCache>();

	private static void logStatis() {
		CodeBuilder builder=new CodeBuilder("    ");
		builder.ln("\n\n---------------------------  CACHE STAT -----------------------------");
		for (DoubleCache cache : CACHES) {
			builder.ln(cache.getName());
			builder.ln(1,"local(本地) : "+cache.getLocalHits()+" , "+Math.round(cache.getLocalHitRate()*100)+"%");
			builder.ln(1,"remote(远程) : "+cache.getRemoteHits()+" , "+Math.round(cache.getRemoteHitRate()*100)+"%"+(cache.isRemoteCacheValid()?"":"\t不可用"));
			builder.ln(1,"generator(实算) : "+cache.getGeneratorHits()+" , "+Math.round(cache.getGeneratorHitsRate()*100)+"%");
		}
		builder.ln("---------------------------  CACHE STAT -----------------------------");
		Logger.info(builder.toString());
	}

	private Cache<K,V> local = null;

	protected Cache<K, V> getLocalCache() {
		return local;
	}

	protected Cache<K, V> getRemoteCache() {
		return remote;
	}

	private Cache<K,V> remote = null;

	private long localHits=0;
	private long remoteHits=0;
	private long generatorHits=0;

	protected boolean isRemoteCacheValid() {
		if(this.remote==null) return false;
		return this.remote.isValid();
	}
	/**
	 * 本地缓存命中次数
	 * */
	protected long getLocalHits() {
		return localHits;
	}

	/**
	 * 远程缓存命中次数
	 * */
	protected long getRemoteHits() {
		return remoteHits;
	}

	/**
	 * 数据缓存命中次数
	 * */
	protected long getGeneratorHits() {
		return generatorHits;
	}

	/**
	 * @param name 名称
	 * @param local  本地缓存，一级缓存
	 * @param remote 远程缓存，二级缓存
	 * */
	public DoubleCache(String name,Cache<K,V> local,Cache<K,V> remote)
	{
		if(StringUtil.noContent(name)) {
			throw new IllegalArgumentException("name is required");
		}
		if(local==null) {
			throw new IllegalArgumentException("local cache is required");
		}
		if(remote==null) {
			throw new IllegalArgumentException("remote cache is required");
		}
		this.setName(name);
		this.local=local;
		this.remote=remote;
		CACHES.add(this);
		startLoggerTask();
	}

	@Override
	public V get(K key) {
		V value=this.local.get(key);
		if(value==null && this.remote!=null && this.remote.isValid()) {
			value=this.remote.get(key);
			remoteHits++;
			if(value!=null) {
				this.local.put(key, value);
			}
		} else {
			localHits++;
		}
		return value;
	}

	@Override
	public Map<K, V> get(Set<K> keys) {
		Map<K,V> data=this.local.get(keys);
		if(data==null && this.remote!=null && this.remote.isValid()) {
			data=this.remote.get(keys);
			remoteHits++;
			if(data!=null) {
				this.local.put(data);
			}
		} else {
			localHits++;
		}
		return data;
	}

	@Override
	public V get(K key, Function<K, V> generator) {
		V value=this.local.get(key);
		if(value!=null) {
			localHits++;
			return value;
		}
		if(this.remote!=null && this.remote.isValid()) {
			value = this.remote.get(key);
			if (value != null) {
				remoteHits++;
				return value;
			}
		}
		//
		value=generator.apply(key);
		generatorHits++;
		//
		this.put(key, value);
		//
		return value;
	}

	@Override
	public Map<K, V> getAll(Set<? extends K> keys) {
		Map<K,V> values=this.local.getAll(keys);
		if(this.remote!=null && this.remote.isValid()) {
			values.putAll(this.remote.getAll(keys));
		}
		return values;
	}

	@Override
	public void put(K key, V value) {
		this.local.put(key, value);
		if(this.remote!=null && this.remote.isValid()) {
			this.remote.put(key, value);
		}
	}

	@Override
	public void put(Map<K, V> kvs) {

	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void put(K key, V value,int expire) {
		if(this.remote!=null && this.remote.isValid()) {
			this.remote.put(key, value,expire);
			this.local.put(key, value);
		} else {
			this.local.put(key, value,expire);
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		this.local.putAll(map);
		if(this.remote!=null && this.remote.isValid()) {
			this.remote.putAll(map);
		}
	}

	@Override
	public V remove(K key) {
		//
		V  localValue=this.local.remove(key);
		V remoteValue=null;
		if(this.remote!=null && this.remote.isValid()) {
			remoteValue=this.remote.remove(key);
		}
		//
		if(localValue!=null) {
			return localValue;
		} else if(remoteValue!=null) {
			return remoteValue;
		} else {
			return null;
		}
	}

	@Override
	public void removeKeyStarts(String keyPrefix) {
		this.local.removeKeyStarts(keyPrefix);
		if(this.remote!=null && this.remote.isValid()) {
			this.remote.removeKeyStarts(keyPrefix);
		}
	}

	@Override
	public void removeAll(Set<? extends K> keys) {
		this.local.removeAll(keys);
		if(this.remote!=null && this.remote.isValid()) {
			this.remote.removeAll(keys);
		}
	}

	@Override
	public boolean exists(K key) {
		boolean ex=this.local.exists(key);
		if(!ex && this.remote!=null && this.remote.isValid()) {
			ex=this.remote.exists(key);
		}
		return ex;
	}

	/**
	 * 本地缓存命中率
	 * @return 命中率
	 * */
	public double getLocalHitRate()
	{
		return (this.localHits+0.0)/(this.localHits+this.remoteHits+this.generatorHits);
	}

	/**
	 * 远程缓存命中率
	 * @return 命中率
	 * */
	public double getRemoteHitRate()
	{
		return (this.remoteHits+0.0)/(this.localHits+this.remoteHits+this.generatorHits);
	}


	/**
	 * 计算(数据库)命中率
	 * @return 命中率
	 * */
	public double getGeneratorHitsRate()
	{
		return (this.generatorHits+0.0)/(this.localHits+this.remoteHits+this.generatorHits);
	}

	@Override
	public void clear() {

		this.local.clear();
		if(this.remote!=null && this.remote.isValid()) {
			this.remote.clear();
		}

	}

	@Override
	public Set<K> keys() {
		if(this.remote!=null && this.remote.isValid()) {
			return remote.keys();
		} else {
			return  local.keys();
		}
	}

	@Override
	public Set<String> keys(String prefix) {
		if(this.remote!=null && this.remote.isValid()) {
			return remote.keys(prefix);
		} else {
			return  local.keys(prefix);
		}
	}

	@Override
	public Map<K, V> values() {
		if(this.remote!=null && this.remote.isValid()) {
			return remote.values();
		} else {
			return  local.values();
		}
	}



}
