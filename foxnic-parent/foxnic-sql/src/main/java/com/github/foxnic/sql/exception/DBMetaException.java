package com.github.foxnic.sql.exception;

/**
 * SQL验证异常
 * @author fangjieli
 *
 */
public class DBMetaException extends RuntimeException {
	
	private static final long serialVersionUID = 6165175980794147859L;

	public DBMetaException(String message)
	{
		super(message);
	}
}
