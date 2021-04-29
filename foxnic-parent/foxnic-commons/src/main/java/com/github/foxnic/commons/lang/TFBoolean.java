package com.github.foxnic.commons.lang;

/**
 * @author fangjieli 
 * */
public enum TFBoolean
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
	
	TFBoolean(Boolean b)
	{
		this.value=b;
	}
	
	private static final String T1="T";
	private static final String T2="TRUE";
	
	private static final String F1="F";
	private static final String F2="FALSE";
	
	public static TFBoolean parse(String s)
	{
		if(s==null) {
			return TFBoolean.NULL;
		}
		s=s.trim();
		if(s.equalsIgnoreCase(T1) || s.equalsIgnoreCase(T2))
		{
			return TFBoolean.TRUE;
		}
		else if(s.equalsIgnoreCase(F1) || s.equalsIgnoreCase(F2))
		{
			return TFBoolean.FALSE;
		}
		else
		{
			return TFBoolean.NULL;
		}
		
	}
	
	public Boolean getValue()
	{
		return value;
	}
}
