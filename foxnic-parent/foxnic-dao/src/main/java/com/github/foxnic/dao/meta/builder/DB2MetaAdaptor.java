package com.github.foxnic.dao.meta.builder;

import java.util.ArrayList;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.expr.Expr;
import com.github.foxnic.sql.expr.SQL;

public class DB2MetaAdaptor extends DBMetaAdaptor {

	@Override
	public RcdSet queryAllTableAndViews(DAO dao,String schema) {
		RcdSet rs=dao.query("SELECT  TABNAME TABLE_NAME,REMARKS TC,TYPE  TABLE_TYPE FROM SYSCAT.TABLES WHERE TABSCHEMA IN ( ?,UPPER(?)) AND TYPE IN  ('T','V')",schema,schema);
		//TABLE_TYPE值包含  T 和  V 处理成 TABLE 和 VIEW
		for (Rcd r : rs) {	
			String tableType=r.getString("TABLE_TYPE");
			if(tableType.equals("T")) {
				r.set("TABLE_TYPE", "TABLE");
			} else if(tableType.equals("V")) {
				r.set("TABLE_TYPE", "VIEW");
			}
		}
		return rs;
	}

	@Override
	public RcdSet queryTableColumns(DAO dao, String schema, String tableName) {
		
		String sql="SELECT S.*,DECODE(GENERATED,'A','YES','NO') AUTO_INCREASE,'' KEY_TYPE,LONGLENGTH DATA_LENGTH, LENGTH CHAR_LENGTH ,NAME COLUMN_NAME,LENGTH NUM_PRECISION  FROM SYSIBM.SYSCOLUMNS S WHERE TBNAME  IN( ?,UPPER(?)) AND TBCREATOR IN( ?,UPPER(?))";
		Expr se=new Expr(sql,tableName,tableName,schema,schema);
		
		RcdSet rs=dao.query(se);
		
		if(rs.size()>0) {
			//重命名列，因为LENGTH字段问题
			rs.changeColumnLabel("TBNAME", "TABLE_NAME");
			rs.changeColumnLabel("NAME", "COLUMN_NAME");
			rs.changeColumnLabel("COLTYPE", "DATA_TYPE");
			rs.changeColumnLabel("LENGTH", "NUM_PRECISION");
			rs.changeColumnLabel("SCALE", "NUM_SCALE");
			rs.changeColumnLabel("NULLS", "NULLABLE");
			rs.changeColumnLabel("DEFAULT", "COLUMN_DEFAULT");
			rs.changeColumnLabel("REMARKS", "COMMENTS");
		}
		//
		RcdSet pkcolumns=dao.query("SELECT A.TABNAME , B.COLNAME COLUMN_NAME  FROM SYSCAT.TABCONST A ,SYSCAT.KEYCOLUSE B WHERE A.CONSTNAME = B.CONSTNAME AND A.TYPE='P' AND A.TABNAME IN (?,UPPER(?)) AND A.TABSCHEMA IN (?,UPPER(?)) ORDER BY COLSEQ ASC",tableName,tableName,schema,schema);
		for (Rcd pk : pkcolumns) {
			Rcd r=rs.find("COLUMN_NAME",pk.getString("COLUMN_NAME"));
			if(r!=null) r.set("KEY_TYPE", "PRI");
		}
		
		return rs;
	}
	
	
	@Override
	public RcdSet queryTableIndexs(DAO dao, String schema, String tableName) {
		
		String[] lines= {
				"SELECT  INDSCHEMA table_owner,INDNAME index_name ,TABSCHEMA table_owner,UNIQUERULE ,COLNAMES,'' CONSTRAINT_TYPE,'' COLUMN_NAME ",
				" FROM syscat.indexes A WHERE TABSCHEMA in  (?,upper(?)) AND TABNAME in (?,upper(?))"
		};
 
		Expr se = new Expr(SQL.joinSQLs(lines), schema, schema, tableName, tableName);
		RcdSet rs = dao.query(se);
		
		//列数据分离到多行
		ArrayList<Rcd> nrs=new ArrayList<>();
		for (Rcd r : rs) {
			
			String uType=r.getString("UNIQUERULE"); //
			if("P".equalsIgnoreCase(uType)) {
				r.set("CONSTRAINT_TYPE", "PRIMARY KEY");
			} else if("U".equalsIgnoreCase(uType)) {
				r.set("CONSTRAINT_TYPE", "UNIQUE");
			} else if("D".equalsIgnoreCase(uType)) {
				r.set("CONSTRAINT_TYPE", "NON-UNIQUE");
			}
			String colstr=r.getString("COLNAMES");
			colstr=StringUtil.removeFirst(colstr, "+");
			String[] cols=colstr.split("\\+");
			if(cols.length==1) {
				r.set("COLUMN_NAME", cols[0]);
			} else {
				for (int i = 1; i < cols.length; i++) {
					Rcd nr=r.clone();
					nr.set("COLUMN_NAME", cols[i]);
					nrs.add(nr);
				}
			}
			
		}
		//
		for (Rcd r : nrs) {
			rs.add(r);
		}
		
		return rs;
	}
	
}
