package com.github.foxnic.sql.expr;

public interface WhereWrapper extends SQL {
	
	/**
	 * 将当前条件表达式转成Where，直接用于语句拼接
	 * */
	public default Where where()
	{
		Where wh = new Where(this.getListParameterSQL(),this.getListParameters());
		return wh;
	}

}
