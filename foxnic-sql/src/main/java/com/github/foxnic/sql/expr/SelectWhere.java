package com.github.foxnic.sql.expr;

/**
 * @author fangjieli
 * */
public class SelectWhere extends ConditionExpression<SelectWhere> 
{
	
	@Override
	protected SQLKeyword getKeyword() {
		return SQLKeyword.WHERE;
	}
 
	@Override
	public Select parent() {
		return (Select)super.parent();
	}
	
	 
	@Override
	public Select top() {
		return (Select)super.top();
	}
}
