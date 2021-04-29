package com.github.foxnic.commons.bean;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;

import com.github.foxnic.commons.lang.DataParser;

/**
 * 属性比较器，比较、排序、过滤时用
 * @author leefangjie
 * */
public class PropertyComparator implements Comparator<Object> {

	private String property=null;
	private boolean ascending;
	
	public PropertyComparator(String property, boolean ascending)
	{
		this.property=property;
		this.ascending=ascending;
	}
	
	private static int compareNulls(Object v1,Object v2,boolean nullsEqual,boolean ascending)
	{
		int ret=9;
		if (v1 == null && v2 == null) {
			ret = nullsEqual? 0 : -1;
		}
		if (v1 == null && v2 != null) {
			ret =  -1;
		}
		if (v1 != null && v2 == null) {
			ret =  1;
		}
		if(ret!=9) {
			ret=ret * (ascending ? 1:-1);
		}
		return ret;
	}
	
	protected Object getPropertyValue(Object owner,String property)
	{
		return BeanUtil.getFieldValue(owner, property);
	}
 
	/**
	 * 比较两个值
	 * @param v1 v1
	 * @param v2 v2
	 * @param nullsEqual  两个null值是否相等
	 * @param ascending 是否升序，比较值时改值无效
	 * @return  大于返回1，等于返回0，小于返回-1
	 * */
	public static int compareValue(Object v1, Object v2, boolean nullsEqual,boolean ascending) {
 
		int r=compareNulls(v1,v2,nullsEqual,ascending);
		if(r!=9) {
			return r;
		}
		
		int factor=ascending?1:-1;
		
		if (v1 instanceof Comparable && v2 instanceof Comparable) {
			Comparable i1=(Comparable)v1;
			Comparable i2=(Comparable)v2;
			return i1.compareTo(i2) * factor;
		}
		else if (v1 instanceof Integer) {
			Integer i1=DataParser.parseInteger(v1);
			Integer i2=DataParser.parseInteger(v2);
			r=compareNulls(i1,i2,nullsEqual,ascending);
			if(r!=9) {
				return r;
			}
			return i1.compareTo(i2) * factor;
 
		} else if (v1 instanceof Double) {
			Double i1=DataParser.parseDouble(v1);
			Double i2=DataParser.parseDouble(v2);
			r=compareNulls(i1,i2,nullsEqual,ascending);
			if(r!=9) {
				return r;
			}
			return i1.compareTo(i2) * factor;
		} else if (v1 instanceof BigDecimal) {
			
			BigDecimal i1=DataParser.parseBigDecimal(v1);
			BigDecimal i2=DataParser.parseBigDecimal(v2);
			r=compareNulls(i1,i2,nullsEqual,ascending);
			if(r!=9) {
				return r;
			}
			return i1.compareTo(i2) * factor;
			
		}  else if (v1 instanceof Date) {
			Date i1=DataParser.parseDate(v1);
			Date i2=DataParser.parseDate(v2);
			r=compareNulls(i1,i2,nullsEqual,ascending);
			if(r!=9) {
				return r;
			}
			return i1.compareTo(i2) * factor;
		}
		else if (v1 instanceof String) {
			String i1=DataParser.parseString(v1);
			String i2=DataParser.parseString(v2);
			r=compareNulls(i1,i2,nullsEqual,ascending);
			if(r!=9) {
				return r;
			}
			return i1.compareTo(i2)  * factor;
		}
		else {
			throw new RuntimeException("不支持的类型:"+v1.getClass().getName());
		}
	}
	
	
	@Override
	public int compare(Object o1, Object o2) {
		Object v1=getPropertyValue(o1, property);
		Object v2=getPropertyValue(o2, property);
		return compareValue(v1, v2,false,this.ascending);
	}
 
}
