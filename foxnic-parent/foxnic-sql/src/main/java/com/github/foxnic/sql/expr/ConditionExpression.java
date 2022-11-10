package com.github.foxnic.sql.expr;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.meta.DBField;

import java.util.*;


class ConditionExpression<E> extends SubSQL implements WhereWrapper
{

	private static final long serialVersionUID = 3640160616186164559L;

	protected ArrayList<SQL> ses=new ArrayList<SQL>();
	protected ArrayList<SQLKeyword> logics=new ArrayList<SQLKeyword>();

	private SQLKeyword startWith=SQLKeyword.AND;

	protected SQLKeyword getKeyword()
	{
		return startWith;
	}

	@SuppressWarnings("unchecked")
	public E startWithAnd()
	{
		this.startWith= SQLKeyword.AND;
		return (E)this;
	}

	@SuppressWarnings("unchecked")
	public E startWithOr()
	{
		this.startWith= SQLKeyword.OR;
		return (E)this;
	}

	@SuppressWarnings("unchecked")
	public E startWithSpace()
	{
		this.startWith= SQLKeyword.SPACER;
		return (E)this;
	}

	@SuppressWarnings("unchecked")
	public E startWithWhere()
	{
		this.startWith= SQLKeyword.WHERE;
		return (E)this;
	}




	public ConditionExpression()
	{}

	public ConditionExpression(Expr se)
	{
		and(se);
	}

	public ConditionExpression(String se,Object... ps)
	{
		and(Expr.create(se,ps));
	}


	@SuppressWarnings("unchecked")
	public E or(Expr se)
	{
		ses.add(se);
		se.setParent(this);
		logics.add(SQLKeyword.OR);
		return (E)this;
	}

	@SuppressWarnings("unchecked")
	public E or(In in)
	{
		ses.add(in);
		in.setParent(this);
		logics.add(SQLKeyword.OR);
		return (E)this;
	}

	@SuppressWarnings("unchecked")
	public E orIf(In in)
	{
		if(in==null || in.isEmpty()) return (E)this;
		return and(in);
	}

	@SuppressWarnings("unchecked")
	public E orIf(Expr se)
	{
		if(se.isEmpty()) {
			return (E)this;
		}
		if(se.isAllParamsEmpty()) {
			return (E)this;
		}

		if(isAllValueIgnored(se.getListParameters(),this.ignoredValues)) {
			return (E)this;
		}

		return or(se);
	}

	@SuppressWarnings("unchecked")
	public E and(Expr se)
	{
		ses.add(se);
		se.setParent(this);
		logics.add(SQLKeyword.AND);
		return (E)this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public E and(ConditionExpression ce) {
		return and(ce,true);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public E and(ConditionExpression ce,boolean bracket) {
		if(ce.isEmpty()) {
			return (E)this;
		}
		ce.startWithSpace();
		String sql=ce.getListParameterSQL();
		if(sql.toUpperCase().trim().startsWith(SQLKeyword.WHERE.name()))
		{
			sql=sql.trim();
			sql=sql.substring(6, sql.length());
		}
		ses.add(new Expr((bracket?"(":"")+sql+(bracket?")":""),ce.getListParameters()));
		ce.setParent(this);
		logics.add(SQLKeyword.AND);
		return (E)this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public E or(ConditionExpression ce) {
		return or(ce,true);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public E or(ConditionExpression ce,boolean bracket)
	{
		if(ce.isEmpty()) {
			return (E)this;
		}
		ce.startWithSpace();
		String sql=ce.getListParameterSQL();
		if(sql.toUpperCase().trim().startsWith(SQLKeyword.WHERE.name()))
		{
			sql=sql.trim();
			sql=sql.substring(6, sql.length());
		}
		ses.add(new Expr((bracket?"(":"")+sql+(bracket?")":""),ce.getListParameters()));
		ce.setParent(this);
		logics.add(SQLKeyword.OR);
		return (E)this;
	}

	@SuppressWarnings("unchecked")
	public E and(In in) {
		Expr expr=in.toExpr();
		ses.add(expr);
		expr.setParent(this);
		logics.add(SQLKeyword.AND);
		return (E)this;
	}

	@SuppressWarnings("unchecked")
	public E andIf(In in,Object... ignoreValue)
	{
		if(in.isEmpty()) {
			return (E)this;
		}

		if(in.isAllParamsEmpty()) {
			return (E)this;
		}

		if(isValuesIgnored(in.getListParameters(),ignoreValue)) {
			return (E)this;
		}

		return and(in);
	}

	@SuppressWarnings("unchecked")
	public E andIf(Expr se,Object... ignoreValue)
	{
		if(se.isEmpty()) {
			return (E)this;
		}

		if(se.isAllParamsEmpty()){
			return (E)this;
		}

		if(isValuesIgnored(se.getListParameters(),ignoreValue)) {
			return (E)this;
		}

		return and(se);
	}


	public E and(String se,Object...ps)
	{
		return and(Expr.create(se,ps));
	}

	/**
	 *
	 * */
	@SuppressWarnings("unchecked")
	public E andIf(String se,Object...ps)
	{
		if(isAllValueIgnored(ps,this.ignoredValues)) {
			return (E)this;
		}
		Expr seN=Expr.create(se,ps);
		if(seN.isAllParamsEmpty()) {
			return (E)this;
		}
		return and(seN);
	}

	public E or(String se,Object...ps)
	{
		return or(Expr.create(se,ps));
	}

	@SuppressWarnings("unchecked")
	public E orIf(String se,Object...ps)
	{
		if(isAllValueIgnored(ps,this.ignoredValues)) {
			return (E)this;
		}
		Expr seN=Expr.create(se,ps);
		if(seN.isAllParamsEmpty()) {
			return (E)this;
		}
		return or(Expr.create(se,ps));
	}


	@Override
	public String getSQL(SQLDialect dialect) {
		if(this.isEmpty()) {
			return "";
		}
		SQLStringBuilder subs = new SQLStringBuilder();
		subs.append(this.getKeyword());
		for(int i=0;i<ses.size();i++) {
			ses.get(i).setIgnorColon(ignorColon);
			if(i==0) {
				subs.append(ses.get(i).getSQL(dialect));
			} else {
				subs.append(logics.get(i).toString(),ses.get(i).getSQL(dialect));
			}
		}
		return  subs.toString();
	}


	@Override
	public String getListParameterSQL() {
		if(this.isEmpty()) {
			return "";
		}
		SQLStringBuilder sql = new SQLStringBuilder();
		sql.append(this.getKeyword());
		for(int i=0;i<ses.size();i++) {
			ses.get(i).setIgnorColon(ignorColon);
			if(i==0) {
				sql.append(ses.get(i).getListParameterSQL());
			} else {
				sql.append(logics.get(i).toString(),ses.get(i).getListParameterSQL());
			}
		}
		return sql.toString();
	}


	@Override
	public Object[] getListParameters() {
		if(this.isEmpty()) {
			return new Object[]{};
		}
		ArrayList<Object> ps=new ArrayList<Object>();
		for(int i=0;i<ses.size();i++)
		{
			ses.get(i).setIgnorColon(ignorColon);
			ps.addAll(Utils.toList(ses.get(i).getListParameters()));
		}
		return ps.toArray(new Object[ps.size()]);
	}


	@Override
	public String getNamedParameterSQL() {
		if(this.isEmpty()) {
			return "";
		}
		SQLStringBuilder sql = new SQLStringBuilder();
		sql.append(this.getKeyword());
		this.beginParamNameSQL();

		for(int i=0;i<ses.size();i++) {
			ses.get(i).setIgnorColon(ignorColon);
			if(i==0) {
				sql.append(ses.get(i).getNamedParameterSQL());
			} else {
				sql.append(logics.get(i).toString(),ses.get(i).getNamedParameterSQL());
			}
		}
		this.endParamNameSQL();
		return sql.toString();
	}


	@Override
	public Map<String, Object> getNamedParameters() {
		HashMap<String, Object> ps=new HashMap<String, Object>();
		if(this.isEmpty()) {
			return ps;
		}
		this.beginParamNameSQL();
		for(int i=0;i<ses.size();i++)
		{
			ses.get(i).setIgnorColon(ignorColon);
			ps.putAll(ses.get(i).getNamedParameters());
		}
		this.endParamNameSQL();
		return ps;
	}

	/**
	 * 判断是否为空
	 * 表达式数量为0,判定为 empty
	 * @return 是否空
	 * */
	@Override
	public boolean isEmpty() {
		return ses.size()==0;
	}

	/**
	 * 默认为CE模式 CE=true
	 * */




	@Override
	public boolean isAllParamsEmpty() {

		for(int i=0;i<ses.size();i++)
		{
			ses.get(i).setIgnorColon(ignorColon);
			if(!ses.get(i).isAllParamsEmpty()) {
				return false;
			}
		}

		return true;
	}

	/**
	 *  等于 =  equal
	 *  @param field 字段
	 *  @param value 值
	 *  @param ignoreValue 忽略的值，如果 value 在 ignoreValue 中，则忽略该表达式
	 *  @return CE,对象自身
	 * */
	public E andEquals(DBField field,Object value,Object... ignoreValue)
	{
		return this.andEquals(field.name(), value, ignoreValue);
	}

	/**
	 *  等于 =  equal
	 *  @param field 字段
	 *  @param value 值
	 *  @param ignoreValue 忽略的值，如果 value 在 ignoreValue 中，则忽略该表达式
	 *  @return CE,对象自身
	 * */
	public E andEquals(String field,Object value,Object... ignoreValue)
	{
		Utils.validateDBIdentity(field);

		if(value==null) return (E)this;

		if(isValueIgnored(value, ignoreValue)) return (E)this;

		return this.and(field+" = ?",value);
	}

	private Object[] ignoredValues=null;

	private static boolean eq(Object v1,Object v2)
	{
		if(v1==null && v2==null) return true;
		else if(v1!=null && v2==null) return false;
		else if(v1==null && v2!=null) return false;
		else {
			return v1.equals(v2);
		}
	}

	/**
	 * 设置当前表达式将要忽略的值
	 * */
	public E ignore(Object... ignoredValues)
	{
		if(ignoredValues==null) ignoredValues=new Object[]{null};
		if(ignoredValues.length==0) return (E)this;
		this.ignoredValues=ignoredValues;
		return (E)this;
	}

	private boolean isAllValueIgnored(Object[] values,Object[] igValues ) {

		if(igValues==null) return false;
		for (Object v : values) {
			boolean ig=false;
			for (Object iv : igValues) {
				if(eq(iv,v)) {
					ig=true;
					break;
				}
			}
			if(ig==false) {
				return false;
			}
		}
		return true;
	}


	private boolean isValueIgnored(Object value, Object... ignoreValue) {

		if(this.ignoredValues!=null) {
			for (Object object : this.ignoredValues) {
				if(eq(value,object)) return true;
			}
		}

		for (Object object : ignoreValue) {
			if(eq(value,object)) return true;
		}
		return false;
	}

	private boolean isValuesIgnored(Object[] values, Object... ignoreValue) {

		boolean ig=isAllValueIgnored(values,this.ignoredValues);
		if(ig) return true;
		ig=isAllValueIgnored(values, ignoreValue);
		return ig;
	}

	/**
	 *  判断null , is null
	 *  @param field 字段
	 *  @return CE,对象自身
	 * */
	public E andIsNull(String field)
	{
		Utils.validateDBIdentity(field);
		return this.and(field+" is null");
	}

	/**
	 * 不等于  !=  not equal
	 *  @param field 字段
	 *  @param value 值
	 *  @param ignoreValue 忽略的值，如果 value 在 ignoreValue 中，则忽略该表达式
	 *  @return CE,对象自身
	 * */
	public E andNotEquals(String field,Object value,Object... ignoreValue)
	{
		Utils.validateDBIdentity(field);
		if(value==null) return (E) this;

		if(isValueIgnored(value, ignoreValue)) return (E)this;

		return this.and(field+" != ?",value);
	}

	/**
	 *  判断非null ， is not null
	 *  @param field 字段
	 *  @return CE,对象自身
	 * */
	public E andIsNotNull(String field)
	{
		Utils.validateDBIdentity(field);
		return this.and(field+" is not null");
	}


	/**
	 * 大于  greater than
	 *  @param field 字段
	 *  @param value 值
	 *  @return CE,对象自身
	 * */
	@SuppressWarnings("unchecked")
	public E andGreaterThan(String field,Object value)
	{
		Utils.validateDBIdentity(field);
		if(value==null) {
			return (E)this;
		}
		return this.and(field+" > ?",value);
	}





	/**
	 *  小于   less than
	 *  @param field 字段
	 *  @param value 值
	 *  @return CE,对象自身
	 * */
	@SuppressWarnings("unchecked")
	public E andLessThan(String field,Object value)
	{
		Utils.validateDBIdentity(field);
		if(value==null) {
			return (E)this;
		}
		return this.and(field+" < ?",value);
	}
	/**
	 *  大于等于   greater than or equal to
	 *  @param field 字段
	 *  @param value 值
	 *  @return CE,对象自身
	 * */
	@SuppressWarnings("unchecked")
	public E andGreaterEqualThan(String field,Object value)
	{
		Utils.validateDBIdentity(field);
		if(value==null) {
			return (E)this;
		}
		return this.and(field+" >=  ?",value);
	}

	/**
	 *  小于等于   less than or equal to
	 *  @param field 字段
	 *  @param value 值
	 *  @return CE,对象自身
	 * */
	@SuppressWarnings("unchecked")
	public E andLessEqualThan(String field,Object value)
	{
		Utils.validateDBIdentity(field);
		if(value==null) {
			return (E)this;
		}
		return this.and(field+" <=  ?",value);
	}

	/**
	 *    field like  %value%
	 *  @param field 字段
	 *  @param value 值
	 *  @return CE,对象自身
	 * */
	@SuppressWarnings("unchecked")
	public E andLike(DBField field,String value,String... ignoreValue) {
		return andLike(field.name(), value, ignoreValue);
	}

	/**
	 *    field like  %value%
	 *  @param field 字段
	 *  @param value 值
	 *  @return CE,对象自身
	 * */
	@SuppressWarnings("unchecked")
	public E andLike(String field,String value,String... ignoreValue)
	{
		Utils.validateDBIdentity(field);
		if(value==null) {
			return (E)this;
		}

		if(this.isValueIgnored(value, ignoreValue)) {
			return (E)this;
		}

		String s=value;
		s = StringUtil.removeFirst(s,"%");
		s = StringUtil.removeLast(s,"%");

		if(StringUtil.isEmpty(s)) {
			return (E)this;
		}

		s="%"+s+"%";

		this.and(field+" like  ?",s);
		return (E)this;
	}


	/**
	 *    field like  %value%
	 *  @param field 字段
	 *  @param value 值
	 *  @return CE,对象自身
	 * */
	@SuppressWarnings("unchecked")
	public E orLike(String field,String value,String... ignoreValue)
	{
		Utils.validateDBIdentity(field);
		if(value==null) {
			return (E)this;
		}

		if(this.isValueIgnored(value, ignoreValue)) {
			return (E)this;
		}

		String s=value;
		s = StringUtil.removeFirst(s,"%");
		s = StringUtil.removeLast(s,"%");

		if(StringUtil.isEmpty(s)) {
			return (E)this;
		}

		s="%"+s+"%";

		this.or(field+" like  ?",s);
		return (E)this;
	}

	/**
	 *    field like  value%
	 *  @param field 字段
	 *  @param value 值
	 *  @return CE,对象自身
	 * */
	@SuppressWarnings("unchecked")
	public E andLeftLike(String field,String value,String... ignoreValue)
	{
		Utils.validateDBIdentity(field);
		if(value==null) {
			return (E)this;
		}

		if(this.isValueIgnored(value, ignoreValue)) {
			return (E)this;
		}

		String s=value;
		s = StringUtil.removeFirst(s,"%");
		s = StringUtil.removeLast(s,"%");
		s=s+"%";

		if(StringUtil.isEmpty(s))
		{
			return (E)this;
		}

		this.and(field+" like  ?",s);
		return (E)this;
	}

	/**
	 *    field like  %value
	 *  @param field 字段
	 *  @param value 值
	 *  @return CE,对象自身
	 * */
	@SuppressWarnings("unchecked")
	public E andRightLike(String field,String value,String... ignoreValue)
	{
		Utils.validateDBIdentity(field);
		if(value==null) {
			return (E)this;
		}

		if(this.isValueIgnored(value, ignoreValue)) {
			return (E)this;
		}

		String s=value;
		s = StringUtil.removeFirst(s,"%");
		s = StringUtil.removeLast(s,"%");
		if(StringUtil.isEmpty(s))
		{
			return (E)this;
		}
		s="%"+s;
		this.and(field+" like  ?",s);
		return (E)this;
	}

	/**
	 *    以 value 开始
	 *  @param field 字段
	 *  @param value 值
	 *  @return CE,对象自身
	 * */
	@SuppressWarnings("unchecked")
	public E andStarts(String field,String value,String... ignoreValue)
	{
		Utils.validateDBIdentity(field);
		if(value==null) {
			return (E)this;
		}

		if(StringUtil.isBlank(value)){
			return (E)this;
		}

		if(this.isValueIgnored(value, ignoreValue)) {
			return (E)this;
		}

		this.and("instr("+field+",?) = 1",value);
		return (E)this;
	}

	/**
	 *    以 value 结尾
	 *  @param field 字段
	 *  @param value 值
	 *  @return CE,对象自身
	 * */
	@SuppressWarnings("unchecked")
	public E andEnds(String field,String value,String... ignoreValue)
	{
		Utils.validateDBIdentity(field);
		if(value==null) {
			return (E)this;
		}
		if(StringUtil.isBlank(value)){
			return (E)this;
		}

		if(this.isValueIgnored(value, ignoreValue)) {
			return (E)this;
		}

		this.and("instr("+field+",?) = length("+field+")-length(?)+1",value,value);
		return (E)this;
	}

	@SuppressWarnings("unchecked")
	public E andContains(String field,String value,String... ignoreValue)
	{
		Utils.validateDBIdentity(field);
		if(value==null) {
			return (E)this;
		}
		if(StringUtil.isBlank(value)){
			return (E)this;
		}

		if(this.isValueIgnored(value, ignoreValue)) {
			return (E)this;
		}
		this.and("instr("+field+",?) > 0",value);
		return (E)this;
	}


	@SuppressWarnings("unchecked")
	public E andNotContains(String field,String value,String... ignoreValue)
	{
		Utils.validateDBIdentity(field);
		if(value==null) {
			return (E)this;
		}
		if(StringUtil.isBlank(value)){
			return (E)this;
		}

		if(this.isValueIgnored(value, ignoreValue)) {
			return (E)this;
		}
		this.and("instr("+field+",?) <= 0",value);
		return (E)this;
	}

	/**
	 *    以 value 结尾
	 *  @param field 字段
	 *  @param minValue 最小值
	 *  @param maxValue 最大值
	 *  @return CE,对象自身
	 * */
	@SuppressWarnings("unchecked")
	public E andBetween(String field,Object minValue,Object maxValue)
	{
		Utils.validateDBIdentity(field);
		if(minValue==null && maxValue==null)
		{
			return (E)this;
		}
		else if(minValue!=null && maxValue==null)
		{
			return this.andGreaterEqualThan(field, minValue);
		}
		else if(minValue==null && maxValue!=null)
		{
			return this.andLessEqualThan(field, maxValue);
		}
		else if(minValue!=null && maxValue!=null)
		{
			this.and(field +" between ? and ?",minValue,maxValue);
		}
		return (E)this;
	}

	public E andIn(DBField field,List<Object> items,Object... ignoreValue) {
		return andIn(field.name(),items,ignoreValue);
	}

	/**
	 *   not in
	 *  @param field 字段
	 *  @param items 值清单
	 *  @return CE,对象自身
	 * */
	public E andIn(String field,Set items)
	{
		return this.andIn(field,items.toArray(new Object[0]));
	}

	/**
	 *    in
	 *  @param field 字段
	 *  @param items 值清单
	 *  @return CE,对象自身
	 * */
	public E andIn(String field,List<Object> items,Object... ignoreValue)
	{
		Utils.validateDBIdentity(field);
		List<Object> itemsAfter=items;
		if(ignoreValue.length>0) {
			itemsAfter=new ArrayList<>();
			for (Object object : items) {
				if(!this.isValueIgnored(object, ignoreValue)) {
					itemsAfter.add(object);
				}
			}
		}
		In in=new In(field, itemsAfter);
		return this.and(in);
	}


	/**
	 *    in
	 *  @param field 字段
	 *  @param items 值清单
	 *  @return CE,对象自身
	 * */
	public E andInIf(String field,Object... items)
	{
		if(items==null || items.length==0) return (E)this;
		return andIn(field,items);
	}


	/**
	 *    in
	 *  @param field 字段
	 *  @param items 值清单
	 *  @return CE,对象自身
	 * */
	public E andInIf(String field,List items)
	{
		if(items==null || items.size()==0) return (E)this;
		return andIn(field,items);
	}

	/**
	 *    in
	 *  @param field 字段
	 *  @param items 值清单
	 *  @return CE,对象自身
	 * */
	public E andIn(String field,Object... items)
	{
		Utils.validateDBIdentity(field);
		In in=new In(field, items);
		return this.and(in);
	}


	/**
	 *    in
	 *  @param field 字段
	 *  @param items 值清单
	 *  @return CE,对象自身
	 * */
	public E andIn(String field,List items)
	{
		Utils.validateDBIdentity(field);
		In in=new In(field, items);
		return this.and(in);
	}

	/**
	 *    in
	 *  @param field 字段
	 *  @param items 值清单
	 *  @return CE,对象自身
	 * */
	public E andIn(String field,Object[] items,Object... ignoreValue)
	{
		return this.andIn(field,Arrays.asList(items),ignoreValue);
	}

	/**
	 *   not in
	 *  @param field 字段
	 *  @param items 值清单
	 *  @return CE,对象自身
	 * */
	public E andNotIn(String field,List<Object> items,Object... ignoreValue)
	{
		Utils.validateDBIdentity(field);
		List<Object> itemsAfter=items;
		if(ignoreValue.length>0) {
			itemsAfter=new ArrayList<>();
			for (Object object : items) {
				if(!this.isValueIgnored(object, ignoreValue)) {
					itemsAfter.add(object);
				}
			}
		}
		In in=new In(field, itemsAfter);
		in.not();
		return this.and(in);
	}

	/**
	 *   not in
	 *  @param field 字段
	 *  @param items 值清单
	 *  @return CE,对象自身
	 * */
	public E andNotIn(String field,Object... items)
	{
		Utils.validateDBIdentity(field);
		In in=new In(field, items);
		in.not();
		return this.and(in);
	}

	/**
	 *   not in
	 *  @param field 字段
	 *  @param items 值清单
	 *  @return CE,对象自身
	 * */
	public E andNotIn(String field,List items)
	{
		return this.andNotIn(field,items.toArray(new Object[0]));
	}

	/**
	 *   not in
	 *  @param field 字段
	 *  @param items 值清单
	 *  @return CE,对象自身
	 * */
	public E andNotIn(String field,Set items)
	{
		return this.andNotIn(field,items.toArray(new Object[0]));
	}

	/**
	 *   not in
	 *  @param field 字段
	 *  @param items 值清单
	 *  @return CE,对象自身
	 * */
	public E andNotIn(String field,Object[] items,Object... ignoreValue)
	{
		return this.andNotIn(field,Arrays.asList(items),ignoreValue);
	}


	/**
	 * 从多行语句生成 ConditionExpression
	 * */
	public static ConditionExpression fromLines(String[] lines,Object... params)
	{
		return new ConditionExpression(SQL.joinSQLs(lines),params);
	}

	/**
	 * 从多行语句生成 ConditionExpression
	 * */
	public static ConditionExpression fromLines(List<String> lines,Object... params)
	{
		return new ConditionExpr(SQL.joinSQLs(lines),params);
	}

	public ConditionExpression clone() {
		ConditionExpression conditionExpr=new ConditionExpression();
		for (SQL se : this.ses) {
			conditionExpr.ses.add(se.clone());
		}
		conditionExpr.logics.addAll(this.logics);
		conditionExpr.startWith=this.startWith;
		return conditionExpr;
	}

	protected E clone(E e) {
		ConditionExpression conditionExpr=(ConditionExpression) e;
		for (SQL se : this.ses) {
			conditionExpr.ses.add(se.clone());
		}
		conditionExpr.logics.addAll(this.logics);
		conditionExpr.startWith=this.startWith;
		return e;
	}

}
