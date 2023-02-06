package com.github.foxnic.sql.expr;

import com.github.foxnic.commons.collection.IPagedList;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.GlobalSettings;
import com.github.foxnic.sql.data.ExprDAO;
import com.github.foxnic.sql.data.ExprRcd;
import com.github.foxnic.sql.data.ExprRcdSet;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.exception.SQLValidateException;
import com.github.foxnic.sql.parser.cache.SQLParserCache;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;



/**
 * SimpleExpression 的缩写 简单表达式，SQL表达式
 * @author fangjieli
 * */
public class Expr extends SubSQL implements QueryableSQL {

	private static final long serialVersionUID = 4922774964031198960L;

	private static SQLParserCache CACHE=null;

	private static synchronized  void putAR(SQLDialect dialect,String sql,AnalyseRsult r)
	{
		if(CACHE==null) {
			CACHE=GlobalSettings.SQL_PARSER_CACHE_TYPE.createSQLParserCache();
		}
		CACHE.put("foxnic:expr:"+dialect.name()+":"+sql,r);
	}

	private static AnalyseRsult getAR(SQLDialect dialect,String sql)
	{
		if(CACHE==null) {
			return null;
		}
		return (AnalyseRsult)CACHE.get("foxnic:expr:"+dialect.name()+":"+sql);
	}

	private static final String PARAM_NAME_SUFFIX_CHARS = "(+-*/ ><=, )\n\r";

	private int paramIndex = -1;
	private ArrayList<Object> paramValues = new ArrayList<Object>();
	private ArrayList<Object> paramValueIndexes = new ArrayList<Object>();

	private String originalSQL = null;
	private ArrayList<String> splitParts = new ArrayList<String>();
	private String lastSqlPart = "";

	/**
	 * 是否换行
	 */
	private boolean isBr=false;
	boolean inited=false;
	private void initIf() {
		if(inited) {
			return;
		}
		inited=true;

		// 只考虑此四种情况，其它不考虑
		isBr="\n".equals(originalSQL) || "\r".equals(originalSQL) || "\r\n".equals(originalSQL) || "\n\r".equals(originalSQL);

		boolean userSet=false;

		AnalyseRsult ar=getAR(this.getSQLDialect(),originalSQL);

		if(ar==null) {
			//分析语句
			analyse(this.originalSQL, originalMap, originalPs);
			splitParts.add(this.lastSqlPart);
			for (int i = 0; i < splitParts.size(); i++) {
				splitParts.set(i, splitParts.get(i).trim());
			}
			ar=new AnalyseRsult(splitParts,paramValueIndexes);
			//校验结果，如果结果错误，抛出异常
			ar.validate();
			putAR(this.getSQLDialect(),originalSQL, ar);
			userSet=true;
			//System.err.println("NC");
		} else {
			try {
				ar.validate();
			} catch (Exception e) {
				//如果缓存中的结果错误，就从新解析
				putAR(this.getSQLDialect(),originalSQL, null);
				inited=false;
				initIf();
			}
			splitParts=ar.getSplitParts();
			paramValueIndexes=ar.getPsIndexes();
			userSet=false;
			//System.err.println("UC");
		}


		//设置参数值
		for (int i = 0; i < this.paramValueIndexes.size(); i++) {
			Object index = this.paramValueIndexes.get(i);
			if (index instanceof String) {
				String indexStr = (String) index;
				if (userSet) {
					this.paramValues.set(i, originalMap.get(indexStr));
				} else {
					this.paramValues.add(originalMap.get(indexStr));
				}
			} else {
				Integer indexInt = (Integer) index;
				if (userSet) {
					this.paramValues.set(i, originalPs[indexInt]);
				} else {
					this.paramValues.add(originalPs[indexInt]);
				}
			}
		}

		// 设置从属关系
		for (Object val : paramValues) {
			if (val instanceof SQL) {
				SQL se = (SQL) val;
				se.setParent(this);
			}
		}
	}


	private Map<String, Object> originalMap=null;
	private Object[] originalPs=null;

	/**
	 * 与new方法等价
	 * */
	public static Expr create(String sql, Object... ps) {
		return new Expr(sql,ps);
	}

	/**
	 * 与new方法等价
	 * */
	public static Expr create(String sql, Map<String, Object> map, Object... ps) {
		return new Expr(sql,map,ps);
	}

	public Expr() {
		this.originalSQL="";
		this.originalMap=new HashMap<String, Object>();
		this.originalPs=new Object[] {};
	}


	public Expr(String sql, Object... ps) {
		if(sql==null) {
			return;
		}
		sql=sql.trim();
		this.originalSQL=sql;
		this.originalMap=new HashMap<String, Object>();
		if(ps==null)
		{
			this.originalPs=new Object[] {ps};
		}
		else
		{
			this.originalPs=ps;
		}
	}

	/**
	 * @param sql  SQL语句，绑定变量以 :PNAME或?方式存在
	 * @param map 用于绑定 :PNAME 方式的绑定变量
	 * @param ps 用于绑定 ? 方式的绑定变量
	 * */
	public Expr(String sql, Map<String, Object> map, Object... ps) {
		if(sql==null) {
			return;
		}
		sql=sql.trim();
		this.originalSQL=sql;
		this.originalMap=map;
		if(ps==null)
		{
			this.originalPs=new Object[] {ps};
		}
		else
		{
			this.originalPs=ps;
		}
	}





	private void err(String msg) {
		(new Exception(msg)).printStackTrace();
	}



	private void analyse(String sql, Map<String, Object> map, Object... ps) {
		sql = " " + sql + " ";
		char[] chars = sql.toCharArray();
		String part1 = "";
		String part2 = sql;
		int i = -1;
		int matchCount = 0;
		char c;
		Character nextChar=null;
		while (true) {

			i++;
			if (i >= chars.length) {
				break;
			}

			c = chars[i];

			if(i+1<chars.length-2)
			{
				nextChar=chars[i+1];
			}
			else
			{
				nextChar=null;
			}
			int z = jumpIf(sql, i);
			if (z == -1) {
				//err("语句" + sql + "，在第" + i + "个字符处,没有找到与之对应的结尾字符,可能存在语法错误!");
				throw new SQLValidateException("语句:" + originalSQL + " , 在 "+Expr.getNearBy(originalSQL,sql,i,splitParts.size()>0)+" 附近存在语法错误!");
			} else {
				if (z != i) {
					i = z;
					continue;
				}
			}

			if (c == '?') {
				matchCount++;
				part1 = part2.substring(0, i);
				this.splitParts.add(part1);
				part2 = part2.substring(i + 1, part2.length());
				lastSqlPart = part2;
				paramIndex++;
				if (paramIndex >= ps.length) {
					err(part1 + "? 处参数个数不足");
					return;
				}

				this.paramValues.add(ps[paramIndex]);
				this.paramValueIndexes.add(paramIndex);

				if (part2.length() > 0) {
					analyse(part2, map, ps);
					return;
				}
			} else if (c == ':' && nextChar!=null && nextChar!='=' && !ignorColon) {
				matchCount++;
				part1 = part2.substring(0, i);
				this.splitParts.add(part1);
				int end = chars.length;
				String pname =null;
				for (int j = i + 1; j < chars.length; j++) {
					char ic = chars[j];
					if (PARAM_NAME_SUFFIX_CHARS.indexOf(ic) > -1) {
						end = j;
						pname = part2.substring(i + 1, end).trim();
						if(!pname.trim().isEmpty())
						{
							break;
						}
					}
				}
//				pname = part2.substring(i + 1, end).trim();

				if (!map.containsKey(pname)) {
					err(part2 + ":" + pname + " 处没有指定参数值");
					return;
				}
				this.paramValues.add(map.get(pname));
				this.paramValueIndexes.add(pname);
				//part2 = part2.substring(end + 1, part2.length()).trim();
				part2 = part2.substring(end, part2.length());
				lastSqlPart = part2;
				if (part2.length() > 0) {
					analyse(part2, map, ps);
					return;
				}
			}

			if (matchCount == 0) {
				this.lastSqlPart = part2;
			}
		}
	}

	public static int jumpIf(String sql, int i) {
		return jumpIf(sql, i, false);
	}

	public static int jumpIf(String sql, int i,boolean includeBracket) {
		String s1 = null;
		if (i < sql.length() - 1) {
			s1 = sql.substring(i, i + 1);
		}
		String s2 = null;
		if (i < sql.length() - 2) {
			s2 = sql.substring(i, i + 2);
		}

		if (s1 != null) {

			if (s1.equals(SQLKeyword.SINGLE_QUATE.toString())) {
				return jumpSingleQuateIf(sql, i);
			} else if (s1.equals(SQLKeyword.LEFT_DOUBLE_QUATE.toString())) {
				return jumpDoubleQuateIf(sql, i);
			}  else if (s1.equals(SQLKeyword.LEFT_BACK_QUATE.toString())) {
				return jumpBackQuateIf(sql, i);
			}


			if (includeBracket && s1.equals(SQLKeyword.LEFT_BRACKET.toString())) {
				return jumpBracketIf(sql, i);
			}

		}

		if (s2 != null) {
			if (s2.equals(SQLKeyword.SINGLE_REMARK.toString()) || s2.equals(SQLKeyword.HASHTAG.toString())) {
				return jumpSingleLineRemarkIf(sql, i);
			} else if (s2.equals(SQLKeyword.LEFT_REMARK.toString())) {
				return jumpMulityLineRemarkIf(sql, i);
			}
		}
		return i;
	}

	private static int jumpBracketIf(String sql, int i) {
		boolean matched = false;
		int brackets=1;
		while (true) {
			i++;
			if (i >= sql.length() - 1) {
				break;
			}
			String c1 = sql.substring(i, i + 1);
			if(c1.equals(SQLKeyword.LEFT_BRACKET.toString()))
			{
				brackets++;
			}
			if (c1.equals(SQLKeyword.RIGHT_BRACKET.toString())) {
				brackets--;
				if(brackets==0)
				{
					matched = true;
					break;
				}
			}
		}
		return matched ? i : -1;
	}

	private static int jumpSingleQuateIf(String sql, int i) {
		boolean matched = false;
		while (true) {
			i++;
			if (i >= sql.length()) {
				break;
			}
			String c1 = sql.substring(i, i + 1);
			String c2 = null;
			if (i + 1 <= sql.length() - 1) {
				c2 = sql.substring(i + 1, i + 2);
			}
			if (c1.equals(SQLKeyword.SINGLE_QUATE.toString())) {
				if (c2 != null) {
					if (c2.equals(SQLKeyword.SINGLE_QUATE.toString())) {
						i++;
					} else {
						matched = true;
						break;
					}
				} else {
					matched = true;
					break;
				}
			}
		}
		return matched ? i : -1;
	}

	private static int jumpBackQuateIf(String sql, int i) {
		boolean matched = false;
		while (true) {
			i++;
			if (i >= sql.length()) {
				break;
			}
			String c1 = sql.substring(i, i + 1);
			String c2 = null;
			if (i + 1 <= sql.length() - 1) {
				c2 = sql.substring(i + 1, i + 2);
			}
			if (c1.equals(SQLKeyword.BACK_QUATE.toString())) {
				if (c2 != null) {
					if (c2.equals(SQLKeyword.BACK_QUATE.toString())) {
						i++;
					} else {
						matched = true;
						break;
					}
				} else {
					matched = true;
					break;
				}
			}
		}
		return matched ? i : -1;
	}

	private static int jumpDoubleQuateIf(String sql, int i) {
		boolean matched = false;
		while (true) {
			i++;
			if (i >= sql.length()) {
				break;
			}
			String c1 = sql.substring(i, i + 1);
			String c2 = null;
			if (i + 1 <= sql.length() - 1) {
				c2 = sql.substring(i + 1, i + 2);
			}
			if (c1.equals(SQLKeyword.DOUBLE_QUATE.toString())) {
				if (c2 != null) {
					if (c2.equals(SQLKeyword.DOUBLE_QUATE.toString())) {
						i++;
					} else {
						matched = true;
						break;
					}
				} else {
					matched = true;
					break;
				}
			}
		}
		return matched ? i : -1;
	}

	private static int jumpMulityLineRemarkIf(String sql, int i) {
		boolean matched = false;
		while (true) {
			i++;
			if (i >= sql.length() - 1) {
				break;
			}
			String c1 = sql.substring(i, i + 2);
			if (c1.equals(SQLKeyword.RIGHT_REMARK.toString())) {
				matched = true;
				break;
			}
		}
		return matched ? i : -1;
	}

	private static int jumpSingleLineRemarkIf(String sql, int i) {
		boolean matched = false;
		while (true) {
			i++;
			if (i >= sql.length()) {
				break;
			}
			String c1 = sql.substring(i, i + 1);
			if (c1.equals(SQLKeyword.LN.toString()) || c1.equals(SQLKeyword.REVERT.toString())) {
				matched = true;
				break;
			}
		}

		if (i == sql.length()) {
			matched = true;
		}

		return matched ? i : -1;
	}

	public String getOriginalSQL() {
		return originalSQL;
	}


//	private Map<String,Object> resultCache=new HashMap<String, Object>();
//	private void cleanResultCache() {
//		resultCache.clear();
//	}

	@Override
	public String getSQL(SQLDialect dialect) {

//		String key="getSQL:"+dialect.name();
//		Object citm=resultCache.get(key);
//		if(citm != null) {
//			return (String) citm ;
//		}

		initIf();
		if(isBr) {
			return "\n";
		}
		if (this.isEmpty() && (this.appends==null || this.appends.isEmpty())) {
			return "";
		}
		SQLStringBuilder sql = new SQLStringBuilder();
		String part = null;
		Object param = null;
		SQL se = null;
		for (int i = 0; i < splitParts.size() - 1; i++) {
			part = splitParts.get(i);
			param = this.paramValues.get(i);
			if (param instanceof SQL) {
				se = (SQL) param;
				se.setIgnorColon(ignorColon);
				sql.append(part,se.getSQL());
			} else {
				String val = Utils.castValue(param,dialect);
				sql.append(part,val);
			}
		}

		if(!this.splitParts.isEmpty()) {
			sql.append(this.splitParts.get(splitParts.size() - 1));
		}

		//实现append部分
		if(appends!=null) {
			for (SQL s : appends) {
				sql.append(s.getSQL());
			}
		}
//		String finalSql=sql.toString();
//		resultCache.put(key, finalSql);
//		return finalSql;
		return sql.toString();
	}

	@Override
	public Object[] getListParameters() {

//		String key="getListParameters";
//		Object citm=resultCache.get(key);
//		if(citm != null) {
//			return (Object[]) citm ;
//		}

		initIf();
		if (isEmpty()) {
			return new Object[] {};
		} else {
			ArrayList<Object> list = new ArrayList<Object>();
			for (Object o : this.paramValues) {
				if (o == null && replaceNull) {
					continue;
				}

				if (o instanceof SQL) {
					SQL se = (SQL) o;
					se.setIgnorColon(ignorColon);
					list.addAll(Utils.toList(se.getListParameters()));
				} else {
					list.add(o);
				}
			}

			if(appends!=null) {
				for (SQL s : appends) {
					list.addAll(Arrays.asList(s.getListParameters()));
				}
			}

			//处理类型
			Object[] array=list.toArray();
			for (int i = 0; i < array.length; i++) {
				array[i]=Utils.parseParmeterValue(array[i]);
			}
//			resultCache.put(key, array);
			return array;
		}
	}

	@Override
	public String getListParameterSQL() {

//		String key="getListParameterSQL:"+this.getSQLDialect().name();
//		Object citm=resultCache.get(key);
//		if(citm != null) {
//			return (String) citm ;
//		}

		initIf();
		if(isBr) {
			return "\n";
		}
		if (this.isEmpty() && (this.appends==null || this.appends.isEmpty())) {
			return "";
		}
		SQLStringBuilder sql = new SQLStringBuilder();
		for (int i = 0; i < splitParts.size() - 1; i++) {
			String part = splitParts.get(i);
			Object param = null;
			try {
				param = this.paramValues.get(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (param != null) {
				if (param instanceof SQL) {
					SQL se = (SQL) param;
					se.setIgnorColon(ignorColon);
					sql.append( part,se.getListParameterSQL());
				} else {
					sql.append(part).append(SQLKeyword.QUESTION);
				}
			} else {
				if(replaceNull) {
					sql.append(part).append(SQLKeyword.NULL);
				} else {
					sql.append(part).append(SQLKeyword.QUESTION);
				}
			}
		}
		sql.append(this.splitParts.get(splitParts.size() - 1));

		//实现append部分
		if(appends!=null) {
			for (SQL s : appends) {
				sql.append(s.getListParameterSQL());
			}
		}
//		String finalSql=sql.toString();
//		resultCache.put(key, finalSql);
//		return finalSql;
		return sql.toString();
	}

	@Override
	public String getNamedParameterSQL() {

//		String key="getNameParameterSQL:"+this.getSQLDialect().name();
//		Object citm=resultCache.get(key);
//		if(citm != null) {
//			return (String) citm ;
//		}

		initIf();
		if(isBr) {
			return "\n";
		}
		if (this.isEmpty() && (this.appends==null || this.appends.isEmpty())) {
			return "";
		}
		this.beginParamNameSQL();
		SQLStringBuilder sql = new SQLStringBuilder();
		for (int i = 0; i < splitParts.size() - 1; i++) {
			String part = splitParts.get(i);
			Object param = this.paramValues.get(i);
			if (param != null && replaceNull) {
				if (param instanceof Expr) {
					Expr se = (Expr) param;
					se.setIgnorColon(ignorColon);
					sql.append(part,se.getNamedParameterSQL());
				} else {
					sql.append(part,this.getNextParamName(true));
				}
			} else {
				if(replaceNull)
				{
					sql.append(part).append(SQLKeyword.NULL);
				}
				else
				{
					sql.append(part,this.getNextParamName(true));
				}
			}
		}
		sql.append(this.splitParts.get(splitParts.size() - 1));

		//实现append部分
		if(appends!=null) {
			for (SQL s : appends) {
				sql.append(s.getNamedParameterSQL());
			}
		}

		this.endParamNameSQL();

//		String finalSql=sql.toString();
//		resultCache.put(key, finalSql);
//		return finalSql;
		return sql.toString();
	}

	@Override
	public Map<String, Object> getNamedParameters() {

//		String key="getNamedParameters";
//		Object citm=resultCache.get(key);
//		if(citm != null) {
//			return (Map<String, Object>) citm ;
//		}

		initIf();
		HashMap<String, Object> ps = new HashMap<>(5);
		if (isEmpty()) {
			return ps;
		}
		this.beginParamNameSQL();
		for (Object o : this.paramValues) {
			if (o == null && replaceNull) {
				continue;
			}
			if (o instanceof SQL) {
				((SQL) o).setIgnorColon(ignorColon);
				Map<String, Object> map = ((SQL) o).getNamedParameters();
				ps.putAll(map);
			} else {
				ps.put(this.getNextParamName(false), o);
			}
		}

		if(appends!=null) {
			for (SQL s : appends) {
				ps.putAll(s.getNamedParameters());
			}
		}

		this.endParamNameSQL();

		//处理类型
		for (Map.Entry<String, Object> e : ps.entrySet()) {
			ps.put(e.getKey(), Utils.parseParmeterValue(e.getValue()));
		}
//		resultCache.put(key, ps);
		return ps;
	}

	/**
	 * 判断是否为空
	 * 语句为空 , 判定为 empty
	 * @return 是否空
	 * */
	@Override
	public boolean isEmpty() {
		boolean empty= this.originalSQL == null ||  StringUtil.isBlank(this.originalSQL);
		if(!empty) return false;
		if(this.appends!=null) {
			for (SQL sql : this.appends) {
				if(!sql.isEmpty()); return false;
			}
		}
		return true;
	}

	/**
	 * 是否所有参数均为空(语句)
	 * */
	@Override
	public boolean isAllParamsEmpty() {
		initIf();

		for (int i = 0; i < this.paramValues.size(); i++) {
			Object o = this.paramValues.get(i);

			if(o==null) continue;

			if (o instanceof SQL) {
				SQL se = (SQL) o;
				se.setIgnorColon(ignorColon);
				if (!se.isAllParamsEmpty()) {
					return false;
				}
			}
//			else if(o instanceof String) {
//				String str = (String) o;
//				if(isCE)
//				{
//
//				}
//				else
//				{
//
//				}
//
//				if (isCE && (o instanceof String)) {
//					String sql = splitParts.get(i).trim().toUpperCase();
//					// 此处可以做更为复杂的判断
//					if (sql.indexOf("LIKE") > -1)
//					{
//						str = str.replace("%", "");
//						str = str.replace("_", "");
//					}
//					if (!str.equals("") && !str.equals("null")) {
//						return false;
//					}
//				} else {
//					return false;
//				}
//
//			}
			else //其它非空类型
			{
				return false;
			}
		}

		return true;
	}

	public Expr append(String se, Object... ps) {
//		cleanResultCache();
		return append(Expr.create(se, ps));
	}

	private ArrayList<SQL> appends=null;

	public Expr append(SQL... ses) {
//		cleanResultCache();
		if(appends==null) {
			appends=new ArrayList<>();
		}
		for (SQL se : ses) {
			if(se==this) {
				throw new IllegalArgumentException("do not allow append self");
			}
			if(se==null) {
				continue;
			}
//			if(se.parent()!=null && se.parent()!=this) {
//				throw new IllegalArgumentException(se.getSQL()+" has parent , can not add to another parent");
//			}
			appends.add(se);
			se.setParent(this);
		}
		return this;

	}

	public Expr appendIf(String se, Object... ps) {
		return appendIf(Expr.create(se, ps));
	}

	public Expr appendIf(SQL... ses) {
//		cleanResultCache();
		if(appends==null) {
			appends=new ArrayList<>();
		}
		for (SQL se : ses) {
			if (se==null || se.isEmpty()) {
				continue;
			}
			if(se==this) {
				throw new IllegalArgumentException("do not allow append self");
			}
			appends.add(se);
			se.setParent(this);
		}
		return this;

	}

	public static int indexOf(String sql,String kw,boolean includeBracket)
	{
		return indexOf(sql,kw,includeBracket,0);
	}

	public static int indexOf(String sql,String kw,boolean includeBracket,int formIndex)
	{
		sql=" "+sql+" ";
		char[] chars=sql.toCharArray();
		int i=formIndex-1;
		while (true) {

			i++;
			if (i >= chars.length) {
				break;
			}

			//char c = chars[i];
			int z = jumpIf(sql, i,includeBracket);
			if (z == -1) {
				//err("语句" + sql + "，在第" + i + "个字符处,没有找到与之对应的结尾字符,可能存在语法错误!");
				return -1;
			} else {
				if (z != i) {
					i = z;
					continue;
				}
			}

			if(i+kw.length()<sql.length())
			{
				String str=sql.substring(i,i+kw.length());
				if(str.equals(kw.toString()))
				{
					return i-1;
				}
			}
		}
		return -1;
	}


	private static final String[]  INVALID_CHARS = {",","%","<","=",">",".","'","\r","\n","\t"," ","　"};

	/**
	 * 简单检查是否是一个有效的数据库对象名称
	 * @param subsql 语句
	 * @return 逻辑值
	 * */
	public static boolean validateSQLName(String subsql)
	{
		if(subsql==null) {
			return false;
		}
		if(subsql.trim().length()==0) {
			return false;
		}
		for (String c : INVALID_CHARS) {
			if(subsql.indexOf(c)!=-1) {
				return false;
			}
		}
		return true;

	}

	private static class AnalyseRsult implements Serializable
	{
		private static final long serialVersionUID = 5982222348904511933L;
		private ArrayList<String> splitParts=null;
		public ArrayList<String> getSplitParts() {
			return splitParts;
		}

		public ArrayList<Object> getPsIndexes() {
			return psIndexes;
		}

		private ArrayList<Object> psIndexes=null;

		public AnalyseRsult(ArrayList<String> parts,ArrayList<Object> indexes)
		{
			this.splitParts=parts;
			this.psIndexes=indexes;
		}

		public void validate()
		{
			if(this.splitParts.size()-1!=this.psIndexes.size())
			{
				String full="";
				for (int j = 0; j < splitParts.size()-1; j++) {
					full+=splitParts.get(j)+" :P"+j+" ";
				}
				full+=splitParts.get(splitParts.size()-1);

				throw new SQLValidateException(full+"语句解析错误,要求参数个数"+(this.splitParts.size()-1)+",实际"+this.psIndexes.size());
			}
		}

	}

	static String getNearBy(String origSQL,String sql,int i,boolean hasPrev)
	{
		int a=i-16;
		int b=i+16;
		if(a<=0) {
			a=0;
		}
		if(b>=sql.length()) {
			b=sql.length()-1;
		}
		String part1=sql.substring(a, b);
		if(hasPrev)
		{
			part1="... "+part1;
		}
		if(b<sql.length()-1)
		{
			part1=part1+" ...";
		}
		throw new SQLValidateException("语句:" + origSQL + " , 在 "+part1+" 附近存在语法错误!");
	}

	/**
	 * 从多行语句生成 SE
	 * */
	public static Expr fromLines(String[] lines,Object... params)
	{
		return new Expr(SQL.joinSQLs(lines),params);
	}

	/**
	 * 从多行语句生成 Expr
	 * */
	public static Expr fromLines(List<String> lines,Object... params)
	{
		return new Expr(SQL.joinSQLs(lines),params);
	}

	private transient ExprDAO dao = null;

	@Override
	public ExprDAO getDAO() {
		return dao;
	}

	@Override
	public Expr setDAO(ExprDAO dao) {
		this.dao = dao;
		return this;
	}



	public Expr getFlattenExpr() {
		return new Expr(this.getListParameterSQL(),this.getListParameterSQL());
	}

	public Expr clone() {
		Expr expr=new Expr();

		expr.paramIndex=this.paramIndex;

		for (Object paramValue : this.paramValues) {
			if(paramValue instanceof SQL) {
				expr.paramValues.add(((SQL)paramValue).clone());
			} else {
				expr.paramValues.add(paramValue);
			}
		}

		for (Object index : this.paramValueIndexes) {
			if(index instanceof SQL) {
				expr.paramValueIndexes.add(((SQL)index).clone());
			} else {
				expr.paramValueIndexes.add(index);
			}
		}

		expr.originalSQL=this.originalSQL;

		expr.splitParts.addAll(this.splitParts);
		expr.lastSqlPart=this.lastSqlPart;
 		expr.isBr=this.isBr;
		expr.inited=false;

		if(this.originalMap!=null) {
			expr.originalMap=new HashMap<>();
			for (Map.Entry<String, Object> e : this.originalMap.entrySet()) {
				if(e.getValue() instanceof SQL) {
					expr.originalMap.put(e.getKey(),((SQL)e.getValue()).clone());
				} else {
					expr.originalMap.put(e.getKey(),e.getValue());
				}
			}
		}

		if(this.originalPs!=null) {
			expr.originalPs=new Object[this.originalPs.length];
			for (int i = 0; i < this.originalPs.length; i++) {
				if(this.originalPs[i] instanceof SQL) {
					expr.originalPs[i]=((SQL)this.originalPs[i]).clone();
				} else {
					expr.originalPs[i]=this.originalPs[i];
				}
			}
		}

		if(this.appends!=null) {
			expr.appends=new ArrayList<>();
			for (SQL append : this.appends) {
				expr.appends.add(append.clone());
			}
		}

		expr.inited=this.inited;

		return expr;
	}


}


