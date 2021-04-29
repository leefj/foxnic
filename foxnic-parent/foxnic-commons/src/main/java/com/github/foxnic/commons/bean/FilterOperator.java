package com.github.foxnic.commons.bean;

import com.github.foxnic.commons.lang.DataParser;

/**
 * 过滤比较操作符，与BeanUtil.filter以及TitySQL中的RcdSet.filter结合使用
 * @author leefangjie
 * */
public abstract class FilterOperator {
	
	public abstract boolean compare(Object a,Object b);
	private FilterOperator reverseOperator=null;
	/**
	 * 取反
	 * @return 取反后的FilterOperator
	 * */
	public FilterOperator reverse()
	{
		if(reverseOperator!=null) {
			return reverseOperator;
		}
		FilterOperator me=this;
		reverseOperator=new FilterOperator() {
			@Override
			public boolean compare(Object a, Object b) {
				return !me.compare(a, b);
			}
		};
		return reverseOperator;
	}
 
	/**
	 * 比较相等，如果两个值都为null，则不相等
	 * */
	public static final FilterOperator EQUALS=new Equals(true);
	
	/**
	 * 大于
	 * */
	public static final FilterOperator GREATER_THAN=new GreaterThan();
	
	/**
	 * 小于
	 * */
	public static final FilterOperator LESS_THAN=new LessThan();
	
	/**
	 * 大于等于
	 * */
	public static final FilterOperator EQUAL_OR_GREATER_THAN=new EqualOrGreaterThan();
	
	/**
	 * 小于等于
	 * */
	public static final FilterOperator EQUAL_OR_LESS_THAN=new EqualOrLessThan();
	
	/**
	 * 字符串包含
	 * */
	public static final FilterOperator CONTAINS=new Contains();

	
	public static class Equals extends FilterOperator
	{
		boolean nullsEqual=false;
		
		
		
		public Equals(boolean nullsEqual)
		{
			this.nullsEqual=nullsEqual;
		}
		
		@Override
		public boolean compare(Object a, Object b) {
			if(nullsEqual) {
				if(a==null && b==null) {
					return true;
				}
			}
			if(a!=null && b!=null && a.equals(b)) {
				return true;
			}
			return false;
		}
	}
 
	public static class GreaterThan extends FilterOperator
	{
		@Override
		public boolean compare(Object a, Object b) {
			int i=PropertyComparator.compareValue(a, b,false,true);
			return i==1;
		}
	}
	
	public static class LessThan extends FilterOperator
	{
		@Override
		public boolean compare(Object a, Object b) {
			int i=PropertyComparator.compareValue(a, b,false,true);
			return i==-1;
		}
	}
	
	
	public static class EqualOrGreaterThan extends FilterOperator
	{
		@Override
		public boolean compare(Object a, Object b) {
			int i=PropertyComparator.compareValue(a, b,false,true);
			return i>=0;
		}
	}
	
	
	public static class EqualOrLessThan extends FilterOperator
	{
		@Override
		public boolean compare(Object a, Object b) {
			int i=PropertyComparator.compareValue(a, b,false,true);
			return i<=0;
		}
	}
	
	public static class Contains extends FilterOperator
	{
		@Override
		public boolean compare(Object a, Object b) {
			 String as=DataParser.parseString(a);
			 String bs=DataParser.parseString(b);
			 if(as==null || bs==null) {
				return false;
			}
			 return as.contains(bs);
		}
	}
	
	
	
	
	

}
