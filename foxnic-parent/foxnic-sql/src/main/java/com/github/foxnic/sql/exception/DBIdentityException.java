package com.github.foxnic.sql.exception;

public class DBIdentityException extends DBMetaException {

	public DBIdentityException(String field)
	{
		super(field+" 不是一个有效的数据库标识");
	}
	
}
