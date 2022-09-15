package com.github.foxnic.dao.spring;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.cache.LocalCache;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.cache.DataCacheManager;
import com.github.foxnic.dao.data.AbstractSet;
import com.github.foxnic.dao.data.*;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.dao.entity.EntityContext;
import com.github.foxnic.dao.entity.EntityUtils;
import com.github.foxnic.dao.excel.DataException;
import com.github.foxnic.dao.exception.TransactionException;
import com.github.foxnic.dao.filter.SQLFilterChain;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBMetaData;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.procedure.StoredProcedure;
import com.github.foxnic.dao.relation.cache.CacheInvalidEventType;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.dao.spec.DBSequence;
import com.github.foxnic.dao.sql.DruidUtils;
import com.github.foxnic.dao.sql.SQLBuilder;
import com.github.foxnic.dao.sql.SQLParser;
import com.github.foxnic.dao.sql.expr.Template;
import com.github.foxnic.dao.sql.loader.SQLoader;
import com.github.foxnic.sql.exception.DBMetaException;
import com.github.foxnic.sql.expr.*;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

public abstract class SpringDAO extends DAO {

	private static BeanNameUtil NC = new BeanNameUtil();

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		if (this.jdbcTemplate == null)
			this.jdbcTemplate = new JdbcTemplate(this.getDataSource());
		return this.jdbcTemplate;
	}

	private NamedParameterJdbcTemplate njdbcTemplate;

	public NamedParameterJdbcTemplate getNamedJdbcTemplate() {
		if (this.njdbcTemplate == null)
			this.njdbcTemplate = new NamedParameterJdbcTemplate(this.getDataSource());
		return this.njdbcTemplate;
	}

	private Boolean isDynamicDataSource = null;

	protected boolean isDynamicDataSource() {
		if (isDynamicDataSource != null)
			return isDynamicDataSource;
		isDynamicDataSource = this.getDataSource() instanceof AbstractRoutingDataSource;
		return isDynamicDataSource;
	}

	private MethodAccess abstractRoutingDataSourceMethodAccess = null;
	private int determineTargetDataSourceIndex = -1;

	/**
	 * 多数据源情况下，实际使用的最终数据源
	 *
	 * @return 逻辑值
	 */
	protected DataSource getFinalDataSource() {
		if (isDynamicDataSource()) {
			if (determineTargetDataSourceIndex == -1) {
				abstractRoutingDataSourceMethodAccess = MethodAccess.get(AbstractRoutingDataSource.class);
				determineTargetDataSourceIndex = abstractRoutingDataSourceMethodAccess
						.getIndex("determineTargetDataSource");
			}
			return (DataSource) abstractRoutingDataSourceMethodAccess.invoke(super.getDataSource(),
					determineTargetDataSourceIndex);
		} else {
			return super.getDataSource();
		}
	}

	/**
	 * 根据ID获得SQL
	 *
	 * @param id 在tql文件中定义的sqlid(不需要以#开始)
	 * @return sql
	 */
	@Override
	public String getSQL(String id) {
		return SQLoader.getSQL(id,null,this.getDBType());
	}

	/**
	 * 根据ID获得SQL
	 *
	 * @param id 在tql文件中定义的sqlid(不需要以#开始)
	 * @param templateKVs Map 对象，或实体对象，用于SQL模版，JFinal Enjoy
	 * @return sql
	 */
	@Override
	public String getSQL(String id,Object templateKVs) {
		return SQLoader.getSQL(id,templateKVs,this.getDBType());
	}

	public Template getTemplate(String id) {
		Template template=new Template(this.getSQL(id));
		return template;
	}

	public Template getTemplate(String id,Object... ps) {
		Template template=new Template(this.getSQL(id),ps);
		return template;
	}

	public Template getTemplate(String id,Map<String,Object> ps) {
		Template template=new Template(this.getSQL(id),ps);
		return template;
	}

//	/**
//	 * 根据ID获得SQL
//	 *
//	 * @param ps 参数
//	 * @param id 在tql文件中定义的sqlid(不需要以#开始)
//	 * @return SQL
//	 */
//	public SQL getSQL(String id, Object... ps) {
//		String sql = this.getSQL(id);
//		return new Expr(sql, ps).setSQLDialect(getSQLDialect());
//	}
//
//	/**
//	 * 根据ID获得SQL
//	 *
//	 * @param id 在tql文件中定义的sqlid(不需要以#开始)
//	 * @param ps 参数
//	 * @return SQL
//	 */
//	public SQL getSQL(String id, Map<String, Object> ps) {
//		String sql = this.getSQL(id);
//		return new Expr(sql, ps).setSQLDialect(this.getSQLDialect());
//	}

	/**
	 * 分页查询记录集
	 *
	 * @param sql   sql语句
	 * @param size  每页行数
	 * @param index 页码
	 * @return RcdSet
	 */
	public RcdSet queryPage(SQL sql, int size, int index) {
		sql.setSQLDialect(this.getSQLDialect());
		return queryPageWithArrayParameters(false, sql.getListParameterSQL(), size, index, sql.getListParameters());
	}

	/**
	 * 分页查询记录集
	 *
	 * @param sql    sql语句
	 * @param size   每页行数
	 * @param index  页码
	 * @param params 参数
	 * @return RcdSet
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RcdSet queryPage(String sql,int size,int index,Map params)
	{
		return queryPageWithMapParameters(false,sql, size, index, params);
	}

	/**
	 * 分页查询记录集
	 *
	 * @param sql    sql语句
	 * @param size   每页行数
	 * @param index  页码
	 * @param params 参数
	 * @return RcdSet
	 */
	@Override
	public  RcdSet queryPage(String sql,int size,int index,Object... params)
	{
		return queryPageWithArrayParameters(false,sql, size, index,params);
	}

	protected SQLFilterChain chain = new SQLFilterChain(this);

	/**
	 * @param fixed 指定单行查询，fixed = true时 不查询count
	 */
	@SuppressWarnings("unchecked")
	private RcdSet queryPageWithArrayParameters(boolean fixed, String sql, int pageSize, int pageIndex,
			Object... params) {

		if (sql.startsWith("#")) {
			sql = getSQL(sql);
		}

		Expr se = new Expr(sql, params);
		se.setSQLDialect(this.getDBType().getSQLDialect());
		latestSQL.set(se);
		SQL resultSql = chain.doFilter(se);
		resultSql.setSQLDialect(this.getDBType().getSQLDialect());

		sql = resultSql.getListParameterSQL();
		params = resultSql.getListParameters();

		if (pageSize < 0) {
			pageSize = 0;
		}
		if (pageSize == 0) {
			pageIndex = 1;
		}

		RcdSet set = new RcdSet();
		set.setDBConnectionIdentity(this.getDBConnectionIdentity());
		set.flagTimePoint();
		int totalPage = 1;
		int totalRecord = 0;
		if (pageSize > 0) {
			if (!fixed) {
				SQL countSql = this.getCountSQL(new Expr(sql, params).setSQLDialect(this.getSQLDialect()), "X");
				final Object[] ps = Utils.filterParameter(countSql.getListParameters());
				if (this.isPrintSQL()) {
					totalRecord = new SQLPrinter<Integer>(this, countSql, countSql) {
						@Override
						protected Integer actualExecute() {
							return getJdbcTemplate().queryForObject(countSql.getListParameterSQL(), Integer.class, ps);
						}
					}.execute();
				} else {
					totalRecord = this.getJdbcTemplate().queryForObject(countSql.getListParameterSQL(), Integer.class,
							ps);
				}
			} else {
				totalRecord = pageSize;
			}
			totalPage = (totalRecord % pageSize) == 0 ? (totalRecord / pageSize) : (totalRecord / pageSize + 1);
			latestSQL.set(new Expr(sql, params).setSQLDialect(this.getSQLDialect()));
			if (totalRecord > 0) {
				set = (RcdSet) this.getPageSet(fixed, set, sql, pageIndex, pageSize, params);
			}
		} else {
			se = new Expr(sql, params);
			se.setSQLDialect(this.getSQLDialect());
			latestSQL.set(se);

			final String fsql = sql;
			final Object[] ps = Utils.filterParameter(params);
			final PreparedStatementSetter setter = new ArgumentPreparedStatementSetter(ps);
			final RcdSet fset = set;

			if (this.isPrintSQL()) {
				new SQLPrinter<Object>(this, se, se) {
					@Override
					protected Object actualExecute() {
						getJdbcTemplate().query(fsql, setter,
								new RcdResultSetExtractor(new RcdRowMapper(fset, 0, getQueryLimit())));
						return fset;
					}
				}.execute();
			} else {
				this.getJdbcTemplate().query(fsql, setter,
						new RcdResultSetExtractor(new RcdRowMapper(fset, 0, this.getQueryLimit())));
			}
		}
		se = new Expr(sql, params);
		se.setSQLDialect(this.getSQLDialect());
		set.setPageInfos(pageSize, pageIndex, totalRecord, totalPage, se);
		return set;

	}

	/**
	 * 如果pageSize不为0,则不分页
	 *
	 * @param fixed 指定单行查询，fixed = true时 不查询count
	 */
	@SuppressWarnings({ "unchecked" })
	private RcdSet queryPageWithMapParameters(boolean fixed, String sql, int pageSize, int pageIndex,
			Map<String, Object> params) {

		if (params == null) {
			params = new HashMap<String, Object>(5);
		}

		if (sql.startsWith("#")) {
			sql = getSQL(sql);
		}
		Expr se = new Expr(sql, params);
		se.setSQLDialect(this.getDBType().getSQLDialect());
		latestSQL.set(se);
		SQL resultSql = chain.doFilter(se);
		resultSql.setSQLDialect(this.getDBType().getSQLDialect());

		sql = resultSql.getNamedParameterSQL();
		params = resultSql.getNamedParameters();

		if (pageSize < 0) {
			pageSize = 0;
		}
		if (pageSize == 0) {
			pageIndex = 1;
		}

		RcdSet set = new RcdSet();
		set.setDBConnectionIdentity(this.getDBConnectionIdentity());
		set.flagTimePoint();

		int totalPage = 1;

		int totalRecord = 0;
		if (pageSize > 0) {
			if (!fixed) {
				SQL countSql = getCountSQL(new Expr(sql, params).setSQLDialect(this.getSQLDialect()), "X");
				Map<String, Object> ps = Utils.filterParameter(countSql.getNamedParameters());
				List<Map<String, Object>> list = null;
				if (this.isPrintSQL()) {
					list = new SQLPrinter<List<Map<String, Object>>>(this, countSql, countSql) {
						@Override
						protected List<Map<String, Object>> actualExecute() {
							return getNamedJdbcTemplate().queryForList(countSql.getNamedParameterSQL(), ps);
						}
					}.execute();

				} else {
					list = getNamedJdbcTemplate().queryForList(countSql.getNamedParameterSQL(), ps);
				}
				totalRecord = DataParser.parseInteger((list.get(0).get("X")));
			} else {
				totalRecord = pageSize;
			}
			totalPage = (totalRecord % pageSize) == 0 ? (totalRecord / pageSize) : (totalRecord / pageSize + 1);

			latestSQL.set(new Expr(sql, params).setSQLDialect(this.getSQLDialect()));

			if (totalRecord > 0) {
				set = (RcdSet) this.getPageSet(fixed, set, sql, pageIndex, pageSize, params);
			}

		} else {
			se = new Expr(sql, params);
			se.setSQLDialect(this.getSQLDialect());
			latestSQL.set(se);
			final String fsql = sql;
			final Map<String, Object> ps = Utils.filterParameter(params);
			final RcdSet fset = set;

			if (this.isPrintSQL()) {
				new SQLPrinter<Object>(this, se, se) {
					@Override
					protected Object actualExecute() {
						getNamedJdbcTemplate().query(fsql, ps,
								new RcdResultSetExtractor(new RcdRowMapper(fset, 0, getQueryLimit())));
						return fset;
					}
				}.execute();
			} else {
				this.getNamedJdbcTemplate().query(fsql, ps,
						new RcdResultSetExtractor(new RcdRowMapper(fset, 0, this.getQueryLimit())));
			}
		}

		if (totalRecord == 0) {
			totalRecord = set.size();
		}
		se = new Expr(sql, params);
		se.setSQLDialect(this.getSQLDialect());
		set.setPageInfos(pageSize, pageIndex, totalRecord, totalPage, se);
		return set;
	}

	protected abstract AbstractSet getPageSet(boolean fixed, AbstractSet set, String sql, int pageIndex, int pageSize,
			Map<String, Object> params);

	protected abstract AbstractSet getPageSet(boolean fixed, AbstractSet set, String sql, int pageIndex, int pageSize,
			Object... params);

	/**
	 * 执行一个SQL语句
	 *
	 * @param sql sql语句
	 * @return 影响的行数
	 */
	@Override
	public Integer execute(SQL sql) {
		sql.setSQLDialect(this.getSQLDialect());
		return execute(sql.getListParameterSQL(), sql.getListParameters());
	}

	/**
	 * 执行一个SQL语句
	 *
	 * @param sql sql语句
	 * @param ps  参数
	 * @return 影响的行数
	 */
	@Override
	public Integer execute(String sql, Map<String, Object> ps) {

		if (sql.startsWith("#")) {
			sql = getSQL(sql);
		}
		Expr se = new Expr(sql, ps);
		se.setSQLDialect(this.getDBType().getSQLDialect());

		latestSQL.set(se);
		SQL resultSql = chain.doFilter(se);
		resultSql.setSQLDialect(this.getDBType().getSQLDialect());
		Map<String, Object> nps = Utils.filterParameter(resultSql.getNamedParameters());
		if (this.isPrintSQL()) {
			return new SQLPrinter<Integer>(this, se, resultSql) {
				@Override
				protected Integer actualExecute() {
					return getNamedJdbcTemplate().update(resultSql.getNamedParameterSQL(), nps);
				}
			}.execute();
		} else {
			return getNamedJdbcTemplate().update(resultSql.getNamedParameterSQL(), nps);
		}

	}

	/**
	 * 执行一个SQL语句
	 *
	 * @param sql sql语句
	 * @param ps  参数
	 * @return 影响的行数
	 */
	@Override
	public Integer execute(String sql, Object... ps) {

		if (sql.startsWith("#")) {
			sql = getSQL(sql);
		}

		Expr se = new Expr(sql, ps);
		se.setSQLDialect(this.getDBType().getSQLDialect());

		latestSQL.set(se);
		final SQL resultSql = chain.doFilter(se);
		resultSql.setSQLDialect(this.getDBType().getSQLDialect());
		final Object[] pps = Utils.filterParameter(resultSql.getListParameters());
		//
		if (this.isPrintSQL()) {
			return new SQLPrinter<Integer>(this, se, resultSql) {
				@Override
				protected Integer actualExecute() {
					return getJdbcTemplate().update(resultSql.getListParameterSQL(), pps);
				}
			}.execute();
		} else {
			try {
				return getJdbcTemplate().update(resultSql.getListParameterSQL(), pps);
			} catch (Exception e) {
				Logger.error("语句执行错误(" + e.getMessage() + ")，\n语句：" + se.getSQL());
				throw e;
			}
		}

	}

	/**
	 * 批量执行
	 *
	 * @param sqls sql语句
	 * @return 批量执行结果
	 */
	@Override
	public int[] batchExecute(String... sqls) {
		Expr se = null;
		SQL resultSql = null;
		for (String sql : sqls) {
			if (sql.startsWith("#")) {
				sql = getSQL(sql);
			}
			se = new Expr(sql);
			se.setSQLDialect(this.getDBType().getSQLDialect());
			latestSQL.set(se);
			resultSql = chain.doFilter(se);
			resultSql.setSQLDialect(this.getDBType().getSQLDialect());
			sql = resultSql.getSQL();

		}

		if (this.isPrintSQL()) {
			return new SQLPrinter<int[]>(this, se, resultSql) {
				@Override
				protected int[] actualExecute() {
					return getJdbcTemplate().batchUpdate(sqls);
				}
			}.execute();

		} else {
			return getJdbcTemplate().batchUpdate(sqls);
		}

	}

	/**
	 * 批量执行
	 *
	 * @param sqls sql语句
	 * @return 批量执行结果
	 */
	@Override
	@Transactional
	public int[] batchExecute(List<SQL> sqls) {
		SQL[] resultSqls = sqls.toArray(new SQL[sqls.size()]);
		return batchExecute(resultSqls);
	}

	/**
	 * 批量执行
	 *
	 * @param sql    sql语句
	 * @param pslist 参数，可通过BatchParamBuilder构建
	 * @return 批量执行结果
	 */
	@Override
	public int[] batchExecute(String sql, List<Object[]> pslist) {
		if (pslist == null || pslist.size() == 0)
			return new int[0];
		ArrayList<Object[]> pslist2 = new ArrayList<Object[]>();
		String sql2 = null;
		Expr se = null;
		SQL resultSql = null;
		for (Object[] ps : pslist) {
			se = new Expr(sql, ps);
			se.setSQLDialect(this.getDBType().getSQLDialect());
			latestSQL.set(se);
			resultSql = chain.doFilter(se);
			resultSql.setSQLDialect(this.getDBType().getSQLDialect());
			if (sql2 == null) {
				sql2 = resultSql.getListParameterSQL();
			}
			pslist2.add(Utils.filterParameter(resultSql.getListParameters()));
		}

		final String tmp = sql2;
		if (this.isPrintSQL() && se != null) {
			return new SQLPrinter<int[]>(this, se, resultSql) {
				@Override
				protected int[] actualExecute() {
					return getJdbcTemplate().batchUpdate(tmp, pslist2);
				}
			}.execute();

		} else {
			return getJdbcTemplate().batchUpdate(sql2, pslist2);
		}

	}

	/**
	 * 批量执行
	 *
	 * @param sqls sql语句
	 * @return 批量执行结果
	 */
	@Override
	@Transactional
	public int[] batchExecute(SQL... sqls) {
		// 进行分组
		HashMap<String, List<Object[]>> eSqls = new HashMap<String, List<Object[]>>(sqls.length / 3);
		for (SQL sql : sqls) {
			sql.setSQLDialect(this.getSQLDialect());
			String psql = sql.getListParameterSQL();
			if (!eSqls.containsKey(psql)) {
				eSqls.put(psql, new ArrayList<Object[]>());
			}
			eSqls.get(psql).add(sql.getListParameters());
		}

		// 分组批量执行
		int[] result = new int[sqls.length];
		int i = 0;
		for (String sql : eSqls.keySet()) {
			int[] x = batchExecute(sql, eSqls.get(sql));
			for (int j = 0; j < x.length; j++) {
				result[i + j] = x[j];
			}
		}
		return result;
	}

	/**
	 * 一次执行多个语句，如果有事务管理器，则事务内，否则非事务<br>
	 * 返回执行的语句数量（最后执行的语句序号），如果未成功执行，返回 0
	 *
	 * @param sqls sql语句
	 * @return 执行成功的语句数量
	 */
	public Integer multiExecute(String... sqls) {
		this.beginTransaction();

		JdbcTemplate jdbcTemplate = new JdbcTemplate(this.getDataSource());

		int i = 0;
		for (String sql : sqls) {
			try {

				if (sql.startsWith("#")) {
					sql = getSQL(sql);
				}
				Expr se = new Expr(sql);
				se.setSQLDialect(this.getDBType().getSQLDialect());
				latestSQL.set(se);
				SQL resultSql = chain.doFilter(se);
				resultSql.setSQLDialect(this.getDBType().getSQLDialect());

				if (this.isPrintSQL()) {
					new SQLPrinter<Integer>(this, se, resultSql) {
						@Override
						protected Integer actualExecute() {
							return jdbcTemplate.update(resultSql.getListParameterSQL(),
									Utils.filterParameter(resultSql.getListParameters()));
						}
					}.execute();

				} else {
					jdbcTemplate.update(resultSql.getListParameterSQL(),
							Utils.filterParameter(resultSql.getListParameters()));
				}
				i += 1;

			} catch (Exception e) {
				Logger.exception(e);
				this.rollback();
				return i;
			}
		}
		this.commit();
		return i;
	}

	/**
	 * 一次执行多个语句，如果有事务管理器，则事务内，否则非事务 <br>
	 * 返回被成功执行的语句数量（最后执行的语句序号减1），如果未成功执行，返回 0
	 *
	 * @param sqls sql语句
	 * @return 执行成功的语句数量
	 */
	public Integer multiExecute(SQL... sqls) {

		this.beginTransaction();

		JdbcTemplate jdbcTemplate = new JdbcTemplate(this.getDataSource());

		int i = 0;
		for (SQL sql : sqls) {

			sql.setSQLDialect(this.getDBType().getSQLDialect());
			latestSQL.set(sql);
			SQL resultSql = chain.doFilter(sql);
			resultSql.setSQLDialect(this.getDBType().getSQLDialect());
			try {
				if (this.isPrintSQL()) {
					new SQLPrinter<Integer>(this, sql, resultSql) {
						@Override
						protected Integer actualExecute() {
							return jdbcTemplate.update(resultSql.getListParameterSQL(),
									Utils.filterParameter(resultSql.getListParameters()));
						}
					}.execute();

				} else {
					jdbcTemplate.update(resultSql.getListParameterSQL(),
							Utils.filterParameter(resultSql.getListParameters()));
				}
				i++;
			} catch (Exception e) {
				Logger.exception(e);
				this.rollback();
				return i;
			}
		}

		this.commit();

		return i;
	}

	/**
	 * 一次执行多个语句，如果有事务管理器，则事务内，否则非事务 返回执行的语句数量（最后执行的语句序号），如果未成功执行，返回 0
	 *
	 * @param sqls SQL的集合，内部元素是String类型或SQL类型，或者是toStirng后返回一个可执行是SQL字符串
	 * @return 执行成功的语句数量
	 */
	public Integer multiExecute(List<Object> sqls) {
		ArrayList<SQL> list = new ArrayList<SQL>();
		for (Object sql : sqls) {
			if (sql == null) {
				continue;
			}
			if (sql instanceof String) {
				String strsql = sql + "";
				if (strsql.startsWith("#")) {
					strsql = getSQL(strsql);
				}
				list.add(new Expr(strsql).setSQLDialect(this.getSQLDialect()));
			} else if (sql instanceof SQL) {
				list.add((SQL) sql);
			} else {
				list.add(new Expr(sql + "").setSQLDialect(this.getSQLDialect()));
			}
		}
		return multiExecute(list.toArray(new SQL[list.size()]));
	}

	/**
	 * 查询记录集
	 *
	 * @param sql sql语句
	 * @return RcdSet
	 */
	@Override
	public RcdSet query(SQL sql) {
		sql.setSQLDialect(this.getSQLDialect());
		return queryPageWithArrayParameters(false, sql.getListParameterSQL(), 0, 0, sql.getListParameters());
	}

	/**
	 * 查询记录集
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return RcdSet
	 */
	@Override
	public RcdSet query(String sql, Object... params) {
		return queryPageWithArrayParameters(false, sql, 0, 0, params);
	}

	/**
	 * 查询记录集
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return RcdSet
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public RcdSet query(String sql, Map params) {
		return queryPageWithMapParameters(false, sql, 0, 0, params);
	}

	/**
	 * 查询单个记录
	 *
	 * @param sql sql语句
	 * @return Rcd
	 */
	@Override
	public Rcd queryRecord(SQL sql) {
		return this.queryRecord(sql.getListParameterSQL(), sql.getListParameters());
	}

	/**
	 * 查询单个记录
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return Rcd
	 */
	@Override
	public Rcd queryRecord(String sql, Object... params) {
		RcdSet rs = queryPageWithArrayParameters(true, sql, 1, 1, params);
		if (rs.size() == 0) {
			return null;
		} else {
			return rs.getRcd(0);
		}
	}

	/**
	 * 查询单个记录
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return Rcd
	 */
	@Override
	public Rcd queryRecord(String sql, Map<String, Object> params) {
		RcdSet rs = queryPageWithMapParameters(true,sql, 0, 1, params);
		if (rs.size() == 0) {
			return null;
		} else {
			return rs.getRcd(0);
		}
	}

	/**
	 * 记录是否存已经在数据表,以主键作为判断依据
	 *
	 * @param r                  记录
	 * @param table              表
	 * @param checkWithOrignalId 是否用原始值(setValue前的值/从数据库查询获得的原始值)来核对数据存在性
	 * @return 是否存在
	 */
	public boolean isRecordExits(Rcd r, String table, boolean checkWithOrignalId) {
		DBTableMeta tm = this.getTableMeta(table);
		if (tm == null) {
			throw new DBMetaException("未发现表" + table + "的Meta数据,请认定表名是否正确");
		}

		List<DBColumnMeta> pks = tm.getPKColumns();
		if (pks == null || pks.size() == 0) {
			throw new DBMetaException("数据表" + table + "未定义主键");
		}
		Where where = new Where();
		String cName = null;
		Object value = null;
		for (DBColumnMeta column : pks) {
			cName = column.getColumn();
			value = checkWithOrignalId ? r.getOriginalValue(cName) : r.getValue(cName);
			if (value == null) {
				throw new DataException(table + "." + cName + " 主键值不允许为空");
			}
			where.and(cName + "=?", value);
		}

		where.setSQLDialect(this.getSQLDialect());
		Integer i = this.queryInteger("select count(1) from " + table + " " + where.getListParameterSQL(),
				where.getListParameters());
		return i > 0;

	}

	private LocalCache<String,RcdSet> tableStructureCache=new LocalCache<>();

	/**
	 * 按数据表创建记录
	 * */
	public Rcd createRecord(String table)
	{
		RcdSet rs=tableStructureCache.get(table);
		if(rs==null) {
			rs=new RcdSet(true);
			rs.setDBConnectionIdentity(this.getDBConnectionIdentity());
			QueryMetaData.bind(this.getTableMeta(table),rs,this);
			tableStructureCache.put(table,rs);
		}
		Rcd r=new Rcd(rs);
		r.setNextSaveAction(SaveAction.INSERT);
		return r;
	}

	/**
	 * 记录是否存已经在数据表,以主键作为判断依据
	 *
	 * @param r                  记录
	 * @param checkWithOrignalId 是否用原始值(setValue前的值/从数据库查询获得的原始值)来核对数据存在性
	 * @return 是否存在
	 */
	public boolean isRecordExits(Rcd r, boolean checkWithOrignalId) {
		if (r.getOwnerSet() == null) {
			throw new DBMetaException("当前记录集不属于任何RcdSet,无法识别表名,请调用带表名参数的 updateRecord 方法");
		}
		String[] tables = r.getOwnerSet().getMetaData().getDistinctTableNames();
		if (tables.length != 1) {
			throw new DBMetaException("无法正确识别表名,无法识别表名,请调用带表名参数的 updateRecord 方法");
		}
		return isRecordExits(r, tables[0], checkWithOrignalId);
	}

	/**
	 * 判断表格是否存在
	 *
	 * @param table 表名
	 * @return 是否存在
	 */
	@Override
	public boolean isTableExists(String table) {
		try {
			this.pausePrintThreadSQL();
			Integer i=queryInteger("select 1 from " + table + " where 1=0");
			if(i==null) i=1;
			this.resumePrintThreadSQL();
			return i==1;
		} catch (Exception e) {
			this.resumePrintThreadSQL();
			if(e instanceof CannotGetJdbcConnectionException) {
				Logger.exception("isTableExists",e);
				return false;
			} else if(e instanceof BadSqlGrammarException && e.getCause() instanceof SQLSyntaxErrorException) {
				String msg=e.getCause().getMessage().toLowerCase();
				if(msg.contains("table") && msg.contains("exist")) {
					return false;
				} else {
					Logger.exception("isTableExists",e);
					return false;
				}
			} else {
				Logger.exception("isTableExists1",e);
				return false;
			}
		}
	}

	/**
	 * 查询单个记录
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return Object
	 */
	@Override
	public Object queryObject(String sql, Map<String, Object> params) {
		Rcd tec = queryRecord(sql, params);
		return tec == null ? null : tec.getValue(0);
	}

	/**
	 * 查询单个对象
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return Object
	 */
	@Override
	public Object queryObject(String sql, Object... params) {
		Rcd tec = queryRecord(sql, params);
		return tec == null ? null : tec.getValue(0);
	}

	/**
	 * 查询单个对象
	 *
	 * @param sql sql语句
	 * @return Object
	 */
	@Override
	public Object queryObject(SQL sql) {
		sql.setSQLDialect(this.getSQLDialect());
		return queryObject(sql.getListParameterSQL(), sql.getListParameters());
	}

	/**
	 * 查询单个整数
	 *
	 * @param sql sql语句
	 * @return 值
	 */
	@Override
	public Integer queryInteger(SQL sql) {
		sql.setSQLDialect(this.getSQLDialect());
		return queryInteger(sql.getListParameterSQL(), sql.getListParameters());
	}

	/**
	 * 查询单个整数
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	@Override
	public Integer queryInteger(String sql, Object... params) {
		return DataParser.parseInteger(queryObject(sql, params));
	}

	/**
	 * 查询单个整数
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	@Override
	public Integer queryInteger(String sql, Map<String, Object> params) {
		return DataParser.parseInteger(queryObject(sql, params));
	}

	/**
	 * 查询单个长整型
	 *
	 * @param sql sql语句
	 * @return 值
	 */
	@Override
	public Long queryLong(SQL sql) {
		sql.setSQLDialect(this.getSQLDialect());
		return queryLong(sql.getListParameterSQL(), sql.getListParameters());
	}

	/**
	 * 查询单个长整型
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	@Override
	public Long queryLong(String sql, Object... params) {
		return DataParser.parseLong(queryObject(sql, params));
	}

	/**
	 * 查询单个长整型
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	@Override
	public Long queryLong(String sql, Map<String, Object> params) {
		return DataParser.parseLong(queryObject(sql, params));
	}

	/**
	 * 查询单个日期
	 *
	 * @param sql sql语句
	 * @return 值
	 */
	@Override
	public Date queryDate(SQL sql) {
		sql.setSQLDialect(this.getSQLDialect());
		return queryDate(sql.getListParameterSQL(), sql.getListParameters());
	}

	/**
	 * 查询单个日期
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	@Override
	public Date queryDate(String sql, Object... params) {
		return DataParser.parseDate(queryObject(sql, params));
	}

	/**
	 * 查询单个日期
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	@Override
	public Date queryDate(String sql, Map<String, Object> params) {
		return DataParser.parseDate(queryObject(sql, params));
	}

	/**
	 * 查询单个BigDecimal
	 *
	 * @param sql sql语句
	 * @return 值
	 */
	@Override
	public BigDecimal queryBigDecimal(SQL sql) {
		sql.setSQLDialect(this.getSQLDialect());
		return queryBigDecimal(sql.getListParameterSQL(), sql.getListParameters());
	}

	/**
	 * 查询单个BigDecimal
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	@Override
	public BigDecimal queryBigDecimal(String sql, Object... params) {
		return DataParser.parseBigDecimal(queryObject(sql, params));
	}

	/**
	 * 查询单个BigDecimal
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	@Override
	public BigDecimal queryBigDecimal(String sql, Map<String, Object> params) {
		return DataParser.parseBigDecimal(queryObject(sql, params));
	}

	/**
	 * 查询单个Double值
	 *
	 * @param sql sql语句
	 * @return 值
	 */
	@Override
	public Double queryDouble(SQL sql) {
		sql.setSQLDialect(this.getSQLDialect());
		return queryDouble(sql.getListParameterSQL(), sql.getListParameters());
	}

	/**
	 * 查询单个Double值
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	@Override
	public Double queryDouble(String sql, Object... params) {
		return DataParser.parseDouble(queryObject(sql, params));
	}

	/**
	 * 查询单个Double值
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	@Override
	public Double queryDouble(String sql, Map<String, Object> params) {
		return DataParser.parseDouble(queryObject(sql, params));
	}

	/**
	 * 查询单个Timestamp值
	 *
	 * @param sql sql语句
	 * @return 值
	 */
	public Timestamp queryTimestamp(SQL sql) {
		return DataParser.parseTimestamp(queryObject(sql));
	}

	/**
	 * 查询单个Timestamp 值
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public Timestamp queryTimestamp(String sql, Object... params) {
		return DataParser.parseTimestamp(queryObject(sql, params));
	}

	/**
	 * 查询单个Timestamp 值
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public Timestamp queryTimestamp(String sql, Map<String, Object> params) {
		return DataParser.parseTimestamp(queryObject(sql, params));
	}

	/**
	 * 查询单个 Time 值
	 *
	 * @param sql sql语句
	 * @return 值
	 */
	public Time queryTime(SQL sql) {
		return DataParser.parseTime(queryObject(sql));
	}

	/**
	 * 查询单个 Time 值
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public Time queryTime(String sql, Object... params) {
		return DataParser.parseTime(queryObject(sql, params));
	}

	/**
	 * 查询单个 Time 值
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	public Time queryTime(String sql, Map<String, Object> params) {
		return DataParser.parseTime(queryObject(sql, params));
	}

	/**
	 * 查询单个字符串
	 *
	 * @param sql sql语句
	 * @return 值
	 */
	@Override
	public String queryString(SQL sql) {
		sql.setSQLDialect(this.getSQLDialect());
		return queryString(sql.getListParameterSQL(), sql.getListParameters());
	}

	/**
	 * 查询单个字符串
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	@Override
	public String queryString(String sql, Object... params) {
		return DataParser.parseString(queryObject(sql, params));
	}

	/**
	 * 查询单个字符串
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 值
	 */
	@Override
	public String queryString(String sql, Map<String, Object> params) {
		return DataParser.parseString(queryObject(sql, params));
	}

	/**
	 * 把记录插入到数据库
	 */
	public boolean insertRecord(Rcd r) {
		return insertRecord(r, r.getOwnerSet().getMetaData().getDistinctTableNames()[0]);
	}

	/**
	 * 把记录插入到数据库，表名自动识别
	 *
	 * @param r 记录
	 * @return 是否成功
	 */
	public boolean insertRecord(Rcd r, String table) {
		return insertRecord(r, table, true);
	}

	/**
	 * 把记录插入到数据库，表名自动识别
	 *
	 * @param r          记录
	 * @param table      数据表
	 * @param ignorNulls 是否忽略空值
	 * @return 是否成功
	 */
	public boolean insertRecord(Rcd r, String table, boolean ignorNulls) {
		Insert insert = SQLBuilder.buildInsert(r, table, this, ignorNulls);
		insert.setSQLDialect(this.getSQLDialect());
		Integer i = 0;
		if (insert.hasValue()) {
			i = this.execute(insert);
			if (i == 1) {
				r.clearDitryFields();
				r.setNextSaveAction(SaveAction.UPDATE);
			}
		}
		return i == 1;
	}

	/**
	 * 把记录从数据库删除
	 *
	 * @param r     记录
	 * @param table 数据表
	 * @return 是否成功
	 */
	public boolean deleteRecord(Rcd r, String table) {
		Delete delete = SQLBuilder.buildDelete(r, table, this);
		delete.setSQLDialect(this.getSQLDialect());
		Integer i = 0;
		if (!delete.isEmpty()) {
			i = this.execute(delete);
			if (i == 1) {
				r.clearDitryFields();
				r.setNextSaveAction(SaveAction.INSERT);
			}
		}
		return i == 1;
	}

	/**
	 * 把记录从数据库删除
	 *
	 * @param r 记录
	 * @return 是否成功
	 */
	public boolean deleteRecord(Rcd r) {
		return deleteRecord(r, r.getOwnerSet().getMetaData().getDistinctTableNames()[0]);
	}

	/**
	 * 把记录保存到数据库
	 *
	 * @param r        记录
	 * @param table    数据表
	 * @param saveMode 保存模式
	 * @return 是否成功
	 */
	public boolean updateRecord(Rcd r, String table, SaveMode saveMode) {
		Update update = SQLBuilder.buildUpdate(r, saveMode, table, this);
		Integer i = 0;
		if (update.hasValue()) {
			i = this.execute(update);
			if (i == 1) {
				r.clearDitryFields();
				r.setNextSaveAction(SaveAction.UPDATE);
			}
		}
		return i == 1;

	}

	/**
	 * 把记录保存到数据库
	 *
	 * @param r        记录
	 * @param saveMode 保存模式
	 * @return 是否成功
	 */
	public boolean updateRecord(Rcd r, SaveMode saveMode) {
		if (r.getOwnerSet() == null) {
			throw new DBMetaException("当前记录集不属于任何RcdSet,无法识别表名,请调用带表名参数的 updateRecord 方法");
		}
		String[] tables = r.getOwnerSet().getMetaData().getDistinctTableNames();
		if (tables.length != 1) {
			throw new DBMetaException("无法正确识别表名,无法识别表名,请调用带表名参数的 updateRecord 方法");
		}
		return updateRecord(r, tables[0], saveMode);
	}

	/**
	 * 保存记录，在确定场景下，建议使用insertRecord或updateRecord以获得更高性能
	 */
	public boolean saveRecord(Rcd r, SaveMode saveMode) {
		if (r.getNextSaveAction() == SaveAction.INSERT) {
			return this.insertRecord(r);
		} else if (r.getNextSaveAction() == SaveAction.UPDATE) {
			return this.updateRecord(r, saveMode);
		} else if (r.getNextSaveAction() == SaveAction.NONE) {
			boolean isExists = this.isRecordExits(r, true);
			r.setNextSaveAction(isExists ? SaveAction.UPDATE : SaveAction.INSERT);
			return saveRecord(r, saveMode);
		}
		return false;
	}

	/**
	 * 保存记录，在确定场景下，建议使用insertRecord或updateRecord以获得更高性能
	 */
	public boolean saveRecord(Rcd r, String table, SaveMode saveMode) {
		if (r.getNextSaveAction() == SaveAction.INSERT) {
			return this.insertRecord(r, table);
		} else if (r.getNextSaveAction() == SaveAction.UPDATE) {
			return this.updateRecord(r, table, saveMode);
		} else if (r.getNextSaveAction() == SaveAction.NONE) {
			boolean isExists = this.isRecordExits(r, table, true);
			r.setNextSaveAction(isExists ? SaveAction.UPDATE : SaveAction.INSERT);
			return saveRecord(r, table, saveMode);
		}
		return false;
	}

	/**
	 * 执行一个Insert语句，并返回某些默认值(自增),如果失败返回-1
	 *
	 * @param insert insert语句
	 * @return 默认字段的值
	 */
	public long insertAndReturnKey(SQL insert) {
		final String sql = insert.getListParameterSQL();
		final Object[] params = insert.getListParameters();
		return insertAndReturnKey(sql, params);
	}

	/**
	 * 执行一个Insert语句，并返回某些默认值,如果失败返回-1
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 默认字段的值
	 */
	public long insertAndReturnKey(final String sql, Map<String, Object> params) {
		return insertAndReturnKey(new Expr(sql, params).setSQLDialect(this.getSQLDialect()));
	}

	/**
	 * 执行一个Insert语句，并返回某些默认值,如果失败返回-1
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 默认字段的值
	 */
	public long insertAndReturnKey(String sql, Object... params) {

		if (sql.startsWith("#")) {
			sql = getSQL(sql);
		}

		Expr se = new Expr(sql, params);
		se.setSQLDialect(this.getDBType().getSQLDialect());
		latestSQL.set(se);
		SQL resultSql = chain.doFilter(se);
		resultSql.setSQLDialect(this.getDBType().getSQLDialect());

		sql = resultSql.getListParameterSQL();
		params = resultSql.getListParameters();

		KeyHolder keyHolder = new GeneratedKeyHolder();
		AutoIncPreparedStatementCreator cr = null;
		try {
			cr = new AutoIncPreparedStatementCreator(this, this.getDataSource().getConnection(), sql,
					Utils.filterParameter(params));
		} catch (SQLException e) {
			Logger.exception(e);
			return -1;
		}

		if (this.isPrintSQL()) {
			final AutoIncPreparedStatementCreator fcr = cr;
			new SQLPrinter<Object>(this, se, resultSql) {
				@Override
				protected Object actualExecute() {
					Integer r=getJdbcTemplate().update(fcr, keyHolder);
					return r;
				}
			}.execute();
		} else {
			try {
				getJdbcTemplate().update(cr, keyHolder);
			} catch (Exception e) {
				Logger.error("语句执行错误(" + e.getMessage() + ")，\n语句：" + se.getSQL());
				throw e;
			}
		}

		cr.close();

		//针对DB2的情况
		if (cr.getAutoAIKey() != null) {
			return cr.getAutoAIKey();
		}
		//尝试单个自增列的情况
		try {
			return keyHolder.getKey().longValue();
		} catch(Exception e) {
			Map<String,Object> ret=keyHolder.getKeys();
			List<String> tables = SQLParser.getAllTables(sql,DruidUtils.getDbType(this.getDBType()));
			if(tables.size()==1) {
				DBTableMeta tm=this.getTableMeta(tables.get(0));
				List<DBColumnMeta> ais=tm.getAIColumns();
				for (DBColumnMeta cm : ais) {
					Long value=DataParser.parseLong(ret.get(cm.getColumn()));
					if(value!=null) {
						return value;
					}
				}
			}
			throw e;
		}


	}

	/**
	 * 获得一个可执行的SE构建器，已经被设置DAO
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 表达式
	 */
	@Override
	public Expr expr(String sql, HashMap<String, Object> params) {
		Expr se = Expr.create(sql, params);
		se.setSQLDialect(this.getDBType().getSQLDialect());
		se.setDAO(this);
		return se;
	}

	/**
	 * 获得一个可执行的SE构建器，已经被设置DAO
	 *
	 * @param sql    sql语句
	 * @param params 参数
	 * @return 表达式
	 */
	@Override
	public Expr expr(String sql, Object... params) {
		if (sql.startsWith("#")) {
			sql = getSQL(sql);
		}
		Expr se = Expr.create(sql, params);
		se.setSQLDialect(this.getDBType().getSQLDialect());
		se.setDAO(this);
		return se;
	}

	/**
	 * 获得一个可执行的select语句构建器，已经被设置DAO
	 *
	 * @return Select
	 */
	@Override
	public Select select() {
		Select select = new Select();
		select.setDAO(this);
		select.setSQLDialect(this.getDBType().getSQLDialect());
		return select;
	}

	/**
	 * 获得一个可执行的insert语句构建器，已经被设置DAO
	 *
	 * @param table 表
	 * @return Insert
	 */
	@Override
	public Insert insert(String table) {
		Insert insert = new Insert(table);
		insert.setDAO(this);
		insert.setSQLDialect(this.getDBType().getSQLDialect());
		return insert;
	}

	@Override
	public Insert insert(Class entityType) {
		 String table=getEntityTableName(entityType);
		 if(StringUtil.isBlank(table)) {
			 throw new IllegalArgumentException("无发识别表名:"+entityType.getName());
		 }
		 return this.insert(table);
	}

	/**
	 * 获得一个可执行的update语句构建器，已经被设置DAO
	 *
	 * @param table 表
	 * @return Update语句
	 */
	@Override
	public Update update(String table) {
		Update update = new Update(table);
		update.setDAO(this);
		update.setSQLDialect(this.getDBType().getSQLDialect());
		return update;
	}

	@Override
	public Update update(Class entityType) {
		 String table=getEntityTableName(entityType);
		 if(StringUtil.isBlank(table)) {
			 throw new IllegalArgumentException("无发识别表名:"+entityType.getName());
		 }
		 return this.update(table);
	}

	/**
	 * 获得一个可执行的delete语句构建器，已经被设置DAO
	 *
	 * @param table 数据表
	 * @return Delete语句
	 */
	@Override
	public Update update(String table, ConditionExpr ce) {
		Update del = update(table);
		del.where().and(ce);
		del.setSQLDialect(this.getDBType().getSQLDialect());
		return del;
	}

	/**
	 * 获得一个可执行的update语句构建器，已经被设置DAO
	 *
	 * @param table 数据表
	 * @param ce    条件表达式
	 * @param ps    条件表达式参数
	 * @return Update语句
	 */
	@Override
	public Update update(String table, String ce, Object... ps) {
		return update(table, new ConditionExpr(ce, ps));
	}

	/**
	 * 获得一个可执行的delete语句构建器，已经被设置DAO
	 *
	 * @param table 数据表
	 * @return Delete语句
	 */
	@Override
	public Delete delete(String table) {
		Delete delete = new Delete();
		delete.from(table);
		delete.setDAO(this);
		delete.setSQLDialect(this.getDBType().getSQLDialect());
		return delete;
	}

	@Override
	public Delete delete(Class entityType) {
		 String table=getEntityTableName(entityType);
		 if(StringUtil.isBlank(table)) {
			 throw new IllegalArgumentException("无发识别表名:"+entityType.getName());
		 }
		 return this.delete(table);
	}

	/**
	 * 获得一个可执行的delete语句构建器，已经被设置DAO
	 *
	 * @param table 数据表
	 * @param ce    条件表达式
	 * @return Delete语句
	 */
	@Override
	public Delete delete(String table, ConditionExpr ce) {
		Delete del = delete(table);
		del.where().and(ce);
		del.setSQLDialect(this.getSQLDialect());
		return del;
	}

	/**
	 * 获得一个可执行的delete语句构建器，已经被设置DAO
	 *
	 * @param table 数据表
	 * @param ce    条件表达式
	 * @param ps    条件表达式参数
	 * @return Delete语句
	 */
	@Override
	public Delete delete(String table, String ce, Object... ps) {
		return delete(table, new ConditionExpr(ce, ps));
	}

	/**
	 * 获得数据库中的表清单
	 *
	 * @return 表清单
	 */
	@Override
	public String[] getTableNames() {
		return DBMetaData.getAllTableNames(this);
	}

	/**
	 * 获得数据库中的字段描述信息
	 *
	 * @param table  表名
	 * @param column 列名
	 * @return DBColumnMeta
	 */
	@Override
	public DBColumnMeta getTableColumnMeta(String table, String column) {
		table = this.getSQLDialect().getDialectProcessor().simplifyTableName(table);
		column = this.getSQLDialect().getDialectProcessor().simplifyTableName(column);
		return DBMetaData.getDBColumn(this, table, column);
	}

	/**
	 * 刷新Meta信息
	 */
	public void refreshMeta() {
		DBMetaData.invalid(this);
	}


	private final ThreadLocal<TransactionStatus> MANUAL_TRANSACTION_STATUS=new ThreadLocal<TransactionStatus>();

	private String transactionManagerBean=null;

	private DataSourceTransactionManager transactionManager=null;

	/**
	 * 获得事务管理器
	 *
	 * @return 事务管理器
	 */
	public DataSourceTransactionManager getTransactionManager() {
		return transactionManager;
	}

	/**
	 * 设置事务管理器
	 *
	 * @param transactionManager 事务管理器
	 */
	public void setTransactionManager(DataSourceTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
		validateDataSource(this.getDataSource());
	}

	/**
	 * 校验数据源
	 * */
	private void validateDataSource(DataSource ds) {
		if(this.getDataSource()==null) return;
		if(this.transactionManager==null) return;
		if(!(this.transactionManager instanceof DataSourceTransactionManager)) return;

		DataSourceTransactionManager tm=(DataSourceTransactionManager)this.transactionManager;
		if(tm.getDataSource()==null) {
			tm.setDataSource(ds);
		} else {
			if(tm.getDataSource()!=ds) {
				throw new TransactionException("事务管理器指定的数据源与当前数据源不一致");
			}
		}
	}

	/**
	 * 获得当前Spring托管的自动事务的事务状态对象
	 *
	 * @return 事务状态对象
	 */
	public TransactionStatus getCurrentAutoTransactionStatus()
	{
		try {
			return TransactionAspectSupport.currentTransactionStatus();
		} catch (NoTransactionException e) {
			 return null;
		}
	}

	/**
	 * 获得手动事务的事务状态对象
	 *
	 * @return 事务状态对象
	 */
	public TransactionStatus getCurrentManualTransactionStatus()
	{
		return MANUAL_TRANSACTION_STATUS.get();
	}

	public void beginTransaction()
	{
		beginTransaction(TransactionDefinition.PROPAGATION_REQUIRED);
	}

	/**
	 * 开始一个事务
	 *
	 * @param propagationBehavior 事务传播行为参数
	 */
	public void beginTransaction(int propagationBehavior)
	{
		if(transactionManager==null) {
			transactionManager=new DataSourceTransactionManager(this.getDataSource());
		}
		TransactionStatus  manualTransactionStatus=getCurrentManualTransactionStatus();
		if(manualTransactionStatus!=null) {
			throw new TransactionException("尚有事务为结束，无法启动新事务");
		}
		//事务定义类
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
	    def.setPropagationBehavior(propagationBehavior);
	    // 返回事务对象
	    TransactionStatus  status = transactionManager.getTransaction(def);
	    MANUAL_TRANSACTION_STATUS.set(status);
	}


	/**
	 * 回滚手动事务
	 */
	public void rollback()
	{
		if(transactionManager==null) {
			Logger.warn("未指定事务管理器，事务开启无效");
			return;
		}
		TransactionStatus  manualTransactionStatus=getCurrentManualTransactionStatus();
		if(manualTransactionStatus!=null) {
			transactionManager.rollback(manualTransactionStatus);
		}
		MANUAL_TRANSACTION_STATUS.set(null);
	}

	/**
	 * 提交手动事务
	 */
	public  void commit()
	{
		if(transactionManager==null) {
			Logger.warn("未指定事务管理器，事务开启无效");
			return;
		}
		TransactionStatus  manualTransactionStatus=getCurrentManualTransactionStatus();
		if(manualTransactionStatus!=null) {
			transactionManager.commit(manualTransactionStatus);
		}
		MANUAL_TRANSACTION_STATUS.set(null);
	}




	protected Insert createInsert4POJO(Object pojo,String table)
	{
		List<String> fields=EntityUtils.getEntityFields(pojo.getClass(),this,table);
		DBTableMeta tm= this.getTableMeta(table);
		if(fields.size()==0) return null;
		Insert  insert = new Insert(table);
		insert.setSQLDialect(this.getSQLDialect());
		Object value = null;
		DBColumnMeta cm=null;
		for (String field : fields) {
			value=BeanUtil.getFieldValue(pojo, field);
			if(DataParser.isBooleanType(value)) {
				if(this.getDBTreaty().isAutoCastLogicField()) {
					Boolean b=(Boolean)value;
					value= b? this.getDBTreaty().getTrueValue():this.getDBTreaty().getFalseValue();
				}
			}
			//校验主键是否为空
			cm= tm.getColumn(field);
			if(cm.isPK() && !cm.isAutoIncrease() && value==null) {
				throw new RuntimeException("未指定主键"+field+"的值");
			}
			if(value!=null) {
				insert.set(field, value);
			}
		}

		if(!insert.isEmpty()) {
			String field=this.getDBTreaty().getCreateUserIdField();
			if(tm.isColumnExists(field) && insert.getValue(field)==null) {
				insert.set(field, this.getDBTreaty().getLoginUserId());
			}
			field=this.getDBTreaty().getCreateTimeField();
			if(tm.isColumnExists(field) && insert.getValue(field)==null) {
				insert.set(field, new Date());
			}
			field=this.getDBTreaty().getVersionField();
			if(tm.isColumnExists(field) && insert.getValue(field)==null) {
				insert.set(field, 1);
			}
			field=this.getDBTreaty().getTenantIdField();
			SQL val=insert.getValue(field);
			if((val==null || val.isEmpty() || StringUtil.isBlank(val.getSQL())) && tm.isColumnExists(field)) {
				insert.set(field, this.getDBTreaty().getActivedTenantId());
			}
		}

		List<DBColumnMeta> aiColumns=tm.getAIColumns();

		insert.setSQLDialect(this.getSQLDialect());
		//针对DB2的特殊处理
		if(this instanceof Db2DAO) {
			for (DBColumnMeta aicolumn : aiColumns) {
				insert.removeField(aicolumn.getColumn());
			}
		}
		return insert;
	}

	/**
	 * 插入 entity 实体到数据里表
	 *
	 * @param entity  数据对象
	 * @return 是否执行成功
	 */
	public boolean insertEntity(Object entity) {
		if(entity==null) return false;
		return this.insertEntity(entity, getEntityTableName(entity.getClass()));
	}

	/**
	 * 插入 entity 实体到数据里表
	 *
	 * @param entities  数据对象
	 * @return 是否执行成功
	 */
	@Transactional
	public boolean insertEntities(List<? extends Entity> entities) {
		List<SQL> inserts=new ArrayList<>();
		String table=null;
		Class poType=null;
		for (Entity e : entities) {
			if(e==null) continue;
			if(table==null) table=getEntityTableName(e.getClass());
			if(poType==null) {
				poType=EntityContext.getProxyType(e.getClass());
			}
			Insert insert=createInsert4POJO(e,table);
			inserts.add(insert);
		}
		int[] rs=this.batchExecute(inserts);
		for (Entity entity : entities) {
			if(this.isCacheSupported(entity)) {
				getDataCacheManager().dispatchJoinCacheInvalidEvent(CacheInvalidEventType.INSERT, table, null, (List<Entity>) entities);
			}
		}
//		if(this.isCacheSupported(entity)) {
//			getDataCacheManager().dispatchJoinCacheInvalidEvent(CacheInvalidEventType.INSERT,table,null,(List<Entity>) entities);
//			SimpleTaskManager.doParallelTask(new Runnable() {
//				@Override
//				public void run() {
//					for (Entity e : entities) {
//						e=queryEntity(e,true);
//						getDataCacheManager().invalidateAccurateCache(e);
//					}
//				}
//			});
//		}
		return true;
	}
	/**
	 * 插入 entity 实体到数据里表
	 *
	 * @param entity  数据对象
	 * @param table 数表
	 * @return 是否执行成功
	 */
	public boolean insertEntity(Object entity,String table)
	{
 		if(entity==null) return false;

		List<String> fields=EntityUtils.getEntityFields(entity.getClass(),this,table);
		DBTableMeta tm= this.getTableMeta(table);
		if(fields.size()==0) return false;

		Object value = null;
		boolean hasAIKey=false;
		DBColumnMeta cm = null;
		DBColumnMeta aiColumn=null;
		for (String field : fields) {
			//校验主键是否为空
			cm = tm.getColumn(field);
			if(cm.isPK()) {
				if(cm.isAutoIncrease()) {
					aiColumn=cm;
					hasAIKey=true;
				} else {
					value=BeanUtil.getFieldValue(entity, field);
					if(value==null) throw new RuntimeException("未指定主键"+field+"的值");
				}
			}
		}

		Insert insert=createInsert4POJO(entity, table);

		if(insert==null) return false;


		long i=-1;
		boolean suc =false;
		if(hasAIKey)
		{
			i=this.insertAndReturnKey(insert);
			//尝试设置主键值
			if(i!=-1) {
				if(tm.getPKColumns().size()==1) {
					BeanUtil.setFieldValue(entity,tm.getPKColumns().get(0).getColumn(),i);
				}
				suc=true;
			}
			EntityContext.clearModifies(entity);

		} else   {
			i=this.execute(insert);
			EntityContext.clearModifies(entity);

			//如果有缓存，有策略
//			if(this.getDataCacheManager()!=null && this.getDataCacheManager().isSupportAccurateCache(entity.getClass())) {
//				entity = this.queryEntity(entity,true);
////				this.getDataCacheManager().invalidateAccurateCache((Entity) entity);
//				this.getDataCacheManager().dispatchJoinCacheInvalidEvent(CacheInvalidEventType.INSERT,this.getDataCacheManager(),table,null,(Entity) entity);
//			}
			suc=i==1;
		}


		if(suc) {
			//如果有缓存，有策略
			if( this.isCacheSupported(entity) ) {
				entity = this.queryEntity(entity,true);
//				this.getDataCacheManager().invalidateAccurateCache((Entity) entity);
				this.getDataCacheManager().dispatchJoinCacheInvalidEvent(CacheInvalidEventType.INSERT,this.getDataCacheManager(),table,null,(Entity) entity);
			}
		}
		return suc;

	}


	protected Update createUpdate4POJO(Object pojo,String table,String tableKey,SaveMode saveMode)
	{
		Entity entity=null;
		if((saveMode == SaveMode.BESET_FIELDS  || saveMode == SaveMode.DIRTY_FIELDS || saveMode == SaveMode.DIRTY_OR_NOT_NULL_FIELDS) && !EntityContext.isManaged(pojo)) {
			throw new IllegalArgumentException("SaveMode "+saveMode.name()+"错误 , 需要使用 EntityContext.create 方法创建实体");
		} else {
			try {
				entity=(Entity)pojo;
			} catch (Exception e) {
				saveMode=SaveMode.NOT_NULL_FIELDS;
				Logger.info(pojo.getClass().getName()+" is not Entity, save as "+saveMode.name()+" mode");
			}
		}

		List<String> fields=EntityUtils.getEntityFields(pojo.getClass(),this,table);
		if(fields.size()==0) return null;
		DBTableMeta tm= this.getTableMeta(table);
		Update  update = new Update(tableKey);
		update.setSQLDialect(this.getSQLDialect());
		Object value = null;
		//循环数据库字段
		for (String field : fields) {
			value=BeanUtil.getFieldValue(pojo, field);
			if(DataParser.isBooleanType(value)) {
				if(this.getDBTreaty().isAutoCastLogicField()) {
					Boolean b=(Boolean)value;
					value= b? this.getDBTreaty().getTrueValue():this.getDBTreaty().getFalseValue();
				}
			}
			if(tm.isPK(field)) {
				if(value==null) {
					throw new IllegalArgumentException("缺少主键["+table+"."+field+"]值");
				}
				update.where().and(field+" = ? ",value);
			}
			else {
				if(saveMode==SaveMode.ALL_FIELDS) {
					update.set(field, value);
				} else if(saveMode==SaveMode.NOT_NULL_FIELDS) {
					if(value!=null) update.set(field, value);
				} else if(saveMode==SaveMode.DIRTY_FIELDS) {
					if(entity.isDirtyProperty(NC.getPropertyName(field))) {
						update.set(field, value);
					}
				} else if(saveMode==SaveMode.DIRTY_OR_NOT_NULL_FIELDS) {
					if(value!=null || entity.isDirtyProperty(NC.getPropertyName(field))) {
						update.set(field, value);
					}
				} else if(saveMode==SaveMode.BESET_FIELDS) {
					if(entity.isBeSetProperty(NC.getPropertyName(field))) {
						update.set(field, value);
					}
				}
			}
		}

		if(!update.isEmpty()) {
			String field=this.getDBTreaty().getUpdateUserIdField();
			if(tm.isColumnExists(field) && update.getValue(field)==null) {
				update.set(field, this.getDBTreaty().getLoginUserId());
			}
			field=this.getDBTreaty().getUpdateTimeField();
			if(tm.isColumnExists(field) && update.getValue(field)==null ) {
				update.set(field, new Date());
			}
			field=this.getDBTreaty().getVersionField();
			if(tm.isColumnExists(field) && update.getValue(field)==null) {
				update.setExpr(field, field+"+1");
			}
		}

		//校验更新条件
		if(update.where().isEmpty()) {
			String[] pknames=BeanUtil.getFieldValueArray(tm.getPKColumns(), "column", String.class);
			throw new IllegalArgumentException("未指定主键值:"+ArrayUtil.join(pknames));
		}

		return update;
	}

	/**
	 * 根据ID值，更新pojo实体到数据里表,根据实体注解自动识别数据表<br>
	 * 如果ID值被修改，可导致错误的更新
	 *
	 * @param entity      数据对象
	 * @param saveMode 保存模式
	 * @return 是否执行成功
	 */
	public boolean updateEntity(Object entity,SaveMode saveMode)
	{
		if(entity==null) return false;
		return this.updateEntity(entity, getEntityTableName(entity.getClass()), saveMode);
	}


	private boolean isCacheSupported(Object entity) {
		DataCacheManager dcm=this.getDataCacheManager();
		if(entity==null || dcm==null) return false;
		Class clz=entity.getClass();
		return dcm.isSupportAccurateCache(clz) || dcm.isForJoinCacheDetection(clz);
	}

	/**
	 * 根据ID值，更新pojo实体到数据里表<br>
	 * 如果ID值被修改，可导致错误的更新
	 *
	 * @param entity      数据对象
	 * @param table     数表
	 * @param saveMode 保存模式
	 * @return 是否执行成功
	 */
	public boolean updateEntity(Object entity,String table,SaveMode saveMode)
	{
		if(entity==null) return false;
		Entity valueBefore=null;
		DataCacheManager dcm=this.getDataCacheManager();
		Boolean isLogicDelete=false;
		if (this.isCacheSupported(entity)) {
			valueBefore=(Entity) this.queryEntity(entity,true);
			DBTableMeta tm=this.getTableMeta(table);
			DBColumnMeta delcol=tm.getColumn(getDBTreaty().getDeletedField());
			if(delcol==null) isLogicDelete=false;
			else {
				Object delVal=BeanUtil.getFieldValue(entity,delcol.getColumn());
				isLogicDelete = getDBTreaty().getTrueValue().equals(delVal);
			}
		}
		Update update=createUpdate4POJO(entity, table,table, saveMode);
		update.setSQLDialect(this.getSQLDialect());
		if(update==null) return false;
		//如果没有字段要更新
		if(update.isEmpty()) return true;
		int i=this.execute(update);
		boolean suc= i==1;
		if(suc && ( entity instanceof Entity )) {
			((Entity)entity).clearModifies();
			if (this.isCacheSupported(entity)) {
				entity=this.queryEntity(entity,true);
				dcm.dispatchJoinCacheInvalidEvent(isLogicDelete?CacheInvalidEventType.DELETE:CacheInvalidEventType.UPDATE,this.getDataCacheManager(),table,valueBefore,(Entity) entity);
			}
		}
		return suc;

	}

	/**
	 * 保存实体数据，根据注解，自动识别表名<br>
	 * 建议使用insertEntity或updateEntity以获得更高性能
	 *
	 * @param entity      数据对象
	 * @param saveMode 保存模式
	 * @return 是否成功
	 */
	public boolean saveEntity(Object entity,SaveMode saveMode) {
		return this.saveEntity(entity, getEntityTableName(entity.getClass()), saveMode);
	}

	/**
	 * 保存实体数据<br>
	 * 建议使用insertPOJO或updatePOJO以获得更高性能
	 *
	 * @param entity      数据
	 * @param table     表
	 * @param saveMode 保存模式
	 * @return 是否成功
	 */
	public boolean saveEntity(Object entity,String table,SaveMode saveMode)
	{
		if(entity==null) {
			throw new DataException("不允许保存空的实体");
		}
		List<String> fields=EntityUtils.getEntityFields(entity.getClass(),this,table);
		DBTableMeta tm= this.getTableMeta(table);
		Object value = null;
		boolean isAnyPKNullValue=false;
		for (String field : fields) {
			value=BeanUtil.getFieldValue(entity, field);
			//校验主键是否为空
			DBColumnMeta cm= tm.getColumn(field);
			if(cm.isPK() && value==null) {
				isAnyPKNullValue=true;
				break;
			}
		}

		if(isAnyPKNullValue) {
			return insertEntity(entity, table);
		}

		if(isEntityExists(entity, table)) {
			return updateEntity(entity, table, saveMode);
		}
		else {
			return insertEntity(entity, table);
		}
	}

	/**
	 * 根据 sample 中的已有信息从数据库删除对应的实体集
	 *
	 * @param sample 查询样例
	 * @param sample  样例对象
	 * @return 删除的行数
	 */
	public int deleteEntities(Object sample)
	{
		if(sample==null) return 0;
		return this.deleteEntities(sample, getEntityTableName(sample.getClass()));
	}

	/**
	 * 根据 sample 中的已有信息从数据库删除对应的实体集
	 *
	 * @param sample 查询样例
	 * @param table  数据表
	 * @return 删除的行数
	 */
	public int deleteEntities(Object sample,String table)
	{
		if(sample==null) return 0;

		List<String> fields=EntityUtils.getEntityFields(sample.getClass(),this,table);
		if(fields.size()==0) return 0;
		Delete  delete = new Delete(table);
		delete.setSQLDialect(this.getSQLDialect());
		Object value = null;
		for (String field : fields) {
			value=BeanUtil.getFieldValue(sample, field);
			if(value!=null) {
				delete.where().and(field+" = ? ",value);
			}
		}
		if(delete.where().isEmpty()) {
			throw new IllegalArgumentException("未指定查询条件");
		}
		int i=this.execute(delete);
		return i;
	}

	@Override
	public int deleteEntities(Class type, ConditionExpr ce) {

//		List<Entity> list = null;
//		if (this.getDataCacheManager()!=null && this.getDataCacheManager().isSupportCache(type)) {
//			//此处需要考虑性能问题
//			list=this.queryEntities(type,ce);
//		}

		int i=deleteEntities(type, getEntityTableName(type), ce);

//		if(i>0 && list!=null) {
//			this.getDataCacheManager().invalidateAccurateCache(list);
//			this.getDataCacheManager().dispatchJoinCacheInvalidEvent(CacheInvalidEventType.DELETE,);
//		}
		return i;
	}


	@Override
	public int deleteEntities(Class type, String table,ConditionExpr ce) {
		List<Entity> list = null;
		if (this.getDataCacheManager()!=null && this.getDataCacheManager().isSupportAccurateCache(type)) {
			//此处需要考虑性能问题
			Expr expr=new Expr("select * from "+table);
			ce.startWithWhere();
			expr.append(ce.getListParameterSQL(),ce.getListParameters());
			list=this.queryEntities(type,ce);
		}

		Expr expr=new Expr("delete from "+table);
		ce.startWithWhere();
		expr.append(ce);

		int i=this.execute(expr);
		if(i>0 && list!=null) {
//			this.getDataCacheManager().invalidateAccurateCache(list);
			this.getDataCacheManager().dispatchJoinCacheInvalidEvent(CacheInvalidEventType.DELETE,table,list,null);
		}
		return i;
	}


	/**
	 * 删除实体，通过注解识别表名
	 *
	 * @param entity 实体
	 * @return 是否成功
	 */
	public  boolean deleteEntity(Object entity) {
		if(entity==null) return false;
		return this.deleteEntity(entity, getEntityTableName(entity.getClass()));
	}

	/**
	 * 删除实体
	 *
	 * @param entity 实体
	 * @param table  数据表
	 * @return 是否成功
	 */
	public  boolean deleteEntity(Object entity,String table)
	{
		if(entity==null) return false;

		Object toInvalidEntity=null;
		if(this.isCacheSupported(entity)) {
			toInvalidEntity=this.queryEntity(entity,true);
		}

		List<String> fields=EntityUtils.getEntityFields(entity.getClass(),this,table);
		if(fields.size()==0) return false;
		DBTableMeta tm= this.getTableMeta(table);
		Delete  delete = new Delete(table);
		delete.setSQLDialect(this.getSQLDialect());
		Object value = null;
		for (String field : fields) {
			value=BeanUtil.getFieldValue(entity, field);
			if(tm.isPK(field)) {
				if(value==null) {
					throw new IllegalArgumentException("缺少主键["+table+"]值");
				}
				delete.where().and(field+" = ? ",value);
			}
		}
		if(delete.where().isEmpty()) {
			String[] pknames=BeanUtil.getFieldValueArray(tm.getPKColumns(), "column", String.class);
			throw new IllegalArgumentException("未指定主键值:"+ArrayUtil.join(pknames));
		}
		int i=this.execute(delete);
		boolean suc=i==1;
		if(suc && this.isCacheSupported(entity)) {
			this.getDataCacheManager().dispatchJoinCacheInvalidEvent(CacheInvalidEventType.DELETE,this.getDataCacheManager(),table,(Entity)toInvalidEntity,null);
		}
		return suc;
	}

	/**
	 * 实体对象是否存已经在数据表,以主键作为判断依据
	 *
	 * @param pojo  数据对象
	 * @param table 数据表
	 * @return 是否存在
	 */
	public boolean isEntityExists(Object pojo,String table)
	{
		if(pojo==null) return false;

		List<String> fields=EntityUtils.getEntityFields(pojo.getClass(),this,table);
		if(fields.size()==0) return false;
		DBTableMeta tm= this.getTableMeta(table);
		Select  select = new Select(table);
		select.select("1");
		select.setSQLDialect(this.getSQLDialect());
		Object value = null;
		for (String field : fields) {
			value=BeanUtil.getFieldValue(pojo, field);
			if(tm.isPK(field)) {
				if(value==null) {
					throw new IllegalArgumentException("缺少主键["+table+"]值");
				}
				select.where().and(field+" = ? ",value);
			}
		}

		//校验查询条件
		if(select.where().isEmpty()) {
			String[] pknames=BeanUtil.getFieldValueArray(tm.getPKColumns(), "column", String.class);
			throw new IllegalArgumentException("未指定主键值:"+ArrayUtil.join(pknames));
		}

		Rcd r=this.queryRecord(select);
		return r!=null;
	}

	private  String getEntityTableName(Class type) {
		String table=com.github.foxnic.sql.entity.EntityUtil.getAnnotationTable(type);
		if(table==null) {
			throw new RuntimeException("实体类 "+type.getName()+" 未通过 MyBatisPlus 注解或 JPA 注解标记表名");
		}
		return table;
	}


//	@Override
//	public <T> T queryEntity(T sample) {
//		if(sample==null) return null;
//		return this.queryEntity(sample,getEntityTableName(sample.getClass()));
//	}



//	@Override
//	public <T> T queryEntity(T sample, String table) {
//		if(sample==null) return null;
//		ConditionExpr ce=SQLBuilder.buildConditionExpr(sample, table, this);
//		return (T)queryEntity((Class<T>)sample.getClass(), table,ce);
//	}


//	@Override
//	public <T> T queryEntity(Class<T> type, Object id) {
//		return this.queryEntity(type,this.getEntityTableName(type),id);
//	}


//	@Override
//	public <T> T queryEntity(Class<T> type, String table, Object id) {
//		DBTableMeta tm=this.getTableMeta(table);
//		if(tm==null) {
//			throw new SQLValidateException("数据表 "+table+" 不存在");
//		}
//		if(tm.getPKColumnCount()!=1) {
//			throw new SQLValidateException("数据表 "+table+" 主键数量不符合要求，要求1个，实际"+tm.getPKColumnCount()+"个");
//		}
//		String field=tm.getPKColumns().get(0).getColumn();
//		ConditionExpr ce=new ConditionExpr();
//		ce.and(field+" = ?", id);
//		//
//		return queryEntity(type, table,ce);
//	}


//	@Override
//	public <T> T queryEntity(Class<T> type, ConditionExpr ce) {
//		return (T)this.queryEntity(type, this.getEntityTableName(type), ce);
//	}


//	@Override
//	public <T> T queryEntity(Class<T> type, String table, ConditionExpr ce) {
//		ce.startWithWhere();
//		Rcd r=this.queryRecord("select * from "+table+" "+ce.getListParameterSQL(),ce.getListParameters());
//		if(r==null) return null;
//		return (T)r.toEntity(type);
//	}

//	@Override
//	public <T> T queryEntity(Class<T> type, String condition, Object... params) {
//		return queryEntity(type, getEntityTableName(type), new ConditionExpr(condition,params));
//	}



	/**
	 * @param sql 条件表达式(ConditionExpr)或完整的 select(Expr) 语句
	 * */
	@Override
	public <T> List<T> queryEntities(Class<T> entityType, SQL sql) {
		SQL finalSQL=sql;
		if(sql instanceof ConditionExpr) {
			ConditionExpr ce=(ConditionExpr)sql;
			ce.startWithWhere();
			finalSQL=new Expr("select * from "+getEntityTableName(entityType)+" t "+ce.getListParameterSQL(),sql.getListParameters());
		}
		return this.query(finalSQL).toEntityList(entityType);
	}

//	@Override
//	public <T> T queryEntity(Class<T> type, String table, String condition, Object... params) {
//		return queryEntity(type, table, new ConditionExpr(condition,params));
//	}

	@Override
	public <T> List<T> queryEntities(T sample) {
		if(sample==null) return new ArrayList<T>();
		return queryEntities(sample,getEntityTableName(sample.getClass()));
	}


	private <T> List<T> queryEntities(T sample, String table) {
		if(sample==null) return new ArrayList<T>();
		ConditionExpr ce=SQLBuilder.buildConditionExpr(sample, table, this);
		ce.startWithWhere();
		Expr expr=new Expr("select * from "+table+" t "+ce.getListParameterSQL(),ce.getListParameters());
		return this.queryEntities((Class<T>)sample.getClass(),expr);
	}

	@Override
	public <T> PagedList<T> queryPagedEntities(T sample, int pageSize, int pageIndex) {
		if(sample==null) return new PagedList<T>(new ArrayList<T>(),0,0,0,0);
		return queryPagedEntities(sample, getEntityTableName(sample.getClass()), pageSize, pageIndex);
	}


	@Override
	public <T> PagedList<T> queryPagedEntities(T sample, String table, int pageSize, int pageIndex) {
		if(sample==null) return new PagedList<T>(new ArrayList<T>(),0,0,0,0);
		ConditionExpr ce=SQLBuilder.buildConditionExpr(sample, table, this);
		ce.startWithWhere();
		RcdSet rs=this.queryPage("select * from "+table+" t "+ce.getListParameterSQL(),pageSize,pageIndex,ce.getListParameters());
		return new PagedList<T>((List<T>)rs.toEntityList(sample.getClass()),rs.getPageSize(),rs.getPageIndex(),rs.getPageCount(),rs.getTotalRowCount());
	}

//	@Override
//	public <T> List<T> queryEntities(Class<T> type, ConditionExpr ce) {
//		return queryEntities(type, getEntityTableName(type), ce);
//	}

	public <T> PagedList<T> queryPagedEntities(Class<T> entityType,int pageSize, int pageIndex, SQL sql){
		SQL finalSQL=sql;
		if(sql instanceof ConditionExpr) {
			ConditionExpr ce=(ConditionExpr)sql;
			String table=this.getEntityTableName(entityType);
			DBTableMeta tm=this.getTableMeta(table);
			DBColumnMeta deleted=tm.getColumn(this.getDBTreaty().getDeletedField());
			if(deleted!=null) {
				Object falseValue=this.getDBTreaty().getFalseValue();
				ce.and(deleted.getColumn() + "=?", falseValue);
			}
			DBColumnMeta tenantId=tm.getColumn(this.getDBTreaty().getTenantIdField());
			if(tenantId!=null && this.getDBTreaty().getActivedTenantId()!=null) {
				ce.and(tenantId.getColumn()+"=?",this.getDBTreaty().getActivedTenantId());
			}
			ce.startWithWhere();
			finalSQL=new Expr("select * from "+getEntityTableName(entityType)+" t "+ce.getListParameterSQL(),sql.getListParameters());
		}
		return this.queryPage(finalSQL,pageSize,pageIndex).toPagedEntityList(entityType);
	}

//	@Override
//	public <T> List<T> queryEntities(Class<T> type, String table, ConditionExpr ce) {
//		ce.startWithWhere();
//		RcdSet rs=this.query("select * from "+table+" "+ce.getListParameterSQL(),ce.getListParameters());
//		return (List<T>)rs.toEntityList(type);
//	}

//	@Override
//	public <T> List<T> queryEntities(Class<T> type, String condition, Object... params) {
//		return queryEntities(type,new ConditionExpr(condition,params));
//	}

//	@Override
//	public <T> List<T> queryEntities(Class<T> type, String table, String condition, Object... params) {
//		return queryEntities(type,table,new ConditionExpr(condition,params));
//	}

//	@Override
//	public <T> PagedList<T> queryPagedEntities(Class<T> type, int pageSize, int pageIndex, ConditionExpr ce) {
//		return queryPagedEntities(type,getEntityTableName(type) ,pageSize, pageIndex, ce);
//	}

//	@Override
//	public <T> PagedList<T> queryPagedEntities(Class<T> type, String table, int pageSize, int pageIndex,ConditionExpr ce) {
//		ce.startWithWhere();
//		RcdSet rs=this.queryPage("select * from "+table+" "+ce.getListParameterSQL(),pageSize,pageIndex,ce.getListParameters());
//		return new PagedList<T>((List<T>)rs.toEntityList(type),rs.getMetaData(),rs.getPageSize(),rs.getPageIndex(),rs.getPageCount(),rs.getTotalRowCount());
//	}

//	@Override
//	public <T> PagedList<T> queryPagedEntities(Class<T> type, int pageSize, int pageIndex, String condition,Object... params) {
//		return queryPagedEntities(type, getEntityTableName(type), pageSize, pageIndex, new ConditionExpr(condition,params));
//	}

//	@Override
//	public <T> PagedList<T> queryPagedEntities(Class<T> type, String table, int pageSize, int pageIndex,String condition, Object... params) {
//		return queryPagedEntities(type, table, pageSize, pageIndex, new ConditionExpr(condition,params));
//	}

	@Override
	public StoredProcedure getStoredProcedure(String name) {
		StoredProcedure p = new StoredProcedure(this.getDataSource(), name, false);
		return p;
	}

	public DBSequence getSequence(String id) {
		return new DBSequence(this,id,this.sequenceTable,this.sequenceProcedure);
	}

	private String sequenceTable="SYS_SEQUENCE";
	private String sequenceProcedure ="NEXT_VAL";

	/**
	 * @return 返回存储序列数据的表
	 */
	public String getSequenceTable() {
		return sequenceTable;
	}

	/**
	 * @param sequenceTable 设置存储序列数据的表
	 */
	public void setSequenceTable(String sequenceTable) {
		this.sequenceTable = sequenceTable;
	}

	/**
	 * @return 返回取得序列的存储过程
	 */
	public String getSequenceProcedure() {
		return sequenceProcedure;
	}

	/**
	 * @param sequenceProcedure 设置取得序列的存储过程
	 */
	public void setSequenceProcedure(String sequenceProcedure) {
		this.sequenceProcedure = sequenceProcedure;
	}


}
