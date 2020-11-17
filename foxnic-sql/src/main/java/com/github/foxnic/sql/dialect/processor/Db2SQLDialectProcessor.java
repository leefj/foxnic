package com.github.foxnic.sql.dialect.processor;

import java.util.Date;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.sql.dialect.datatype.DB2DataMappingSet;
import com.github.foxnic.sql.dialect.datatype.DataTypeMappingSet;

public class Db2SQLDialectProcessor extends SQLDialectProcessor {

	
	private SQLExpr nowExpr=null;
	@Override
	public SQLExpr getNowExpr() {
		if(nowExpr!=null) return nowExpr;
		nowExpr = new SQLMethodInvokeExpr("sysdate");
		return nowExpr;
	}

	private SQLExpr nowTimestampExpr=null;
	@Override
	public SQLExpr getNowTimestampExpr() {
		if(nowTimestampExpr!=null) return nowTimestampExpr;
		nowTimestampExpr=new SQLMethodInvokeExpr("current timestamp");
		((SQLMethodInvokeExpr)nowTimestampExpr).addParameter(getNowExpr());
		return nowTimestampExpr;
	}
	
	@Override
	public String castCharInjection(String chars) {
		chars=chars.replaceAll("'", "''");
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
		name="`"+name+"`";
		return name;
	}

	private DB2DataMappingSet db2DataMappingSet = new DB2DataMappingSet();
	
	@Override
	public DataTypeMappingSet getDataTypeMappingSet() {
		return db2DataMappingSet;
	}

	/*
	 * private DBMetaAdaptor dbMetaAdaptor=new DB2MetaAdaptor();
	 * 
	 * @Override public DBMetaAdaptor getDBMetaAdaptor() { return dbMetaAdaptor; }
	 */

}
