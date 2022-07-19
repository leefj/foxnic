package com.github.foxnic.sql.expr;

import java.util.List;

import com.github.foxnic.commons.lang.StringUtil;

/**
 * 条件表达式
 * @author fangjieli
 *
 */
public class ConditionExpr extends ConditionExpression<ConditionExpr> implements SQL,WhereWapper {

	/**
	 *
	 */
	private static final long serialVersionUID = 389424364154666679L;

	/**
	 * 创建一个空的CE
	 * @return CE
	 * */
	public static ConditionExpr create()
	{
		return new ConditionExpr();
	}

	/**
	 * 将值转换为  %value%,如果无效的like表达式则返回null
	 * @param value 值
	 * @return 字符串
	 * */
	public static String valueOfLike(Object value)
	{
		if(value==null) return null;
		String str = pureValue4Like(value);
		return StringUtil.hasContent(str)? "%"+str+"%":null;
	}

	/**
	 * 将值转换为  value%,如果无效的like表达式则返回null
	 * @param value 值
	 * @return 字符串
	 * */
	public static String valueOfLeftLike(Object value)
	{
		if(value==null) return null;
		String str = pureValue4Like(value);
		return StringUtil.hasContent(str)? str+"%":null;
	}

	/**
	 * 将值转换为  %value,如果无效的like表达式则返回null
	 * @param value 值
	 * @return 字符串
	 * */
	public static String valueOfRightLike(Object value)
	{
		if(value==null) return null;
		String str = pureValue4Like(value);
		return StringUtil.hasContent(str)? "%"+str:null;
	}

	private static String pureValue4Like(Object value) {
		String str=value.toString();
		str=str.trim();
		str=StringUtil.removeFirst(str, "%");
		str=StringUtil.removeLast(str, "%");
		return str;
	}

	/**
	 * 构造器
	 * */
	public ConditionExpr() {}

	/**
	 * 构造器
	 * @param se 条件表达式，SQL语句
	 * */
	public ConditionExpr(Expr se)
	{
		if(!se.isEmpty())
		{
			and(se);
		}
	}
	/**
	 * 构造器
	 * @param se 条件表达式，SQL语句，可带问号占位符
	 * @param ps 参数，se中有几个问号就有几个参数
	 * */
	public ConditionExpr(String se,Object... ps)
	{
		if(!StringUtil.isBlank(se)) {
			and(Expr.create(se,ps));
		}
	}




	/**
	 * 从多行语句生成 SE
	 * */
	public static ConditionExpr fromLines(String[] lines,Object... params)
	{
		return new ConditionExpr(SQL.joinSQLs(lines),params);
	}

	/**
	 * 从多行语句生成 SE
	 * */
	public static ConditionExpr fromLines(List<String> lines,Object... params)
	{
		return new ConditionExpr(SQL.joinSQLs(lines),params);
	}



}
