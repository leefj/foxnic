package com.github.foxnic.sql.expr;

/**
 * having 子句
 * @author fangjieli
 *
 */
public class Having extends ConditionExpression<Having>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4287620601894417415L;

	@Override
	protected SQLKeyword getKeyword() {
		return SQLKeyword.GROUP$HAVING;
	}
	
	
	@Override
	public GroupBy parent()
	{
		return (GroupBy)super.parent();
	}
	
	/**
	 * 返回顶层语句
	 * */
	@Override
	public Select top()
	{
		return (Select)super.top();
	}
	
	

}
