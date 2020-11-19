package com.github.foxnic.dao.spring;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.data.AbstractSet;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdResultSetExtractor;
import com.github.foxnic.dao.data.RcdRowMapper;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.data.SaveAction;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.excel.DataException;
import com.github.foxnic.dao.filter.SQLFilterChain;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.dao.sql.SQLParserUtil;
import com.github.foxnic.sql.exception.DBMetaException;
import com.github.foxnic.sql.expr.Delete;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.Insert;
import com.github.foxnic.sql.expr.SQL;
import com.github.foxnic.sql.expr.Update;
import com.github.foxnic.sql.expr.Utils;
import com.github.foxnic.sql.expr.Where;

public abstract class SpringDAO extends DAO {

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
	
	private Boolean isDynamicDataSource=null;
	
	protected boolean isDynamicDataSource()
	{
		if(isDynamicDataSource!=null) return isDynamicDataSource;
		isDynamicDataSource = this.getDataSource() instanceof AbstractRoutingDataSource;
		return isDynamicDataSource;
	}
	
	
	private MethodAccess abstractRoutingDataSourceMethodAccess = null;
	private int determineTargetDataSourceIndex=-1;
	
	/**
	 * 多数据源情况下，实际使用的最终数据源
	 * @return 逻辑值
	 * */
	protected DataSource getFinalDataSource()
	{
		if(isDynamicDataSource()) {
			if(determineTargetDataSourceIndex==-1) { 
				abstractRoutingDataSourceMethodAccess=MethodAccess.get(AbstractRoutingDataSource.class);
				determineTargetDataSourceIndex=abstractRoutingDataSourceMethodAccess.getIndex("determineTargetDataSource");
			}
			return (DataSource)abstractRoutingDataSourceMethodAccess.invoke(super.getDataSource(), determineTargetDataSourceIndex);
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
	public String getSQL(String id)
	{
		return null;
//		return SQLoader.getSQL(id,this.getDBType());
	}
	
	/**
	 * 根据ID获得SQL 
	 * 
	 * @param ps 参数
	 * @param id 在tql文件中定义的sqlid(不需要以#开始)
	 * @return SQL
	 */
	public SQL getSQL(String id,Object... ps)
	{
		String sql=this.getSQL(id);
		return new Expr(sql,ps);
	}
	
	/**
	 * 根据ID获得SQL
	 * 
	 * @param id 在tql文件中定义的sqlid(不需要以#开始)
	 * @param ps 参数
	 * @return SQL
	 */
	public SQL getSQL(String id,Map<String,Object> ps)
	{
		String sql=this.getSQL(id);
		return new Expr(sql,ps);
	}
	
	
	
	/**
	 * 分页查询记录集
	 * 
	 * @param sql    sql语句
	 * @param size   每页行数
	 * @param index  页码
	 * @return RcdSet
	 */
	public RcdSet queryPage(SQL sql,int size,int index)
	{
		sql.setSQLDialect(this.getSQLDialect());
		return queryPageWithArrayParameters(false,sql.getListParameterSQL(), size, index,sql.getListParameters());
	}
	
	
	private ThreadLocal<SQL> latestSQL = new ThreadLocal<SQL>();
	
	protected SQLFilterChain chain=new SQLFilterChain(this);
	
	/**
	 * @param fixed  指定单行查询，fixed = true时 不查询count
	 * */
	@SuppressWarnings("unchecked")
	private RcdSet queryPageWithArrayParameters(boolean fixed,String sql, int pageSize, int pageIndex, Object... params) {

		if(sql.startsWith("#"))
		{
			sql=getSQL(sql);
		}
		
		Expr se=new Expr(sql, params);
		se.setSQLDialect(this.getDBType().getSQLDialect());
		latestSQL.set(se);
		SQL resultSql=chain.doFilter(se);
		resultSql.setSQLDialect(this.getDBType().getSQLDialect());
		
		sql=resultSql.getListParameterSQL();
		params=resultSql.getListParameters();
		
		
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
			if(!fixed)
			{
				SQL countSql = this.getCountSQL(new Expr(sql,params),"X");
				final Object [] ps=Utils.filterParameter(countSql.getListParameters());
				if(this.isDisplaySQL())
				{
					totalRecord=new SQLPrinter<Integer>(this,countSql,countSql) {
						@Override
						protected Integer actualExecute() {
							return getJdbcTemplate().queryForObject(countSql.getListParameterSQL(), Integer.class,  ps);
						}
					}.execute();
				} else {
					totalRecord = this.getJdbcTemplate().queryForObject(countSql.getListParameterSQL(), Integer.class,  ps);
				}
			}
			else
			{
				totalRecord = pageSize;
			}
			totalPage = (totalRecord % pageSize) == 0 ? (totalRecord / pageSize) : (totalRecord / pageSize + 1);
			latestSQL.set(new Expr(sql, params));
			if(totalRecord>0) {
				set = (RcdSet)this.getPageSet(fixed,set, sql, pageIndex, pageSize, params);
			}
		} else {
			se=new Expr(sql, params);
			latestSQL.set(se);
			
			final String fsql=sql;
			final Object[] ps=Utils.filterParameter(params);
			final PreparedStatementSetter setter = new ArgumentPreparedStatementSetter(ps);
			final RcdSet fset=set;
			
			if(this.isDisplaySQL()) {
				new SQLPrinter<Object>(this,se,se) {
					@Override
					protected List<Object> actualExecute() {
						getJdbcTemplate().query(fsql,  setter, new RcdResultSetExtractor(new RcdRowMapper(fset,0,getQueryLimit())));
						return null;
					}
				}.execute();
			} else {
				this.getJdbcTemplate().query(fsql,  setter, new RcdResultSetExtractor(new RcdRowMapper(fset,0,this.getQueryLimit())));
			}
		}
		se=new Expr(sql, params);
		se.setSQLDialect(this.getSQLDialect());
		set.setPageInfos(pageSize, pageIndex, totalRecord, totalPage, se);
		return set;

	}
	
	/**
	 * 如果pageSize不为0,则不分页
	 *  @param fixed  指定单行查询，fixed = true时 不查询count
	 * */
	@SuppressWarnings({ "unchecked" })
	private RcdSet queryPageWithMapParameters(boolean fixed,String sql, int pageSize, int pageIndex, Map<String, Object> params) {
		
		if (params == null) {
			params = new HashMap<String, Object>(5);
		}
		
		if(sql.startsWith("#"))
		{
			sql=getSQL(sql);
		}
		Expr se=new Expr(sql, params);
		se.setSQLDialect(this.getDBType().getSQLDialect());
		latestSQL.set(se);
		SQL resultSql=chain.doFilter(se);
		resultSql.setSQLDialect(this.getDBType().getSQLDialect());
		
		sql=resultSql.getNameParameterSQL();
		params=resultSql.getNameParameters();
		
		
		
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
			if(!fixed)
			{
				SQL countSql = getCountSQL(new Expr(sql,params),"X");
				Map<String,Object> ps=Utils.filterParameter(countSql.getNameParameters());
				List<Map<String, Object>> list = null;
				if(this.isDisplaySQL())
				{
					list=new SQLPrinter<List<Map<String, Object>>>(this,countSql,countSql) {
						@Override
						protected List<Map<String, Object>> actualExecute() {
							return getNamedJdbcTemplate().queryForList(countSql.getNameParameterSQL(), ps);
						}
					}.execute();
					
				} else {
					list = getNamedJdbcTemplate().queryForList(countSql.getNameParameterSQL(), ps);
				}
				totalRecord = DataParser.parseInteger((list.get(0).get("X")));
			}
			else
			{
				totalRecord=pageSize;
			}
			totalPage = (totalRecord % pageSize) == 0 ? (totalRecord / pageSize) : (totalRecord / pageSize + 1);

			latestSQL.set(new Expr(sql, params));
			
			if(totalRecord>0) {
				set = (RcdSet)this.getPageSet(fixed,set, sql, pageIndex, pageSize, params);
			}
			
		} else {
			se=new Expr(sql, params);
			latestSQL .set(se);
			final String fsql=sql;
			final Map<String, Object> ps=Utils.filterParameter(params);
			final RcdSet fset=set;
			
			if(this.isDisplaySQL()) {
				new SQLPrinter<Object>(this,se,se) {
					@Override
					protected List<Object> actualExecute() {
						getNamedJdbcTemplate().query(fsql, ps, new RcdResultSetExtractor(new RcdRowMapper(fset,0,getQueryLimit())));
						return null;
					}
				}.execute();
			} else {
				this.getNamedJdbcTemplate().query(fsql, ps, new RcdResultSetExtractor(new RcdRowMapper(fset,0,this.getQueryLimit())));
			}
		}

		if (totalRecord == 0) {
			totalRecord = set.size();
		}
		se=new Expr(sql, params);
		se.setSQLDialect(this.getSQLDialect());
		set.setPageInfos(pageSize, pageIndex, totalRecord, totalPage, se);
		return set;
	}
 
	protected abstract AbstractSet getPageSet(boolean fixed,AbstractSet set,String sql,int pageIndex,int pageSize,Map<String, Object> params);
	
	protected abstract AbstractSet getPageSet(boolean fixed,AbstractSet set, String sql, int pageIndex,int pageSize, Object... params);
	
	
	
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

		if(sql.startsWith("#")) {
			sql=getSQL(sql);
		}
		Expr se=new Expr(sql, ps);
		se.setSQLDialect(this.getDBType().getSQLDialect());
 
		latestSQL.set(se);
		SQL resultSql=chain.doFilter(se);
		resultSql.setSQLDialect(this.getDBType().getSQLDialect());
		Map<String, Object> nps= Utils.filterParameter(resultSql.getNameParameters());
		if(this.isDisplaySQL()) {
			return new SQLPrinter<Integer>(this,se,resultSql) {
				@Override
				protected Integer actualExecute() {
					return getNamedJdbcTemplate().update(resultSql.getNameParameterSQL(), nps);
				}
			}.execute();
		} else {
			return getNamedJdbcTemplate().update(resultSql.getNameParameterSQL(), nps);
		}
	 
	}
	
	/**
	 * 执行一个SQL语句
	 * 
	 * @param sql sql语句
	 * @param ps 参数
	 * @return 影响的行数
	 */
	@Override
	public Integer execute(String sql, Object... ps) {
		
		if(sql.startsWith("#")) {
			sql=getSQL(sql);
		}
		
		Expr se=new Expr(sql, ps);
		se.setSQLDialect(this.getDBType().getSQLDialect());
 
		latestSQL.set(se);
		final SQL resultSql=chain.doFilter(se);
		resultSql.setSQLDialect(this.getDBType().getSQLDialect());
		final Object[] pps=Utils.filterParameter(resultSql.getListParameters());
		//
		if(this.isDisplaySQL())
		{
			return new SQLPrinter<Integer>(this,se,resultSql) {
				@Override
				protected Integer actualExecute() {
					return getJdbcTemplate().update(resultSql.getListParameterSQL(), pps);
				}
			}.execute();
		}
		else
		{
			try {
				return getJdbcTemplate().update(resultSql.getListParameterSQL(), pps);
			} catch (Exception e) {
				Logger.error("语句执行错误("+e.getMessage()+")，\n语句："+se.getSQL());
				throw e;
			}
		}
 
	}
	
	
	/**
	 * 查询记录集
	 * 
	 * @param sql    sql语句
	 * @return RcdSet
	 */
	@Override
	public RcdSet query(SQL sql) {
		sql.setSQLDialect(this.getSQLDialect());  
		return queryPageWithArrayParameters(false,sql.getListParameterSQL(), 0, 0, sql.getListParameters());
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
		return queryPageWithArrayParameters(false,sql, 0, 0, params);
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
		return queryPageWithMapParameters(false,sql, 0, 0, params);
	}
	
	
	
	/**
	 * 查询单个记录
	 * 
	 * @param sql sql语句
	 * @return Rcd
	 */
	@Override
	public Rcd queryRecord(SQL sql) {
		 return this.queryRecord(sql.getListParameterSQL(),sql.getListParameters());
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
		RcdSet rs = queryPageWithArrayParameters(true,sql, 1, 1, params);
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
	public boolean isRecordExits(Rcd r,String table,boolean checkWithOrignalId)
	{
		DBTableMeta tm=this.getTableMeta(table);
		if(tm==null)
		{
			throw new DBMetaException("未发现表"+table+"的Meta数据,请认定表名是否正确");
		}
		
		List<DBColumnMeta> pks = tm.getPKColumns();
		if(pks==null || pks.size()==0)
		{
			throw new DBMetaException("数据表"+table+"未定义主键");
		}
		Where where=new Where();
		String cName=null;
		Object value=null;
		for (DBColumnMeta column : pks) {
			cName=column.getColumn();
			value = checkWithOrignalId? r.getOriginalValue(cName):r.getValue(cName);
			if(value==null)
			{
				throw new DataException(table+"."+cName+" 主键值不允许为空");
			}
			where.and(cName+"=?", value);
		}
		
		where.setSQLDialect(this.getSQLDialect());
		Integer i=this.queryInteger("select count(1) from "+table+" "+where.getListParameterSQL(),where.getListParameters());
		return i>0;
		
	}
	
	/**
	 * 记录是否存已经在数据表,以主键作为判断依据
	 * 
	 * @param r                  记录
	 * @param checkWithOrignalId 是否用原始值(setValue前的值/从数据库查询获得的原始值)来核对数据存在性
	 * @return 是否存在
	 */
	public boolean isRecordExits(Rcd r,boolean checkWithOrignalId)
	{
		if(r.getOwnerSet()==null)
		{
			throw new DBMetaException("当前记录集不属于任何RcdSet,无法识别表名,请调用带表名参数的 updateRecord 方法");
		}
		String[] tables=r.getOwnerSet().getMetaData().getDistinctTableNames();
		if(tables.length!=1)
		{
			throw new DBMetaException("无法正确识别表名,无法识别表名,请调用带表名参数的 updateRecord 方法");
		}
		return isRecordExits(r, tables[0],checkWithOrignalId);
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
	 * */
	public boolean insertRecord(Rcd r)
	{
		return insertRecord(r,r.getOwnerSet().getMetaData().getDistinctTableNames()[0]);
	}
	
	/**
	 * 把记录插入到数据库，表名自动识别
	 * 
	 * @param r 记录
	 * @return 是否成功
	 */
	public boolean insertRecord(Rcd r,String table)
	{
		return insertRecord(r,table,true);
	}
	
	/**
	 * 把记录插入到数据库，表名自动识别
	 * 
	 * @param r 记录
	 * @param table 数据表
	 * @param ignorNulls 是否忽略空值
	 * @return 是否成功
	 */
	public boolean insertRecord(Rcd r,String table,boolean ignorNulls)
	{
		Insert insert=SQLParserUtil.buildInsert(r, table, this,ignorNulls);
		 
		Integer i=0;
		if(insert.hasValue())
		{
			i=this.execute(insert);
			if(i==1)
			{
				r.clearDitryFields();
				r.setNextSaveAction(SaveAction.UPDATE);
			}
		}
		return i==1;
	}
	
	/**
	 * 把记录从数据库删除
	 * 
	 * @param r     记录
	 * @param table 数据表
	 * @return 是否成功
	 */
	public boolean deleteRecord(Rcd r,String table)
	{
		Delete delete =SQLParserUtil.buildDelete(r, table, this);
		Integer i=0;
		if(!delete.isEmpty())
		{
			i=this.execute(delete);
			if(i==1)
			{
				r.clearDitryFields();
				r.setNextSaveAction(SaveAction.INSERT);
			}
		}
		return i==1;
	}
	
	/**
	 * 把记录从数据库删除
	 * 
	 * @param r 记录
	 * @return 是否成功
	 */
	public boolean deleteRecord(Rcd r)
	{
		return deleteRecord(r,r.getOwnerSet().getMetaData().getDistinctTableNames()[0]);
	}
	
	/**
	 * 把记录保存到数据库
	 * 
	 * @param r        记录
	 * @param table    数据表
	 * @param saveMode 保存模式
	 * @return 是否成功
	 */
	public boolean updateRecord(Rcd r,String table,SaveMode saveMode)
	{
		Update update=SQLParserUtil.buildUpdate(r, saveMode, table, this);
 
		Integer i = 0;
		if(update.hasValue())
		{
			i = this.execute(update);
			if(i==1)
			{
				r.clearDitryFields();
				r.setNextSaveAction(SaveAction.UPDATE);
			}
		}
		return i==1;
		
	}
	
	/**
	 * 把记录保存到数据库
	 * 
	 * @param r        记录
	 * @param saveMode 保存模式
	 * @return 是否成功
	 */
	public boolean updateRecord(Rcd r,SaveMode saveMode)
	{
		if(r.getOwnerSet()==null)
		{
			throw new DBMetaException("当前记录集不属于任何RcdSet,无法识别表名,请调用带表名参数的 updateRecord 方法");
		}
		String[] tables=r.getOwnerSet().getMetaData().getDistinctTableNames();
		if(tables.length!=1)
		{
			throw new DBMetaException("无法正确识别表名,无法识别表名,请调用带表名参数的 updateRecord 方法");
		}
		return updateRecord(r,tables[0],saveMode);
	}
	
	/**
	 * 保存记录，在确定场景下，建议使用insertRecord或updateRecord以获得更高性能
	 * */
	public boolean saveRecord(Rcd r,SaveMode saveMode)
	{
		if(r.getNextSaveAction()==SaveAction.INSERT)
		{
			return this.insertRecord(r);
		}
		else if(r.getNextSaveAction()==SaveAction.UPDATE)
		{
			return this.updateRecord(r,saveMode);
		}
		else if(r.getNextSaveAction()==SaveAction.NONE)
		{
			boolean isExists=this.isRecordExits(r,true);
			r.setNextSaveAction(isExists?SaveAction.UPDATE:SaveAction.INSERT);
			return saveRecord(r,saveMode);
		}
		return false;
	}
	
	/**
	 * 保存记录，在确定场景下，建议使用insertRecord或updateRecord以获得更高性能
	 * */
	public boolean saveRecord(Rcd r,String table,SaveMode saveMode)
	{
		if(r.getNextSaveAction()==SaveAction.INSERT)
		{
			return this.insertRecord(r,table);
		}
		else if(r.getNextSaveAction()==SaveAction.UPDATE)
		{
			return this.updateRecord(r,table,saveMode);
		}
		else if(r.getNextSaveAction()==SaveAction.NONE)
		{
			boolean isExists=this.isRecordExits(r,table,true);
			r.setNextSaveAction(isExists?SaveAction.UPDATE:SaveAction.INSERT);
			return saveRecord(r,table,saveMode);
		}
		return false;
	}
	
	
}
