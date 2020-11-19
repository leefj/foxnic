package com.github.foxnic.dao.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBMapping;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.dao.sql.SQLParserUtil;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;
import com.github.foxnic.sql.exception.DBMetaException;
import com.github.foxnic.sql.exception.NoFieldException;
import com.github.foxnic.sql.expr.SQL;

/**
 * 查询结果元数据
 * 
 * @author fangjieli
 */
public class QueryMetaData implements Serializable {

	private static final DefaultNameConvertor NC = new DefaultNameConvertor();
	/**
	 * 
	 */
	private static final long serialVersionUID = 3959245702470021386L;

	private int columnCount = 0;

	/**
	 * 列数量
	 * 
	 * @return 数量
	 */
	public int getColumnCount() {
		return columnCount;
	}

	/**
	 * 设置列数量
	 * 
	 * @param c 列数量
	 */
	protected void setColumnCount(int c) {
		columnCount = c;
	}

	private ArrayList<String> catalogNames = new ArrayList<String>();

	/**
	 * 设置列所在表的分类名称（Schema）
	 * 
	 * @param name 分类名称
	 */
	protected void addCatalogName(String name) {
		catalogNames.add(name);
	}

	/**
	 * 获得列所在表的分类名称（Schema）, 不同的数据库与JDBC驱动，支持不一致
	 * 
	 * @param i 列序号
	 * @return 分类名
	 */
	public String getCatalogName(int i) {
		return catalogNames.get(i);
	}

	private ArrayList<String> columnClassNames = new ArrayList<String>();

	/**
	 * 设置列数据类型名称
	 * 
	 * @param className 类名
	 */
	protected void addColumnClassName(String className) {
		columnClassNames.add(className);
	}

	/**
	 * 获得列的类型名
	 * 
	 * @param i 列序号
	 * @return 类型名
	 */
	public String getColumnClassName(int i) {
		return columnClassNames.get(i);
	}

	private ArrayList<String> columnLabel = new ArrayList<String>();

	/**
	 * 设置列标签
	 * 
	 * @param label 类名
	 */
	protected void addColumnLabel(String label) {
		columnLabel.add(label);
	}

	/**
	 * 获得列标签，标签与SQL中查询字段一致
	 * 
	 * @param i 列序号
	 * @return 标签
	 */
	public String getColumnLabel(int i) {
		return columnLabel.get(i);
	}

	private List<String> fields = null;

	/**
	 * 获得字段清单,与 getColumnLabels 一致
	 * 
	 * @return 列名列表
	 */
	public List<String> getFields() {
		if (fields != null)
			return fields;
		fields = Arrays.asList(this.getColumnLabels());
		return fields;
	}

	/**
	 * 获得字段清单,与 getFields 一致
	 * 
	 * @return 列名列表
	 */
	public String[] getColumnLabels() {
		String[] arr = new String[columnLabel.size()];
		return columnLabel.toArray(arr);
	}

	private ArrayList<Integer> columnType = new ArrayList<Integer>();

	/**
	 * 添加 JDBC 列类型
	 * 
	 * @param type java.sql.Types中的类型常量
	 */
	protected void addColumnType(Integer type) {
		columnType.add(type);
	}

	/**
	 * 获得 JDBC 列类型
	 * 
	 * @param i 列序号
	 * @return 类型，java.sql.Types中的类型常量
	 */
	public Integer getColumnType(int i) {
		return columnType.get(i);
	}

	private ArrayList<String> columnTypeName = new ArrayList<String>();

	/**
	 * 添加列类型名称
	 * 
	 * @param typeName 类型名
	 */
	protected void addColumnTypeName(String typeName) {
		columnTypeName.add(typeName);
	}

	/**
	 * 获得列类型名称
	 * 
	 * @param i 列序号
	 * @return 类型名称
	 */
	public String getColumnTypeName(int i) {
		return columnTypeName.get(i);
	}

	private ArrayList<String> schemaName = new ArrayList<String>();

	/**
	 * 添加列数据库Schema名称
	 * 
	 * @param schName Schema名称
	 */
	protected void addSchemaName(String schName) {
		schemaName.add(schName);
	}

	/**
	 * 获得列数据库Schema名称
	 * 
	 * @param i 列序号
	 * @return Schema名称
	 */
	public String getSchemaName(int i) {
		return schemaName.get(i);
	}

	private ArrayList<String> tableName = new ArrayList<String>();

	/**
	 * 添加列所在的表名
	 * 
	 * @param table 表名
	 */
	protected void addTableName(String table) {
		tableName.add(table);
	}

	/**
	 * 获得列所在的表名
	 * 
	 * @param i 列序号
	 * @return 表名
	 */
	public String getTableName(int i) {
		String table = tableName.get(i);
		if (StringUtil.noContent(table) && sqlTables != null && sqlTables.length == 1) {
			table = sqlTables[0];
		}
		return table;
	}

	private String[] distinctTableNames = null;

	/**
	 * 获得去重后的查询表清单
	 * 
	 * @return 清单
	 */
	public String[] getDistinctTableNames() {
		if (distinctTableNames != null)
			return distinctTableNames;
		HashSet<String> tables = new HashSet<>();
		for (String table : tableName) {
			if (!StringUtil.hasContent(table))
				continue;
			if (!tables.contains(table)) {
				tables.add(table);
			}
		}

		if (tables.isEmpty()) {
			if (this.getSQL() != null) {
				List<String> aTables = SQLParserUtil.getAllTables(this.getSQL().getListParameterSQL(),
						DBMapping.getDruidDBType(this.getSQL().getSQLDialect()));
				distinctTableNames = aTables.toArray(new String[aTables.size()]);
			} else {
				distinctTableNames = tables.toArray(new String[tables.size()]);
			}
		} else {
			distinctTableNames = tables.toArray(new String[tables.size()]);
		}
		return distinctTableNames;
	}

	private HashMap<String, Integer> nameIndexMap = new HashMap<String, Integer>();
	private HashMap<String, Integer> varIndexMap = new HashMap<String, Integer>();

	/**
	 * 设置列名与索引位置的对照关系
	 * 
	 * @param field 列名
	 * @param index 索引位置
	 */
	protected void setMap(String field, int index) {
		nameIndexMap.put(field.toUpperCase().trim(), index);
		varIndexMap.put(NC.getPropertyName(field.trim()), index);
	}

	/**
	 * 通过列名获得索引位置
	 * 
	 * @param field 列名
	 * @return 索引位置
	 */
	public int name2index(String field) {
		Integer i = nameIndexMap.get(field.toUpperCase());
		if (i == null)
			i = varIndexMap.get(field);
		if (i == null)
			i = varIndexMap.get(field.toLowerCase());
		if (i == null) {
			return -1;
//			throw new DBMetaException("字段 "+field+" 不存在或无法识别");
		} else {
			return i;
		}
	}

	private SQL sql = null;

	/**
	 * 获得查询语句
	 * 
	 * @return 查询语句
	 */
	public SQL getSQL() {
		return sql;
	}

	private String[] sqlTables = null;

	/**
	 * 设置查询语句
	 * 
	 * @param sql 设置查询语句
	 */
	protected void setSQL(SQL sql) {
		initSQLTableIf(sql);
		this.sql = sql;
	}

	private void initSQLTableIf(SQL sql) {
		if (sqlTables == null) {
			sqlTables = SQLParserUtil.getAllTables(sql.getSQL(), DBMapping.getDruidDBType(this.getSQL().getSQLDialect()))
					.toArray(new String[0]);
		}
	}

	private SQL pagedSQL = null;

	/**
	 * 获得分页查询语句
	 * 
	 * @return 查询语句
	 */
	public SQL getPagedSQL() {
		return pagedSQL;
	}

	/**
	 * 设置分页查询语句
	 * 
	 * @param pagedSQL 分页查询语句
	 */
	protected void setPagedSQL(SQL pagedSQL) {
		initSQLTableIf(pagedSQL);
		this.pagedSQL = pagedSQL;
	}

	private long sqlTime = 0;

	/**
	 * 数据库端返回耗时
	 * 
	 * @return 时长，毫秒
	 */
	public long getSqlTime() {
		return sqlTime;
	}

	/**
	 * 设置查询时长
	 * 
	 * @param sqlTime 查询时长
	 */
	protected void setSqlTime(long sqlTime) {
		this.sqlTime = sqlTime;
	}

	/**
	 * 产生记录集耗时
	 * 
	 * @return 取数时长
	 */
	public long getDataTime() {
		return dataTime;
	}

	/**
	 * 设置取数时长
	 * 
	 * @param dataTime 取数时长
	 */
	protected void setDataTime(long dataTime) {
		this.dataTime = dataTime;
	}

	private long dataTime = -1;

	/**
	 * 查询耗时，包括数据处理的事件
	 * 
	 * @return 时长，毫秒
	 */
	public long getQueryTime() {
		return sqlTime + dataTime;
	}

	/**
	 * 清空语句信息
	 */
	public void clearSQLInfo() {
		this.pagedSQL = null;
		this.sql = null;
	}

	/**
	 * 情况时间信息
	 */
	public void clearTimeInfo() {
		this.dataTime = 0;
		this.sqlTime = 0;
	}

	private static HashMap<String, QueryMetaData> QUERYMETADATA_CACHE = new HashMap<>();

	/**
	 * 三个对象建立关系，绑定，内部用
	 * 
	 * @param tm  DBTableMeta
	 * @param rs  RcdSet
	 * @param dao DAO
	 * @return QueryMetaData
	 */
	public static QueryMetaData bind(DBTableMeta tm, RcdSet rs, DAO dao) {
		String key = dao.getDBConnectionIdentity() + "@" + tm.getTableName();

		QueryMetaData meta = QUERYMETADATA_CACHE.get(key);
		if (meta == null) {
			meta = new QueryMetaData();
			int columnCount = 0;

			List<DBColumnMeta> columns = tm.getColumns();
			for (DBColumnMeta column : columns) {
				meta.addCatalogName("ByTable");
				meta.addColumnClassName(null);
				meta.addColumnLabel(column.getColumn());
				meta.addColumnType(-1);
				meta.addColumnTypeName(null);
				meta.addSchemaName("ByTableData");
				meta.addTableName(tm.getTableName());
				meta.setMap(column.getColumn(), columnCount);
				columnCount++;
			}
			meta.setColumnCount(columnCount);

			QUERYMETADATA_CACHE.put(key, meta);
		}

		rs.setRawMetaData(meta);
		return meta;
	}

	/**
	 * 变更列名
	 * 
	 * @param oldLabel 旧列标签、旧列名
	 * @param newLabel 新列标签、新列名
	 */
	public void changeColumnLabel(String oldLabel, String newLabel) {
		if (!StringUtil.hasContent(newLabel) || !StringUtil.hasContent(oldLabel)) {
			throw new DBMetaException("名称不允许为空");
		}

		oldLabel = oldLabel.trim();
		newLabel = newLabel.trim();

		if (newLabel.equals(oldLabel))
			return;

		// 老的名称不区分大小写
		int i = this.name2index(oldLabel);
		if (i == -1) {
			throw new NoFieldException(oldLabel);
		}

		int j = this.name2index(newLabel);
		if (j != -1) {
			columnLabel.set(i, newLabel);
		} else {
			columnLabel.set(i, newLabel);
			nameIndexMap.remove(oldLabel.toUpperCase());
			nameIndexMap.put(newLabel.toUpperCase(), i);

			varIndexMap.remove(NC.getPropertyName(oldLabel));
			varIndexMap.put(NC.getPropertyName(newLabel), i);
		}

	}

	/**
	 * 复制
	 * 
	 * @return QueryMetaData
	 */
	@SuppressWarnings("unchecked")
	@Override
	public QueryMetaData clone() {

		QueryMetaData meta = new QueryMetaData();
		meta.catalogNames = (ArrayList<String>) this.catalogNames.clone();
		meta.columnClassNames = (ArrayList<String>) this.columnClassNames.clone();

		meta.columnCount = this.columnCount;
		meta.columnLabel = (ArrayList<String>) this.columnLabel.clone();
		meta.columnType = (ArrayList<Integer>) this.columnType.clone();
		meta.columnTypeName = (ArrayList<String>) this.columnTypeName.clone();
		meta.dataTime = this.dataTime;
		meta.nameIndexMap = (HashMap<String, Integer>) this.nameIndexMap.clone();
		meta.varIndexMap = (HashMap<String, Integer>) this.varIndexMap.clone();
		meta.pagedSQL = this.pagedSQL;
		meta.schemaName = this.schemaName;
		meta.sql = this.sql;
		meta.sqlTime = this.sqlTime;
		meta.tableName = this.tableName;
		meta.fields = null;
		return meta;
	}

	/**
	 * 删除列
	 * 
	 * @param i 列序号
	 */
	public void deleteColumn(int i) {

		this.fields = null;
		this.catalogNames.remove(i);
		this.columnClassNames.remove(i);
		this.columnLabel.remove(i);
		this.columnType.remove(i);
		this.columnTypeName.remove(i);

		ArrayList<String> ks = new ArrayList<String>();
		for (String k : nameIndexMap.keySet()) {
			if (i == nameIndexMap.get(k)) {
				ks.add(k);
			}
		}
		for (String k : ks) {
			nameIndexMap.remove(k);
		}

		this.columnCount--;
	}

}