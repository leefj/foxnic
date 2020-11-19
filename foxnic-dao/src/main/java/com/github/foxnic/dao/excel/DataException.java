package com.github.foxnic.dao.excel;

/**
 * 数据异常
 * @author fangjieli
 *
 */
public class DataException extends RuntimeException {
	
	private static final long serialVersionUID = 6165175980794147859L;

	public DataException(String message)
	{
		super(message);
	}
}
