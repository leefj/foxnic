package com.github.foxnic.sql.expr;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.collection.TypedHashMap;
import com.github.foxnic.sql.data.ExprDAO;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.entity.EntityUtil;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author fangjieli
 * */
public class Update extends DML implements Setter,ExecutableSQL {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1060000732752011912L;
	
	private ArrayList<SQL> values=new ArrayList<SQL>();
	private ArrayList<String> fields=new ArrayList<String>();
	private String table=null;
	public String getTable() {
		return table;
	}
	
	public Map<String,SQL> getValues() {
		Map<String,SQL> map=new HashMap<String,SQL>();
		for (int i = 0; i < fields.size(); i++) {
			map.put(fields.get(i), values.get(i));
		}
		return map;
	}

	private String tableAlias=null;
	private UpdateWhere where=new UpdateWhere();
	
	public static Update create(String table)
	{
		return new Update(table);
	}

	public UpdateWhere where()
	{
		return this.where;
	}
	
	public UpdateWhere where(String ce,Object... ps)
	{
		return this.where.and(ce,ps);
	}
	
	public Update()
	{
		this.where.setParent(this);
	}
	public Update(String table)
	{
		this.where.setParent(this);
		this.update(table);
	}
	public Update(DBTable table)
	{
		 this(table.name());
	}
	
	public Update update(String table,String alias)
	{
		Utils.validateDBIdentity(table);
		Utils.validateDBIdentity(alias);
		this.table=table;
		this.tableAlias=alias;
		return this;
	}
	
	public Update update(String table)
	{
		Utils.validateDBIdentity(table);
		this.table=table;
		this.tableAlias=null;
		return this;
	}
	
	public Update set(DBField fld,Object val) {
		return this.set(fld.name(), val);
	}
	
	public Update set(String fld,Object val)
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

	

	/**
	 * 使用名值对的形式批量设置设置
	 * */
	public Update sets(Object... nvs)
	{
		TypedHashMap<String,Object> map=TypedHashMap.asMap(nvs);
		for(String key:map.keySet()) {
			this.set(key, map.get(key));
		}
		return this;
	}
	
	/**
	 * 使用名值对的形式批量设置设置
	 * */
	public Update setsIf(Object... nvs)
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
	public Update setIf(String fld,Object val)
	{
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
	
	/**
	 * 是否有set值
	 * @return 逻辑值
	 * */
	public boolean hasValue()
	{
		return !values.isEmpty();
	}
	
	public Update set(String fld,SQL se)
	{
		if(se==null) {
			se=new Expr("?",null);
		}
		int i=indexOf(fld);
		if(i==-1)
		{
			values.add(se);
			if(se!=null ) {
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
	public Update setIf(String fld,SQL se)
	{
		if(se==null) {
			return this;
		}
		if(se.isEmpty()) {
			return this;
		}
		return set(fld,se);
	}
	
	public Update setExpr(String fld,Expr se)
	{
		return set(fld,se);
	}
	public Update setExprIf(String fld,Expr se)
	{
		return setIf(fld,se);
	}
	
	public Update setExpr(String fld,String se,Object... ps)
	{
		return set(fld,Expr.create(se,ps));
	}
	
	public Update setExprIf(String fld,String se,Object... ps)
	{
		return setIf(fld,Expr.create(se,ps));
	}
	
	
	
	
	@Override
	public String getSQL(SQLDialect dialect) {
		if(this.isEmpty()) {
			return "";
		}
		
		SQLStringBuilder sql=new SQLStringBuilder();
		sql.append(SQLKeyword.UPDATE).append(this.table);
		if(this.tableAlias!=null) {
			sql.append(this.tableAlias);
		}
		sql.append(SQLKeyword.UPDATE$SET);
		
		for(int i=0;i<fields.size();i++) {
			values.get(i).setIgnorColon(ignorColon);
			sql.append(fields.get(i),SQLKeyword.OP$EAUALS.toString(),values.get(i).getSQL(dialect));
			if(i<fields.size()-1) {
				sql.append(SQLKeyword.COMMA);
			}	
		}
		
		if(!this.where().isEmpty()) {
			where.setIgnorColon(ignorColon);
			sql.append(this.where().getSQL(dialect));
		}
		
		return sql.toString();
	}

	
	@Override
	public String getListParameterSQL() {
		if(this.isEmpty()) {
			return "";
		}
		
		SQLStringBuilder sql=new SQLStringBuilder();
		sql.append(SQLKeyword.UPDATE).append(this.table);
		if(this.tableAlias!=null) {
			sql.append(this.tableAlias);
		}
		sql.append(SQLKeyword.UPDATE$SET);
		
		for(int i=0;i<fields.size();i++) {
			values.get(i).setIgnorColon(ignorColon);
			sql.append(fields.get(i),SQLKeyword.OP$EAUALS.toString(),values.get(i).getListParameterSQL());
			if(i<fields.size()-1) {
				sql.append(SQLKeyword.COMMA);
			}	
		}
		
		if(!this.where().isEmpty()) {
			where.setIgnorColon(ignorColon);
			sql.append(this.where().getListParameterSQL());
		}
		
		return sql.toString();
	}

	
	@Override
	public Object[] getListParameters() {
		if(this.isEmpty()) {
			return new Object[]{};
		}
		ArrayList<Object> ps=new ArrayList<Object>();
		for(SQL val:values) {
			val.setIgnorColon(ignorColon);
			ps.addAll(Utils.toList(val.getListParameters()));
		}
		
		if(!this.where().isEmpty()) {
			where.setIgnorColon(ignorColon);
			ps.addAll(Utils.toList(this.where().getListParameters()));
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
		sql.append(SQLKeyword.UPDATE).append(this.table);
		if(this.tableAlias!=null) {
			sql.append(this.tableAlias);
		}
		sql.append(SQLKeyword.UPDATE$SET);
		
		for(int i=0;i<fields.size();i++) {
			values.get(i).setIgnorColon(ignorColon);
			sql.append(fields.get(i),SQLKeyword.OP$EAUALS.toString(),values.get(i).getNamedParameterSQL());
			if(i<fields.size()-1) {
				sql.append(SQLKeyword.COMMA);
			}	
		}
		
		if(!this.where().isEmpty()) {
			where.setIgnorColon(ignorColon);
			sql.append(this.where().getNamedParameterSQL());
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
		for(SQL val:values)
		{
			val.setIgnorColon(ignorColon);
			ps.putAll(val.getNamedParameters());
		}
		if(!this.where().isEmpty())
		{
			where.setIgnorColon(ignorColon);
			ps.putAll(this.where().getNamedParameters());
		}
		this.endParamNameSQL();
		return ps;
	}

	/**
	 * 判断是否为空
	 * 更新的字段数为0 , 判定为 empty
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
		 
		
		if(!this.where().isAllParamsEmpty()) {
			return false;
		}
	
		return true;
	}
	
	
 
	/**
	 * 用实体设置Update的值
	 * @param pojo pojo对象
	 * @param ignorNulls 是否忽略空值
	 * @return Update
	 * */
	public Update setPOJO(Object pojo,boolean ignorNulls)
	{
		Map<String,Object> vals=BeanUtil.toMap(pojo);
		Map<String,String> flds=EntityUtil.getDBFields(pojo.getClass());
		for (Entry<String,String> e : flds.entrySet()) {
			this.set(e.getValue(), vals.get(e.getKey()));
		}
		return this;
	}
	
	  
	
	

	/**
	 * 是否将表名，字段名用引号引起来
	 * */
	public Update quote(boolean b)
	{
		quotes=b;
		return this;
	}
	
	/**
	 * 将表名，字段名用引号引起来<br>
	 * 默认无引号
	 * */
	public Update quote()
	{
		quotes=true;
		return this;
	}
	
	/**
	 * 把Update语句转换成Insert语句
	 * */
	public Insert toInsert() {
		Insert insert=new Insert(this.table);
		for (int i = 0; i < fields.size();i++) {
			insert.set(fields.get(i), values.get(i));
		}
		return insert;
	}
	
	/**
	 * 把Insert语句里的字段值填充到当的Update语句
	 * */
	public Update sets(Insert insert) {
		Map<String,SQL> map=insert.getValues();
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
	public Update setDAO(ExprDAO dao) {
		this.dao=dao;
		return this;
	}
 
}
