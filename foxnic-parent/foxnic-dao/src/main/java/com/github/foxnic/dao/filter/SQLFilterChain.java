package com.github.foxnic.dao.filter;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.Token;
import com.alibaba.druid.util.JdbcConstants;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.meta.DBMapping;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.exception.SQLValidateException;
import com.github.foxnic.sql.expr.SQL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * SQL过滤器链
 * @author fangjieli
 *
 */
public class SQLFilterChain {

	private DAO dao;
	private DbType dbType=JdbcConstants.MYSQL;
	public SQLFilterChain(DAO dao)
	{
		this.dao=dao;
		dbType=DBMapping.getDruidDBType(dao.getDBType());
	}

	private ArrayList<SQLFilter> chain=new ArrayList<SQLFilter>();

	public void addFilter(SQLFilter filter) {

		for (SQLFilter f : chain) {
			if(f==filter) {
				return;
			}
			if(f.getClass().equals(filter.getClass())) {
				return;
			}
		}

		chain.add(filter);
		filter.setChain(this);
		filter.setDAO(dao);
		sortByPriority();
	}

	private void sortByPriority() {
		Collections.sort(chain, new Comparator<SQLFilter>() {

			@Override
			public int compare(SQLFilter o1, SQLFilter o2) {
				if(o1.getPriority()<o2.getPriority())
				{
					return -1;
				}
				else if(o1.getPriority()>o2.getPriority())
				{
					return 1;
				}
				else
				{
					return 0;
				}
			}});
	}

	private boolean skipFilter(String sql)
	{
		sql=sql.toLowerCase().trim();
		return sql.indexOf("create")==0 || sql.indexOf("alter")==0 || sql.indexOf("drop")==0;
	}

	public SQL doFilter(SQL sql) {
		return doFilter(sql,false);
	}

	public SQL doFilter(SQL sql,boolean validateWhere) {
		String sqlstr=sql.getNamedParameterSQL();
		if(skipFilter(sqlstr)) return sql;

		List<SQLStatement> stmtList = null;
		try {
			stmtList = SQLUtils.parseStatements(sqlstr, dbType);
		} catch (Exception e) {
			//跳过关键字解析异常
			if(e instanceof ParserException) {
				for (Token token : Token.values()) {
					if(e.getMessage().contains("token "+token.name)) {
						return sql;
					}
				}

			}
			 Logger.error("SQL语句解析失败\n:"+sqlstr,e);
		}

		if(stmtList==null) {
			throw new SQLValidateException("SQL语句解析失败:\n"+sqlstr);
		}

		if(stmtList.size()==0) {
			throw new SQLValidateException("未发现有效的SQL语句,"+sqlstr);
		}
		if(stmtList.size()>1) {
			throw new SQLValidateException("不支持同时执行多个SQL语句,"+sqlstr);
		}
		SQLStatement stmt = stmtList.get(0);

		if(validateWhere) {
			validateWhere(stmtList,sqlstr);
		}

		SQLFilterObject sqlobj=new SQLFilterObject(dbType,sql.getSQLDialect());
		sqlobj.setStatement(stmt);
		sqlobj.setSql(sql.getNamedParameterSQL(),sql.getNamedParameters());

		for (SQLFilter filter : chain) {
			sqlobj=filter.doStatementFilter(sqlobj);
			if(stmt instanceof SQLSelectStatement) {
				sqlobj=filter.doSelectFilter(sqlobj);
			} else if(stmt instanceof SQLInsertStatement) {
				sqlobj=filter.doInsertFilter(sqlobj);
			} else if(stmt instanceof SQLUpdateStatement) {
				sqlobj=filter.doUpdateFilter(sqlobj);
			} else if(stmt instanceof SQLDeleteStatement) {
				sqlobj=filter.doDeleteFilter(sqlobj);
			}
		}

		return sqlobj.getSQLObject();

	}

	private void validateWhere(List<SQLStatement> stmtList,String sql) {
		if(this.dao.getDBTreaty().isAllowDeleteWithoutWhere() && this.dao.getDBTreaty().isAllowUpdateWithoutWhere()) {
			return;
		}
		try {
			if (stmtList != null) {
				for (SQLStatement stmt : stmtList) {
					if (!this.dao.getDBTreaty().isAllowDeleteWithoutWhere()) {
						if (stmt instanceof SQLDeleteStatement) {
							SQLDeleteStatement delete = (SQLDeleteStatement) stmt;
							if (delete.getWhere() == null) {
								throw new SQLValidateException("当前执行的语句 "+sql+" , 缺少 where 条件");
							}
						}
					}
					if (!this.dao.getDBTreaty().isAllowDeleteWithoutWhere()) {
						if (stmt instanceof SQLUpdateStatement) {
							SQLUpdateStatement update = (SQLUpdateStatement) stmt;
							if (update.getWhere() == null) {
								throw new SQLValidateException("当前执行的语句 "+sql+" , 缺少 where 条件");
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
