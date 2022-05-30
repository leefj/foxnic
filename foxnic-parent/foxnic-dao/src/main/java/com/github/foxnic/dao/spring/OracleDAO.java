package com.github.foxnic.dao.spring;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementSetter;

import com.github.foxnic.dao.data.AbstractSet;
import com.github.foxnic.dao.data.DataResultSetExtractor;
import com.github.foxnic.dao.data.DataRowMapper;
import com.github.foxnic.dao.data.DataSet;
import com.github.foxnic.dao.data.RcdResultSetExtractor;
import com.github.foxnic.dao.data.RcdRowMapper;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.meta.lob.IClobDAO;
import com.github.foxnic.dao.meta.lob.OracleClobDAO;
import com.github.foxnic.dao.meta.DBMapping;
import com.github.foxnic.dao.sql.SQLParser;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.Utils;
import com.github.foxnic.sql.meta.DBType;

public class OracleDAO extends SpringDAO {

	public DBType getDBType() {
		return DBType.ORACLE;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected AbstractSet getPageSet(boolean fixed,AbstractSet set,String sql,int pageIndex,int pageSize,Map<String, Object> params)
	{

		boolean isDual=isDualTable(sql);
		String querySql=null;
		int begin=0;
		if(isDual) {
			querySql=sql;
		} else {
			if (pageIndex <= 0) {
				pageIndex = 1;
				params=Utils.filterParameter(params);
			}
			begin = (pageIndex - 1) * pageSize + 1;

			params.put("PAGED_QUERY_ROW_BEGIN", new Integer(begin));
			params.put("PAGESIZE", new Integer(pageSize));
			querySql = "SELECT * FROM ( SELECT PAGED_QUERY.*,ROWNUM PAGED_QUERY_ROWNUM FROM ( "
					+ sql
					+ " ) PAGED_QUERY) WHERE PAGED_QUERY_ROWNUM <= :PAGED_QUERY_ROW_BEGIN + :PAGESIZE - 1 AND PAGED_QUERY_ROWNUM>=:PAGED_QUERY_ROW_BEGIN";

			params=Utils.filterParameter(params);
		}

		if(this.isPrintSQL())
		{
			if(this.isPrintSQL()) {
				Expr se=new Expr(querySql,params);
				se.setSQLDialect(this.getSQLDialect());
				final Map<String,Object> ps=params;
				final String exSQL=querySql;
				final int exBegin=begin;
				new SQLPrinter<Integer>(this,se,se) {
					@Override
					protected Integer actualExecute() {

						if(set instanceof RcdSet)
						{
							getNamedJdbcTemplate().query(exSQL, ps, new RcdResultSetExtractor(new RcdRowMapper((RcdSet)set,exBegin,getQueryLimit())));
						}
						else if(set instanceof DataSet)
						{
							getNamedJdbcTemplate().query(exSQL,ps, new DataResultSetExtractor(new DataRowMapper((DataSet)set,getQueryLimit())));
						}

						return 0;

					}
				}.execute();
			}
		}  else {
			if(set instanceof RcdSet)
			{
				this.getNamedJdbcTemplate().query(querySql, params, new RcdResultSetExtractor(new RcdRowMapper((RcdSet)set,begin,this.getQueryLimit())));
			}
			else if(set instanceof DataSet)
			{
				this.getNamedJdbcTemplate().query(querySql,params, new DataResultSetExtractor(new DataRowMapper((DataSet)set,this.getQueryLimit())));
			}
		}
		Expr se=new Expr(querySql,params);
		se.setSQLDialect(this.getSQLDialect());
		set.setPagedSQL(se);

		//移除辅助列
		if(set instanceof RcdSet) {
			RcdSet rs=(RcdSet)set;
			if(rs.hasColumn("PAGED_QUERY_ROWNUM")) {
				rs.removeColumn("PAGED_QUERY_ROWNUM");
			}
		}

		return set;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected AbstractSet getPageSet(boolean fixed,AbstractSet set, String sql, int pageIndex,
			int pageSize, Object... params)
	{

		boolean isDual=isDualTable(sql);

		String querySql=null;
		Object[] ps = null;
		int begin =0;


		if(isDual) {
			querySql=sql;
			ps=Utils.filterParameter(params);
		} else {

			if (pageIndex <= 0) {
				pageIndex = 1;
			}

			begin = (pageIndex - 1) * pageSize + 1;

			ps = new Object[params.length + 3];
			System.arraycopy(params, 0, ps, 0, params.length);
			ps[params.length] = begin;
			ps[params.length + 1] = pageSize;
			ps[params.length + 2] = begin;

			querySql = "SELECT * FROM ( SELECT PAGED_QUERY.*,ROWNUM PAGED_QUERY_ROWNUM FROM ( "
			+ sql
			+ " ) PAGED_QUERY) WHERE PAGED_QUERY_ROWNUM <= ? + ? - 1 AND PAGED_QUERY_ROWNUM>=?";

			ps=Utils.filterParameter(ps);
		}

		if(this.isPrintSQL())
		{
			if(this.isPrintSQL()) {
				Expr se=new Expr(querySql,ps);
				se.setSQLDialect(this.getSQLDialect());
				new SQLPrinter<Integer>(this,se,se) {
					@Override
					protected Integer actualExecute() {
						return 0;
					}
				}.execute();
			}
		}

		PreparedStatementSetter setter = new ArgumentPreparedStatementSetter(ps);

		if(set instanceof RcdSet)
		{

			try {
				this.getJdbcTemplate().query(querySql, setter, new RcdResultSetExtractor(new RcdRowMapper((RcdSet)set,begin,this.getQueryLimit())));
			} catch (DataAccessException e) {
				if(e.getCause().getMessage().contains("ORA-02287")) {
					List<String> tables=SQLParser.getAllTables(sql, DBMapping.getDruidDBType(this.getDBType().getSQLDialect()));
					if(tables.size()==1 && "dual".equalsIgnoreCase(tables.get(0))) {
						try {
							this.getJdbcTemplate().query(sql, setter, new RcdResultSetExtractor(new RcdRowMapper((RcdSet)set,begin,this.getQueryLimit())));
						} catch (DataAccessException e1) {
							throw e1;
						}
					}
				} else {
					throw e;
				}
			}
		} else if(set instanceof DataSet) {
			this.getJdbcTemplate().query(querySql,setter, new DataResultSetExtractor(new DataRowMapper((DataSet)set,this.getQueryLimit())));
		}

		Expr se=new Expr(querySql,ps);
		se.setSQLDialect(this.getSQLDialect());
		set.setPagedSQL(se);

		//移除辅助列
		if(set instanceof RcdSet) {
			RcdSet rs=(RcdSet)set;
			if(rs.hasColumn("PAGED_QUERY_ROWNUM")) {
				rs.removeColumn("PAGED_QUERY_ROWNUM");
			}
		}

		return set;
	}

	private boolean isDualTable(String sql) {
		boolean isDual=sql.toLowerCase().contains("dual");
		if(isDual) {
			isDual=false;
			List<String> tables=SQLParser.getAllTables(sql, this.getDBType());
			if(tables!=null && tables.size()==0) {
				isDual=true;
			}
		}
		return isDual;
	}


	@Override
	protected String getSchema(String url) {
		return this.getUserName();
	}

	/**
	 * 暂留，；必要时可使用
	 * */
	private String getSchemaByUrl(String url) {
		int a,b;
		String schema = null;

		a=url.indexOf("@");
		String  flag=url.substring(a+1, a+3);
		if("//".equals(flag))
		{
			a=url.indexOf("//");
			a=url.indexOf("/",a+2);
			b=url.indexOf("?");
			if(b==-1) {
				b=url.length();
			}
			schema=url.substring(a+1,b);
		}
		else
		{
			a=url.lastIndexOf(":");
			b=url.length();
			schema=url.substring(a+1,b);
		}
		return schema;
	}

	@Override
	public Date getDateTime()
	{
		return this.queryDate("SELECT SYSDATE FROM DUAL");
	}

	@Override
	public IClobDAO getClobDAO() {
		if(clobDAO==null)
		{
			clobDAO=new OracleClobDAO(this.getDataSource());
		}
		return clobDAO;
	}



}
