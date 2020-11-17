package com.github.foxnic.sql.dao;

import java.util.Date;

import com.github.foxnic.sql.data.AbstractRcd;
import com.github.foxnic.sql.data.AbstractRcdSet;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.SQL;

public abstract class DAO {

	public abstract AbstractRcdSet query(Expr se);

	public abstract AbstractRcdSet query(String sql);

	public abstract AbstractRcdSet query(String sql, Object... ps);

	public abstract Long queryLong(SQL  se);

	public abstract String queryString(SQL se);

	public abstract Date queryDate(SQL se);

	public abstract Integer queryInteger(SQL se);

	public abstract AbstractRcd queryRecord(SQL se);

	public abstract AbstractRcdSet queryPage(SQL se, int pageSize, int pageIndex);
	
}
