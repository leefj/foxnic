package com.github.foxnic.sql.data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;

import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.expr.Delete;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.Insert;
import com.github.foxnic.sql.expr.SQL;
import com.github.foxnic.sql.expr.Select;
import com.github.foxnic.sql.expr.Update;

public interface ExprDAO {

	/**
	 * 获得一个可执行的SE构建器，已经被设置DAO
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 表达式
	 */
	public abstract Expr expr(String sql, HashMap<String, Object> params);

	/**
	 * 获得一个可执行的SE构建器，已经被设置DAO
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 表达式
	 */
	public abstract Expr expr(String sql, Object... params);

	/**
	 * 获得一个可执行的select语句构建器，已经被设置DAO
	 * 
	 * @return Select
	 */
	public abstract Select select();

	/**
	 * 获得一个可执行的insert语句构建器，已经被设置DAO
	 * 
	 * @param table 表
	 * @return Insert
	 */
	public abstract Insert insert(String table);	
	
	
	/**
	 * 获得一个可执行的update语句构建器，已经被设置DAO
	 * 
	 * @param table 表
	 * @return Update语句
	 */
	public abstract Update update(String table);

	/**
	 * 获得一个可执行的update语句构建器，已经被设置DAO
	 * 
	 * @param table 数据表
	 * @param ce    条件表达式
	 * @param ps    条件表达式参数
	 * @return Update语句
	 */
	public abstract Update update(String table, String ce, Object... ps);

	/**
	 * 获得一个可执行的update语句构建器，已经被设置DAO
	 * 
	 * @param table 数据表
	 * @param ce    条件表达式
	 * @return Update语句
	 */
	public abstract Update update(String table, ConditionExpr ce);

	/**
	 * 获得一个可执行的delete语句构建器，已经被设置DAO
	 * 
	 * @param table 数据表
	 * @return Delete语句
	 */
	public abstract Delete delete(String table);

	/**
	 * 获得一个可执行的delete语句构建器，已经被设置DAO
	 * 
	 * @param table 数据表
	 * @param ce    条件表达式
	 * @param ps    条件表达式参数
	 * @return Delete语句
	 */
	public abstract Delete delete(String table, String ce, Object... ps);

	/**
	 * 获得一个可执行的delete语句构建器，已经被设置DAO
	 * 
	 * @param table 数据表
	 * @param ce    条件表达式
	 * @return Delete语句
	 */
	public abstract Delete delete(String table, ConditionExpr ce);
	
	
	/**
	 * 分页查询记录集
	 * 
	 * @param sql   sql语句
	 * @param size  每页行数
	 * @param index 页码
	 * @return RcdSet
	 */
	public abstract ExprRcdSet queryPage(SQL sql, int size, int index);
	
	
	/**
	 * 查询单个记录
	 * 
	 * @param sql sql语句
	 * @return Rcd
	 */
	public abstract ExprRcd queryRecord(SQL sql);
	
	/**
	 * 查询单个对象
	 * 
	 * @param sql sql语句
	 * @return Rcd
	 */
	public abstract Object queryObject(SQL sql);
	
	/**
	 * 查询单个字符串
	 * 
	 * @param sql sql语句
	 * @return 值
	 */
	public abstract String queryString(SQL sql);
	
	
	/**
	 * 查询单个整数
	 * 
	 * @param sql sql语句
	 * @return 值
	 */
	public abstract Integer queryInteger(SQL sql);
	
	/**
	 * 查询单个长整型
	 * 
	 * @param sql sql语句
	 * @return 值
	 */
	public abstract Long queryLong(SQL sql);
	
	
	/**
	 * 查询单个日期
	 * 
	 * @param sql sql语句
	 * @return 值
	 */
	public abstract Date queryDate(SQL sql);
	
	
	/**
	 * 查询单个BigDecimal
	 * 
	 * @param sql sql语句
	 * @return 值
	 */
	public abstract BigDecimal queryBigDecimal(SQL sql);
	
	/**
	 * 查询单个Double值
	 * 
	 * @param sql sql语句
	 * @return 值
	 */
	public abstract Double queryDouble(SQL sql);
	
	
	/**
	 * 查询单个Timestamp值
	 * 
	 * @param sql sql语句
	 * @return 值
	 */
	public abstract Timestamp queryTimestamp(SQL sql);
	
	
	/**
	 * 执行一个SQL语句
	 * 
	 * @param sql sql语句
	 * @return 影响的行数
	 */
	public abstract Integer execute(SQL sql);
	
	/**
	 * 查询记录集
	 * 
	 * @param sql    sql语句
	 * @return RcdSet
	 */
	public abstract ExprRcdSet query(SQL se);
}
