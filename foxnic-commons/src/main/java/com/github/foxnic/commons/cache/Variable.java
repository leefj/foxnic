package com.github.foxnic.commons.cache;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;

/**
 * 变量，用于包装值，可设置值有效期
 * */
public class Variable  {
 
	private ExpireType expireType=ExpireType.LIVE;
	
	/**
	 * 获得超时类型，默认类型 ExpireType.LIVE
	 * @param  expireType ExpireType 枚举中的类型；
	 * */
	public void setExpireType(ExpireType expireType) {
		if(!isExpire()) {
			this.time=System.currentTimeMillis();
		}
		this.expireType = expireType;
		
	}
	
	/**
	 * 获得超时类型，默认类型 ExpireType.LIVE
	 * @return ExpireType
	 * */
	public ExpireType getExpireType() {
		return expireType;
	}

	private Object value=null;
	
	/**
	 * 是否过期
	 * @return 是否过期
	 * */
	public boolean isExpire() {
		return expire>0 && System.currentTimeMillis()-time>expire;
	}
	/**
	 * 获取值，如果超时，返回 null
	 * @return 值
	 * */
	public Object getValue() {
		if(isExpire()) {
			return null;
		} else
		{
			if(expireType==ExpireType.IDLE) {
				this.time=System.currentTimeMillis();
			}
			return value;
		}
	}
	/**
	 * 设置值
	 * @param value 值
	 * */
	public void setValue(Object value) {
		this.value = value;
		this.time=System.currentTimeMillis();
	}
	
	private long expire=0;

	/**
	 * @return 超时时长，毫秒
	 * */
	public long getExpire() {
		return expire;
	}

	/**
	 * @param expire  超时时间，单位毫秒
	 */
	public void setExpire(long expire) {
		if(expireType==ExpireType.IDLE) {
			this.time=System.currentTimeMillis();
		}
		this.expire = expire;
	}
	
	private long time=0;

	/**
	 * 获得最后一次设置值的时间戳
	 * @return 时间戳
	 * */
	public long getLastSetTime() {
		return time;
	}
	
	/**
	 * @param value 值
	 * @param  expire 超时时间，单位毫秒
	 */
	public Variable(Object value,int expire)
	{
		this.setValue(value);
		this.setExpire(expire);
	}
	
	/**
	 * @param value 值
	 * @param  expire 超时时间，单位毫秒
	 * @param expireType 超时类型
	 */
	public Variable(Object value,int expire,ExpireType expireType)
	{
		this.setValue(value);
		this.setExpire(expire);
		this.setExpireType(expireType);
	}
	
	/**
	 * @param value 值
	 * */
	public Variable(Object value)
	{
		this.setValue(value);
	}
	
	/**
	 * @return 字符串值
	 * */
	public String stringValue()
	{
		return DataParser.parseString(getValue());
	}
	
	/**
	 * @return 整型值
	 * */
	public Integer integerValue()
	{
		return DataParser.parseInteger(getValue());
	}
	
	/**
	 * @return 短整型值
	 * */
	public Short shortValue()
	{
		return DataParser.parseShort(getValue());
	}
	
	/**
	 * @return 长整型值
	 * */
	public Long longValue()
	{
		return DataParser.parseLong(getValue());
	}
	
	/**
	 * @return 浮点型值
	 * */
	public Float floatValue()
	{
		return DataParser.parseFloat(getValue());
	}
	
	/**
	 * @return 双精度型值
	 * */
	public Double doubleValue()
	{
		return DataParser.parseDouble(getValue());
	}
	
	/**
	 * @return BigDecimal型值
	 * */
	public BigDecimal bigDecimalValue()
	{
		return DataParser.parseBigDecimal(getValue());
	}
	
	/**
	 * @return BigInteger型值
	 * */
	public BigInteger bigIntegerValue()
	{
		return DataParser.parseBigInteger(getValue());
	}
	
	/**
	 * @return Boolean[]型值
	 * */
	public Boolean[] booleanArrayValue()
	{
		return booleanArrayValue(null);
	}
	
	/**
	 * @param splitchar 分割符
	 * @return Boolean[]型值
	 * */
	public Boolean[] booleanArrayValue(String splitchar)
	{
		Object raw=this.getValue();
		Boolean[] result = DataParser.parseBooleanArray(raw);
		if(result!=null) {
			return result;
		}
		if(raw instanceof String)
		{
			String str=this.stringValue();
			if(StringUtil.hasContent(splitchar))
			{
				if(str==null) {
					return null;
				}
				return DataParser.parseBooleanArray(str.split(splitchar));
			}
			else
			{
				throw new IllegalArgumentException("缺少 splitchar 参数");
			}
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * @return Boolean型值
	 * */
	public Boolean booleanValue()
	{
		return DataParser.parseBoolean(getValue());
	}
	
	/**
	 * @return Date[] 型值
	 * */
	public Date[] dateArrayValue()
	{
		return dateArrayValue(null);
	}
	
	/**
	 * @return Date[] 型值
	 * @param splitchar 分割符
	 * */
	public Date[] dateArrayValue(String splitchar)
	{
		Object raw=this.getValue();
		Date[] result = DataParser.parseDateArray(raw);
		if(result!=null) {
			return result;
		}
		if(raw instanceof String)
		{
			String str=this.stringValue();
			if(StringUtil.hasContent(splitchar))
			{
				if(str==null) {
					return null;
				}
				return DataParser.parseDateArray(str.split(splitchar));
			}
			else
			{
				throw new IllegalArgumentException("缺少 splitchar 参数");
			}
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * @return  Date型值
	 * */
	public Date dateValue()
	{
		return DataParser.parseDate(getValue());
	}
	
	/**
	 * @return String[] 型值
	 * */
	public String[] stringArrayValue()
	{
		return stringArrayValue(null);
	}
	
	/**
	 * @return String[] 型值
	 * @param splitchar 分割符
	 * */
	public String[] stringArrayValue(String splitchar)
	{
		Object raw=this.getValue();
		String[] result = DataParser.parseStringArray(raw);
		if(result!=null) {
			return result;
		}
		if(raw instanceof String)
		{
			String str=this.stringValue();
			if(StringUtil.hasContent(splitchar))
			{
				if(str==null) {
					return null;
				}
				return str.split(splitchar);
			}
			else
			{
				throw new IllegalArgumentException("缺少 splitchar 参数");
			}
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * @return Integer[] 型值
	 * */
	public Integer[] integerArrayValue()
	{
		return integerArrayValue(null);
	}
	
	/**
	 * @return Integer[] 型值
	 * @param splitchar 分割符
	 * */
	public Integer[] integerArrayValue(String splitchar)
	{
		Object raw=this.getValue();
		Integer[] result = DataParser.parseIntegerArray(raw);
		if(result!=null) {
			return result;
		}
		if(raw instanceof String)
		{
			String str=this.stringValue();
			if(StringUtil.hasContent(splitchar))
			{
				if(str==null) {
					return null;
				}
				return DataParser.parseIntegerArray(str.split(splitchar));
			}
			else
			{
				throw new IllegalArgumentException("缺少 splitchar 参数");
			}
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * @return Double[] 型值
	 * */
	public Double[] doubleArrayValue()
	{
		return doubleArrayValue(null);
	}
	
	/**
	 * @return Double[] 型值
	 * @param splitchar 分割符
	 * */
	public Double[] doubleArrayValue(String splitchar)
	{
		Object raw=this.getValue();
		Double[] result = DataParser.parseDoubleArray(raw);
		if(result!=null) {
			return result;
		}
		if(raw instanceof String)
		{
			String str=this.stringValue();
			if(StringUtil.hasContent(splitchar))
			{
				if(str==null) {
					return null;
				}
				return DataParser.parseDoubleArray(str.split(splitchar));
			}
			else
			{
				throw new IllegalArgumentException("缺少 splitchar 参数");
			}
		}
		else
		{
			return null;
		}
	}
 
	@Override
	public String toString() {
		if(this.name!=null) {
			return this.name+" = " + this.value;
		} else {
			return this.value+"";
		}
		
	}
	
	private String name=null;
	
	/**
	 * 获得变量名称
	 * @return 名称
	 * */
	public String getName() {
		return name;
	}

	/**
	 * 设置变量名称
	 * @param name 名称
	 * */
	public void setName(String name) {
		this.name = name;
	}
	
}
