package com.github.foxnic.dao.exception;

public class TransactionException extends RuntimeException {

	private static final long serialVersionUID = -2347114750554166191L;

	public TransactionException(String message)
	{
		super(message);
	}
}
