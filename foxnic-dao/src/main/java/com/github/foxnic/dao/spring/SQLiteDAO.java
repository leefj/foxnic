package com.github.foxnic.dao.spring;

import java.util.Date;
import java.util.Map;

import com.github.foxnic.dao.data.AbstractSet;
import com.github.foxnic.dao.data.DataResultSetExtractor;
import com.github.foxnic.dao.data.DataRowMapper;
import com.github.foxnic.dao.data.DataSet;
import com.github.foxnic.dao.data.RcdResultSetExtractor;
import com.github.foxnic.dao.data.RcdRowMapper;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.Utils;
import com.github.foxnic.sql.meta.DBType;

/**
 * @author 李方捷
 * */
public class SQLiteDAO extends SpringDAO {
	
	@Override
	protected AbstractSet getPageSet(boolean fixed,AbstractSet set,String sql,int pageIndex,int pageSize,Map<String, Object> params)
	{
		if (pageIndex <= 0) {
			pageIndex = 1;
		}
		int begin = (pageIndex - 1) * pageSize + 1;
		
		params.put("PAGED_QUERY_ROW_BEGIN", new Integer(begin));
		params.put("PAGESIZE", new Integer(pageSize));
		String querySql = "SELECT * FROM( "
				+ sql
				+ " ) PAGED_QUERY LIMIT :PAGED_QUERY_ROW_BEGIN,:PAGESIZE ";
		if(set instanceof RcdSet)
		{
			this.getNamedJdbcTemplate().query(querySql, Utils.filterParameter(params), new RcdResultSetExtractor(new RcdRowMapper((RcdSet)set,begin,this.getQueryLimit())));
		}
		else if(set instanceof DataSet)
		{
			this.getNamedJdbcTemplate().query(querySql,Utils.filterParameter(params), new DataResultSetExtractor(new DataRowMapper((DataSet)set,this.getQueryLimit())));
		}
		Expr se=new Expr(querySql,params);
		se.setSQLDialect(this.getSQLDialect());
		set.setPagedSQL(se);
		return set;
	}
	
	@Override
	protected AbstractSet getPageSet(boolean fixed,AbstractSet set, String sql, int pageIndex,
			int pageSize, Object... params)
	{
		if (pageIndex <= 0) {
			pageIndex = 1;
		}
		int begin = (pageIndex - 1) * pageSize + 1;

		Object[] ps = new Object[params.length + 2];
		System.arraycopy(params, 0, ps, 0, params.length);
		ps[params.length] = begin;
		ps[params.length + 1] = pageSize;
		
		String querySql = "SELECT * FROM( "
		+ sql
		+ " ) PAGED_QUERY LIMIT ?,? ";
		if(set instanceof RcdSet)
		{
//			this.jdbcTemplate.query(querySql, new RcdRowMapper((RcdSet)set),ps);
			this.getJdbcTemplate().query(querySql, Utils.filterParameter(ps), new RcdResultSetExtractor(new RcdRowMapper((RcdSet)set,begin,this.getQueryLimit())));
		}
		else if(set instanceof DataSet)
		{
//			this.jdbcTemplate.query(querySql, new DataRowMapper((DataSet)set),ps);
			this.getJdbcTemplate().query(querySql,Utils.filterParameter(ps), new DataResultSetExtractor(new DataRowMapper((DataSet)set,this.getQueryLimit())));
		}
		Expr se=new Expr(querySql,ps);
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
		return this.queryDate("select datetime('now')");
	}

	@Override
	public DBType getDBType() {
		return DBType.SQLITE;
	}

}
