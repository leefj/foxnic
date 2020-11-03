package com.github.foxnic.commons.lang;

/**
 * @author fangjieli 
 * */
public enum BITBoolean
{
	/**
	 * True
	 * */
	TRUE(true),
	/**
	 * False
	 * */
	FALSE(false),
	/**
	 * null
	 * */
	NULL(null);
	
	Boolean value;
	
	BITBoolean(Boolean b)
	{
		this.value=b;
	}
	
	private static final String ZERO="0";
 
	
	private static final String ONE="1";
	 
	
	public static BITBoolean parse(String s)
	{
		if(s==null) {
			return BITBoolean.NULL;
		}
		
		if(s.equalsIgnoreCase(ONE))
		{
			return BITBoolean.TRUE;
		}
		else if(s.equalsIgnoreCase(ZERO))
		{
			return BITBoolean.FALSE;
		}
		else
		{
			return BITBoolean.NULL;
		}
		
	}
	
	public Boolean getValue()
	{
		return value;
	}
}
