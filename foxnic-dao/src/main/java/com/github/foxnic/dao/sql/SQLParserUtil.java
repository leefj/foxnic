package com.github.foxnic.dao.sql;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.util.JdbcConstants;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.excel.DataException;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBMapping;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.dao.sql.tablefinder.DB2TableNameFinder;
import com.github.foxnic.dao.sql.tablefinder.ITableNameFinder;
import com.github.foxnic.dao.sql.tablefinder.MySQLTableNameFinder;
import com.github.foxnic.dao.sql.tablefinder.OracleTableNameFinder;
import com.github.foxnic.dao.sql.tablefinder.SQLServerTableNameFinder;
import com.github.foxnic.sql.GlobalSettings;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.exception.DBMetaException;
import com.github.foxnic.sql.expr.Delete;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.Insert;
import com.github.foxnic.sql.expr.SQL;
import com.github.foxnic.sql.expr.Update;
import com.github.foxnic.sql.parser.cache.SQLParserCache;

/**
 * 
 * @author fangjieli
 *
 */
public class SQLParserUtil {

	public static List<String> getAllTables(SQLStatement stmt , DbType dbType) {
		StringWriter out = new StringWriter();
		ITableNameFinder finder=DBMapping.getDruidDBType(dbType);
		stmt.accept(finder);
		return finder.getAllTables();
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
		for (SQLStatement stmt : stmtList) {
			tables.addAll(getAllTables(stmt,dbType));
		}
		
		//fix other 
		if(!fix && tables.size()==0 ) {
			if(DbType.mysql != dbType) {
				return  getAllTables(sql,DbType.mysql,true);
			} else if(DbType.oracle != dbType) {
				return  getAllTables(sql,DbType.oracle,true);
			} else if(DbType.db2 != dbType) {
				return  getAllTables(sql,DbType.db2,true);
			} else if(DbType.sqlserver != dbType) {
				return  getAllTables(sql,DbType.sqlserver,true);
			} else if(DbType.sqlite != dbType) {
				return  getAllTables(sql,DbType.sqlite,true);
			}
		}
		TABLE_CACHE.put(sql, tables);
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
	
	
	
	
	public static Delete buildDelete(Rcd r,String table,DAO dao)
	{
		Delete delete=new Delete();
		delete.from(table);
		DBTableMeta tm=dao.getTableMeta(table);
		if(tm==null)
		{
			throw new DBMetaException("未发现表"+table+"的Meta数据,请认定表名是否正确");
		}
		
		List<DBColumnMeta> pks = tm.getPKColumns();
		String cName=null;
		Object value=null;
 
		for (DBColumnMeta column : pks) {
			cName=column.getColumn();
			value= r.getOriginalValue(cName);
			if(value==null) {
				throw new DataException(table+"."+cName+" 主键值不允许为空");
			}
			delete.where().and(cName+"=?", value);
		}
		
		return delete;
	}
	
	
	public static Delete buildDelete(Rcd r) {
		 return buildDelete(r,r.getOwnerSet().getMetaData().getDistinctTableNames()[0],DAO.getInstance(r.getOwnerSet()));
	}
	
	public static Update buildUpdate(Rcd r,SaveMode saveMode)
	{
		 return buildUpdate(r,saveMode,r.getOwnerSet().getMetaData().getDistinctTableNames()[0], DAO.getInstance(r.getOwnerSet()));
	}
	
	public static Update buildUpdate(Rcd r,SaveMode saveMode,String table,DAO dao)
	{
		Update update=new Update(table);
		DBTableMeta tm=dao.getTableMeta(table);
		if(tm==null)
		{
			throw new DBMetaException("未发现表"+table+"的Meta数据,请认定表名是否正确");
		}
		Expr seVal=null;
		List<DBColumnMeta> pks = tm.getPKColumns();
		if(pks==null || pks.size()==0)
		{
			throw new DBMetaException("数据表"+table+"未定义主键");
		}
		
		List<DBColumnMeta> columns=tm.getColumns();
		Object value=null;
		boolean dirty=false;
		String cName=null;
		
		for (DBColumnMeta column : columns) {
			cName=column.getColumn();
			seVal=r.getExpr(cName);
			
			value= r.getValue(cName);
			//处理类型的值
			value=column.getDBDataType().cast(value);
			
			if(saveMode==SaveMode.ALL_FIELDS) {
				if(seVal!=null) {
					update.setExpr(cName, seVal);
				} else {
					update.set(cName,value);
				}
			} else if(saveMode==SaveMode.DIRTY_FIELDS) {
				dirty=r.isDirty(cName);
				if(dirty) {
					if(seVal!=null) {
						update.setExpr(cName, seVal);
					} else {
						update.set(cName,value);
					}
				}
			} else if(saveMode==SaveMode.NOT_NULL_FIELDS) {
				if(value!=null || seVal!=null) {
					if(seVal!=null) {
						update.setExpr(cName, seVal);
					} else {
						update.set(cName,value);
					}
				}
			}
		}
		
		for (DBColumnMeta column : pks) {
			cName=column.getColumn();
			value= r.getOriginalValue(cName);
			if(value==null) {
				throw new DataException(table+"."+cName+" 主键值不允许为空");
			}
			update.where().and(cName+"=?", value);
		}
		
		return update;
	}
	
	
	public static Insert buildInsert(Rcd r)
	{
		 return buildInsert(r,r.getOwnerSet().getMetaData().getDistinctTableNames()[0],DAO.getInstance(r.getOwnerSet()),true);
	}
	
	public static Insert buildInsert(Rcd r,boolean ignorNulls)
	{
		 return buildInsert(r,r.getOwnerSet().getMetaData().getDistinctTableNames()[0],DAO.getInstance(r.getOwnerSet()),ignorNulls);
	}

	public static Insert buildInsert(Rcd r,String table,DAO dao,boolean ignorNulls)
	{
		Insert insert=new Insert(table);
		DBTableMeta tm=dao.getTableMeta(table);
		if(tm==null)
		{
			throw new DBMetaException("未发现表"+table+"的Meta数据,请认定表名是否正确");
		}
		
		List<DBColumnMeta> columns=tm.getColumns();
		Expr seVal=null;
		Object val=null;
		for (DBColumnMeta column : columns) {
			seVal=r.getExpr(column.getColumn());
			
			val=r.getValue(column.getColumn());
			//make value cast
			val=column.getDBDataType().cast(val);
 
			if(seVal==null)
			{
				if(ignorNulls)
				{
					if(val!=null)
					{
						insert.set(column.getColumn(),val);
					}
				}
				else
				{
					insert.set(column.getColumn(),val);
				}
			}
			else
			{
				insert.setExpr(column.getColumn(), seVal);
			}
		}
		return insert;
	}
	
	
	/**
	 * 把Insert语句转成对象, 高级功能请参看StatementUtil 类
	 * @param sql SQL语句
	 * @param dialect 方言
	 * @return Insert对象
	 * */
	public static Insert parseInsert(String sql,SQLDialect dialect)
	{
		List<Insert> inserts=StatementUtil.parseInsert(sql, dialect);
		if(inserts.size()==0) return null;
		return inserts.get(0);
	}
	
	/**
	 * 把Insert语句转成对象，使用GlobalSettings中默认的方言，高级功能请参看StatementUtil 类
	 * @param sql SQL语句
	 * @return Insert对象
	 * */
	public static Insert parseInsert(String sql)
	{
		return parseInsert(sql,GlobalSettings.DEFAULT_SQL_DIALECT);
	}
	
}
