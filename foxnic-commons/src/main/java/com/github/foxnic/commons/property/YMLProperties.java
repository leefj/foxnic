package com.github.foxnic.commons.property;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.github.foxnic.commons.cache.Variable;

/**
 * 从YML读取配置信息
 * */
public class YMLProperties {
	
	private Map<String,Object> data = null;
	
	public YMLProperties()
	{
		data = new HashMap<String, Object>();
	}
	/**
	 * @param path 文件路径
	 * */
	public YMLProperties(String path)
	{
		Yaml yaml = new Yaml();
        try {
        	data = (Map)yaml.loadAs(new FileInputStream(path),Map.class);
        } catch (Exception e) {
            throw new RuntimeException("文件读取异常："+path,e);
        }
	}
	
	public YMLProperties(File file) {
		this(file.getAbsolutePath());
	}
	
	
	
	/**
	 * 获得属性值
	 * @param name 属性名，层级间用小数点隔开
	 * @return  Variable
	 **/
	public Variable getProperty(String name)
	{
		if(data==null) return new Variable(null) ;
		String[] sp=name.split("\\.");
		Object value=null;
		try {
			value=readProperty(data,sp,0);
		} catch (Exception e) {
			throw new RuntimeException("读取异常",e);
		}
		return new Variable(value);
	}

	
	private Object readProperty(Map<String,Object> map,String[] sp, int i) {
		if(i>=sp.length) return null;
		Object value=map.get(sp[i]);
		if(value instanceof Map)
		{
			return readProperty((Map)value, sp, i+1);
		}
		else
		{
			return value;
		}
	}
	
}
