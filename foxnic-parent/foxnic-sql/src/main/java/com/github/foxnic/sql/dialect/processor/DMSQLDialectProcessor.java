package com.github.foxnic.sql.dialect.processor;

import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.sql.dialect.datatype.DataTypeMappingSet;
import com.github.foxnic.sql.dialect.datatype.DmDataMappingSet;
import com.github.foxnic.sql.dialect.datatype.OracleDataMappingSet;

import java.util.Date;

public class DMSQLDialectProcessor extends SQLDialectProcessor {

//	private SQLExpr nowExpr=null;
//	@Override
//	public SQLExpr getNowExpr() {
//		if(nowExpr!=null) return nowExpr;
//		nowExpr = new SQLIdentifierExpr("sysdate");
//		return nowExpr;
//	}
//
//	private SQLExpr nowTimestampExpr=null;
//	@Override
//	public SQLExpr getNowTimestampExpr() {
//		if(nowTimestampExpr!=null) return nowTimestampExpr;
//		nowTimestampExpr=new SQLIdentifierExpr("systimestamp");
//		return nowTimestampExpr;
//	}

	@Override
	public String castCharInjection(String chars) {
		chars=chars.replaceAll("'", "\\'\\'");
		return chars;
	}

	@Override
	public String getToDateTimeSQL(Date date) {
		String dstr = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
		dstr = " to_date('" + dstr + "','yyyy-mm-dd hh24:mi:ss') ";
		return dstr;
	}

	@Override
	public String quotes(String name) {
		name=removeQuotations(name,'`','"');
		name="\""+name+"\"";
		return name;
	}


	private DmDataMappingSet dmDataMappingSet = new DmDataMappingSet();

	@Override
	public DataTypeMappingSet getDataTypeMappingSet() {
		return dmDataMappingSet;
	}

	/*
	 * private DBMetaAdaptor dbMetaAdaptor=new OracleMetaAdaptor();
	 *
	 * @Override public DBMetaAdaptor getDBMetaAdaptor() { return dbMetaAdaptor; }
	 */

}
