package com.github.foxnic.commons.lang;

public class ObjectUtil {
	
	
	/**
	 * 判断两个对象是否相等
	 *
	 * @param v1 对象1
	 * @param v2 对象2
	 * @param nullEqual 两个值都为null时是否相等
	 * @return 逻辑值
	 */
	public static boolean equals(Object v1,Object v2,boolean nullsEqual)
	{
		if(v1==null && v2==null) return nullsEqual;
		else if(v1!=null && v2==null) return false;
		else if(v1==null && v2!=null) return false;
		else {
			return v1.equals(v2);
		}
	}
	
	/**
	 * 判断两个对象是否相等，如果两个值都为 null 判为相等
	 *
	 * @param v1 对象1
	 * @param v2 对象2
	 * @return 逻辑值
	 */
	public static boolean equals(Object v1,Object v2)
	{
		return equals(v1, v2,true);
	}
	

}
