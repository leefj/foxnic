package com.github.foxnic.sql.exception;

public class NoFieldException extends DBMetaException {

	public NoFieldException(String field)
	{
		super("字段 "+field+" 不存在");
	}
	
}
