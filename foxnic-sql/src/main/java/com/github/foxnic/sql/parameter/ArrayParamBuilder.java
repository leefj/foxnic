package com.github.foxnic.sql.parameter;

import java.util.ArrayList;

/**
 * 数组参数构建器
 * */
public class ArrayParamBuilder {
	
	private ArrayList<Object>  array=new ArrayList<Object>();
	
	/**
	 * 添加参数
	 * @param objects 参数
	 * @return ArrayParamBuilder
	 * */
	public ArrayParamBuilder add(Object...objects)
	{
		for (Object object : objects) {
			this.array.add(object);
		}
		return this;
	}
	
	/**
	 * ParamBuilder pb3=(new ParamBuilder()).add("leefj",10); <br>
	 *	dao.execute("update table set name=? where id=?",pb2.array());
	 *  @return 实际用于DAO方法传入的参数
	 * */
	public Object[] getArray()
	{
		return this.array.toArray();
	}
}
