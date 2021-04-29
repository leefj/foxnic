package com.github.foxnic.sql.exception;

/**
 * SQL验证异常
 * @author fangjieli
 *
 */
public class SQLValidateException extends RuntimeException {
	
	private static final long serialVersionUID = 6165175980794147859L;

	public SQLValidateException(String message)
	{
		super(message);
	}
}
