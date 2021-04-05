package com.github.foxnic.dao.spec;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.data.AbstractSet;
import com.github.foxnic.dao.data.*;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.lob.IClobDAO;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBMetaData;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.relation.RelationManager;
import com.github.foxnic.dao.relation.RelationSolver;
import com.github.foxnic.dao.sql.SQLParser;
import com.github.foxnic.sql.GlobalSettings;
import com.github.foxnic.sql.data.ExprDAO;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.expr.*;
import com.github.foxnic.sql.meta.DBType;
import com.github.foxnic.sql.treaty.DBTreaty;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DAO implements ExprDAO {

	private static ArrayList<DAO> INSTANCES = new ArrayList<>();
	private static HashMap<String, DAO> INSTANCE_MAP = new HashMap<>();
 
	protected ThreadLocal<SQL> latestSQL = new ThreadLocal<SQL>();

	private RelationManager relationManager;

	public RelationManager getRelationManager() {
		return relationManager;
	}

	public void setRelationManager(RelationManager relationManager) {
		this.relationManager = relationManager;
	}

	protected static void regist(DAO dao) {
		if (!INSTANCES.contains(dao)) {
			GlobalSettings.DEFAULT_SQL_DIALECT=dao.getSQLDialect();
			INSTANCES.add(dao);
			INSTANCE_MAP.put(dao.getDBConnectionIdentity(), dao);
		}
	}

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
		regist(this);
	}

	public DataSource getDataSource() {
		return datasource;
	}
 
	/**
	 * 乞丐版快速连接数据库 ，默认使用 DruidDataSource 
	 * @param driver 数据库驱动
	 * @param url 连接字符串
	 * @param user 账户
	 * @param password 密码
	 */
	public void setDataSource(String driver,String url,String user,String password)
	{
		DruidDataSource ds = new DruidDataSource();
		ds.setDriverClassName(driver);
		ds.setUrl(url);
		ds.setUsername(user);
		ds.setPassword(password);
		this.setDataSource(ds);
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

	private boolean isPrintSQL = false;
	private static ThreadLocal<Boolean> isPrintThreadSQL = new ThreadLocal<Boolean>();
	private static ThreadLocal<String> sqlPrintTitle = new ThreadLocal<String>();
	private static ThreadLocal<Boolean> isPrintSQLSimple = new ThreadLocal<Boolean>();

	/**
	 * 是否打印语句
	 * 
	 * @return 是否打印语句
	 */
	public boolean isPrintSQL() {
		Boolean isPrintThreadSQL=DAO.isPrintThreadSQL.get();
		if(isPrintThreadSQL==null) {
			return isPrintSQL;
		} else {
			return isPrintThreadSQL;
		}
	}

	/**
	 * 设置是否打印SQL语句，打印SQL语句对性能有影响
	 * 
	 * @param isPrintSQL 是否打印语句
	 */
	public void setPrintSQL(boolean isPrintSQL) {
		this.isPrintSQL = isPrintSQL;
	}
	
	/**
	 * 设置当前线程是否打印SQL语句。<br>打印SQL语句对性能有影响 ，线程级别
	 * 
	 * @param isPrintSQL 是否打印语句
	 */
	public void setPrintThreadSQL(boolean isPrintSQL) {
		this.isPrintThreadSQL.set(isPrintSQL);
	}
	
	/**
	 * 是否使用简洁模式打印 SQL 语句
	 * 
	 * @param simple 是否使用简洁模式打印 SQL 语句
	 */
	public void setPrintSQLSimple(boolean simple) {
		this.isPrintSQLSimple.set(simple);
	}
	
	/**
	 * 设置打印语句的位置
	 * 
	 * @param title 标题
	 */
	public void setPrintSQLTitle(String title) {
		this.sqlPrintTitle.set(title);
	}
	
	/**
	 * 设置打印语句的位置
	 * 
	 */
	public Boolean isPrintSQLSimple() {
		return this.isPrintSQLSimple.get();
	}
	
	/**
	 * 设置打印语句的位置
	 * 
	 */
	public String getPrintSQLTitle() {
		return this.sqlPrintTitle.get();
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
 
	
	/**
	 * 刷新Meta信息
	 */
	public abstract void refreshMeta();

	/**
	 * 获得数据库中的字段描述信息
	 * 
	 * @param table  表名
	 * @param column 列名
	 * @return DBColumnMeta
	 */
	public abstract DBColumnMeta getTableColumnMeta(String table, String column);

	/**
	 * 获得数据库中的表清单
	 * 
	 * @return 表清单
	 */
	public abstract String[] getTableNames();
	

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
			return SQLParser.getCountSQL(sql,name);
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
	
	
	/**
	 * 批量执行
	 * 
	 * @param sqls sql语句
	 * @return 批量执行结果
	 */
	public abstract int[] batchExecute(String... sqls);

	/**
	 * 批量执行
	 * 
	 * @param sqls sql语句
	 * @return 批量执行结果
	 */
	public abstract int[] batchExecute(List<SQL> sqls);

	/**
	 * 批量执行
	 * 
	 * @param sql    sql语句
	 * @param pslist 参数，可通过BatchParamBuilder构建
	 * @return 批量执行结果
	 */
	public abstract int[] batchExecute(String sql, List<Object[]> pslist);

	/**
	 * 批量执行
	 * 
	 * @param sqls sql语句
	 * @return 批量执行结果
	 */
	public abstract int[] batchExecute(SQL... sqls);
	
	
	/**
	 * 一次执行多个语句，如果有事务管理器，则事务内，否则非事务<br>
	 * 返回执行的语句数量（最后执行的语句序号），如果未成功执行，返回 0
	 * 
	 * @param sqls sql语句
	 * @return 执行成功的语句数量
	 */
	public abstract Integer multiExecute(String... sqls);

	/**
	 * 一次执行多个语句，如果有事务管理器，则事务内，否则非事务 返回执行的语句数量（最后执行的语句序号），如果未成功执行，返回 0
	 * 
	 * @param sqls sql语句
	 * @return 执行成功的语句数量
	 */
	public abstract Integer multiExecute(SQL... sqls);

	/**
	 * 一次执行多个语句，如果有事务管理器，则事务内，否则非事务 返回执行的语句数量（最后执行的语句序号），如果未成功执行，返回 0
	 * 
	 * @param sqls SQL的集合，内部元素是String类型或SQL类型，或者是toStirng后返回一个可执行是SQL字符串
	 * @return 执行成功的语句数量
	 */
	public abstract Integer multiExecute(List<Object> sqls);
	
	
	/**
	 * 查询记录集
	 * 
	 * @param sql    sql语句
	 * @return RcdSet
	 */
	public abstract RcdSet query(SQL se);
 
	/**
	 * 查询记录集
	 * 
	 * @param sql    sql语句
	 * @param ps 参数
	 * @return RcdSet
	 */
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
	 * @param se    sql语句
	 * @param pageSize   每页行数
	 * @param pageIndex  页码
	 * @return RcdSet
	 */
	public abstract RcdSet queryPage(SQL se, int pageSize, int pageIndex);
	
	/**
	 * 分页查询记录集
	 * 
	 * @param sql    sql语句
	 * @param size   每页行数
	 * @param index  页码
	 * @param params 参数
	 * @return RcdSet
	 */
	@SuppressWarnings("rawtypes")
	public abstract RcdSet queryPage(String sql, int size, int index, Map params);
 
	/**
	 * 分页查询记录集
	 * 
	 * @param sql    sql语句
	 * @param size   每页行数
	 * @param index  页码
	 * @param params 参数
	 * @return RcdSet
	 */
	public abstract RcdSet queryPage(String sql, int size, int index, Object... params);
	

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
	 * 查询单个BigDecimal
	 * 
	 * @param sql sql语句
	 * @return 值
	 */
	public abstract BigDecimal queryBigDecimal(SQL sql);

	/**
	 * 查询单个BigDecimal
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public abstract BigDecimal queryBigDecimal(String sql, Object... params);

	/**
	 * 查询单个BigDecimal
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public abstract BigDecimal queryBigDecimal(String sql, Map<String, Object> params);
	
	
	/**
	 * 查询单个Double值
	 * 
	 * @param sql sql语句
	 * @return 值
	 */
	public abstract Double queryDouble(SQL sql);

	/**
	 * 查询单个Double值
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public abstract Double queryDouble(String sql, Object... params);

	/**
	 * 查询单个Double值
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public abstract Double queryDouble(String sql, Map<String, Object> params);

	/**
	 * 查询单个Timestamp值
	 * 
	 * @param sql sql语句
	 * @return 值
	 */
	public abstract Timestamp queryTimestamp(SQL sql);

	/**
	 * 查询单个Timestamp 值
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public abstract Timestamp queryTimestamp(String sql, Object... params);

	/**
	 * 查询单个Timestamp 值
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public abstract Timestamp queryTimestamp(String sql, Map<String, Object> params);
	
	
	/**
	 * 查询单个 Time 值
	 * 
	 * @param sql sql语句
	 * @return 值
	 */
	public abstract Time queryTime(SQL sql);

	/**
	 * 查询单个 Time 值
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public abstract Time queryTime(String sql, Object... params);
	
	/**
	 * 查询单个 Time 值
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public abstract Time queryTime(String sql, Map<String, Object> params);
	

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
	public abstract Rcd queryRecord(String sql, Map<String, Object> params);

	/**
	 * 查询单个记录
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return Rcd
	 */
	public abstract Rcd queryRecord(String sql, Object... params);
	
	
	
	/**
	 * 根据 sample 属性值查询匹配的数据，匹配多个时返回第一个<br>
	 * null值属性不参与条件判断
	 * 
	 * @param <T>    实体类型
	 * @param sample 查询样例
	 * @return 传入与返回的不是同一个实体
	 */
	public abstract <T> T queryEntity(T sample);
	
	/**
	 * 根据 sample 属性值查询匹配的数据，匹配多个时返回第一个<br>
	 * null值属性不参与条件判断
	 * 
	 * @param <T>    实体类型
	 * @param sample 查询样例
	 * @param table 指定数据表
	 * @return 传入与返回的不是同一个实体
	 */
	public abstract <T> T queryEntity(T sample,String table);
	
	
	/**
	 * 按主键值查询单个实体<br>
	 * 如果对应的表未设置主键或存在多个主键，则抛出异常
	 * 
	 * @param <T>    实体类型
	 * @param id 主键值
	 * @return 返回实体对象
	 */
	public abstract <T> T queryEntity(Class<T> type,Object id);
	
	/**
	 * 按主键值查询单个实体<br>
	 * 如果对应的表未设置主键或存在多个主键，则抛出异常
	 * 
	 * @param <T>    实体类型
	 * @param id 主键值
	 * @param table 指定数据表
	 * @return 返回实体对象
	 */
	public abstract <T> T queryEntity(Class<T> type,String table,Object id);
	
	/**
	 * 按查询条件返回单个实体
	 * 
	 * @param <T>    实体类型
	 * @param type 实体类型
	 * @param ce 条件表达式
	 * @return 返回实体对象
	 */
	public  abstract <T> T queryEntity(Class<T> type,ConditionExpr ce);
	
	/**
	 * 按查询条件返回单个实体
	 * 
	 * @param <T>    实体类型
	 * @param type 实体类型
	 * @param table 指定数据表
	 * @param ce 条件表达式
	 * @return 返回实体对象
	 */
	public  abstract <T> T queryEntity(Class<T> type,String table,ConditionExpr ce);
	
	/**
	 * 按查询条件返回单个实体
	 * 
	 * @param <T>    实体类型
	 * @param type 实体类型
	 * @param condition 条件表达式
	 * @param params 条件表达式的参数
	 * @return 返回实体对象
	 */
	public abstract <T> T queryEntity(Class<T> type,String condition,Object... params);
	
	/**
	 * 按查询条件返回单个实体
	 * 
	 * @param <T>    实体类型
	 * @param type 实体类型
	 * @param table 指定数据表
	 * @param condition 条件表达式
	 * @param params 条件表达式的参数
	 * @return 返回实体对象
	 */
	public abstract <T> T queryEntity(Class<T> type,String table,String condition,Object... params);
	
 
	 

	/**
	 * 根据 sample 中的已有信息从数据库载入对应的实体集<br>
	 * 
	 * @param <T>    实体类型
	 * @param sample 查询样例
	 * @return List
	 */
	@SuppressWarnings("rawtypes")
	public abstract <T> List<T> queryEntities(T sample);


	public abstract <T> List<T> queryEntities(Class<T> entityType, SQL sql);
	
	
	/**
	 * 根据 sample 中的已有信息从数据库载入对应的实体集<br>
	 * 
	 * @param <T>    实体类型
	 * @param tables 指定表名
	 * @param sample 查询样例
	 * @return List
	 */
	@SuppressWarnings("rawtypes")
	public abstract <T> List<T> queryEntities(T sample,String tables);

	/**
	 * 根据sample中的已有信息从数据库载入对应的实体集
	 * 
	 * @param <T>       实体类型
	 * @param sample    查询样例
	 * @param pageSize  每页行数
	 * @param pageIndex 页码
	 * @return PagedList
	 */
	@SuppressWarnings("rawtypes")
	public abstract <T> PagedList<T> queryPagedEntities(T sample, int pageSize, int pageIndex);

	/**
	 * 根据sample中的已有信息从数据库载入对应的实体集
	 *
	 * @param <T>       实体类型
	 * @param entityType    查询样例
	 * @param sql    SQL 语句
	 * @param pageSize  每页行数
	 * @param pageIndex 页码
	 * @return PagedList
	 */
	public abstract <T> PagedList<T> queryPagedEntities(Class<T> entityType,SQL sql, int pageSize, int pageIndex);
	/**
	 * 根据sample中的已有信息从数据库载入对应的实体集
	 * 
	 * @param <T>       实体类型
	 * @param sample    查询样例
	 * @param table    指定查询的数据表
	 * @param pageSize  每页行数
	 * @param pageIndex 页码
	 * @return PagedList
	 */
	@SuppressWarnings("rawtypes")
	public abstract <T> PagedList<T> queryPagedEntities(T sample,String table, int pageSize, int pageIndex);

	
	/**
	 * 查询实体集
	 * 
	 * @param <T>       实体类型
	 * @param entitySetType 实体集类型
	 * @param ce  条件表达式
	 * @return List
	 */
	public abstract <T> List<T> queryEntities(Class<T> type,ConditionExpr ce);
	
	/**
	 * 查询实体集
	 * 
	 * @param <T>       实体类型
	 * @param type 实体集类型
	 * @param table 指定查询的数据表
	 * @param ce  条件表达式
	 * @return List
	 */
	public abstract <T> List<T> queryEntities(Class<T> type,String table,ConditionExpr ce);
	
	
	/**
	 * 查询实体集
	 * 
	 * @param <T>       实体类型
	 * @param type    实体集类型
	 * @param condition 条件表达式
	 * @param params 条件表达式参数
	 * @return List
	 */
	public abstract <T>  List<T> queryEntities(Class<T> type,String condition ,Object... params);
	
	
	/**
	 * 查询实体集
	 * 
	 * @param <T>       实体类型
	 * @param type    实体集类型
	 * @param table 指定查询的数据表
	 * @param condition 条件表达式
	 * @param params 条件表达式参数
	 * @return List
	 */
	public abstract <T>  List<T> queryEntities(Class<T> type,String table,String condition ,Object... params);
	
	/**
	 * 查询实体集分页
	 * 
	 * @param <T>       实体类型
	 * @param type    实体集类型
	 * @param pageSize  每页行数
	 * @param pageIndex 页码
	 * @param ce 条件表达式
	 * @return PagedList
	 */
	public abstract <T> PagedList<T> queryPagedEntities(Class<T> type,int pageSize,int pageIndex,ConditionExpr ce);
	
	
	/**
	 * 查询实体集分页
	 * 
	 * @param <T>       实体类型
	 * @param type    实体集类型
	 * @param table	指定查询的数据表
	 * @param pageSize  每页行数
	 * @param pageIndex 页码
	 * @param ce 条件表达式
	 * @return PagedList
	 */
	public abstract <T> PagedList<T> queryPagedEntities(Class<T> type,String table,int pageSize,int pageIndex,ConditionExpr ce);
	
	/**
	 * 查询实体集分页
	 * 
	 * @param <T>       实体类型
	 * @param type    实体集类型
	 * @param pageSize  每页行数
	 * @param pageIndex 页码
	 * @param condition 条件表达式
	 * @param params 条件表达式参数
	 * @return PagedList
	 */
	public abstract <T> PagedList<T> queryPagedEntities(Class<T> type,int pageSize,int pageIndex,String condition ,Object... params);
	
	
	/**
	 * 查询实体集分页
	 * 
	 * @param <T>       实体类型
	 * @param type    实体集类型
	 * @param table	指定查询的数据表
	 * @param pageSize  每页行数
	 * @param pageIndex 页码
	 * @param condition 条件表达式
	 * @param params 条件表达式参数
	 * @return PagedList
	 */
	public abstract <T> PagedList<T> queryPagedEntities(Class<T> type,String table,int pageSize,int pageIndex,String condition ,Object... params);
	

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
	 * 判断表格是否存在
	 * 
	 * @param table 表名
	 * @return 是否存在
	 */
	public abstract boolean isTableExists(String table);
	
	
	
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
 
	/**
	 * 执行一个Insert语句，并返回某些默认值(自增),如果失败返回-1
	 * 
	 * @param insert insert语句
	 * @return 默认字段的值
	 */
	public abstract long insertAndReturnKey(SQL insert);

	/**
	 * 执行一个Insert语句，并返回某些默认值,如果失败返回-1
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 默认字段的值
	 */
	public abstract long insertAndReturnKey(final String sql, Map<String, Object> params);

	/**
	 * 执行一个Insert语句，并返回某些默认值,如果失败返回-1
	 * 
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 默认字段的值
	 */
	public abstract long insertAndReturnKey(String sql, Object... params);
	
	
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
	 * 获得一个可执行的insert语句构建器，已经被设置DAO
	 * 
	 * @param entityType 实体类型，若无法识别表名，则抛出异常
	 * @return Insert
	 */
	public abstract Insert insert(Class entityType);
	
	
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
	 * @param entityType 实体类型，如无法识别表名，则抛出异常
	 * @return Update语句
	 */
	public abstract Update update(Class entityType);

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
	 * @param entityType 实体类型，若无法识别表名，则抛出异常
	 * @return Delete语句
	 */
	public abstract Delete delete(Class entityType);

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
	 * 获得当前Spring托管的自动事务的事务状态对象
	 * 
	 * @return 事务状态对象
	 */
	public abstract TransactionStatus getCurrentAutoTransactionStatus();

	/**
	 * 获得手动事务的事务状态对象
	 * 
	 * @return 事务状态对象
	 */
	public abstract TransactionStatus getCurrentManualTransactionStatus();

	/**
	 * 获得事务管理器
	 * 
	 * @return 事务管理器
	 */
	public abstract DataSourceTransactionManager getTransactionManager();

	/**
	 * 设置事务管理器
	 * 
	 * @param transactionManager 事务管理器
	 */
	public abstract void setTransactionManager(DataSourceTransactionManager transactionManager);

	/**
	 * 开始一个事务
	 */
	public abstract void beginTransaction();

	/**
	 * 开始一个事务
	 * 
	 * @param propagationBehavior 事务传播行为参数
	 */
	public abstract void beginTransaction(int propagationBehavior);

	/**
	 * 回滚手动事务
	 */
	public abstract void rollback();

	/**
	 * 提交手动事务
	 */
	public abstract void commit();
	
	
	
	/**
	 * 插入 pojo 实体到数据里表
	 * 
	 * @param entity  数据对象
	 * @param table 数表
	 * @return 是否执行成功
	 */
	public abstract boolean insertEntity(Object entity, String table);
	
	
	/**
	 * 插入 pojo 实体到数据里表 , 通过注解识别数据表
	 * 
	 * @param entity  数据对象
	 * @return 是否执行成功
	 */
	public abstract boolean insertEntity(Object entity);

	/**
	 * 根据ID值，更新pojo实体到数据里表<br>
	 * 如果ID值被修改，可导致错误的更新
	 * 
	 * @param entity      数据对象
	 * @param table     数表
	 * @param saveMode 保存模式
	 * @return 是否执行成功
	 */
	public abstract boolean updateEntity(Object entity, String table, SaveMode saveMode);
	
	
	/**
	 * 根据ID值，更新pojo实体到数据里表,根据实体注解自动识别数据表<br>
	 * 如果ID值被修改，可导致错误的更新
	 * 
	 * @param entity      数据对象
	 * @param saveMode 是否保存空值
	 * @return 是否执行成功
	 */
	public abstract boolean updateEntity(Object entity, SaveMode saveMode);
	
	/**
	 * 保存实体数据<br>
	 * 建议使用insertPOJO或updatePOJO以获得更高性能
	 * 
	 * @param entity      数据
	 * @param table     表
	 * @param saveMode 保存模式
	 * @return 是否成功
	 */
	public abstract boolean saveEntity(Object entity, String table, SaveMode saveMode);
	
	
	/**
	 * 保存实体数据，根据注解，自动识别表名<br>
	 * 建议使用insertEntity或updateEntity以获得更高性能
	 * 
	 * @param entity      数据
	 * @param saveMode 保存模式
	 * @return 是否成功
	 */
	public abstract boolean saveEntity(Object entity, SaveMode saveMode);
	
	/**
	 * 以 sample 作为查询条件，从数据库删除对应的记录
	 * 
	 * @param sample 查询样例
	 * @param table  数据表
	 * @return 删除的行数
	 */
	public abstract int deleteEntities(Object sample, String table);
	
	
	/**
	 * 以 sample 作为查询条件，从数据库删除对应的记录<br>
	 * 从注解中识别表名
	 * 
	 * @param sample 查询样例
	 * @return 删除的行数
	 */
	public abstract int deleteEntities(Object sample);
	
	
	/**
	 * 删除符合条件的记录<br>
	 * 从注解中识别表名
	 * 
	 * @param type 指定实体类型
	 * @param ce 条件
	 * @return 删除的行数
	 */
	public abstract int deleteEntities(Class type,ConditionExpr ce);
	
	/**
	 * 删除符合条件的记录<br>
	 * 从注解中识别表名
	 * 
	 * @param type 指定实体类型
	 * @param table 数据表
	 * @param ce 条件
	 * @return 删除的行数
	 */
	public abstract int deleteEntities(Class type,String table,ConditionExpr ce);
	
	/**
	 * 删除实体
	 * 
	 * @param entity 实体
	 * @param table  数据表
	 * @return 是否成功
	 */
	public abstract boolean deleteEntity(Object entity, String table);
	
	/**
	 * 删除实体，通过注解识别表名
	 * 
	 * @param entity 实体
	 * @return 是否成功
	 */
	public abstract boolean deleteEntity(Object entity);
	
	
	/**
	 * 删除符合条件的记录<br>
	 * 从注解中识别表名
	 * 
	 * @param type 指定实体类型
	 * @param ce 条件
	 * @return 删除的行数
	 */
	public int deleteEntity(Class type,ConditionExpr ce) {
		return this.deleteEntities(type, ce);
	}
	
	/**
	 * 实体对象是否存已经在数据表,以主键作为判断依据
	 * 
	 * @param pojo  数据对象
	 * @param table 数据表
	 * @return 是否存在
	 */
	public abstract boolean isEntityExists(Object pojo, String table);
	
	
	/**
	 * 调试查看
	 */
	@Override
	public String toString() {
		initConnectionInfo();
		String info="user="+this.getUserName()+"\nurl="+this.getUrl();
		SQL sql=latestSQL.get();
		if(sql!=null) {
			info+="\nsql="+sql.getSQL();
		}
		return info;

	}
	
	/**
	 * 获得最后执行的语句(线程独立)
	 * */
	public SQL getLatestSQL() {
		return latestSQL.get();
	}
	
	/**
	 * 获得最后执行的语句(线程独立)
	 * */
	public String getLatestSQLString() {
		SQL sql=getLatestSQL();
		if(sql==null) return null;
		return sql.getSQL();
	}

	public DataSource getDatasource() {
		return datasource;
	}

	public String getDbIdentity() {
		return dbIdentity;
	}

	public String getUrl() {
		return url;
	}


	private RelationSolver relationSolver;

	public <E extends Entity,T extends Entity> void join(E po, Class<T> targetType) {
		if(relationSolver==null) relationSolver=new RelationSolver(this);
		relationSolver.join(po,targetType);
	}

	public <E extends Entity,T extends Entity> void join(Collection<E> pos, Class<T> targetType) {
		if(relationSolver==null) relationSolver=new RelationSolver(this);
		relationSolver.join(pos,targetType);
	}

}
