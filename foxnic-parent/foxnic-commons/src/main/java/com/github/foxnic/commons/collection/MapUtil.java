package com.github.foxnic.commons.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.bean.FilterOperator;
import com.github.foxnic.commons.bean.PropertyComparator;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.lang.DataParser;

/**
 * @author LeeFangJie
 * 用于处理map或类似map结构的数据
 */
public class MapUtil {

	public static final String DOT_REGEX = "\\.";
	public static final String DOT = ".";



	/**
	 * 名值对转换
	 * @param keyValuePairs key value pair list
	 * @return 返回一个 JSONObject 对象
	 * */
	public static JSONObject asJSONObject(Object... keyValuePairs)
	{
		return   (JSONObject)fillStringKeyMap(new JSONObject(),0,keyValuePairs);
	}

	/**
	 * 名值对转换
	 * @param keyValuePairs key value pair list
	 * @return 返回一个 JSONObject 对象
	 * */
	public static JSONObject asLowerKeyJSONObject(Object... keyValuePairs)
	{
		return   (JSONObject)fillStringKeyMap(new JSONObject(),-1,keyValuePairs);
	}

	/**
	 * 名值对转换
	 * @param keyValuePairs key value pair list
	 * @return 返回一个 JSONObject 对象
	 * */
	public static JSONObject asUpperKeyJSONObject(Object... keyValuePairs)
	{
		return   (JSONObject)fillStringKeyMap(new JSONObject(),1,keyValuePairs);
	}


	/**
	 * 名值对转换
	 * @param json 将要填充的JSON对象
	 * @param keyValuePairs key value pair list
	 * @return 返回一个 JSONObject 对象
	 * */
	public static JSONObject fillJSONObject(JSONObject json, Object... keyValuePairs)
	{
		return   (JSONObject)fillStringKeyMap(json,0,keyValuePairs);
	}


	/**
	 * 名值对转换
	 * @param json 将要填充的JSON对象
	 * @param keyValuePairs key value pair list
	 * @return 返回一个 JSONObject 对象
	 * */
	public static JSONObject fillLowerKeyJSONObject(JSONObject json, Object... keyValuePairs)
	{
		return   (JSONObject)fillStringKeyMap(json,-1,keyValuePairs);
	}

	/**
	 * 名值对转换
	 * @param json 将要填充的JSON对象
	 * @param keyValuePairs key value pair list
	 * @return 返回一个 JSONObject 对象
	 * */
	public static JSONObject fillUpperKeyJSONObject(JSONObject json, Object... keyValuePairs)
	{
		return   (JSONObject)fillStringKeyMap(json,1,keyValuePairs);
	}


	/**
	 * 名值对转换
	 * @param keyValuePairs key value pair list
	 * @return 返回一个map对象
	 * */
	@SuppressWarnings({ "rawtypes"})
	public static Map asMap(Object... keyValuePairs)
	{
		return fillMap(null,keyValuePairs);
	}

	/**
	 * 名值对转换
	 * @param keys key list
	 * @param value 值
	 * @return 返回一个map对象
	 * */
	@SuppressWarnings({ "rawtypes"})
	public static <K,V> Map<K,V> asMap(List<K> keys,V value)
	{
		 Map<K,V> map =new HashMap<>();
		for (K key : keys) {
			map.put(key,value);
		}
		return map;
	}
	/**
	 * 名值对转换
	 * @param map  将要填充的map对象
	 * @param keyValuePairs key value pair list
	 * @return 返回一个map对象
	 * */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map fillMap(Map map,Object... keyValuePairs)
	{
		if(map==null) {
			map=new HashMap(keyValuePairs.length/2+1);
		}
		for (int i = 0; i < keyValuePairs.length; i++) {
			Object p = keyValuePairs[i];
			i++;
			if (i >= keyValuePairs.length) {
				map.put(p, null);
				break;
			}
			Object v = keyValuePairs[i];
			map.put(p, v);
		}
		return map;
	}



	/**
	 * 名值对转换
	 * @param keyValuePairs key value pair  list
	 * @return 返回一个map对象
	 * */
	public static Map<String,Object> asStringKeyMap(Object... keyValuePairs)
	{
		return fillStringKeyMap(null,0,keyValuePairs);
	}

	/**
	 * 名值对转换,键值均为小写
	 * @param keyValuePairs key value pair  list
	 * @return 返回一个map对象
	 * */
	public static Map<String,Object> asLowerKeyMap(Object... keyValuePairs)
	{
		return fillStringKeyMap(null,-1,keyValuePairs);
	}

	/**
	 * 名值对转换,键值均为大写
	 * @param keyValuePairs key value pair  list
	 * @return 返回一个map对象
	 * */
	public static Map<String,Object> asUpperKeyMap(Object... keyValuePairs)
	{
		return fillStringKeyMap(null,1,keyValuePairs);
	}


	/**
	 * 名值对转换
	 * @param map 需要填充的map对象
	 * @param keyValuePairs key value pair  list
	 * @return 返回一个map对象
	 * */
	public static Map<String,Object> fillStringKeyMap(Map<String,Object> map,Object... keyValuePairs)
	{
		return fillStringKeyMap(map,0,keyValuePairs);
	}

	/**
	 * 名值对转换,键值均为小写
	 * @param map 需要填充的map对象
	 * @param keyValuePairs key value pair  list
	 * @return 返回一个map对象
	 * */
	public static Map<String,Object> fillLowerKeyMap(Map<String,Object> map,Object... keyValuePairs)
	{
		return fillStringKeyMap(map,-1,keyValuePairs);
	}

	/**
	 * 名值对转换,键值均为大写
	 * @param map 需要填充的map对象
	 * @param keyValuePairs key value pair  list
	 * @return 返回一个map对象
	 * */
	public static Map<String,Object> fillUpperKeyMap(Map<String,Object> map,Object... keyValuePairs)
	{
		return fillStringKeyMap(map,1,keyValuePairs);
	}


	/**
	 * 名值对转换
	 * @param map 需要填充的map对象
	 * @param caseSensitive  key是否大小写敏感
	 * @param keyValuePairs key value pair  list
	 * @return 返回一个map对象
	 * */
	@SuppressWarnings({"unchecked" })
	private static Map<String,Object> fillStringKeyMap(Map<String,Object> map,int caseType,Object... keyValuePairs)
	{
		if(map==null) {
			map=new HashMap<String, Object>();
		}
		String key=null;
		for (int i = 0; i < keyValuePairs.length; i++) {
			if(i%2==1) {
				continue;
			}
			key=(keyValuePairs[i]+"");
			if(caseType==-1) {
				key=key.toLowerCase();
			}
			if(caseType==1) {
				key=key.toUpperCase();
			}
			keyValuePairs[i]=key;
		}
		return fillMap(map,keyValuePairs);

	}

	/**
	 * 名值对转换
	 * @param caseSensitive  key是否大小写敏感
	 * @param keyValuePairs key value pair  list
	 * @return 返回一个map对象
	 * */
	public static Map<String,Object> asStringKeyMap(boolean caseSensitive,Object... keyValuePairs)
	{
		return fillStringKeyMap(null,caseSensitive,keyValuePairs);
	}


	/**
	 *  提取map清单中的指定key的值
	 *  @param <T> 值类型
	 *  @param list map清单
	 *  @param key 将要提取的Key值
	 *  @param type  要求返回的值类型
	 *  @return 返回List
	 * */
	@SuppressWarnings("rawtypes")
	public static <T> List<T> getValueList(Collection<Map> list,String key,Class<T> type)
	{
		List<T> values=new ArrayList<T>();
		Object v=null;
		for (Map map : list) {
			v=map.get(key);
			values.add(DataParser.parse(type, v));
		}
		return values;
	}

	/**
	 *  提取JSONArray中元素的指定key的值
	 *  @param <T> 值类型
	 *  @param list 元素为 JSONObject 的 JSONArray
	 *  @param key 将要提取的Key值
	 *  @param type  要求返回的值类型
	 *  @return 返回List
	 * */
	public static <T> List<T> getValueList(JSONArray list,String key,Class<T> type)
	{
		List<T> values=new ArrayList<T>();
		Object v=null;
		JSONObject json=null;
		for (int i = 0; i < list.size(); i++) {
			json=list.getJSONObject(i);
			if(json!=null) {
				v=json.get(key);
			} else {
				v=null;
			}
			values.add(DataParser.parse(type, v));
		}
		return values;
	}

	/**
	 *  提取JSONArray中元素的指定key的值
	 *  @param <T> 值类型
	 *  @param list 元素为 JSONObject 的 JSONArray
	 *  @param key 将要提取的Key值
	 *  @param type  要求返回的值类型
	 *  @return 返回List
	 * */
	public static <T> Set<T> getValueSet(JSONArray list,String key,Class<T> type)
	{
		Set<T> values=new HashSet<T>();
		Object v=null;
		JSONObject json=null;
		for (int i = 0; i < list.size(); i++) {
			json=list.getJSONObject(i);
			if(json!=null) {
				v=json.get(key);
			} else {
				v=null;
			}
			values.add(DataParser.parse(type, v));
		}
		return values;
	}

	/**
	 *  提取map清单中的指定key的值
	 *  @param <T> 值类型
	 *  @param list map清单
	 *  @param key 将要提取的Key值
	 *  @param type  要求返回的值类型
	 *  @return 返回List
	 * */
	@SuppressWarnings("rawtypes")
	public static <T> Set<T> getValueSet(Collection<Map> list,String key,Class<T> type)
	{
		Set<T> values=new HashSet<T>();
		Object v=null;
		for (Map map : list) {
			v=map.get(key);
			values.add(DataParser.parse(type, v));
		}
		return values;
	}


	/**
	 *  提取map清单中的指定key的值
	 *  @param <T> 值类型
	 *  @param list map清单
	 *  @param key 将要提取的Key值
	 *  @param type  要求返回的值类型
	 *  @return 返回数组
	 * */
	@SuppressWarnings("rawtypes")
	public static <T> T[] getValueArray(Collection<Map> list,String key,Class<T> type)
	{
		T[] values=ArrayUtil.createArray(type, list.size());
		Object v=null;
		int i=0;
		for (Map map : list) {
			v=map.get(key);
			values[i]=DataParser.parse(type, v);
			i++;
		}
		return values;
	}

	/**
	 *  提取JSONArray中元素的指定key的值
	 *  @param <T> 值类型
	 *  @param list 元素为 JSONObject 的 JSONArray
	 *  @param key 将要提取的Key值
	 *  @param type  要求返回的值类型
	 *  @return 返回Array
	 * */
	public static <T> T[] getValueArray(JSONArray list,String key,Class<T> type)
	{
		T[] values=ArrayUtil.createArray(type, list.size());
		Object v=null;
		JSONObject json=null;
		for (int i = 0; i < list.size(); i++) {
			json=list.getJSONObject(i);
			if(json!=null) {
				v=json.get(key);
			} else {
				v=null;
			}
			values[i]=DataParser.parse(type, v);
		}
		return values;
	}



	/**
	 * 把记录集转换成Map形式 如果单个字段，使用原始值作为键；如果是多字段，则用它们的值下划线连接
	 * 	@param  list map清单
	 * 	@param field key字段
	 * @return Map
	 */
	@SuppressWarnings("rawtypes")
	public static Map<Object, List<Map>> groupAsMap(Collection<Map> list, String... field) {

		HashMap<Object, List<Map>> map = new HashMap<Object, List<Map>>(5);
		Object key = null;
		List<Map> glist = null;
		for (Map r : list) {
			key = makeGroupKey(r, field);
			glist = map.get(key);
			if (glist == null) {
				glist = new ArrayList<Map>();
				map.put(key, glist);
			}
			glist.add(r);
		}
		return map;
	}


	/**
	 * 把记录集转换成Map形式 如果单个字段，使用原始值作为键；如果是多字段，则用它们的值下划线连接
	 * @param <K> 键值类型
	 * @param  list map清单
	 * 	@param field key字段
	 * @param keyType 键值类型
	 * @return Map
	 */
	@SuppressWarnings("rawtypes")
	public static <K> Map<K, List<Map>> groupAsMap(Collection<Map> list,String field,Class<K> keyType) {

		HashMap<K, List<Map>> map = new HashMap<K, List<Map>>(5);
		K key = null;
		List<Map> glist = null;
		for (Map r : list) {
			key = DataParser.parse(keyType,r.get(field));
			glist = map.get(key);
			if (glist == null) {
				glist = new ArrayList<Map>();
				map.put(key, glist);
			}
			glist.add(r);
		}
		return map;
	}

	/**
	 * 获得用于分组的key,各个字段间用下划线隔开 如果一个key则使用原始值作为key，如果多个key则使用
	 * @param r Map数据
	 * @param field 用于key制作的字段
	 * @return key
	 */
	public static Object makeGroupKey(Map r, String... field) {
		if (field.length == 0) {
			throw new IllegalArgumentException("至少包含一个字段");
		}

		if (field.length == 1) {
			return r.get(field[0]);
		} else {
			String key = "";
			for (int i = 0; i < field.length; i++) {
				key += r.get(field[i]) + "_";
			}
			if (key.length() > 0) {
				key = key.substring(0, key.length() - 1);
			}
			return key;
		}
	}

	/**
	 * 把 Java Bean 转成 map，以属性名作为key
	 * @param bean  Java Bean
	 * @return Map
	 * */
	public static Map<String,Object> toMap(Object bean)
	{
		 return BeanUtil.toMap(bean);
	}

	/**
	 * 把 Map的List 转成 map，以属性名作为key
	 * @param <K> 键值类型
 	 * @param maps map 清单
	 * @param key 键值
	 * @return Map
	 * */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <K> Map<K,Map> toMap(Collection<Map> maps,K key)
	{
		  Map<K,Map> mps=new HashMap<K, Map>();
		  K keyValue=null;
		  for (Map map : maps) {
			  keyValue=(K)map.get(key);
			  mps.put(keyValue,map);
		}
		return mps;
	}


	/**
	 * 批量设置Map值
 	 * @param maps map 清单
	 * @param key 键值
	 * @param value 值
	 * */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void setValue(Collection<Map> maps, Object key, Object value) {
		for (Map map : maps) {
			map.put(key, value);
		}
	}

	/**
	 * 批量设置Map值
 	 * @param maps map 清单
	 * @param key 键值
	 * */
	@SuppressWarnings({ "rawtypes" })
	public static void removeKey(Collection<Map> maps, Object key) {
		for (Map map : maps) {
			map.remove(key);
		}
	}


	/**
	 * 过滤，保留与value值相等的元素
	 * @param maps Map清单
	 * @param field 属性名
	 * @param value 值
	 * @return 过滤结果
	 * */
	@SuppressWarnings("rawtypes")
	public static List<Map> filter(Collection<Map> maps , String field, Object value) {
		return filter(maps, field, value,FilterOperator.EQUALS);
	}

	/**
	 * 过滤，并指定比较器
	 * @param	maps Map清单
	 * @param 	field 属性名
	 * @param 	value 值
 	 * @param  filterOperator  比较器，可以直接从 FilterOperator 定义的常量获得
	 * @return 过滤结果
	 * */
	@SuppressWarnings("rawtypes")
	public static List<Map> filter(Collection<Map> maps , String field, Object value,FilterOperator filterOperator) {
		Object tmp=null;
		List<Map> result=new ArrayList<Map>();
		for (Map t : maps) {
			tmp=t.get(field);
			if(filterOperator.compare(tmp,value)) {
				result.add(t);
			}
		}
		return result;
	}

	/**
	 * 排序
	 * @param maps Map清单
	 * @param field 属性
	 * @param ascending  是否升序
 	 * @param  nullslast  null值是否排最后
 	 * @return 	排序结果
	 * */
	@SuppressWarnings("rawtypes")
	public static List<Map> sort(Collection<Map> maps,final String field, final boolean ascending, final boolean nullslast) {
		List<Map> nulls=filter(maps, field, null);
		List<Map> notNulls=filter(maps, field, null,FilterOperator.EQUALS.reverse());
		Collections.sort(notNulls, new PropertyComparator(field,ascending));
		if(nullslast) {
			notNulls.addAll(nulls);
		}
		else {
			notNulls.addAll(0, nulls);
		}
		return notNulls;
	}

	private static Object getValueInContainer(Object container,String key)
	{
		if(container==null) return null;
		if(container instanceof Map) {
			return ((Map)container).get(key);
		} else if(container.getClass().isArray()) {
			Integer index=DataParser.parseInteger(key);
			if(index == null) {
				throw new IllegalArgumentException("key="+key+" 非数字");
			}
			return ((Object[])container)[index];
		} else if(container instanceof List) {
			Integer index=DataParser.parseInteger(key);
			if(index == null) {
				throw new IllegalArgumentException("key="+key+" 非数字");
			}
			return ((List)container).get(index);
		} else {
			try {
				return BeanUtil.getFieldValue(container, key);
			} catch (Exception e) {
				return null;
			}
		}

	}


	/**
	 * 获取路径下的值
	 *
	 * @param <T> the generic type
	 * @param ps the map value container
	 * @param path the path , like  lucy.name.1.xxx
	 * @param type the return value type
	 * @return the value
	 */
	public static <T>  T getValue(Map ps,String path,Class<T> type)
	{
		if(!path.contains(DOT)) return DataParser.parse(type, ps.get(path));
		String[] parts=path.split(DOT_REGEX);

		int i=0;
		Object value=null;
		Object container=ps;
		String currKey=null;
		String nextKey=null;

		while(true) {

			currKey=parts[i];
			if(i<parts.length-1) {
				nextKey=parts[i+1];
			} else {
				nextKey=null;
			}

			//获取值
			value=getValueInContainer(container, currKey);

			//如果已经没有下一个key
			if(nextKey==null) {
				if(i==parts.length-1) {
					//已经到了路径的最后部分
					return DataParser.parse(type, value);
				} else {
					//未到路径的最后部分
					return null;
				}
			}

			//如果还有下一个key，继续深入
			if(value==null) return null;

			container = value;

			i++;

		}

	}

}
