package com.github.foxnic.commons.collection;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class TypedHashMap.
 *
 * @author LeeFangJie
 * @param <K> the key type
 * @param <V> the value type
 */
public class TypedHashMap<K,V> extends TreeMap<K, V> {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7286423883564775893L;

	/**
	 * Instantiates a new typed hash map.
	 */
	public TypedHashMap(){}
	
	/** The case sensitive. */
	private boolean caseSensitive=false;
	 
	/**
	 * 当元素为0个时有效.
	 *
	 * @param caseSensitive if character case sensitive
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		if(size()==0) {
			this.caseSensitive = caseSensitive;
		}
	}

	/**
	 * Checks if is case sensitive.
	 *
	 * @return true, if is case sensitive
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	/**
	 * Instantiates a new typed hash map.
	 *
	 * @param isCaseSensitive if character case sensitive
	 */
	public TypedHashMap(boolean isCaseSensitive)
	{
		this.caseSensitive=isCaseSensitive;
	}
	
	/**
	 * Instantiates a new typed hash map.
	 *
	 * @param map 从map构建
	 * @param isCaseSensitive if character case sensitive
	 */
	public TypedHashMap(HashMap<K, V> map,boolean isCaseSensitive)
	{
		this.caseSensitive=isCaseSensitive;
		if(map==null) {
			return;
		}
		for(K key:map.keySet())
		{
			V value=map.get(key);
			this.put(key, value);
		}
	}
	
	/**
	 * Gets the int.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public int getInt(K key)
	{
		return DataParser.parseInteger(this.get(key)).intValue();
	}
	
	/**
	 * Gets the integer.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public Integer getInteger(K key)
	{
		return DataParser.parseInteger(this.get(key));
	}
	
	
	 
 
	/**
	 * Gets the string.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public String getString(K key)
	{
		return DataParser.parseString(this.get(key));
	}
	
//	/**
//	 * *
//	 * 把逗号分割的字符串转化成字符串数组.
//	 *
//	 * @param key get value by key
//	 * @return  返回指定类型的值
//	 */
//	public String[] getStringArray(K key)
//	{
//		try {
//			return (String[])this.get(key);
//		}
//		catch (Exception e) {
//			String r=getString(key);
//			if(r!=null && r.length()>0)
//			{
//				return r.split(",");
//			}
//			return null;
//		}
//	}
	
	/**
	 * Gets the float.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public Float getFloat(K key)
	{
		return DataParser.parseFloat(this.get(key));
	}
	
	/**
	 * Gets the double.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public Double getDouble(K key)
	{
		return DataParser.parseDouble(this.get(key));
	}
	
	/**
	 * Gets the date.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public Date getDate(K key)
	{
		return DataParser.parseDate(this.get(key));
	}
	
	/**
	 * Gets the boolean.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public Boolean getBoolean(K key)
	{
		return DataParser.parseBoolean(this.get(key));
	}
	
	/**
	 * Gets the long.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public Long getLong(K key)
	{
		return DataParser.parseLong(this.get(key));
	}
	
	/**
	 * Gets the short.
	 *
	 * @param key get value by key
	 * @return  返回指定类型的值
	 */
	public Short getShort(K key)
	{
		return DataParser.parseShort(this.get(key));
	}
	
	/**
	 * Gets the. value of key 
	 *
	 * @param key the key , single key or a path like  user.name
	 * @return the value
	 */
	@Override
	public V get(Object key) {
		return this.get(key, null);
	}
	
	/**
	 * Gets the. value of key 
	 *
	 * @param <T> the generic type
	 * @param key the key , single key or a path like  user.name
	 * @param type the type will return 
	 * @return the value
	 */
	public <T> T get(Object key,Class<T> type) {
		Object value1=internalGet(key);
		if(value1!=null) {
			return DataParser.parse(type, value1);
		}
		if(key!=null && (key instanceof String) && ((String)key).contains(MapUtil.DOT)) {
			return MapUtil.getValue(this, (String)key, type);
		}
		return null;
	}
	
	/**
	 * Internal get.
	 *
	 * @param key get value by key
	 * @return  返回值
	 */
 
	private V internalGet(Object key) {
		if(key==null) {
			return null;
		}
		if(caseSensitive) {
			return super.get(key);
		}
		else
		{
			if(key instanceof String)
			{
				String skey=(key.toString()).toLowerCase();
				return super.get((K)skey);
			}
			else
			{
				return super.get(key); 
			}
		}
	}
	
	/**
	 * Gets the JSON array.
	 *
	 * @param key get value by key
	 * @return JSONArray 类型值
	 */
	public JSONArray getJSONArray(K key)
	{
		JSONArray jsonAray=null;
		Object value=this.get(key);
		if(value!=null && value instanceof JSONArray)
		{
			jsonAray=(JSONArray)value;
		}
		if(jsonAray==null)
		{
			String  val=getString(key);
			try {
				jsonAray=JSONArray.parseArray(val);
			} catch (Exception e) {
				if(val!=null) {
					//尝试转换为数组
					val=val.trim();
					if(!val.startsWith("[") || !val.endsWith("]")) {
						String[] os=(String[])DataParser.parseArray(String[].class, val);
						jsonAray=new JSONArray(Arrays.asList(os));
					}
				}
			}
		}
		 
		return jsonAray;
	}
	
	public String[] getStringArray(Object key) {
		return (String[])getArray(key,String.class);
	}
	
	public Integer[] getIntegerArray(Object key) {
		String[] arr = getCleanQuartsArray(key);
		return  ArrayUtil.castArrayType(arr, Integer.class);
	}

	private String[] getCleanQuartsArray(Object key) {
		String[] arr=this.getStringArray(key);
		String v=null;
		for (int i = 0; i < arr.length; i++) {
			v=arr[i];
			if(v==null) continue;
			v=v.trim();
			v=StringUtil.removeFirst(v, "'");
			v=StringUtil.removeFirst(v, "\"");
			v=StringUtil.removeLast(v, "'");
			v=StringUtil.removeLast(v, "\"");
			arr[i]=v;
		}
		return arr;
	}
	
	public Double[] getDoubleArray(Object key) {
		String[] arr = getCleanQuartsArray(key);
		return  ArrayUtil.castArrayType(arr, Double.class);
	}
	
	public Date[] getDateArray(Object key) {
		String[] arr = getCleanQuartsArray(key);
		return  ArrayUtil.castArrayType(arr, Date.class);
	}
	
	private <T> T[] getArray(Object key,Class<T> elementType) {
	//public <T> T[] getArray(Object key,Class<T> type) {
		Object value=this.get(key);
		if(value==null) return null;
		if(value.getClass().isArray()) {
			return (T[])ArrayUtil.castArrayType((Object[])value, elementType);
		}
		//
		if(value instanceof String) {
			Object[] arr= DataParser.parseArray(Object[].class, (String)value);
			return (T[])ArrayUtil.castArrayType(arr, elementType);
		}
		return null;
	}
	
	/**
	 * Gets the JSON object.
	 *
	 * @param key get value by key
	 * @return JSONObject类型值
	 */
	public JSONObject getJSONObject(K key)
	{
		JSONObject jsonObject=null;
		Object value=this.get(key);
		if(value!=null && value instanceof JSONObject) {
			jsonObject=(JSONObject)value;
		}
		if(jsonObject==null) {
			String  val=getString(key);
			jsonObject=JSONObject.parseObject(val);
		}
		return jsonObject;
	}
	
	
	/** The characteristic code. */
	private  String characteristicCode = null;
 
	/**
	 * 获得特征码.
	 *
	 * @return  特征码
	 */
	public String getCharacteristicCode() {
		
		if(characteristicCode==null)
		{
			StringBuilder buf=new StringBuilder();
			for ( K  key : this.keySet()) {
				buf.append(key+"="+this.get(key)+";");
			}
			characteristicCode=MD5Util.encrypt32(buf.toString());
		}
		return characteristicCode;
	}

	/**
	 * Removes the.
	 *
	 * @param key the key
	 * @return the v
	 */
	@Override
	public V remove(Object key) {
		characteristicCode=null;
		return super.remove(key);
	}
	
	
	/**
	 * Clear.
	 */
	@Override
	public void clear() {
		characteristicCode=null;
		super.clear();
	}
	
	/**
	 * Contains key.
	 *
	 * @param key the key
	 * @return true, if successful
	 */
	@Override
	public boolean containsKey(Object key) {
		if(caseSensitive) {
			return super.containsKey(key);
		}
		else {
			if(key instanceof String) {
				String skey=(key.toString()).toLowerCase();
				return super.containsKey((K)skey);
			}
			else {
				return super.containsKey(key);
			}
		}
	}
	
	/** The orignal keys. */
	private Set<K> orignalKeys=null;
	
	
	/**
	 * Gets the extra map.
	 *
	 * @return the extra map
	 */
	public Map<K,V> getExtraMap()
	{
		HashMap<K, V> map=new HashMap<>();
		if(orignalKeys==null) return map;
		for (K key : orignalKeys) {
			map.put(key,this.get(key));
		}
		return map;
	}
	
	
	
	/**
	 * Put.
	 *
	 * @param key the key
	 * @param value the value
	 * @return the v
	 */
	@Override
	public V put(K key, V value) {
		characteristicCode=null;
		if(caseSensitive)
		{
			return super.put(key, value);
		}
		else
		{
			if(key instanceof String)
			{
				if(orignalKeys==null) {
					orignalKeys=new HashSet<>();
				}
				orignalKeys.add(key);
				String skey=(key.toString()).toLowerCase();
				return super.put((K)skey, value);
			}
			else
			{
				return super.put(key, value);
			}
		}
	}
	
	/**
	 * Put all.
	 *
	 * @param m the m
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		characteristicCode=null;
		for (K key : m.keySet()) {
			put(key, m.get(key));
		}
	}
	
	/**
	 * 名值对转换.
	 *
	 * @param keyValuePairs the key value pairs
	 * @return TypedHashMap
	 */
	@SuppressWarnings("rawtypes")
	public static TypedHashMap asMap(Object... keyValuePairs)
	{
		 return (TypedHashMap)MapUtil.fillMap(new TypedHashMap(),keyValuePairs);
	}

	/**
	 * 获取值，如果么以后或null则返回默认值 defaultValue.
	 *
	 * @param key 键
	 * @param defaultValue 默认值
	 * @return 值
	 */
	@Override
	public V getOrDefault(Object key, V defaultValue) {
		if(this.get(key)==null)
		{
			return defaultValue;
		}
		else
		{
			return super.getOrDefault(key, defaultValue);
		}
	}
	
	/**
	 * 从JSONObject构造.
	 *
	 * @param value JSONObject
	 * @return TypedHashMap
	 */
	public static TypedHashMap fromJSONObject(JSONObject value)
	{
		TypedHashMap map=new TypedHashMap();
		for (String key : value.keySet()) {
			map.put(key, value.get(key));
		}
		return map;
	}
 
}
