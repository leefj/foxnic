package com.github.foxnic.dao.spring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.GlobalSettings;
import com.github.foxnic.sql.expr.SQL;

public abstract class SQLPrinter<T> {

	private static HashSet<String> IGNORE_PREFIX=new HashSet<>();
	
	public static void addIgnorePrefix(String pkg)
	{
		pkg=StringUtil.removeLast(pkg, ".")+".";
		IGNORE_PREFIX.add(pkg);
		
	}
	
	private long statrtPoint=0;
 
	private DAO dao;
	
	protected SQL inSQL=null;
	protected SQL finalSQL=null;
	
	public SQLPrinter(DAO dao,SQL inSQL,SQL finalSQL)
	{
		this.dao=dao;
		this.inSQL=inSQL;
		this.finalSQL=finalSQL;
	}
	
	protected abstract T actualExecute();
	 
	
	public T execute()
	{
		boolean error=false;
		T result = null ;
		try {
			statrtPoint=System.currentTimeMillis();
			result = actualExecute();
			error=false;
			return result;
		} catch (Exception e) {
			error=true;
			throw e;
		}
		finally
		{
			printIn(result,error);
		}
	}
 
	private void printIn(Object result,boolean error)
	{
		long cost=System.currentTimeMillis()-statrtPoint;
		if(!dao.isDisplaySQL()) return;
	 
		String str=inSQL.getNamedParameterSQL();
		String snap=str;
		if(snap.length()>80)
		{
			snap=snap.substring(0,78)+"...";
		}
		
		snap=snap.replace("\n\r", " ");
		snap=snap.replace("\r\n", " ");
		snap=snap.replace("\n", " ");
		snap=snap.replace("\r", " ");
		snap=snap.replace("\t", " ");
 
		
		String trace=StringUtil.toString(new Throwable());
		String[] traceLines=trace.split("\n");
		List<String> lns=new ArrayList<>();
		String ln=null;
		boolean notAdd=true;
		for (int i = 1; i < traceLines.length; i++) {
			ln=traceLines[i].trim();
			ln=ln.substring(3).trim();
			notAdd=ln.startsWith(com.github.foxnic.dao.GlobalSettings.PACKAGE+".") ||  ln.startsWith(com.github.foxnic.sql.GlobalSettings.PACKAGE+".") || ln.startsWith("org.springframework.") || ln.startsWith("sun.") || ln.startsWith("java.") || ln.startsWith("javax.") 
					|| ln.contains("$$EnhancerBySpringCGLIB$$") || ln.contains("$$FastClassBySpringCGLIB$$") || ln.startsWith("org.apache.catalina.core.");
			
			if(notAdd) continue;
			
			for (String pfx : IGNORE_PREFIX) {
				if(ln.startsWith(pfx)) {
					notAdd=true;
					break;
				}
			}
			
			if(notAdd) continue;
			
			lns.add(ln);
			if(lns.size()>=3) break;
		}
 
		CodeBuilder cb=new CodeBuilder();
		cb.ln("┏━━━━━ SQL ["+snap+"] ━━━━━ ");
		if(lns.size()>0) {
			cb.ln("┣ 位置：");
			for (String e : lns) {
				cb.ln("┣━\tat "+e);
			}
		}
		cb.ln("┣ 传入：");
		cb.ln("┣━ SQL：\t"+str);
		cb.ln("┣━ 参数：\t"+JSONObject.toJSONString(inSQL.getNamedParameters()));
		cb.ln("┣━ 调试：\t"+inSQL.getSQL());
		cb.ln("┣ 执行：");
		cb.ln("┣━ SQL：\t"+finalSQL.getListParameterSQL());
		cb.ln("┣━ 参数：\t"+JSONObject.toJSONString(finalSQL.getNamedParameters()));
		cb.ln("┣━ 调试：\t"+finalSQL.getSQL());
		if(!error)
		{
			cb.ln("┣ 结果： ");
			cb.ln("┣━ 耗时：\t"+cost+"ms");
			
			if(result instanceof RcdSet)
			{
				result="RcdSet,size="+((RcdSet)result).size();
			}
			else if(result instanceof Rcd)
			{
				result="Rcd,"+((Rcd)result).toJSONObject();
			}
			
			cb.ln("┣━ 返回：\t"+result);
		}
		cb.ln("┗━━━━━ SQL ["+snap+"] ━━━━━ ");
		Logger.info("\n"+cb.toString());
		
	}
	
	 
	
	
	
	
}
