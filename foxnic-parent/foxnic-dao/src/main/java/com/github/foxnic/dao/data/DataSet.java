package com.github.foxnic.dao.data;

import java.io.Serializable;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.sql.exception.NoFieldException;

 
/**
 * 用列结构存放数据
 * @author 李方捷
 * */
public class DataSet extends AbstractSet implements Serializable {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = -7941977234014586566L;
	
	
	public static final String TYPE_TIMESTAMP = "java.sql.Timestamp";
	public static final String TYPE_BIGDECIMAL = "java.math.BigDecimal";
	public static final String TYPE_STRING = "java.lang.String";
	public static final String TYPE_LONG = "java.lang.Long";
	public static final String TYPE_DOUBLE = "java.lang.Double";
	public static final String TYPE_INTEGER = "java.lang.Integer";
	
	
	protected ArrayList<Object> data=new ArrayList<Object>();
	protected QueryMetaData metaData = null;
	@Override
	protected QueryMetaData getRawMetaData()
	{
		return metaData;
	}
	@Override
	protected void setRawMetaData(QueryMetaData meta)
	{
		this.metaData=meta;
	}
	
	private boolean returnPrimitiveValue=false;
 
	/**
	 * 结果是否返回简单类型
	 * @return 逻辑值
	 * */
	public boolean isReturnPrimitiveValue() {
		return returnPrimitiveValue;
	}

	/**
	 * @param size 大小
	 * @param returnPrimitiveValue 是否简单类型
	 * */
	public DataSet(int size,boolean returnPrimitiveValue){
		//适当扩容
		this.size=size; 
		this.returnPrimitiveValue=returnPrimitiveValue;
	}
	
	private int size=0; 
	
	@Override
	public int size()
	{
		return size;
	}
	
	
	void setRealSizeInternal(int size)
	{
		this.size=size;
	}
	
	
	
	@Override
	protected void initeMetaData(ResultSetMetaData meta) throws SQLException {
		super.initeMetaData(meta);
		String type=null;
		
		for (int i = 0; i <metaData.getColumnCount() ; i++) {
			//需要创建指定类型的数组
			type=this.metaData.getColumnClassName(i);
			data.add(createColumnArray(type));
		}
	}
	
	
	
	private Object createColumnArray(String type)
	{
		if(TYPE_LONG.equals(type))
		{
			if(returnPrimitiveValue)
			{
				return new long[this.size];
			}
			else
			{
				return new Long[this.size];
			}
		}
		else if(TYPE_STRING.equals(type))
		{
			return new String[this.size];
		}
		else if(TYPE_INTEGER.equals(type))
		{
			if(returnPrimitiveValue)
			{
				return new int[this.size];
			}
			else
			{
				return new Integer[this.size];
			}
		}
		else if(TYPE_BIGDECIMAL.equals(type) || TYPE_DOUBLE.equals(type))
		{
			if(returnPrimitiveValue)
			{
				return new double[this.size];
			}
			else
			{
				return new Double[this.size];
			}
		}
		else if(TYPE_TIMESTAMP.equals(type))
		{
			return new Date[this.size];
		}
		
		return new Object[this.size];
	}
	
	void addValueInternal(int row,int column,Object value)
	{
		Object columnData=data.get(column);
		if(columnData instanceof Date[])
		{
			((Date[])columnData)[row]=(Date)value;
		}
		else if(columnData instanceof double[])
		{
			if(value!=null) {
				((double[])columnData)[row]=(double)value;
			}
		}
		else if(columnData instanceof Double[])
		{
			((Double[])columnData)[row]=(Double)value;
		}
		else if(columnData instanceof Integer[])
		{
			((Integer[])columnData)[row]=(Integer)value;
		}
		else if(columnData instanceof int[])
		{
			if(value!=null) {
				((int[])columnData)[row]=((Integer)value).intValue();
			}
		}
		
		else if(columnData instanceof Long[])
		{
			((Long[])columnData)[row]=(Long)value;
		}
		else if(columnData instanceof long[])
		{
			if(value!=null) {
				((long[])columnData)[row]=((Long)value).longValue();
			}
		}
		
		else if(columnData instanceof String[])
		{
			((String[])columnData)[row]=(String)value;
		}
	}
	
	/**
	 * 得到通过 field和data描述的JSON数据
	 * @return JSONObject
	 * */
	public JSONObject toJson()
	{
		JSONObject json=new JSONObject();
		json.put("fields",metaData.getColumnLabels());
		json.put("data",this.data);
		return json;
	}
	
	/**
	 * 获得列值
	 * @param index 列顺序
	 * @return 列值
	 * */
	public Object[] getValues(int index)
	{
		return (Object[])this.data.get(index);
	}
	
	/**
	 * 获得列值
	 * @param name 列名
	 * @return 列值
	 * */
	public Object[] getValues(String name)
	{
		int index=this.metaData.name2index(name);
		if(index==-1) {
			throw new NoFieldException(name);
		}
		return (Object[])this.data.get(index);
	}
	 
	/**
	 * 获得Date类型列值
	 * @param index 列顺序
	 * @return 列值
	 * */
	public Date[] getDates(int index)
	{
		return (Date[])this.data.get(index);
	}
	
	/**
	 * 获得Date类型列值
	 * @param name 字段名
	 * @return 列值
	 * */
	public Date[] getDates(String name)
	{
		int index=this.metaData.name2index(name);
		if(index==-1) {
			throw new NoFieldException(name);
		}
		return getDates(index);
	}
	
	/**
	 * 获得String类型列值
	 * @param index 列顺序
	 * @return 列值
	 * */
	public String[] getStrings(int index)
	{
		return (String[])this.data.get(index);
	}
	
	/**
	 * 获得String类型列值
	 * @param name 字段名
	 * @return 列值
	 * */
	public String[] getStrings(String name)
	{
		int index=this.metaData.name2index(name);
		if(index==-1) {
			throw new NoFieldException(name);
		}
		return getStrings(index);
	}
	
	/**
	 * 获得非装箱的简单列值(double)
	 * @param index 列顺序
	 * @return 列值
	 * */
	public double[] getPrimitiveDoubles(int index)
	{
		try {
			return (double[])this.data.get(index);
		} catch (Exception e) {
			Logger.error("请在查询时指定 returnPrimitiveValue 为 true",e);
			return null;
		}
	}
	
	/**
	 * 获得非装箱的简单列值(double)
	 * @param name 列名
	 * @return 列值
	 * */
	public double[] getPrimitiveDoubles(String name)
	{
		int index=this.metaData.name2index(name);
		if(index==-1) {
			throw new NoFieldException(name);
		}
		return getPrimitiveDoubles(index);
	}
 
	/**
	 * 获得装箱的列值(Double)
	 * @param index 列序号
	 * @return 列值
	 * */
	public Double[] getDoubles(int index)
	{
		return (Double[])this.data.get(index);
	}
	
	/**
	 * 获得装箱的列值(Double)
	 * @param name 列名
	 * @return 列值
	 * */
	public Double[] getDoubles(String name)
	{
		int index=this.metaData.name2index(name);
		if(index==-1) {
			throw new NoFieldException(name);
		}
		return getDoubles(index);
	}
	
	 
	
	
	/**
	 * 获得装箱的列值(Integer)
	 * @param index 列序号
	 * @return 列值
	 * */
	public Integer[] getIntegers(int index)
	{
//		if(this.data.get(index) instanceof Integer[])
//		{
		try {
			return (Integer[])this.data.get(index);
		} catch (Exception e) {
			Object[] arr=(Object[])this.data.get(index);
			Integer[] integers=new Integer[arr.length];
			for (int i = 0; i < arr.length; i++) {
				integers[i]=DataParser.parseInteger(arr[i]);
			}
			return integers;
		}
//		}
	}
	
	/**
	 * 获得装箱的列值(Integer)
	 * @param name 列名
	 * @return 列值
	 * */
	public Integer[] getIntegers(String name)
	{
		int index=this.metaData.name2index(name);
		if(index==-1) {
			throw new NoFieldException(name);
		}
		return getIntegers(index);
	}
	
	
	/**
	 * 获得非装箱的列值(int)
	 * @param index 列序号
	 * @return 列值
	 * */
	public int[] getPrimitiveInts(int index)
	{
		try {
			return (int[])this.data.get(index);
		} catch (Exception e) {
			//尝试double
			double[] arr=(double[])this.data.get(index);
			int[] integers=new int[arr.length];
			for (int i = 0; i < arr.length; i++) {
				integers[i]=(int)arr[i];
			}
			return integers;
		}
	}
	
	/**
	 * 获得非装箱的列值(int)
	 * @param name 列名
	 * @return 列值
	 * */
	public int[] getPrimitiveInts(String name)
	{
		int index=this.metaData.name2index(name);
		if(index==-1) {
			throw new NoFieldException(name);
		}
		return getPrimitiveInts(index);
	}
	
	
	/**
	 * 获得装箱的列值(Long)
	 * @param index 列序号
	 * @return 列值
	 * */
	public Long[] getLongs(int index)
	{
		return (Long[])this.data.get(index);
	}
	
	/**
	 * 获得装箱的列值(Long)
	 * @param name 列名
	 * @return 列值
	 * */
	public Long[] getLongs(String name)
	{
		int index=this.metaData.name2index(name);
		if(index==-1) {
			throw new NoFieldException(name);
		}
		return getLongs(index);
	}
	
	
	/**
	 * 获得非装箱的列值(long)
	 * @param index 列序号
	 * @return 列值
	 * */
	public long[] getPrimitiveLongs(int index)
	{
		return (long[])this.data.get(index);
	}
	
	/**
	 * 获得非装箱的列值(long)
	 * @param name 列名
	 * @return 列值
	 * */
	public long[] getPrimitiveLongs(String name)
	{
		int index=this.metaData.name2index(name);
		return getPrimitiveLongs(index);
	}
 
	
}


