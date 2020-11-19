package com.github.foxnic.dao.spec;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.data.AbstractSet;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.lob.IClobDAO;
import com.github.foxnic.dao.meta.DBMetaData;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.sql.SQLParserUtil;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.SQL;
import com.github.foxnic.sql.meta.DBType;
import com.github.foxnic.sql.treaty.DBTreaty;

public abstract class DAO {

	private static ArrayList<DAO> INSTANCES = new ArrayList<>();
	private static HashMap<String, DAO> INSTANCE_MAP = new HashMap<>();

	/**
	 * @return DAO
	 */
	public static DAO getInstance(AbstractSet set) {
		return DAO.getInstance(set.getDBIdentity());
	}

	public static DAO getInstance(String identity) {
		if (StringUtil.isBlank(identity)) {
			return null;
		}
		return INSTANCE_MAP.get(identity);
	}

	private DataSource datasource = null;

	public void setDataSource(DataSource ds) {
		this.datasource = ds;
	}

	public DataSource getDataSource() {
		return datasource;
	}

	private String dbIdentity;
	private String url;
	private String userName;

	public String getDBConnectionIdentity() {
		if (dbIdentity != null) {
			return dbIdentity;
			// 其它类型的连接池暂不支持
		}

		initConnectionInfo();

		if (url == null || userName == null) {
			return null;
		}
		dbIdentity = url.toLowerCase().trim() + ":" + userName.toLowerCase().trim();
		dbIdentity = MD5Util.encrypt16(dbIdentity);
		return dbIdentity;
	}

	private static final String[] URL_METHOD_NAMES = { "getUrl", "getURL", "getJdbcUrl" };
	private static final String[] USER_METHOD_NAMES = { "getUsername", "getUser", "getUserName" };

	private static Map<DataSource, String> DS_URL = new ConcurrentHashMap<DataSource, String>();
	private static Map<DataSource, String> DS_USER_NAME = new ConcurrentHashMap<DataSource, String>();

	private void initConnectionInfo() {

		DataSource cds = this.datasource;
		boolean dynamic = isDynamicDataSource();
		if (!dynamic && url != null) {
			return;
		}

		if (dynamic) {
			cds = getFinalDataSource();
			// 使用缓存提高效率
			url = DS_URL.get(cds);
			userName = DS_USER_NAME.get(cds);
			if (url != null)
				return;
		}

		int errors = 0;

		for (String m : URL_METHOD_NAMES) {
			try {
				url = (String) cds.getClass().getMethod(m).invoke(cds);
			} catch (Exception e) {
				errors++;
			}
			if (!StringUtil.isEmpty(url))
				break;
		}

		DS_URL.put(cds, url);

		for (String m : USER_METHOD_NAMES) {
			try {
				userName = (String) cds.getClass().getMethod(m).invoke(cds);
			} catch (Exception e) {
				errors++;
			}
			if (!StringUtil.isEmpty(userName))
				break;
		}
		DS_USER_NAME.put(cds, userName);
		if (url == null && userName == null && errors == (URL_METHOD_NAMES.length + USER_METHOD_NAMES.length)) {
			throw new RuntimeException(cds.getClass().getName() + " is not support");
		}

	}

	/**
	 * 是否使用动态多数据源
	 * 
	 * @return 逻辑值
	 */
	protected boolean isDynamicDataSource() {
		return false;
	}

	/**
	 * 多数据源情况下，实际使用的最终数据源
	 * 
	 * @return 逻辑值
	 */
	protected DataSource getFinalDataSource() {
		return this.getDataSource();
	}

	private boolean isDisplaySQL = false;

	/**
	 * 是否打印语句
	 * 
	 * @return 是否打印语句
	 */
	public boolean isDisplaySQL() {
		return isDisplaySQL;
	}

	/**
	 * 设置是否打印SQL语句，打印SQL语句对性能有影响
	 * 
	 * @param isDisplaySQL 是否打印语句
	 */
	public void setDisplaySQL(boolean isDisplaySQL) {
		this.isDisplaySQL = isDisplaySQL;
	}

	private int queryLimit = -1;

	/**
	 * 查询行数限制，默认-1，无限制
	 * 
	 * @return 行数
	 */
	public int getQueryLimit() {
		return queryLimit;
	}

	/**
	 * 查询行数限制，默认-1，无限制
	 * 
	 * @param queryLimit 查询限制
	 */
	public void setQueryLimit(int queryLimit) {
		this.queryLimit = queryLimit;
	}

	public abstract DBType getDBType();
	
	/**
	 * 获得当前DAO的Schema
	 * @param url 从连接字符串解析Schema
	 * @return Schema名称
	 */
	protected abstract String getSchema(String url);
	
	
	private String schema = null;
	
	/**
	 * 获得当前DAO的Schema
	 * 
	 * @return Schema名称
	 */
	public String getSchema() {
		
		if(!this.isDynamicDataSource())
		{
			if (schema != null) {
				return schema;
			}
		}

		initConnectionInfo();
		
		if(schema==null)
		{
			try {
				schema=this.getDataSource().getConnection().getSchema();
			} catch (Exception e) {
				Logger.error("异常",e);
			}
		}

		if (url != null && schema==null) {
			schema = this.getSchema(url);
		}

		return schema;
	}
	
	
	private DBTreaty dbTreaty = new DBTreaty();
	
	/**
	 * 针对数据库的一些约定的设置
	 * 
	 * @param dbTreaty 约定配置
	 */
	public void setDBTreaty(DBTreaty dbTreaty)
	{
		this.dbTreaty=dbTreaty;
	}
	/**
	 * 针对数据库的一些约定的设置
	 * 
	 * @return 约定配置
	 */
	public DBTreaty getDBTreaty()
	{
		return dbTreaty;
	}
	
	
	/**
	 * 获取当前库的方言
	 * 
	 * @return 语句方言
	 */
	public SQLDialect getSQLDialect() {
		return this.getDBType().getSQLDialect();
	}
	
	/**
	 * 获取数据库的当前时间，不适合循环调用
	 * 
	 * @return 数据库的当前时间
	 */
	public abstract Date getDateTime();

	
	protected IClobDAO clobDAO=null;
	/**
	 * 获得一个可用于Clob类型数据操作的DAO
	 * 
	 * @return IClobDAO
	 */

	public IClobDAO getClobDAO() {
		return clobDAO;
	}
	
	/**
	 * 获得统计总行数的SQL
	 * @param sql 查询语句
	 * @param name 总行字段名称
	 * @return SQL，改写后的SQL语句
	 * */
	protected  SQL getCountSQL(SQL sql,String name) {
		 try {
			return SQLParserUtil.getCountSQL(sql,name);
		} catch (Exception e) {
			return new Expr("select count(1) "+name+" from ("+sql+") A");
		}
	}
	
	/**
	 * 获得连接的数据库账户
	 * 
	 * @return 数据库账户
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * 获得数据库中表定义的信息
	 * 
	 * @param table 表名
	 * @return DBTableMeta
	 */
	/**
	 * 获得数据库中表定义的信息
	 * 
	 * @param table 表名
	 * @return DBTableMeta
	 */
 
	public DBTableMeta getTableMeta(String table)
	{
		return DBMetaData.getTableMetaData(this, table);
	}
	
	
	/**
	 * 根据ID获得SQL
	 * 
	 * @param id 在tql文件中定义的sqlid(不需要以#开始)
	 * @return sql
	 */
	public abstract String getSQL(String id);

	/**
	 * 根据ID获得SQL
	 * 
	 * @param ps 参数
	 * @param id 在tql文件中定义的sqlid(不需要以#开始)
	 * @return SQL
	 */
	public abstract SQL getSQL(String id, Object... ps);

	/**
	 * 根据ID获得SQL
	 * 
	 * @param id 在tql文件中定义的sqlid(不需要以#开始)
	 * @param ps 参数
	 * @return SQL
	 */
	public abstract SQL getSQL(String id, Map<String, Object> ps);
	
	
	
	/**
	 * 执行一个SQL语句
	 * 
	 * @param sql sql语句
	 * @return 影响的行数
	 */
	public abstract Integer execute(SQL sql);

	
	/**
	 * 执行一个SQL语句
	 * 
	 * @param sql sql语句
	 * @param ps  参数
	 * @return 影响的行数
	 */
	public abstract Integer execute(String sql, Map<String, Object> ps);

	/**
	 * 执行一个SQL语句
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 影响的行数
	 */
	public abstract Integer execute(String sql, Object... params);
	
	

	public abstract RcdSet query(SQL se);
 
	public abstract RcdSet query(String sql, Object... ps);
	/**
	 * 查询记录集
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return RcdSet
	 */
	public abstract RcdSet query(String sql, Map params);

	/**
	 * 分页查询记录集
	 * 
	 * @param sql    sql语句
	 * @param size   每页行数
	 * @param index  页码
	 * @return RcdSet
	 */
	public abstract RcdSet queryPage(SQL se, int pageSize, int pageIndex);
	

	/**
	 * 查询单个对象
	 * 
	 * @param sql sql语句
	 * @return Rcd
	 */
	public abstract Object queryObject(SQL sql);

	/**
	 * 查询单个对象
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return Object
	 */
	public abstract Object queryObject(String sql, Object... params);

	/**
	 * 查询单个对象
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return Object
	 */
	public abstract Object queryObject(String sql, Map<String, Object> params);
	
	
	/**
	 * 查询单个整数
	 * 
	 * @param sql sql语句
	 * @return 值
	 */
	public abstract Integer queryInteger(SQL sql);

	/**
	 * 查询单个整数
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public abstract Integer queryInteger(String sql, Object... params);

	/**
	 * 查询单个整数
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public abstract Integer queryInteger(String sql, Map<String, Object> params);
	
	
	/**
	 * 查询单个长整型
	 * 
	 * @param sql sql语句
	 * @return 值
	 */
	public abstract Long queryLong(SQL sql);

	/**
	 * 查询单个长整型
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public abstract Long queryLong(String sql, Object... params);

	/**
	 * 查询单个长整型
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public abstract Long queryLong(String sql, Map<String, Object> params);

	/**
	 * 查询单个字符串
	 * 
	 * @param sql sql语句
	 * @return 值
	 */
	public abstract String queryString(SQL sql);

	/**
	 * 查询单个字符串
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public abstract String queryString(String sql, Object... params);

	/**
	 * 查询单个字符串
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public abstract String queryString(String sql, Map<String, Object> params);

	/**
	 * 查询单个日期
	 * 
	 * @param sql sql语句
	 * @return 值
	 */
	public abstract Date queryDate(SQL sql);

	/**
	 * 查询单个日期
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public abstract Date queryDate(String sql, Object... params);

	/**
	 * 查询单个日期
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public abstract Date queryDate(String sql, Map<String, Object> params);

	 

	/**
	 * 查询单个记录
	 * 
	 * @param sql sql语句
	 * @return Rcd
	 */
	public abstract Rcd queryRecord(SQL sql);

	/**
	 * 查询单个记录
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return Rcd
	 */
	public abstract Rcd queryRecord(String sql, Object... params);
	

	/**
	 * 记录是否存已经在数据表,以主键作为判断依据
	 * 
	 * @param r                  记录
	 * @param table              表
	 * @param checkWithOrignalId 是否用原始值(setValue前的值/从数据库查询获得的原始值)来核对数据存在性
	 * @return 是否存在
	 */
	public abstract boolean isRecordExits(Rcd r, String table, boolean checkWithOrignalId);

	/**
	 * 记录是否存已经在数据表,以主键作为判断依据
	 * 
	 * @param r                  记录
	 * @param checkWithOrignalId 是否用原始值(setValue前的值/从数据库查询获得的原始值)来核对数据存在性
	 * @return 是否存在
	 */
	public abstract boolean isRecordExits(Rcd r, boolean checkWithOrignalId);
	
	
	
	
	
	
	
	/**
	 * 把记录插入到数据库
	 * 
	 * @param r     记录
	 * @param table 数表
	 * @return 是否成功
	 */
	public abstract boolean insertRecord(Rcd r, String table);

	/**
	 * 把记录插入到数据库，表名自动识别
	 * 
	 * @param r 记录
	 * @return 是否成功
	 */
	public abstract boolean insertRecord(Rcd r);

	/**
	 * 把记录插入到数据库，表名自动识别
	 * 
	 * @param r          记录
	 * @param table      数据表
	 * @param IgnorNulls 是否忽略空值
	 * @return 是否成功
	 */
	public abstract boolean insertRecord(Rcd r, String table, boolean IgnorNulls);

	/**
	 * 把记录从数据库删除
	 * 
	 * @param r     记录
	 * @param table 数据表
	 * @return 是否成功
	 */
	public abstract boolean deleteRecord(Rcd r, String table);

	/**
	 * 把记录从数据库删除
	 * 
	 * @param r 记录
	 * @return 是否成功
	 */
	public abstract boolean deleteRecord(Rcd r);

	/**
	 * 保存记录
	 * 
	 * @param r        记录
	 * @param saveMode 保存模式
	 * @return 是否成功
	 */
	public abstract boolean saveRecord(Rcd r, SaveMode saveMode);

	/**
	 * 保存记录
	 * 
	 * @param r        记录
	 * @param table    数据表
	 * @param saveMode 保存模式
	 * @return 是否成功
	 */
	public abstract boolean saveRecord(Rcd r, String table, SaveMode saveMode);

	/**
	 * 把记录保存到数据库
	 * 
	 * @param r        记录
	 * @param table    数据表
	 * @param saveMode 保存模式
	 * @return 是否成功
	 */
	public abstract boolean updateRecord(Rcd r, String table, SaveMode saveMode);

	/**
	 * 把记录保存到数据库
	 * 
	 * @param r        记录
	 * @param saveMode 保存模式
	 * @return 是否成功
	 */
	public abstract boolean updateRecord(Rcd r, SaveMode saveMode);
}
