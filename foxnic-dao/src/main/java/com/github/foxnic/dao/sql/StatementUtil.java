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
import com.github.foxnic.dao.meta.DBMapping;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.expr.Insert;

/**
 * 把字符串的SQL语句转成语句对象
 * */
public class StatementUtil {

	public static class StatementValue  {
		
		private String table=null;
 
		private String field=null;
		private Object value=null;
		boolean isExpr=false;
		
		/**
		 * 字段名
		 * @return 字段名
		 * */
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
	 * @param sql SQL语句
	 * @param dialect 方言
	 * @return Insert对象
	 * */
	public static List<List<StatementValue>> parseInsertValue(String sql,SQLDialect dialect)
	{
		List<SQLStatement> stmtList=null;
		try {
			stmtList = SQLUtils.parseStatements(sql, DBMapping.getDruidDBType(dialect));
		} catch (Exception e) {
			throw new ParserException("SQL语句解析失败,语法错误,"+sql,e);
		}
 
		if(stmtList.size()!=1) {
			throw new ParserException("QL语句解析失败,包含多个语句,"+sql);
		}
		
		SQLStatement stmt=stmtList.get(0);
		if(!(stmt instanceof SQLInsertStatement)) {
			throw new ParserException("SQL语句解析失败,不是一个Insert语句,"+sql);
		}
		
		SQLInsertStatement insert=(SQLInsertStatement)stmt;
		
		List<SQLExpr> columns=insert.getColumns();
		List<ValuesClause> valuesClauses= insert.getValuesList();
		if(valuesClauses.size()!=1) {
			throw new ParserException("SQL语句解析失败,不支持多个Value子句,"+sql);
		}
		String table=insert.getTableName().getSimpleName();
		List<List<StatementValue>> all=new ArrayList<List<StatementValue>>(valuesClauses.size());
		String filed=null;
		StatementValue sqlValue=null;
		
		for (ValuesClause valuesClause : valuesClauses) {
			
			List<SQLExpr> values=valuesClause.getValues();
			List<StatementValue> list=new ArrayList<StatementUtil.StatementValue>();
			all.add(list);
			
			for (int i = 0; i < columns.size(); i++) {
				 
				sqlValue=getFieldValue(values.get(i));
			 
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
	 * @param sql SQL语句
	 * @param dialect 方言
	 * @return Insert对象
	 * */
	public static List<Insert> parseInsert(String sql,SQLDialect dialect)
	{
		List<Insert> inserts=new ArrayList<Insert>();
		List<List<StatementValue>> all=parseInsertValue(sql, dialect);
		if(all==null || all.size()==0) return inserts;
		
		for (List<StatementValue> values : all) {
			
			if(values==null || values.size()==0) continue;
			Insert insert=new Insert(values.get(0).getTable());
			inserts.add(insert);
			
			for (StatementValue value : values) {
				if(value.isExpr()) {
					insert.setExpr(value.getField(), value.getValue().toString());
				} else {
					insert.set(value.getField(), value.getValue());
				}
			}
		}
		return inserts;
	}
	

	private static StatementValue getFieldValue(SQLExpr value) {
		Object fieldValue = null;
		StatementValue sValue=new StatementValue();
		sValue.setExpr(false);
		if(value instanceof SQLSequenceExpr) {
			fieldValue=((SQLSequenceExpr)value).getSequence().getSimpleName()+"."+((SQLSequenceExpr)value).getFunction().name();
			sValue.setExpr(true);
		} else if(value instanceof SQLCharExpr) {
			fieldValue=((SQLCharExpr)value).getText();
		} else if(value instanceof OracleSQLObjectImpl) {
			fieldValue=((OracleSQLObjectImpl)value).toString();
			sValue.setExpr(true);
		} else {
			throw new RuntimeException("无法识别的类型:"+value.getClass().getName());
		}
		
		sValue.setValue(fieldValue);
		
		return sValue;
	}
	
	private static String getFieldName(SQLExpr column) {
		String filed=null;
		SQLIdentifierExpr identifierColumn;
		if(column instanceof SQLIdentifierExpr){
			identifierColumn=(SQLIdentifierExpr)column;
			filed=identifierColumn.getName();
		}
		return filed;
	}
	

	
	
	
}
