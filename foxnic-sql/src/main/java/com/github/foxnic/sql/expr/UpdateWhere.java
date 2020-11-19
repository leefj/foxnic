package com.github.foxnic.sql.expr;

/**
 * @author fangjieli
 * */
public class UpdateWhere extends ConditionExpression<UpdateWhere> 
{
	
	@Override
	protected SQLKeyword getKeyword() {
		return SQLKeyword.WHERE;
	}
	
	
	@Override
	public Update parent() {
		return (Update)super.parent();
	}
	
	
	@Override
	public Update top() {
		return (Update)super.top();
	}
 
}
