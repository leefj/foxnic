package com.github.foxnic.sql.parameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.github.foxnic.commons.collection.MapUtil;

 
 
/**
 * Map参数构建器
 * */
public class MapParamBuilder {
	 
	/**
	 * 使用名值交替参数创建
	 * @param nvlist 名值交替参数
	 * @return DAO 可用的Map参数
	 * */
	public static Map<String,Object> create(Object... nvlist)
	{
		MapParamBuilder builder=new MapParamBuilder();
		return builder.sets(nvlist).map();
	}
	
	private HashMap<String,Object>  map=new HashMap<String,Object>();
	
	/**
	 * 加入参数,返回当前对象
	 * @param name 参数名
	 * @param value 参数值
	 * @return  MapParamBuilder
	 * */
	public MapParamBuilder set(String name,Object value)
	{
		map.put(name, value);
		return this;
	}
 
	/**
	 * 批量设置参数列表<br>
	 * 例如： SQLParam.create("name","leefj","age",25,"address","ningbo");
	 * @param nvlist 名称，值参数列表
	 * @return  MapParamBuilder
	 * */
	public MapParamBuilder sets(Object... nvlist)
	{
		
		Map<String,Object> m=MapUtil.asStringKeyMap(nvlist);
		map.putAll(m);
		return this;
	}
	
	/**
	 * 获得map类型的参数 :
	 * Map&lt;String,Object&gt; ps=(new ParamBuilder()).set("a",1).sets("name","leefj","age",38).map();  <br>
	 * @return DAO可用的Map类型参数
	 * */
	public Map<String,Object> map()
	{
		return Collections.unmodifiableMap(map);
	}
}
