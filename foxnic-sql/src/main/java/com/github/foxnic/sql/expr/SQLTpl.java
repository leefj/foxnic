package com.github.foxnic.sql.expr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.exception.SQLValidateException;
import com.github.foxnic.sql.parser.cache.LocalCacheImpl;
import com.github.foxnic.sql.parser.cache.SQLParserCache;

/**
 * tql文件中定义的SQL模板 绑定的段落使用 #{NAME} 形式
 * @author fangjieli
 */
public class SQLTpl extends SubSQL {

	private static final long serialVersionUID = 1102385812104665003L;
	
	private static SQLParserCache CACHE=null;
	
	/**
	 * 放入解析结果，避免相同语句反复解析
	 * */
	private static synchronized  void putAR(SQLDialect dialect,String sql,AnalyseRsult r)
	{
		if(CACHE==null) {
			CACHE=new LocalCacheImpl();
		}
		CACHE.put(dialect.name()+":"+sql,r);
	}
	
	private static AnalyseRsult getAR(SQLDialect dialect,String sql)
	{
		if(CACHE==null) {
			CACHE=new LocalCacheImpl();
		}
		return (AnalyseRsult)CACHE.get(dialect.name()+":"+sql);
	}
	
	/**
	 * 原始表达式
	 */
	public Expr origSE = null;
	private String sqlId=null;
	private String origSQL=null;
	
	private Expr finalSQL=null;
	
	private List<String> placeHolders=new ArrayList<>();
	private List<String> splitParts=new ArrayList<>();
	private String lastSqlPart=null;
	boolean inited=false;
	
	
	/**
	 * @param sql SQL语句，绑定变量以 :PNAME或?方式存在
	 * @param map 用于绑定 :PNAME 方式的绑定变量
	 * @param ps  用于绑定 ? 方式的绑定变量
	 */
	public SQLTpl(String sql, Map<String, Object> map, Object... ps) {
		origSE = new Expr(sql, map, ps);
		origSQL=sql;
		 
	}

	/**
	 * @param sql SQL语句，绑定变量以 :PNAME或?方式存在
	 * @param map 用于绑定 :PNAME 方式的绑定变量
	 */
	public SQLTpl(String sql, Map<String, Object> map) {
		origSE = new Expr(sql, map);
		origSQL=sql;
		 
	}

	/**
	 * @param sql SQL语句，绑定变量以 :PNAME或?方式存在
	 * @param ps  用于绑定 ? 方式的绑定变量
	 */
	public SQLTpl(String sql, Object... ps) {
		origSE = new Expr(sql, ps);
		origSQL=sql;
 
	}
	
	
	
	private void initIf() {
		if(inited) {
			return;
		}
		inited=true;
		
		AnalyseRsult ar=getAR(this.getSQLDialect(),origSQL);
		
		if(ar==null)
		{
			analyse(origSE.getListParameterSQL());
			this.splitParts.add(this.lastSqlPart);
			ar=new AnalyseRsult(splitParts,placeHolders);
			putAR(this.getSQLDialect(), origSQL, ar);
		}
		else
		{
			this.splitParts=ar.getSplitParts();
			this.placeHolders=ar.getPlaceHolders();
		}
		
		

	}
	
	private HashMap<String, SQL> placeHolderSQLs=null;
	
	/**
	 * 设置占位符的SQL语句或SQL表达式
	 * @param placeHolder 占位符
	 * @param sql 语句
	 * @return SQLTpl
	 * */
	public SQLTpl setPlaceHolder(String placeHolder,SQL sql)
	{
		if(placeHolderSQLs==null) {
			placeHolderSQLs=new HashMap<>();
		}
		placeHolderSQLs.put(placeHolder, sql);
		finalSQL=null;
		return this;
	}
	
	
	/**
	 * 设置占位符的SQL语句或SQL表达式
	 * @param placeHolder 占位符
	 * @param sql 语句
	 * @param ps 参数
	 * @return SQLTpl
	 * */
	public SQLTpl setPlaceHolder(String placeHolder,String sql,Object... ps)
	{
		return setPlaceHolder(placeHolder,new Expr(sql,ps));
	}
	
	/**
	 * 设置占位符的SQL语句或SQL表达式
	 * @param placeHolder 占位符
	 * @param sql 语句
	 * @return SQLTpl
	 * */
	public SQLTpl setPlaceHolder(String placeHolder,String sql)
	{
		return setPlaceHolder(placeHolder,new Expr(sql));
	}

	private Character getNextChar(char[] chars,int i)
	{
		if(i+1>=chars.length) {
			return null;
		}
		return chars[i+1];
	}
	
	private void analyse(String sql) {

		int i = -1;
		sql = " " + sql + " ";
		char[] chars = sql.toCharArray();
		String part1 = "";
		String part2 = sql;
		
		
		int matchCount = 0;
		char c;
		Character c_next = null;
		while (true) {

			i++;
			if (i >= chars.length) {
				break;
			}

			c = chars[i];
 
			c_next=getNextChar(chars,i);
			
			int z = Expr.jumpIf(sql, i, false);
			if (z == -1) {
				throw new SQLValidateException("语句:" + origSQL + " , 在 "+Expr.getNearBy(origSQL,sql,i,splitParts.size()>0)+" 附近存在语法错误!");
			} else {
				if (z != i) {
					i = z;
					continue;
				}
			}

			if ('#' == c && '{' == c_next) {
 
				matchCount++;
				part1 = part2.substring(0, i);
				splitParts.add(part1);
				int end = chars.length;
				
				char ic;
				Character ic_next;

				String tplname=null;
				for (int j = i + 1; j < chars.length; j++) {
					ic = chars[j];
 
					ic_next=getNextChar(chars, j);
					if ('}' == ic) {
//					if ('}' == ic && '}' == ic_next) {
						end = j;
						tplname = part2.substring(i + 2, end);
						if (!tplname.trim().isEmpty()) {
							break;
						}
					}
				}
				
				this.placeHolders.add(tplname);
				
				part2 = part2.substring(end+2, part2.length());
				lastSqlPart = part2;
				if (part2.length() > 0) {
					analyse(part2);
					return;
				}
				
			}
			
			if (matchCount == 0) {
				this.lastSqlPart = part2;
			}

		}

	}

	
	public void buildFinalSQLIf()
	{

//		if(finalSQL!=null) {
//			finalSQL.setDAO(this.getDAO());
//			return;
//		}
		
		initIf();
		
		Object[] ps=origSE.getListParameters();
		String placeHolder=null;
		
		String part=this.splitParts.get(0);
		Expr header=new Expr(part,ps);
		if(ps.length>0) {
			try {
				ps=ArrayUtil.subArray(ps, header.getListParameters().length, ps.length-1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ps=ArrayUtil.subArray(ps, header.getListParameters().length, ps.length-1);
			}
		}
		String tplSQL=null;
		for (int i = 1; i < this.splitParts.size(); i++) {
			
			placeHolder=this.placeHolders.get(i-1);
			
			//判断是否是一个SQL-ID
			tplSQL=null;
//			if(placeHolder.startsWith("#"))
//			{
//				tplSQL=SQLoader.getSQL(placeHolder, this.getSQLDialect().getDbType());
//			}
			
			SQL phSQL=this.placeHolderSQLs.get(placeHolder);
			if(phSQL==null && tplSQL==null)
			{
				throw new IllegalArgumentException("未设置占位符:"+placeHolder);
			}

			if(phSQL!=null)
			{
				header=header.append(phSQL);
			}
			else
			{
				if(tplSQL!=null)
				{
					header=header.append(tplSQL);
				}
			}

			part=this.splitParts.get(i);
			Expr partSE=new Expr(part,ps);
			
			if(i<this.splitParts.size()-1 && ps.length>0)
			{
				ps=ArrayUtil.subArray(ps, partSE.getListParameters().length, ps.length);
			}
			
			header=header.append(partSE);
			
		}
		
		finalSQL = header;
		finalSQL.setParent(this);
		//finalSQL.setDAO(this.getDAO());

	}
	

	@Override
	public String getSQL(SQLDialect dialect) {
		buildFinalSQLIf();
		return finalSQL.getSQL(dialect);
	}

	@Override
	public String getListParameterSQL() {
		buildFinalSQLIf();
		return finalSQL.getListParameterSQL();
	}

	@Override
	public Object[] getListParameters() {
		buildFinalSQLIf();
		return finalSQL.getListParameters();
	}

	@Override
	public String getNamedParameterSQL() {
		buildFinalSQLIf();
		this.beginParamNameSQL();
		String str=finalSQL.getListParameterSQL();
		this.endParamNameSQL();
		return str;
	}

	@Override
	public Map<String, Object> getNamedParameters() {
		buildFinalSQLIf();
		this.beginParamNameSQL();
		Map<String, Object> map=finalSQL.getNamedParameters();
		this.endParamNameSQL();
		return map;
	}

	@Override
	public boolean isEmpty() {
		buildFinalSQLIf();
		return finalSQL.isEmpty();
	}

	@Override
	public boolean isAllParamsEmpty() {
		buildFinalSQLIf();
		return finalSQL.isAllParamsEmpty();
	}
 
	private static class AnalyseRsult implements Serializable
	{
		private static final long serialVersionUID = -6318109139390942413L;
		private List<String> splitParts=null;
		
		public List<String> getSplitParts() {
			return splitParts;
		}

		public List<String> getPlaceHolders() {
			return placeHolders;
		}

		private List<String> placeHolders=null;
		
		public AnalyseRsult(List<String> parts,List<String> placeHolders)
		{
			this.splitParts=parts;
			this.placeHolders=placeHolders;
		}
	}

	 
}


 