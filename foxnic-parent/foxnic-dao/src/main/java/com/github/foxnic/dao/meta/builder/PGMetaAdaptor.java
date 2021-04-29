package com.github.foxnic.dao.meta.builder;

import java.util.HashMap;

import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.meta.DBIndexMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.dao.sql.SQLParser;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.SQL;

public class PGMetaAdaptor extends DBMetaAdaptor {

	@Override
	public RcdSet queryAllTableAndViews(DAO dao,String schema) {
		RcdSet rs=dao.query("SELECT tb.table_name, d.description tc,tb.table_type FROM information_schema.tables tb JOIN pg_class c ON c.relname = tb.table_name LEFT JOIN pg_description d ON d.objoid = c.oid AND d.objsubid = '0' WHERE tb.table_schema = ?",schema);
		return rs;
	}
	
	
	private HashMap<String, String> pkIndexName=new HashMap<String, String>();

	@Override
	public RcdSet queryTableColumns(DAO dao, String schema, String tableName) {
		// 不包含主键信息
		String[] lines = { 
				"select col.table_name, col.column_name,col.data_type, col.character_maximum_length char_length,",
				"(case when col.numeric_precision is not null then col.numeric_precision", 
				"when col.datetime_precision is not null then col.datetime_precision", 
				"when col.interval_precision is not null then col.interval_precision", 
				"else null end) num_precision,",
				"col.numeric_scale num_scale,col.is_nullable nullable,col.column_default column_default,d.description \"comments\" , ",
				"(case when col.identity_increment is null then 'NO' else 'YES' end) auto_increase,null key_type,",
				"(case when col.numeric_precision is null then character_maximum_length else numeric_precision end) data_length",
				"from information_schema.columns col",
				"join pg_class c on c.relname = col.table_name",
				"left join pg_description d on d.objoid = c.oid and d.objsubid = col.ordinal_position",
				"where col.table_schema in (?,upper(?)) and col.table_name in (?,upper(?))",
				"order by col.table_name, col.ordinal_position"
		};

		Expr se = new Expr(SQL.joinSQLs(lines), schema,schema,tableName, tableName);
		RcdSet rs = dao.query(se);

		
		 
		// 补充主键信息
		RcdSet pkcolumns = dao.query("select pg_constraint.conname as pk_name from pg_constraint inner join pg_class on pg_constraint.conrelid = pg_class.oid where pg_class.relname in(?,upper(?)) and pg_constraint.contype = 'p'",tableName, tableName);
		
		if(pkcolumns.size()>0) {
			String pkName=pkcolumns.getRcd(0).getString("pk_name");
			pkIndexName.put(schema+"."+tableName, pkName);
			String def=dao.queryString("select indexdef from PG_INDEXES where tablename in(?,upper(?))  and indexname=?",tableName, tableName,pkName);
			DBIndexMeta dim=SQLParser.parseIndexCreationStatement(def, dao.getSQLDialect());
			
			pkcolumns.changeColumnLabel("pk_name", "column_name");
			while(pkcolumns.size()>0) {
				pkcolumns.remove(0);
			}
			for (String f: dim.getFields()) {
				Rcd r=new Rcd(pkcolumns);
				r.set("column_name", f);
				pkcolumns.add(r);
			}
			
		}
		//
		for (Rcd pk : pkcolumns) {
			Rcd r = rs.find("COLUMN_NAME", pk.getString("COLUMN_NAME"));
			if (r != null)
				r.set("KEY_TYPE", "PRI");
		}
		return rs;
	}

	@Override
	public RcdSet queryTableIndexs(DAO dao, String schema, String tableName) {
		 
		String pkName=pkIndexName.get(schema+"."+tableName);

		RcdSet rs = dao.query("SELECT '' INDEX_NAME,'' TABLE_OWNER,'' TABLE_NAME,'' NON_UNIQUE,'' TABLESPACE_NAME,'' COLUMN_NAME,'' SORT,null COLUMN_LENGTH,'' CONSTRAINT_TYPE");
		rs.remove(0);
		RcdSet defs = dao.query("select * from pg_indexes where tablename in (?,upper(?))",tableName,tableName);
		String cType="";
		for (Rcd r : defs) {
			DBIndexMeta dim=SQLParser.parseIndexCreationStatement(r.getString("indexdef"), dao.getSQLDialect());
			for (String f : dim.getFields()) {
				Rcd dr=new Rcd(rs);
				dr.set("INDEX_NAME", dim.getName());
				dr.set("TABLE_NAME", dim.getTable());
				dr.set("COLUMN_NAME", f);
				
				if(dim.getName().equals(pkName)) {
					cType="PRIMARY KEY";
				} else if(dim.isUnique()) {
					cType="UNIQUE";
				} else {
					cType="";
				}
				dr.set("CONSTRAINT_TYPE", cType);
				rs.add(dr);
			}
		}

		return rs;
	}
	
}
