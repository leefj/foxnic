package com.github.foxnic.commons.lang;

/**
 * @author fangjieli 
 * */
public enum OFBoolean
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
	
	OFBoolean(Boolean b)
	{
		this.value=b;
	}
	
	private static final String T1="ON";
 	
	private static final String F1="OFF";
 
	
	public static OFBoolean parse(String s)
	{
		if(s==null) {
			return OFBoolean.NULL;
		}
		s=s.trim();
		if(s.equalsIgnoreCase(T1))
		{
			return OFBoolean.TRUE;
		}
		else if(s.equalsIgnoreCase(F1))
		{
			return OFBoolean.FALSE;
		}
		else
		{
			return OFBoolean.NULL;
		}
		
	}
	
	public Boolean getValue()
	{
		return value;
	}
}
