package com.github.foxnic.sql.expr;

import com.github.foxnic.commons.collection.IPagedList;
import com.github.foxnic.sql.data.ExprRcd;
import com.github.foxnic.sql.data.ExprRcdSet;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;



/**
 * @author fangjieli
 */
public interface QueryableSQL  extends ExecutableSQL
{

	/**
	 * 通过当前语句查询数据集，使用默认DAO，请见getDAO()方法
	 * @return RcdSet
	 * */
	default  <T extends ExprRcdSet> T query() {
		return (T)getDAO().query(this);
	}

	default <T> List<T> queryEntities(Class<T> type) {
		return getDAO().queryEntities(type, this);
	}

	default <T> T queryEntity(Class<T> type) {
		return (T)getDAO().queryEntity(type,this);
	}


	default <T> IPagedList<T> queryPagedEntities(Class<T> type, int pageSize, int pageIndex) {
		return getDAO().queryPagedEntities(type, pageSize,pageIndex,this);
	};

	/**
	 * 通过当前语句查询分页的数据集，使用默认DAO，请见getDAO()方法
	 * @return RcdSet
	 * */
	default <T extends ExprRcdSet> T queryPage(int pageSize,int pageIndex)
	{
		return (T)getDAO().queryPage(this, pageSize, pageIndex);
	}

	/**
	 * 通过当前语句查询Rcd值，数据集的第一行，使用默认DAO，请见getDAO()方法
	 * @return Rcd
	 * */
	default <T extends ExprRcd> T queryRecord() {
		return (T)getDAO().queryRecord(this);
	}

	/**
	 * 通过当前语句查询整形值，数据集的第一行第一列，使用默认DAO，请见getDAO()方法
	 * @return Integer
	 * */
	default Integer queryInteger() {
		return getDAO().queryInteger(this);
	}

	/**
	 * 通过当前语句查询String值，数据集的第一行第一列，使用默认DAO，请见getDAO()方法
	 * @return String
	 * */
	default String queryString() {
		return getDAO().queryString(this);
	}

	/**
	 * 通过当前语句查询Long值，数据集的第一行第一列，使用默认DAO，请见getDAO()方法
	 * @return Long
	 * */
	default Long queryLong() {
		return getDAO().queryLong(this);
	}

	/**
	 * 通过当前语句查询Date值，数据集的第一行第一列，使用默认DAO，请见getDAO()方法
	 * @return Date
	 * */
	default Date queryDate() {
		return getDAO().queryDate(this);
	}

	/**
	 * 通过当前语句查询BigDecimal值，数据集的第一行第一列，使用默认DAO，请见getDAO()方法
	 * @return BigDecimal
	 * */
	default BigDecimal queryBigDecimal() {
		return getDAO().queryBigDecimal(this);
	}

	/**
	 * 通过当前语句查询Double值，数据集的第一行第一列，使用默认DAO，请见getDAO()方法
	 * @return Double
	 * */
	default Double queryDouble() {
		return getDAO().queryDouble(this);
	}

	/**
	 * 通过当前语句查询Timestamp值，数据集的第一行第一列，使用默认DAO，请见getDAO()方法
	 * @return Timestamp
	 * */
	default Timestamp queryTimestamp() {
		return getDAO().queryTimestamp(this);
	}


}
