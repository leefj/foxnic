package com.github.foxnic.sql.expr;

import java.util.List;

/**
 * @author fangjieli
 * */
public class Where extends ConditionExpression<Where> implements SQL
{
	public Where(String se,Object... ps)
	{
		super(se,ps);
	}
	
	public Where(Expr se)
	{
		super(se);
	}
	
	public Where()
	{}
	
	@Override
	protected SQLKeyword getKeyword() {
		return SQLKeyword.WHERE;
	}
	
	/**
	 * 从多行语句生成 SE 
	 * */
	public static Where fromLines(String[] lines,Object... params) 
	{
		return new Where(SQL.joinSQLs(lines),params);
	}
	
	/**
	 * 从多行语句生成 SE 
	 * */
	public static Where fromLines(List<String> lines,Object... params)
	{
		return new Where(SQL.joinSQLs(lines),params);
	}
}

 



