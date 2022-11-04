package com.github.foxnic.dao.sql;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.meta.DBIndexMeta;
import com.github.foxnic.dao.meta.DBMapping;
import com.github.foxnic.dao.sql.tablefinder.ITableNameFinder;
import com.github.foxnic.sql.GlobalSettings;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.SQL;
import com.github.foxnic.sql.meta.DBType;
import com.github.foxnic.sql.parser.cache.SQLParserCache;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fangjieli
 *
 */
public class SQLParser {

	public static List<String> getAllTables(SQLStatement stmt , DbType dbType) {
		ITableNameFinder finder=DBMapping.getDruidDBType(dbType);
		return finder.getAllTables(stmt);
	}


	private static  SQLParserCache TABLE_CACHE = null;

	/**
	 * 获得语句中的所有表
	 * @param sql 语句
	 * @param dbType 数据库类型，JdbcConstants的常量
	 * @return 表名
	 * */
	@SuppressWarnings("unchecked")
	public static List<String> getAllTables(String sql, DbType dbType) {
		return getAllTables(sql,dbType,false);
	}

	/**
	 * 获得语句中的所有表
	 * @param sql 语句
	 * @param dbType 数据库类型，JdbcConstants的常量
	 * @return 表名
	 * */
	@SuppressWarnings("unchecked")
	public static List<String> getAllTables(String sql, DBType dbType) {
		return getAllTables(sql,DruidUtils.getDbType(dbType),false);
	}

	/**
	 * 获得语句中的所有表
	 * @param sql 语句
	 * @param dbType 数据库类型，JdbcConstants的常量
	 * @return 表名
	 * */
	@SuppressWarnings("unchecked")
	public static List<String> getAllTables(String sql, DbType dbType,boolean fix) {

		if(TABLE_CACHE==null) {
			TABLE_CACHE = GlobalSettings.SQL_PARSER_CACHE_TYPE.createSQLParserCache();
		}

		Object value=TABLE_CACHE.get(dbType+":"+sql);
		ArrayList<String> tables = null;
		if(value!=null)
		{
			try {
				tables = (ArrayList<String>)TABLE_CACHE.get(dbType+":"+sql);
			} catch (Exception e) {
			}
			if(tables!=null) return tables;
		}

		List<SQLStatement> stmtList = null;
		try {
			stmtList = SQLUtils.parseStatements(sql, dbType);
		} catch (Exception e) {
			 Logger.error("语句解析异常:\n"+sql,e);
		}
		tables=new ArrayList<String>();
		if(stmtList!=null) {
			for (SQLStatement stmt : stmtList) {
				tables.addAll(getAllTables(stmt,dbType));
			}
		}

		//fix other
//		if(!fix && tables.size()==0 ) {
//			if(DbType.mysql != dbType) {
//				return  getAllTables(sql,DbType.mysql,true);
//			} else if(DbType.oracle != dbType) {
//				return  getAllTables(sql,DbType.oracle,true);
//			} else if(DbType.db2 != dbType) {
//				return  getAllTables(sql,DbType.db2,true);
//			} else if(DbType.sqlserver != dbType) {
//				return  getAllTables(sql,DbType.sqlserver,true);
//			} else if(DbType.sqlite != dbType) {
//				return  getAllTables(sql,DbType.sqlite,true);
//			} else if(DbType.postgresql != dbType) {
//				return  getAllTables(sql,DbType.postgresql,true);
//			}
//		}
		TABLE_CACHE.put(dbType+":"+sql, tables);
		return tables;
	}



	/**
	 * 移除Select语句中的OrderBy部分
	 * @param sql SQ语句
	 * @return  无order by子句的语句
	 * */
	public static SQL removeOrderBy(SQL sql)
	{
		String sqlstr=sql.getListParameterSQL();

		List<SQLStatement> stmtList = null;
		stmtList = SQLUtils.parseStatements(sqlstr, DBMapping.getDruidDBType(sql.getSQLDialect()));

		SQLStatement statement = stmtList.get(0);
		SQLSelectStatement select = (SQLSelectStatement)statement;
		SQLSelectQuery query= select.getSelect().getQuery();
		SQLSelectQueryBlock block=(SQLSelectQueryBlock)query;
		SQLOrderBy orderBy=block.getOrderBy();
		if(orderBy==null)
		{
			return sql;
		}
		else
		{
			block.setOrderBy(null);
			Expr se=new Expr(select.toString(),sql.getListParameters());
			se.setSQLDialect(sql.getSQLDialect());
			return se;
		}
	}

	/**
	 * 改写SQL语句，加入select 字段
	 * @param sql 语句
	 * @param field 要加入的字段
	 * @return 改写后的SQL语句
	 * */
	public static SQL addSelectItem(SQL sql,String... field)
	{
		String sqlstr=sql.getListParameterSQL();

		List<SQLStatement> stmtList = null;
		stmtList = SQLUtils.parseStatements(sqlstr, DBMapping.getDruidDBType(sql.getSQLDialect()));

		SQLStatement statement = stmtList.get(0);
		SQLSelectStatement select = (SQLSelectStatement)statement;
		SQLSelectQuery query= select.getSelect().getQuery();
		SQLSelectQueryBlock block=(SQLSelectQueryBlock)query;


		for (String f : field) {
			block.addSelectItem(new SQLIdentifierExpr(f));
		}

		Expr se=new Expr(select.toString(),sql.getListParameters());
		se.setSQLDialect(sql.getSQLDialect());

		return se;

	}

	/**
	 * 改写为count的统计语句
	 * @param sql 语句
	 * @param countField count 字段名
	 * @return SQL语句
	 * */
	public static SQL getCountSQL(SQL sql,String countField)
	{
		sql=removeOrderBy(sql);

		String sqlstr=sql.getListParameterSQL();

		List<SQLStatement> stmtList = null;
		stmtList = SQLUtils.parseStatements(sqlstr, DBMapping.getDruidDBType(sql.getSQLDialect()));

		SQLStatement statement = stmtList.get(0);
		SQLSelectStatement select = (SQLSelectStatement)statement;
		SQLSelectQuery query= select.getSelect().getQuery();
		SQLSelectQueryBlock block=(SQLSelectQueryBlock)query;
		boolean distinct=block.getDistionOption()>0;
		List<SQLSelectItem> sis=block.getSelectList();
		StringBuilder sistr=new StringBuilder();
		for (SQLSelectItem si : sis) {
			sistr.append(si.toString()+",");
		}
		Object[] ps=sql.getListParameters();
		Expr se=new Expr(sistr.toString(),ps);
		if(ps.length>0)
		{
			ps=ArrayUtil.subArray(ps, se.getListParameters().length, ps.length);
		}
		//非distinct，优化查询字段
		if(!distinct)
		{
			//去除字段
			block.getSelectList().clear();
			block.addSelectItem(new SQLIntegerExpr(1));
		}
		se=new Expr("select count(1) "+countField+" from ("+select.toString()+") A",ps);
		se.setSQLDialect(sql.getSQLDialect());
		return se;
	}

	public static DBIndexMeta parseIndexCreationStatement(String sql,SQLDialect dialect) {
		sql=dealWithIndexCreationStatement(sql);
		List<SQLStatement> stmtList = null;
		stmtList = SQLUtils.parseStatements(sql, DBMapping.getDruidDBType(dialect));
		SQLCreateIndexStatement cis=(SQLCreateIndexStatement)stmtList.get(0);
		String name=cis.getName().getSimpleName();
		String type=cis.getType();
		boolean unique="UNIQUE".equalsIgnoreCase(type);
		String table=cis.getTableName();
		List<SQLSelectOrderByItem> columns=cis.getIndexDefinition().getColumns();
		ArrayList<String> fields=new ArrayList<String>();
		for (SQLSelectOrderByItem f : columns) {
			String zz=f.getExpr().toString();
			fields.add(zz);
		}
		DBIndexMeta im=new DBIndexMeta(name, table, false, unique, fields.toArray(new String[0]));
		return im;

	}

//	public static void main(String[] args) {
//		String aa="CREATE UNIQUE INDEX daily_pkey ON ONLY stock.daily USING btree (year, sid, day)";
//		aa=dealWithIndexCreationStatement(aa);
//		//aa=aa.replace(" ON ONLY "," ON");
//		//aa="CREATE INDEX index_name ON table_name (column_name)";
//
//		parseIndexCreationStatement(aa,SQLDialect.PLSQL);
//
//	}

	/**
	 * 目前是针对PG去掉一些不必要的语法
	 * */
	private static String dealWithIndexCreationStatement(String stmt) {
		String lower=stmt.toLowerCase();
		//
		String key=" on only ";
		int i=lower.indexOf(key);
		if(i>0) {
			String p1 = stmt.substring(0, i);
			String p2 = stmt.substring(i + key.length());
			stmt = p1 + " on " + p2;
		}
		//
		lower=stmt.toLowerCase();
		key=" using ";
		i=lower.indexOf(key);
		if(i>0) {
			int j=stmt.indexOf("(",i);
			if(j>0) {
				String p1 = stmt.substring(0, i);
				String p2 = stmt.substring(j);
				stmt = p1 + " " + p2;
			}
		}

		return stmt;
	}


}
