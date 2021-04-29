package com.github.foxnic.sql.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.collection.TypedHashMap;
import com.github.foxnic.sql.data.ExprDAO;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.entity.EntityUtil;
 
 
/**
 * 
 * Insert语句
 * @author fangjieli
 *
 */
public class Insert extends DML implements Setter,ExecutableSQL  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8678474724289840844L;
	
	private ArrayList<SQL> values=new ArrayList<SQL>();
	private ArrayList<String> fields=new ArrayList<String>();
	private String table=null;
	
	public Map<String,SQL> getValues() {
		Map<String,SQL> map=new HashMap<String,SQL>();
		for (int i = 0; i < fields.size(); i++) {
			map.put(fields.get(i), values.get(i));
		}
		return map;
	}
	
	
	/**
	 * 获取已经设置的值
	 * @param 字段名
	 * @return 值
	 * */
	public SQL getValue(String field) {
		int i=this.indexOf(field);
		if(i==-1) return null;
		return this.values.get(i);
	}
	
	public Insert()
	{}
	
	public boolean removeField(String field)
	{
		int i=fields.indexOf(field);
		if(i==-1) i=fields.indexOf(field.toUpperCase());
		if(i==-1) i=fields.indexOf(field.toLowerCase());
		if(i==-1) return false;
		fields.remove(i);
		values.remove(i);
		return true;
	}
	
	public Insert(String table) 
	{
		Utils.validateDBIdentity(table);
		this.table=table;
	}

	public Insert into(String table)
	{
		Utils.validateDBIdentity(table);
		this.table=table;
		return this;
	}
 
	/**
	 * 是否有set值
	 * @return 逻辑值
	 * */
	public boolean hasValue()
	{
		return !values.isEmpty();
	}
	
	public Insert set(String fld,Object val)
	{
		Utils.validateDBIdentity(fld);
		
		val=Utils.parseParmeterValue(val);
		
		if(val instanceof SQL)
		{
			set(fld,(SQL)val);
		}
		else
		{
			set(fld,Expr.create(SQLKeyword.QUESTION.toString(),val));
		}
		return this;
	}
	
	public Insert sets(Object... nvs)
	{
		TypedHashMap<String,Object> map=TypedHashMap.asMap(nvs);
		for(String key:map.keySet())
		{
			this.set(key, map.get(key));
		}
		return this;
	}
	
	public Insert setsIf(Object... nvs)
	{
		TypedHashMap<String,Object> map=TypedHashMap.asMap(nvs);
		for(String key:map.keySet())
		{
			this.setIf(key, map.get(key));
		}
		return this;
	}
	
	/**
	 * 设置值，如果 value 等于 null 或 value 是一个空语句则不生效
	 * */
	public Insert setIf(String fld,Object val)
	{
		Utils.validateDBIdentity(fld);
		if(val==null) {
			return this;
		}
		if(val instanceof SQL)
		{
			setIf(fld,(SQL)val);
		}
		else
		{
			setIf(fld,Expr.create(SQLKeyword.QUESTION.toString(),val));
		}
		return this;
	}
	
	private int indexOf(String fld)
	{
		for (int j = 0; j < fields.size(); j++) {
			String f=fields.get(j);
			if(f.equalsIgnoreCase(fld))
			{
				return j;
			}
		}
		return -1; 
	}
	
	 
	
	public Insert set(String fld,SQL se)
	{
		Utils.validateDBIdentity(fld);
		if(se==null) {
			se=new Expr("?",null);
		}
		
		int i=indexOf(fld);
		if(i==-1)
		{
			values.add(se);
			if(se!=null) {
				se.setParent(this);
			}
			fields.add(checkSQLName(fld));
		}
		else
		{
			SQL old=values.get(i);
			if(old!=null) {
				old.setParent(null);
			}
			values.set(i, se);
			if(se!=null) {
				se.setParent(this);
			}
		}
		return this;
		
	}
	/**
	 * 设置值，如果 value 等于 null 或 value 是一个空语句则不生效
	 * */
	public Insert setIf(String fld,SQL se)
	{
		if(se==null) {
			return this;
		}
		if(se.isEmpty()) {
			return this;
		}
		return set(fld,se);
	}
	
	public Insert setExpr(String fld,Expr se)
	{
		return set(fld,se);
	}
	
	public Insert setExprIf(String fld,Expr se)
	{
		if(se==null) {
			return this;
		}
		if(se.isEmpty()) {
			return this;
		}
		return setExpr(fld, se);
	}
	
	public Insert setExpr(String fld,String se,Object... ps)
	{
		return set(fld,Expr.create(se,ps));
	}
	
	public Insert setExprIf(String fld,String se,Object... ps)
	{
		return setIf(fld,Expr.create(se,ps));
	}
	
	
	
	
	
	
	
	
	@Override
	public String getSQL(SQLDialect dialect) {
		
		if(this.isEmpty()) {
			return "";
		}
		SQLStringBuilder sql=new SQLStringBuilder();
		sql.append(SQLKeyword.INSERT,SQLKeyword.INSERT$INTO);
		sql.append(putInQuotes(this.table)).append(SQLKeyword.LEFT_BRACKET);
		for(int i=0;i<fields.size();i++) {
			sql.append(putInQuotes(fields.get(i)));
			if(i<fields.size()-1) {
				sql.append(SQLKeyword.COMMA.toString());
			}	
		}
		sql.append(SQLKeyword.RIGHT_BRACKET);
		sql.append(SQLKeyword.VALUES,SQLKeyword.LEFT_BRACKET);
		for(int i=0;i<values.size();i++)
		{
			if(values.get(i)!=null)
			{
				values.get(i).setIgnorColon(ignorColon);
				sql.append(values.get(i).getSQL(dialect));
			}
			else
			{
				sql.append(SQLKeyword.NULL);
			}
			if(i<values.size()-1) {
				sql.append(SQLKeyword.COMMA);
			}	
		}
		sql.append(SQLKeyword.RIGHT_BRACKET);
		return sql.toString();
		
	}

	
	@Override
	public String getListParameterSQL() {
		
		if(this.isEmpty()) {
			return "";
		}
		SQLStringBuilder sql=new SQLStringBuilder();
		sql.append(SQLKeyword.INSERT,SQLKeyword.INSERT$INTO);
		sql.append(putInQuotes(this.table)).append(SQLKeyword.LEFT_BRACKET);
		for(int i=0;i<fields.size();i++) {
			sql.append(putInQuotes(fields.get(i)));
			if(i<fields.size()-1) {
				sql.append(SQLKeyword.COMMA.toString());
			}	
		}
		sql.append(SQLKeyword.RIGHT_BRACKET);
		sql.append(SQLKeyword.VALUES,SQLKeyword.LEFT_BRACKET);
		for(int i=0;i<values.size();i++)
		{
			if(values.get(i)!=null)
			{
				values.get(i).setIgnorColon(ignorColon);
				sql.append(values.get(i).getListParameterSQL());
			}
			else
			{
				sql.append(SQLKeyword.QUESTION);
			}
			if(i<values.size()-1) {
				sql.append(SQLKeyword.COMMA);
			}	
		}
		sql.append(SQLKeyword.RIGHT_BRACKET);
		return sql.toString();
		
	}

	
	@Override
	public Object[] getListParameters() {
		if(this.isEmpty()) {
			return new Object[]{};
		}
		ArrayList<Object> ps=new ArrayList<Object>();
		for(SQL val:values)
		{
			val.setIgnorColon(ignorColon);
			ps.addAll(Utils.toList(val.getListParameters()));
		}
		return ps.toArray(new Object[ps.size()]);
	}

	
	@Override
	public String getNamedParameterSQL() {
		if(this.isEmpty()) {
			return "";
		}
		this.beginParamNameSQL();
		SQLStringBuilder sql=new SQLStringBuilder();
		sql.append(SQLKeyword.INSERT,SQLKeyword.INSERT$INTO);
		sql.append(putInQuotes(this.table)).append(SQLKeyword.LEFT_BRACKET);
		for(int i=0;i<fields.size();i++) {
			sql.append(putInQuotes(fields.get(i)));
			if(i<fields.size()-1) {
				sql.append(SQLKeyword.COMMA.toString());
			}	
		}
		sql.append(SQLKeyword.RIGHT_BRACKET);
		sql.append(SQLKeyword.VALUES,SQLKeyword.LEFT_BRACKET);
		for(int i=0;i<values.size();i++)
		{
			values.get(i).setIgnorColon(ignorColon);
			sql.append(values.get(i).getNamedParameterSQL());
			if(i<values.size()-1) {
				sql.append(SQLKeyword.COMMA);
			}	
		}
		sql.append(SQLKeyword.RIGHT_BRACKET);
		this.endParamNameSQL();
		return sql.toString();
	}

	
	@Override
	public Map<String, Object> getNamedParameters() {
		
		HashMap<String, Object> ps=new HashMap<>(5);
		if(this.isEmpty()) {
			return ps;
		}
		this.beginParamNameSQL();
		for(SQL val:values)
		{
			val.setIgnorColon(ignorColon);
			ps.putAll(val.getNamedParameters());
		}
		this.endParamNameSQL();
		return ps;
	}

	/**
	 * 判断是否为空
	 * 插入字段数为0 , 判定为 empty
	 * @return 是否空
	 * */
	@Override
	public boolean isEmpty() {
		return fields.size()==0;
	}

	
	@Override
	public boolean isAllParamsEmpty() {
 
		for(SQL val:values)
		{
			val.setIgnorColon(ignorColon);
			if(!val.isAllParamsEmpty()) {
				return false;
			}
		}
		
		return true;
	}
 
	 
	/**
	 * 把实体中的属性值设置到语句中
	 * @param entity 实体对象
	 * @return Insert
	 * */
	public Insert setEntity(Object entity)
	{
		Map<String,Object> vals=BeanUtil.toMap(entity);
		Map<String,String> flds=EntityUtil.getDBFields(entity.getClass());
		for (Entry<String,String> e : flds.entrySet()) {
			this.set(e.getValue(), vals.get(e.getKey()));
		}
		return this;
	}
	
	/**
	 * 把实体中的非null的属性值设置到语句中
	 * @param entity 实体对象
	 * @return Insert
	 * */
	public Insert setEntityIf(Object entity)
	{
		if(entity==null) {
			return this;
		}
		 
		Map<String,Object> vals=BeanUtil.toMap(entity);
		Map<String,String> flds=EntityUtil.getDBFields(entity.getClass());
		for (Entry<String,String> e : flds.entrySet()) {
			this.setIf(e.getValue(), vals.get(e.getKey()));
		}
	 
		return this;
	}
	
 
	
	
	/**
	 * 是否将表名，字段名用引号引起来
	 * @param b 是否引号引起来
	 * @return Insert
	 * */
	public Insert quote(boolean b)
	{
		quotes=b;
		return this;
	}
	
	/**
	 * 将表名，字段名用引号引起来<br>
	 * 未调用此方法前，默认无引号
	 * @return Insert
	 * */
	public Insert quote()
	{
		quotes=true;
		return this;
	}
 
	
 
	
	/**
	 * 把Insert语句转换成Update语句
	 * */
	public Update toUpdate(ConditionExpr condition) {
		Update update=new Update(this.table);
		for (int i = 0; i < fields.size();i++) {
			update.set(fields.get(i), values.get(i));
		}
		update.where().and(condition);
		return update;
	}
	
	/**
	 * 把Update语句里的字段值填充到当的Insert语句
	 * */
	public Insert sets(Update update) {
		Map<String,SQL> map=update.getValues();
		for (Entry<String,SQL> e : map.entrySet()) {
			this.set(e.getKey(), e.getValue());
		}
		return this;
	}
	

	
	private ExprDAO dao=null;
	
	/**
	 * 使用 setDAO()方法指定的DAO执行当前语句
	 * @return 执行结果，行数
	 * */
	@Override
	public Integer execute()
	{
		return getDAO().execute(this);
	}
 
	@Override
	public ExprDAO getDAO() {
		return dao;
	}

	 
	@Override
	public Insert setDAO(ExprDAO dao) {
		this.dao=dao;
		return this;
	}
}
