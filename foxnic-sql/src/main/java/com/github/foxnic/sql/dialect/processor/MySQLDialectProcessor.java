package com.github.foxnic.sql.dialect.processor;

import java.util.Date;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.sql.dialect.datatype.DataTypeMappingSet;
import com.github.foxnic.sql.dialect.datatype.MySQLDataTypeMappingSet;

public class MySQLDialectProcessor extends SQLDialectProcessor {

	
	private SQLExpr nowExpr=null;
	@Override
	public SQLExpr getNowExpr() {
		if(nowExpr!=null) return nowExpr;
		nowExpr = new SQLMethodInvokeExpr("now");
		return nowExpr;
	}

	private SQLExpr nowTimestampExpr=null;
	@Override
	public SQLExpr getNowTimestampExpr() {
		if(nowTimestampExpr!=null) return nowTimestampExpr;
		nowTimestampExpr=new SQLMethodInvokeExpr("unix_timestamp");
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
		dstr = " str_to_date('" + dstr + "','%Y-%m-%d %H:%i:%s') ";
		return dstr;
	}
	
	@Override
	public String quotes(String name) {
		name=removeQuotations(name,'`','"');
		name="`"+name+"`";
		return name;
	}
	
	private MySQLDataTypeMappingSet mySQLDataTypeMappingSet = new MySQLDataTypeMappingSet();
	
	@Override
	public DataTypeMappingSet getDataTypeMappingSet() {
		return mySQLDataTypeMappingSet;
	}
	
//	private  DBMetaAdaptor dbMetaAdaptor=new MySQLMetaAdaptor();
//	
//	@Override
//	public DBMetaAdaptor getDBMetaAdaptor() {
//		return dbMetaAdaptor;
//	}

}
