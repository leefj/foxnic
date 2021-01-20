package com.github.foxnic.dao.meta.builder;

import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.SQL;

public class OracleMetaAdaptor extends DBMetaAdaptor {

	@Override
	public RcdSet queryAllTableAndViews(DAO dao,String schema) {
		RcdSet rs=dao.query("SELECT T.TABLE_NAME,COMMENTS TC ,TABLE_TYPE  FROM USER_TABLES T,USER_TAB_COMMENTS C WHERE T.TABLE_NAME=C.TABLE_NAME UNION ALL SELECT VIEW_NAME,COMMENTS TC,'VIEW' TABLE_TYPE FROM USER_VIEWS T ,USER_TAB_COMMENTS C  WHERE  T.VIEW_NAME=C.TABLE_NAME");
		return rs;
	}

	@Override
	public RcdSet queryTableColumns(DAO dao, String schema, String tableName) {
		// 不包含主键信息
		String[] lines = { "SELECT",
				"C.TABLE_NAME,C.COLUMN_NAME,C.DATA_TYPE,C.DATA_LENGTH,C.CHAR_LENGTH,C.DATA_PRECISION NUM_PRECISION,C.DATA_SCALE NUM_SCALE,C.NULLABLE,C.DATA_DEFAULT COLUMN_DEFAULT,CM.COMMENTS,'NO' AUTO_INCREASE,NULL KEY_TYPE,",
				" FROM  USER_TAB_COLUMNS C",
				" LEFT JOIN  USER_COL_COMMENTS  CM ON C.TABLE_NAME=CM.TABLE_NAME AND C.COLUMN_NAME=CM.COLUMN_NAME",
				" WHERE C.TABLE_NAME IN (?,UPPER(?))" };

		Expr se = new Expr(SQL.joinSQLs(lines), tableName, tableName);
		RcdSet rs = dao.query(se);

		// 补充主键信息
		RcdSet pkcolumns = dao.query("SELECT  CON.CONSTRAINT_NAME,CONSTRAINT_TYPE,STATUS,COLUMN_NAME,POSITION FROM USER_CONSTRAINTS CON,USER_CONS_COLUMNS COL WHERE CON.CONSTRAINT_NAME=COL.CONSTRAINT_NAME AND CON.CONSTRAINT_TYPE='P' AND CON.TABLE_NAME IN (?,UPPER(?)) ORDER BY POSITION",tableName, tableName);
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
		String[] sqls= {
				"SELECT I.INDEX_NAME,I.TABLE_OWNER,I.TABLE_NAME,I.UNIQUENESS NON_UNIQUE,I.TABLESPACE_NAME,C.COLUMN_NAME,C.COLUMN_POSITION SORT,C.COLUMN_LENGTH,I.UNIQUENESS CONSTRAINT_TYPE",
				" FROM USER_INDEXES I, USER_IND_COLUMNS C",
				" WHERE I.INDEX_NAME = C.INDEX_NAME AND  TABLE_OWNER IN  (?,UPPER(?))  AND I.TABLE_NAME IN  (?,UPPER(?))  ",
				" ORDER BY INDEX_NAME,COLUMN_POSITION ASC"
		};
		Expr se=new Expr(SQL.joinSQLs(sqls),schema,schema,tableName,tableName);
		RcdSet rs = dao.query(se);
		return rs;
	}
	
}
