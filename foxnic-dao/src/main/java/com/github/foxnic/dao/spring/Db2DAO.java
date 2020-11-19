package com.github.foxnic.dao.spring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.util.Assert;

import com.github.foxnic.commons.log.Logger;
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

public class Db2DAO  extends SpringDAO {
	
	
	private static class Db2PreparedStatementCreator implements PreparedStatementCreator, SqlProvider {

		private final String sql;

		public Db2PreparedStatementCreator(String sql) {
			Assert.notNull(sql, "SQL must not be null");
			this.sql = sql;
		}

		@Override
		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
			PreparedStatement ps= con.prepareStatement(this.sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			return ps;
		}

		@Override
		public String getSql() {
			return this.sql;
		}
	}
	
	@Override
	public DBType getDBType() {
		return DBType.DB2;
	}

	@Override
	protected AbstractSet getPageSet(boolean fixed,AbstractSet set,String sql,int pageIndex,int pageSize,Map<String, Object> params)
	{
		Expr se=new Expr(sql,params);
		return getPageSet(fixed,set, se.getListParameterSQL(), pageIndex, pageSize, se.getListParameters());
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
 
		String querySql = "SELECT * FROM ( "
		+ sql
		+ " ) PAGED_QUERY FETCH FIRST "+(begin+pageSize)+" ROWS ONLY ";
		
		Object[] ps=Utils.filterParameter(params);
		
		if(this.isDisplaySQL())
		{
			if(this.isDisplaySQL()) {
				Expr se=new Expr(querySql,ps);
				new SQLPrinter<Integer>(this,se,se) {
					@Override
					protected Integer actualExecute() {
						return 0;
					}
				}.execute();
			}
		}  
		
		PreparedStatementSetter setter=new ArgumentPreparedStatementSetter(ps);
		
		if(set instanceof RcdSet)
		{
			
			if(!fixed) {
				Db2PreparedStatementCreator psc=new Db2PreparedStatementCreator(querySql);
				try {
					this.getJdbcTemplate().query(psc,setter, new RcdResultSetExtractor(new RcdRowMapper((RcdSet)set,begin,this.getQueryLimit())));
				} catch(Exception e) {
					Logger.error("分页错误，请确认查询结果中是否包含CLOB等特殊类型的字段",e);
				}
			} else {
				try {
					this.getJdbcTemplate().query(querySql,setter, new RcdResultSetExtractor(new RcdRowMapper((RcdSet)set,begin,this.getQueryLimit())));
				} catch(Exception e) {
					Logger.error("查询错误",e);
				}
			}
				
		}
		else if(set instanceof DataSet)
		{
			this.getJdbcTemplate().query(querySql,setter, new DataResultSetExtractor(new DataRowMapper((DataSet)set,this.getQueryLimit())));
		}
		set.setPagedSQL(new Expr(querySql,params));
		return set;
	}
	
	@Override
	protected String getSchema(String url)
	{
		int a,b;
		a=url.indexOf("//");
		a=url.indexOf("/",a+2);
		b=url.indexOf("currentSchema",a);
		if(b==-1)  {
			Logger.error("请指定在连接字符串中指定 currentSchema 参数");
			return null;
		}
		a=url.indexOf("=",b);
		b=url.indexOf(";",a);
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
}
