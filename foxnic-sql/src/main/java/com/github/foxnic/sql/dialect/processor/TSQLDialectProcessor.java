package com.github.foxnic.sql.dialect.processor;

import java.util.Date;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.sql.dialect.datatype.DataTypeMappingSet;

public class TSQLDialectProcessor extends SQLDialectProcessor {

	private SQLExpr nowExpr=null;
	@Override
	public SQLExpr getNowExpr() {
		if(nowExpr!=null) return nowExpr;
		nowExpr = new SQLMethodInvokeExpr("GETDATE");
		return nowExpr;
	}

	private SQLExpr nowTimestampExpr=null;
	@Override
	public SQLExpr getNowTimestampExpr() {
		if(nowTimestampExpr!=null) return nowTimestampExpr;
		//待改进
		nowTimestampExpr= new SQLMethodInvokeExpr("GETDATE"); 
		return nowTimestampExpr;
	}
	
	@Override
	public String castCharInjection(String chars) {
		//待验证
		chars=chars.replaceAll("'", "\\\\'");
		return chars;
	}
	
	@Override
	public String getToDateTimeSQL(Date date) {
		//待验证
		String dstr = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
		dstr = " str_to_date('" + dstr + "','%Y-%m-%d %H:%i:%s') ";
		return dstr;
	}

	@Override
	public String quotes(String name) {
		// TODO 未实现
		return name;
	}

	@Override
	public DataTypeMappingSet getDataTypeMappingSet() {
		// TODO Auto-generated method stub
		return null;
	}
	
//	private  DBMetaAdaptor dbMetaAdaptor=null;
//	
//	@Override
//	public DBMetaAdaptor getDBMetaAdaptor() {
//		return dbMetaAdaptor;
//	}

}
