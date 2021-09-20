package com.github.foxnic.sql.parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

 


/**
 * 批量参数构建器
 * */
public class BatchParamBuilder {
	
	private ArrayList<Object[]>  list=new ArrayList<Object[]>();
	
	public BatchParamBuilder() {}
	
	/**
	 * 参数个数
	 * @return 个数
	 * */
	public int size()
	{
		return list.size();
	}

	/**
	 * 是否有条目
	 * @return 个数
	 * */
	public boolean hasItem()
	{
		return !list.isEmpty();
	}
	
 
	/**
	 * 添加参数
	 * @param objects 参数
	 * @return ArrayParamBuilder
	 * */
	public BatchParamBuilder add(Object...objects)
	{
		if(objects==null)
		{
			throw new IllegalArgumentException("参数不允许为空");
		}
		
		if(objects.length==0)
		{
			throw new IllegalArgumentException("请指定至少1个参数");
		}
		
		if(list.size()==0)
		{
			list.add(objects);
		}
		else
		{
			if(objects.length!=list.get(0).length)
			{
				throw new IllegalArgumentException("加入的参数与之前的参数个数不一致");
			}
			else
			{
				list.add(objects);
			}
		}
		return this;
	}
	/**
	 * get batch parameter list
	 * @return DAO可用的参数
	 * */
	public List<Object[]> getBatchList()
	{
		return Collections.unmodifiableList(list);
	}
	
	/**
	 * clear all parameters
	 * */
	public void clear()
	{
		this.list.clear();
	}
 
}
