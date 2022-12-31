package com.github.foxnic.sql.expr;

import com.github.foxnic.sql.data.ExprDAO;
 
/**
 * 可执行语句
 * @author fangjieli
 *
 */
public interface ExecutableSQL extends SQL
{
	/**
	 * 使用 setDAO()方法指定的DAO执行当前语句
	 * @return 执行结果
	 * */
	default Integer execute() {
		return getDAO().execute(this);
	}
	/**
	 * 获得通过setDAO设置的DAO对象，如未设置，则返回 @Primary 注解的 DAO，非Spring环境下，将返回第一个初始化的DAO对象
	 * @return DAO
	 * */
	ExprDAO getDAO() ;
	
	ExecutableSQL setDAO(ExprDAO dao);
}
