package com.github.foxnic.dao.filter;

import java.util.Map;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.SQL;

/**
 * SQL过滤器对象
 * @author fangjieli
 *
 */
public class SQLFilterObject {
	
	public SQLFilterObject(DbType dbType,SQLDialect dialect)
	{
		this.dbType=dbType;
		this.dialect=dialect;
	}
	
	private SQLDialect dialect;
	
	public SQLDialect getDialect() {
		return dialect;
	}

	private DbType dbType;
	
	public DbType getDbType() {
		return dbType;
	}
 
	private SQLStatement statement;
	
	private String sql;
	
	public SQLStatement getStatement() {
		return statement;
	}

	public void setStatement(SQLStatement statement) {
		this.statement = statement;
	}

	public String getSql() {
		return sql;
	}
	
	public SQL getSQLObject() {
		return new Expr(this.sql,this.params);
	}
	
	private Map<String, Object> params=null;

	public Map<String, Object> getParams() {
		return params;
	}

	public void setSql(String sql,Map<String, Object> params) {
		this.sql = sql;
		this.params=params;
	}
	
	public SQLSelectStatement getSelectStatement() {
		if(statement instanceof SQLSelectStatement)
		{
			return (SQLSelectStatement)statement;
		}
		else
		{
			return null;
		}
	}
	
	public SQLInsertStatement getInsertStatement() {
		if(statement instanceof SQLInsertStatement)
		{
			return (SQLInsertStatement)statement;
		}
		else
		{
			return null;
		}
	}
	
	public SQLUpdateStatement getUpdateStatement() {
		if(statement instanceof SQLUpdateStatement)
		{
			return (SQLUpdateStatement)statement;
		}
		else
		{
			return null;
		}
	}
	
	public SQLDeleteStatement getDeleteStatement() {
		if(statement instanceof SQLDeleteStatement)
		{
			return (SQLDeleteStatement)statement;
		}
		else
		{
			return null;
		}
	}
	
}
