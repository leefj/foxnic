package com.github.foxnic.commons.environment;

/**
 * @author fangjieli 
 * */
public enum OSType {
	/**
	 * Windows系统
	 * */
	WINDOWS, 
	/**
	 * Linux系统
	 * */
	LINUX,
	/**
	 * Unix系统
	 * */
	UNIX,
	/**
	 * MAC
	 * */
	MAC, 
	/**
	 * 无法识别的系统
	 * */
	UNKNOW;

	public static OSType getOSType() {
		String osName = System.getProperties().get("os.name").toString().toUpperCase();
		for(OSType os:OSType.values())
		{
			if(osName.indexOf(os.name().toUpperCase())!=-1) 
			{
				return os;
			}
		}
		return UNKNOW;

	}
	
	public static OSType parse(String str)
	{
		if(str==null) {
			return OSType.UNKNOW;
		}
		for(OSType os:OSType.values())
		{
			if(str.equalsIgnoreCase(os.name()))
			{
				return os;
			}
		}
		return UNKNOW;
	}
	
	
	public static boolean isWindows()
	{
		return OSType.getOSType()==OSType.WINDOWS;
	}
	
	public static boolean isLinux()
	{
		return OSType.getOSType()==OSType.LINUX;
	}
	
	public static boolean isUnix()
	{
		return OSType.getOSType()==OSType.UNIX;
	}
	
	public static boolean isMac()
	{
		return OSType.getOSType()==OSType.MAC;
	}
	
}
