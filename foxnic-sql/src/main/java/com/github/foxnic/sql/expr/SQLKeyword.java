package com.github.foxnic.sql.expr;

/**
 * 
 * @author fangjieli
 *
 */
public enum SQLKeyword
{
	SELECT("SELECT"),
	FROM("FROM"),
	WHERE("WHERE"),
	ORDER("ORDER"),
	ORDER$BY("BY"),
	GROUP("GROUP"),
	GROUP$BY("BY"),
	GROUP$HAVING("HAVING"),
	UPDATE("UPDATE"),
	UPDATE$SET("SET"),
	INSERT("INSERT"),
	DELETE("DELETE"),
	INSERT$INTO("INTO"),
	VALUES("VALUES"),
	DISTINCT("DISTINCT"),
	UNION("UNION"),
	UNION$ALL("ALL"), 
	MINUS("MINUS"), 
	INTERSECT("INTERSECT"), 
	SINGLE_QUATE("'"), 
	BACK_QUATE("`"),
	LEFT_BACK_QUATE("`"),
	RIGHT_BACK_QUATE("`"),
	LEFT_SINGLE_QUATE("'"), 
	RIGHT_SINGLE_QUATE("'"), 
	
	DOUBLE_QUATE("\""), 
	LEFT_DOUBLE_QUATE("\""), 
	RIGHT_DOUBLE_QUATE("\""),
	 
	RIGHT_BRACKET(")"),
	LEFT_BRACKET("("),
	SPACER(" "),
	AS("AS"),
	OP$EAUALS("="),
	OP$PLUS("+"),
	OP$MINUS("-"),
	OP$DIVIDE("/"),
	OP$MULTIPLY("*"),
	BETWEEN("between"),
//	OP$STRING_CONCAT("||"), 不通用
	SELECT_STAR("*"),
	COMMA(","),
	RIGHT_REMARK("*/"),
	LEFT_REMARK("/*"),
	SINGLE_REMARK("--"),
	HASHTAG("#"),
	LN("\n"),
	REVERT("\r"),
	CASE("CASE"),
	CASE$END("END"),
	CASE$WHEN("WHEN"),
	DOT("."),
	
	AND("AND"),
	OR("OR"),
	QUESTION("?"),
	COLON (":"),
	LAST("LAST"),
	FIRST("FIRST"),
	ASC("ASC"),
	DESC("DESC"),
	NULLS("NULLS"),
	NULL("NULL"),
	NOT("NOT"),
	IN("IN");
	
	private String keyword;

	private SQLKeyword(String kw)
	{
		this.keyword=kw;
	}
	@Override
	public String toString()
	{
		return this.keyword;
	}
	
	public int length()
	{
		return this.keyword.length();
	}
}

