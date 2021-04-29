package com.github.foxnic.sql.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.data.ExprDAO;
import com.github.foxnic.sql.dialect.SQLDialect;

/**
 * 删除语句
 * 
 * @author fangjieli
 *
 */
public class Delete extends DML implements ExecutableSQL {

	private String table = null;

	public String getTable() {
		return table;
	}

	private String tableAlias = null;
	private DeleteWhere where = new DeleteWhere();

	public static Delete init() {
		return new Delete();
	}

	public DeleteWhere where() {
		return this.where;
	}

	public DeleteWhere where(String ce, Object... ps) {
		return this.where.and(ce, ps);
	}

	public Delete() {
		this.where.setParent(this);
	}

	public Delete(String table) {
		this();
		this.from(table);
	}

	public Delete from(String table, String alias) {
		Utils.validateDBIdentity(table);
		Utils.validateDBIdentity(alias);
		this.table = table;
		this.tableAlias = alias;
		return this;
	}

	public Delete from(String table) {
		Utils.validateDBIdentity(table);
		this.table = table;
		this.tableAlias = null;
		return this;
	}

	@Override
	public String getSQL(SQLDialect dialect) {
		if (this.isEmpty()) {
			return "";
		}
		SQLStringBuilder sql = new SQLStringBuilder();
		sql.append(SQLKeyword.DELETE, SQLKeyword.FROM).append(this.table);
		if (this.tableAlias != null) {
			sql.append(this.tableAlias);
		}
		if (!this.where().isEmpty()) {
			where().setIgnorColon(ignorColon);
			sql.append(this.where().getSQL(dialect));
		}
		return sql.toString();
	}

	@Override
	public String getListParameterSQL() {
		if (this.isEmpty()) {
			return "";
		}
		SQLStringBuilder sql = new SQLStringBuilder();
		sql.append(SQLKeyword.DELETE, SQLKeyword.FROM).append(this.table);
		if (this.tableAlias != null) {
			sql.append(this.tableAlias);
		}
		if (!this.where().isEmpty()) {
			where().setIgnorColon(ignorColon);
			sql.append(this.where().getListParameterSQL());
		}
		return sql.toString();
	}

	@Override
	public Object[] getListParameters() {
		if (this.isEmpty()) {
			return new Object[] {};
		}
		ArrayList<Object> ps = new ArrayList<Object>();
		if (!this.where().isEmpty()) {
			where().setIgnorColon(ignorColon);
			ps.addAll(Utils.toList(this.where().getListParameters()));
		}
		return ps.toArray(new Object[ps.size()]);
	}

	@Override
	public String getNamedParameterSQL() {
		if (this.isEmpty()) {
			return "";
		}
		this.beginParamNameSQL();
		SQLStringBuilder sql = new SQLStringBuilder();
		sql.append(SQLKeyword.DELETE, SQLKeyword.FROM).append(this.table);
		if (this.tableAlias != null) {
			sql.append(this.tableAlias);
		}
		if (!this.where().isEmpty()) {
			where().setIgnorColon(ignorColon);
			sql.append(this.where().getNamedParameterSQL());
		}
		this.endParamNameSQL();
		return sql.toString();
	}

	@Override
	public Map<String, Object> getNamedParameters() {
		HashMap<String, Object> ps = new HashMap<String, Object>();
		if (this.isEmpty()) {
			return ps;
		}
		this.beginParamNameSQL();
		if (!this.where().isEmpty()) {
			where().setIgnorColon(ignorColon);
			ps.putAll(this.where().getNamedParameters());
		}
		this.endParamNameSQL();
		return ps;
	}

	/**
	 * 判断是否为空 表名无内容(空白或null) , 判定为 empty
	 * 
	 * @return 是否空
	 */
	@Override
	public boolean isEmpty() {
		return StringUtil.noContent(this.table);
	}

	@Override
	public boolean isAllParamsEmpty() {
		where().setIgnorColon(ignorColon);
		return this.where().isAllParamsEmpty();
	}

	private ExprDAO dao = null;

	/**
	 * 使用 setDAO()方法指定的DAO执行当前语句
	 * 
	 * @return 执行结果，行数
	 */
	@Override
	public Integer execute() {
		return getDAO().execute(this);
	}

	@Override
	public ExprDAO getDAO() {
		return dao;
	}

	@Override
	public Delete setDAO(ExprDAO dao) {
		this.dao = dao;
		return this;
	}

}
