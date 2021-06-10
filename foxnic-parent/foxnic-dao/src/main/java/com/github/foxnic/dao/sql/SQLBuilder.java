package com.github.foxnic.dao.sql;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLSequenceExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement.ValuesClause;
import com.alibaba.druid.sql.dialect.oracle.ast.OracleSQLObjectImpl;
import com.alibaba.druid.sql.parser.ParserException;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.entity.EntityUtils;
import com.github.foxnic.dao.excel.DataException;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBMapping;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.GlobalSettings;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.exception.DBMetaException;
import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.expr.Delete;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.Insert;
import com.github.foxnic.sql.expr.Update;

/**
 * 把字符串的SQL语句转成语句对象
 */
public class SQLBuilder {

	public static class StatementValue {

		private String table = null;

		private String field = null;
		private Object value = null;
		boolean isExpr = false;

		/**
		 * 字段名
		 * 
		 * @return 字段名
		 */
		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		public boolean isExpr() {
			return isExpr;
		}

		public void setExpr(boolean isExpr) {
			this.isExpr = isExpr;
		}

		public String getTable() {
			return table;
		}

		public void setTable(String table) {
			this.table = table;
		}

	}

	/**
	 * 把Insert语句转成数据对象
	 * 
	 * @param sql     SQL语句
	 * @param dialect 方言
	 * @return Insert对象
	 */
	public static List<List<StatementValue>> parseInsertValue(String sql, SQLDialect dialect) {
		List<SQLStatement> stmtList = null;
		try {
			stmtList = SQLUtils.parseStatements(sql, DBMapping.getDruidDBType(dialect));
		} catch (Exception e) {
			throw new ParserException("SQL语句解析失败,语法错误," + sql, e);
		}

		if (stmtList.size() != 1) {
			throw new ParserException("QL语句解析失败,包含多个语句," + sql);
		}

		SQLStatement stmt = stmtList.get(0);
		if (!(stmt instanceof SQLInsertStatement)) {
			throw new ParserException("SQL语句解析失败,不是一个Insert语句," + sql);
		}

		SQLInsertStatement insert = (SQLInsertStatement) stmt;

		List<SQLExpr> columns = insert.getColumns();
		List<ValuesClause> valuesClauses = insert.getValuesList();
		if (valuesClauses.size() != 1) {
			throw new ParserException("SQL语句解析失败,不支持多个Value子句," + sql);
		}
		String table = insert.getTableName().getSimpleName();
		List<List<StatementValue>> all = new ArrayList<List<StatementValue>>(valuesClauses.size());
		String filed = null;
		StatementValue sqlValue = null;

		for (ValuesClause valuesClause : valuesClauses) {

			List<SQLExpr> values = valuesClause.getValues();
			List<StatementValue> list = new ArrayList<SQLBuilder.StatementValue>();
			all.add(list);

			for (int i = 0; i < columns.size(); i++) {

				sqlValue = getFieldValue(values.get(i));

				filed = getFieldName(columns.get(i));

				sqlValue.setTable(table);
				sqlValue.setField(filed);

				list.add(sqlValue);
			}
		}

		return all;
	}

	/**
	 * 把Insert语句转成对象
	 * 
	 * @param sql     SQL语句
	 * @param dialect 方言
	 * @return Insert对象
	 */
	public static List<Insert> parseMultiInsert(String sql, SQLDialect dialect) {
		List<Insert> inserts = new ArrayList<Insert>();
		List<List<StatementValue>> all = parseInsertValue(sql, dialect);
		if (all == null || all.size() == 0)
			return inserts;

		for (List<StatementValue> values : all) {

			if (values == null || values.size() == 0)
				continue;
			Insert insert = new Insert(values.get(0).getTable());
			inserts.add(insert);

			for (StatementValue value : values) {
				if (value.isExpr()) {
					insert.setExpr(value.getField(), value.getValue().toString());
				} else {
					insert.set(value.getField(), value.getValue());
				}
			}
		}
		return inserts;
	}

	/**
	 * 把Insert语句转成对象, 高级功能请参看StatementUtil 类
	 * 
	 * @param sql     SQL语句
	 * @param dialect 方言
	 * @return Insert对象
	 */
	public static Insert parseInsert(String sql, SQLDialect dialect) {
		List<Insert> inserts = SQLBuilder.parseMultiInsert(sql, dialect);
		if (inserts.size() == 0)
			return null;
		return inserts.get(0);
	}

	/**
	 * 把Insert语句转成对象，使用GlobalSettings中默认的方言，高级功能请参看StatementUtil 类
	 * 
	 * @param sql SQL语句
	 * @return Insert对象
	 */
	public static Insert parseInsert(String sql) {
		return parseInsert(sql, GlobalSettings.DEFAULT_SQL_DIALECT);
	}

	private static StatementValue getFieldValue(SQLExpr value) {
		Object fieldValue = null;
		StatementValue sValue = new StatementValue();
		sValue.setExpr(false);
		if (value instanceof SQLSequenceExpr) {
			fieldValue = ((SQLSequenceExpr) value).getSequence().getSimpleName() + "."
					+ ((SQLSequenceExpr) value).getFunction().name();
			sValue.setExpr(true);
		} else if (value instanceof SQLCharExpr) {
			fieldValue = ((SQLCharExpr) value).getText();
		} else if (value instanceof OracleSQLObjectImpl) {
			fieldValue = ((OracleSQLObjectImpl) value).toString();
			sValue.setExpr(true);
		} else {
			throw new RuntimeException("无法识别的类型:" + value.getClass().getName());
		}

		sValue.setValue(fieldValue);

		return sValue;
	}

	private static String getFieldName(SQLExpr column) {
		String filed = null;
		SQLIdentifierExpr identifierColumn;
		if (column instanceof SQLIdentifierExpr) {
			identifierColumn = (SQLIdentifierExpr) column;
			filed = identifierColumn.getName();
		}
		return filed;
	}

	public static Insert buildInsert(Rcd r) {
		return buildInsert(r, r.getOwnerSet().getMetaData().getDistinctTableNames()[0],
				DAO.getInstance(r.getOwnerSet()), true);
	}
	
	public static Insert buildInsert(Rcd r, boolean ignorNulls) {
		return buildInsert(r, r.getOwnerSet().getMetaData().getDistinctTableNames()[0],
				DAO.getInstance(r.getOwnerSet()), ignorNulls);
	}

	public static Insert buildInsert(Rcd r,String table, boolean ignorNulls) {
		return buildInsert(r, table,DAO.getInstance(r.getOwnerSet()), ignorNulls);
	}

	public static Insert buildInsert(Rcd r, String table, DAO dao, boolean ignorNulls) {
		Insert insert = new Insert(table);
		DBTableMeta tm = dao.getTableMeta(table);
		if (tm == null) {
			throw new DBMetaException("未发现表" + table + "的Meta数据,请认定表名是否正确");
		}

		List<DBColumnMeta> columns = tm.getColumns();
		Expr seVal = null;
		Object val = null;
		int idx=-1;
		for (DBColumnMeta column : columns) {
			idx=r.getOwnerSet().getMetaData().name2index(column.getColumn());
			if(idx==-1) continue;
			seVal = r.getExpr(column.getColumn());

			val = r.getValue(column.getColumn());
			// make value cast
			val = column.getDBDataType().cast(val);

			if (seVal == null) {
				if (ignorNulls) {
					if (val != null) {
						insert.set(column.getColumn(), val);
					}
				} else {
					insert.set(column.getColumn(), val);
				}
			} else {
				insert.setExpr(column.getColumn(), seVal);
			}
		}
		insert.setSQLDialect(dao.getSQLDialect());
		return insert;
	}

	public static Delete buildDelete(Rcd r,String table,DAO dao)
	{
		Delete delete=new Delete();
		delete.from(table);
		DBTableMeta tm=dao.getTableMeta(table);
		if(tm==null)
		{
			throw new DBMetaException("未发现表"+table+"的Meta数据,请认定表名是否正确");
		}
		
		List<DBColumnMeta> pks = tm.getPKColumns();
		String cName=null;
		Object value=null;
 
		for (DBColumnMeta column : pks) {
			cName=column.getColumn();
			value= r.getOriginalValue(cName);
			if(value==null) {
				throw new DataException(table+"."+cName+" 主键值不允许为空");
			}
			delete.where().and(cName+"=?", value);
		}
		delete.setSQLDialect(dao.getSQLDialect());
		return delete;
	}
	
	public static Delete buildDelete(Rcd r) {
		return buildDelete(r, r.getOwnerSet().getMetaData().getDistinctTableNames()[0],
				DAO.getInstance(r.getOwnerSet()));
	}

	public static Update buildUpdate(Rcd r, SaveMode saveMode) {
		return buildUpdate(r, saveMode, r.getOwnerSet().getMetaData().getDistinctTableNames()[0],
				DAO.getInstance(r.getOwnerSet()));
	}

	public static Update buildUpdate(Rcd r, SaveMode saveMode, String table, DAO dao) {
		Update update = new Update(table);
		DBTableMeta tm = dao.getTableMeta(table);
		if (tm == null) {
			throw new DBMetaException("未发现表" + table + "的Meta数据,请认定表名是否正确");
		}
		Expr seVal = null;
		List<DBColumnMeta> pks = tm.getPKColumns();
		if (pks == null || pks.size() == 0) {
			throw new DBMetaException("数据表" + table + "未定义主键");
		}

		List<DBColumnMeta> columns = tm.getColumns();
		Object value = null;
		boolean dirty = false;
		String cName = null;

		for (DBColumnMeta column : columns) {
			cName = column.getColumn();
			seVal = r.getExpr(cName);

			value = r.getValue(cName);
			// 处理类型的值
			value = column.getDBDataType().cast(value);

			if (saveMode == SaveMode.ALL_FIELDS) {
				if (seVal != null) {
					update.setExpr(cName, seVal);
				} else {
					update.set(cName, value);
				}
			} else if (saveMode == SaveMode.DIRTY_FIELDS) {
				dirty = r.isDirty(cName);
				if (dirty) {
					if (seVal != null) {
						update.setExpr(cName, seVal);
					} else {
						update.set(cName, value);
					}
				}
			} else if (saveMode == SaveMode.NOT_NULL_FIELDS) {
				if (value != null || seVal != null) {
					if (seVal != null) {
						update.setExpr(cName, seVal);
					} else {
						update.set(cName, value);
					}
				}
			}
		}

		for (DBColumnMeta column : pks) {
			cName = column.getColumn();
			value = r.getOriginalValue(cName);
			if (value == null) {
				throw new DataException(table + "." + cName + " 主键值不允许为空");
			}
			update.where().and(cName + "=?", value);
		}
		update.setSQLDialect(dao.getSQLDialect());
		return update;
	}

	public static ConditionExpr buildConditionExpr(Object sample, String table, DAO dao) {
		List<String> fields = EntityUtils.getEntityFields(sample.getClass(), dao, table);
		ConditionExpr ce = new ConditionExpr();
		Object value = null;
		for (String field : fields) {
			value = BeanUtil.getFieldValue(sample, field);
			value = dao.getDBTreaty().revertLogicToDBValue(value);
			ce.andIf(field + " = ?", value);
		}
		ce.setSQLDialect(dao.getSQLDialect());
		return ce;
	}

}
