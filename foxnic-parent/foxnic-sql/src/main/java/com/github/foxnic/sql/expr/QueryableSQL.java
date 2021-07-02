package com.github.foxnic.sql.expr;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.github.foxnic.commons.collection.IPagedList;
import com.github.foxnic.sql.data.ExprRcd;
import com.github.foxnic.sql.data.ExprRcdSet;

 

/**
 * @author fangjieli
 */
public interface QueryableSQL  extends ExecutableSQL
{
	/**
	 * 通过当前语句查询数据集，使用默认DAO，请见getDAO()方法
	 * @return RcdSet
	 * */
	public ExprRcdSet query();
	
	
	public <T> List<T> queryEntities(Class<T> type);
	
	public <T> IPagedList<T> queryPagedEntities(Class<T> type, int pageSize, int pageIndex);
	
	/**
	 * 通过当前语句查询分页的数据集，使用默认DAO，请见getDAO()方法
	 * @return RcdSet
	 * */
	public ExprRcdSet queryPage(int pageSize,int pageIndex);
	
	/**
	 * 通过当前语句查询Rcd值，数据集的第一行，使用默认DAO，请见getDAO()方法
	 * @return Rcd
	 * */
	public ExprRcd queryRecord() ;
	
	/**
	 * 通过当前语句查询整形值，数据集的第一行第一列，使用默认DAO，请见getDAO()方法
	 * @return Integer
	 * */
	public Integer queryInteger() ;
	
	/**
	 * 通过当前语句查询String值，数据集的第一行第一列，使用默认DAO，请见getDAO()方法
	 * @return String
	 * */
	public String queryString() ;
	
	/**
	 * 通过当前语句查询Long值，数据集的第一行第一列，使用默认DAO，请见getDAO()方法
	 * @return Long
	 * */
	public Long queryLong() ;
	
	/**
	 * 通过当前语句查询Date值，数据集的第一行第一列，使用默认DAO，请见getDAO()方法
	 * @return Date
	 * */
	public Date queryDate() ;
	
	/**
	 * 通过当前语句查询BigDecimal值，数据集的第一行第一列，使用默认DAO，请见getDAO()方法
	 * @return BigDecimal
	 * */
	public BigDecimal queryBigDecimal() ;
	
	/**
	 * 通过当前语句查询Double值，数据集的第一行第一列，使用默认DAO，请见getDAO()方法
	 * @return Double
	 * */
	public Double queryDouble() ;
	
	/**
	 * 通过当前语句查询Timestamp值，数据集的第一行第一列，使用默认DAO，请见getDAO()方法
	 * @return Timestamp
	 * */
	public Timestamp queryTimestamp();
 

}
