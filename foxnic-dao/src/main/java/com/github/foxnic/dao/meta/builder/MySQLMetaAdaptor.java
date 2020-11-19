package com.github.foxnic.dao.meta.builder;

import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.SQL;

public class MySQLMetaAdaptor extends DBMetaAdaptor {

	@Override
	public RcdSet queryAllTableAndViews(DAO dao,String schema) {
		RcdSet rs=dao.query("SELECT TABLE_NAME,TABLE_COMMENT TC,TABLE_TYPE FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA IN(?,UPPER(?))",schema,schema);
		//TABLE_TYPE值包含  VIEW 和  BASE TABLE 处理成 TABLE 和 VIEW
		for (Rcd r : rs) {	
			String tableType=r.getString("TABLE_TYPE");
			if(tableType.equals("BASE TABLE")) {
				r.set("TABLE_TYPE", "TABLE");
			}
		}
		return rs;
	}

	
	@Override
	public RcdSet queryTableColumns(DAO dao, String schema, String tableName) {
		String[] lines = { "SELECT DISTINCT TABLE_NAME,COLUMN_NAME,DATA_TYPE, CHARACTER_MAXIMUM_LENGTH CHAR_LENGTH, CHARACTER_OCTET_LENGTH DATA_LENGTH , ",
				"NUMERIC_PRECISION NUM_PRECISION,NUMERIC_SCALE NUM_SCALE,IS_NULLABLE NULLABLE, (CASE WHEN EXTRA='auto_increment' THEN 'YES' ELSE 'NO' END)  AUTO_INCREASE,",
				"COLUMN_KEY KEY_TYPE,COLUMN_COMMENT COMMENTS,COLUMN_DEFAULT ",
				"FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA IN(?,UPPER(?)) AND LOWER(TABLE_NAME)=?" };
		Expr se = new Expr(SQL.joinSQLs(lines), schema, schema, tableName.toLowerCase());
		RcdSet rs = dao.query(se);
		return rs;
	}


	@Override
	public RcdSet queryTableIndexs(DAO dao, String schema, String tableName) {
		
		String[] lines = {
				"SELECT A.TABLE_NAME,A.NON_UNIQUE,A.INDEX_NAME,A.SEQ_IN_INDEX SORT,A.COLUMN_NAME,IFNULL(B.CONSTRAINT_TYPE,'NORMAL') CONSTRAINT_TYPE FROM INFORMATION_SCHEMA.STATISTICS A ",
				"LEFT JOIN INFORMATION_SCHEMA.TABLE_CONSTRAINTS B ON A.TABLE_SCHEMA=B.TABLE_SCHEMA AND A.TABLE_NAME=B.TABLE_NAME AND A.INDEX_NAME=B.CONSTRAINT_NAME ",
				"WHERE A.TABLE_SCHEMA IN (?,UPPER(?))  AND A.TABLE_NAME IN (?,UPPER(?)) ",
				"ORDER BY A.TABLE_NAME,A.INDEX_NAME,A.SEQ_IN_INDEX" };

		Expr se = new Expr(SQL.joinSQLs(lines), schema, schema, tableName, tableName);
		RcdSet rs = dao.query(se);
		return rs;
	}
	
}
