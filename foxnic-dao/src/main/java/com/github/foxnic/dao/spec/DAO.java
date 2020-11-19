package com.github.foxnic.dao.spec;

import java.util.Date;

import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.SQL;

public abstract class DAO {

	public abstract RcdSet query(Expr se);

	public abstract RcdSet query(String sql);

	public abstract RcdSet query(String sql, Object... ps);

	public abstract Long queryLong(SQL  se);

	public abstract String queryString(SQL se);

	public abstract Date queryDate(SQL se);

	public abstract Integer queryInteger(SQL se);

	public abstract Rcd queryRecord(SQL se);

	public abstract RcdSet queryPage(SQL se, int pageSize, int pageIndex);
}
