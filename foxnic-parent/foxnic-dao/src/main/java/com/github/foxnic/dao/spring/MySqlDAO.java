package com.github.foxnic.dao.spring;

import com.github.foxnic.dao.data.*;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.Utils;
import com.github.foxnic.sql.meta.DBType;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.util.Date;
import java.util.Map;



public class MySqlDAO extends SpringDAO {

	public DBType getDBType() {
		return DBType.MYSQL;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected AbstractSet getPageSet(boolean fixed,AbstractSet set,String sql,int pageIndex,int pageSize,Map<String, Object> params)
	{
		if (pageIndex <= 0) {
			pageIndex = 1;
		}
		int begin = (pageIndex - 1) * pageSize ;

		params.put("PAGED_QUERY_ROW_BEGIN", new Integer(begin));
		params.put("PAGESIZE", new Integer(pageSize));
		String querySql = "SELECT * FROM ( "
				+ sql
				+ " ) PAGED_QUERY LIMIT :PAGED_QUERY_ROW_BEGIN,:PAGESIZE ";

		params=Utils.filterParameter(params);


		if(this.isPrintSQL()) {
			Expr se=new Expr(querySql,params);
			new Expr(querySql,params);
			new SQLPrinter<Integer>(this,se,se) {
				@Override
				protected Integer actualExecute() {
					return 0;
				}
			}.execute();
		}


		if(set instanceof RcdSet)
		{
			this.getNamedJdbcTemplate().query(querySql, params, new RcdResultSetExtractor(new RcdRowMapper((RcdSet)set,begin,this.getQueryLimit())));
		}
		else if(set instanceof DataSet)
		{
			this.getNamedJdbcTemplate().query(querySql,params, new DataResultSetExtractor(new DataRowMapper((DataSet)set,this.getQueryLimit())));
		}
		Expr se=new Expr(querySql,params);
		se.setSQLDialect(this.getSQLDialect());
		set.setPagedSQL(se);
		return set;
	}


	@SuppressWarnings("unchecked")
	@Override
	protected AbstractSet getPageSet(boolean fixed,AbstractSet set, String sql, int pageIndex,
			int pageSize, Object... params)
	{
		if (pageIndex <= 0) {
			pageIndex = 1;
		}
		int begin = (pageIndex - 1) * pageSize ;

		Object[] ps = new Object[params.length + 2];
		System.arraycopy(params, 0, ps, 0, params.length);
		ps[params.length] = begin;
		ps[params.length + 1] = pageSize;

		String querySql = "SELECT * FROM ( "
		+ sql
		+ " ) PAGED_QUERY LIMIT ?,? ";
		final Object[] ps1=Utils.filterParameter(ps);

		PreparedStatementSetter setter = new ArgumentPreparedStatementSetter(ps1);
		final MySqlDAO me=this;

		final  boolean isRcdSet=(set instanceof RcdSet);

		if(this.isPrintSQL()) {
			Expr se=new Expr(querySql,ps1);
			new SQLPrinter<AbstractSet>(this,se,se) {
				@Override
				protected AbstractSet actualExecute() {
					if(isRcdSet) {
						me.getJdbcTemplate().query(querySql, setter, new RcdResultSetExtractor(new RcdRowMapper((RcdSet)set,begin,me.getQueryLimit())));
					} else {
						me.getJdbcTemplate().query(querySql,setter, new DataResultSetExtractor(new DataRowMapper((DataSet)set,me.getQueryLimit())));
					}
					return set;
				}
			}.execute();
		} else {
			if(isRcdSet) {
				this.getJdbcTemplate().query(querySql, setter, new RcdResultSetExtractor(new RcdRowMapper((RcdSet) set, begin, this.getQueryLimit())));
			} else {
				this.getJdbcTemplate().query(querySql, setter, new DataResultSetExtractor(new DataRowMapper((DataSet) set, this.getQueryLimit())));
			}
		}

		Expr se = new Expr(querySql, ps1);
		se.setSQLDialect(this.getSQLDialect());
		set.setPagedSQL(se);
		return set;
	}





	@Override
	protected String getSchema(String url)
	{
		int a,b;
		a=url.indexOf("//");
		a=url.indexOf("/",a+2);
		b=url.indexOf("?");
		if(b==-1) {
			b=url.length();
		}
		String schema=url.substring(a+1,b);
		return schema;
	}

	@Override
	public Date getDateTime()
	{
		return this.queryDate("select now()");
	}

	@Override
	public DataSourceTransactionManager getTransactionManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTransactionManager(DataSourceTransactionManager transactionManager) {
		// TODO Auto-generated method stub

	}




}
