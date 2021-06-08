package com.github.foxnic.sql.expr;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.foxnic.sql.data.ExprDAO;
import com.github.foxnic.sql.data.ExprPagedList;
import com.github.foxnic.sql.data.ExprRcd;
import com.github.foxnic.sql.data.ExprRcdSet;
import com.github.foxnic.sql.dialect.SQLDialect;

/**
 * @author fangjieli
 * */
public class Select extends DML  implements QueryableSQL
{
	/**
	 * 创建一个Select并指定表名
	 * */
	public static Select create(String table) {
		return new Select(table);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 7744128845057304700L;
	
	private ArrayList<SQL> tables =new ArrayList<SQL>();
	private ArrayList<String> tableAliases=new ArrayList<String>();
	
	
	private ArrayList<SQL> fields=new ArrayList<SQL>();
	private ArrayList<String> fieldsAliases=new ArrayList<String>();
	
	private ArrayList<String> fieldsPrefix=new ArrayList<String>();
	private String currentFieldPrefix=null;
	
	
	
	private SelectWhere where=new SelectWhere();
	private SelectOrderBy orderBy=new SelectOrderBy();
	private GroupBy groupBy=new GroupBy();
	
	 
	public SelectWhere where()
	{
		return where;
	}
	
	public SelectWhere where(String ce,Object... ps)
	{
		return this.where.and(ce,ps);
	}
	
	public GroupBy groupBy()
	{
		return groupBy;
	}
	
	public SelectOrderBy orderBy() 
	{
		return orderBy;
	}
	
	public static Select init()
	{
		return new Select();
	}
	
	 
	public Select(String table)
	{
		this.from(table);
		//
		this.where.setParent(this);
		this.orderBy.setParent(this);
		this.groupBy.setParent(this);
	}
	
	public Select()
	{
		this.where.setParent(this);
		this.orderBy.setParent(this);
		this.groupBy.setParent(this);
	}

	public Select from(Expr table,String alias)
	{
		Utils.validateDBIdentity(alias);
		this.tables.add(table);
		table.setParent(this);
		this.tableAliases.add(alias);
		currentFieldPrefix=null;
		return this;
	}
	
	public Select from(String table,String alias,Object... ps)
	{
		Utils.validateDBIdentity(table);
		Utils.validateDBIdentity(alias);
		currentFieldPrefix=null;
		return from(Expr.create(table,ps),alias);
	}
	
	public Select from(Select table,String alias)
	{
		Utils.validateDBIdentity(alias);
		this.tables.add(table);
		table.setParent(this);
		this.tableAliases.add(alias);
		currentFieldPrefix=null;
		return this;
	}
	
	public Select from(String table)
	{
		Utils.validateDBIdentity(table);
		currentFieldPrefix=null;
		return from(Expr.create(table),null);
	}
	
	public Select froms(String... table)
	{
		for(String tab:table)
		{
			Utils.validateDBIdentity(tab);
			from(Expr.create(tab),null);
		}
		currentFieldPrefix=null;
		return this;
	}
	
	public Select fromAs(String... tableOrAlias)
	{
		String[] tabs;
		if(tableOrAlias.length%2==1)
		{
			tabs=new String[tableOrAlias.length+1];
			for(int i=0;i<tableOrAlias.length;i++) {
				tabs[i]=tableOrAlias[i];
			}
			tabs[tabs.length-1]=null;
		} else {
			tabs=tableOrAlias;
		}
		
		String table = null;
		String alias = null;
		for(int i=0;i<tabs.length;i++)
		{
			table=tabs[i];
			i++;
			alias=tabs[i];
			
			Utils.validateDBIdentity(table);
			Utils.validateDBIdentity(alias);
			
			from(Expr.create(table),alias); 
		}
		currentFieldPrefix=null;
		return this;
	}
	
	/**
	 * 设置字段前缀，调用此方法后，所有之后增加的字段将使用统一前缀<br>
	 * 使用最后一个表最为默认前缀
	 * 前缀: select A.* from tab A 其中 A.*中的A即为前缀
	 * @return Select语句
	 * */
	public Select prefix()
	{
		int last=tableAliases.size()-1;
		String pfx=tableAliases.get(last);
		if(pfx==null || pfx.length()==0) {
			pfx=tables.get(last).getSQL();
		}
		return prefix(pfx);
	}
	
	/**
	 * 设置字段前缀，调用此方法后，所有之后增加的字段将使用统一前缀<br>
	 * 前缀: select A.* from tab A 其中 A.*中的A即为前缀
	 * @param pfx 前缀
	 * @return Select语句
	 * */
	public Select prefix(String pfx)
	{
		currentFieldPrefix=pfx;
		return this;
	}
	
	public Select select(Expr se,String alias)
	{
		this.fields.add(se);
		se.setParent(this);
		this.fieldsAliases.add(alias);
		this.fieldsPrefix.add(currentFieldPrefix);
		return this;
	}
	
	public Select select(Select se,String alias)
	{
		this.fields.add(se);
		se.setParent(this);
		this.fieldsAliases.add(alias);
		this.fieldsPrefix.add(currentFieldPrefix);
		return this;
	}
	
	public Select select(String fld,Object... ps)
	{
		 return this.select(Expr.create(fld,ps), "");
	}

	public Select select(String fld,String alias,Object... ps)
	{
		 return this.select(Expr.create(fld,ps), alias);
	}
	public Select selects(Expr... se)
	{
		for(Expr s:se)
		{
			this.select(s, null);
		}
		return this;
	}
	
	public Select selects(String... fld)
	{
		 for(String s:fld)
		 {
			 this.select(s,null);
		 }
		 return this;
	}

	public Select selects(Select... se)
	{
		for(Select s:se)
		{
			this.select(s,null);
		}
		return this;
	}
	
	public Select selectAs(String... fldOrAlias)
	{
		String[] fields;
		if(fldOrAlias.length%2==1)
		{
			fields=new String[fldOrAlias.length+1];
			for(int i=0;i<fldOrAlias.length;i++) {
				fields[i]=fldOrAlias[i];
			}
			fields[fields.length-1]=null;
		} else {
			fields=fldOrAlias;
		}
		
		for(int i=0;i<fields.length;i++)
		{
			String field=fields[i];
			i++;
			String alias=fields[i];
			this.select(field,alias);
		}
		return this;
	}
	
 
	
	@Override
	public String getSQL(SQLDialect dialect) {
		SQLStringBuilder sql = new SQLStringBuilder();
		if(this.isEmpty()) {
			return "";
		}
		sql.append(SQLKeyword.SELECT);
		if(this.fields.size()==0) {
			sql.append(SQLKeyword.SELECT_STAR);
		} else {
			for(int i=0;i<this.fields.size();i++) {
				fields.get(i).setIgnorColon(ignorColon);
				sql.append((this.fieldsPrefix.get(i)==null?"":this.fieldsPrefix.get(i)+SQLKeyword.DOT.toString())+this.fields.get(i).getSQL(dialect),(this.fieldsAliases.get(i)==null?"":this.fieldsAliases.get(i)));
				if(i<this.fields.size()-1) {
					sql.append(SQLKeyword.COMMA);
				}
			}
		}
		sql.append(SQLKeyword.FROM);
		for(int i=0;i<this.tables.size();i++) {
			tables.get(i).setIgnorColon(ignorColon);
			
			String sub=bracketSQL(this.tables.get(i).getSQL(dialect));
			
			sql.append(sub,(this.tableAliases.get(i)==null?"":this.tableAliases.get(i)));
			if(i<this.tables.size()-1) {
				sql.append(SQLKeyword.COMMA);
			}
		}
		
		if(!this.where().isEmpty())
		{
			where().setIgnorColon(ignorColon);
			sql.append(where().getSQL(dialect));
		}
		
		if(!this.groupBy().isEmpty())
		{
			groupBy().setSQLDialect(dialect);
			groupBy().setIgnorColon(ignorColon);
			sql.append(groupBy().getSQL(dialect));
		}
		if(!this.orderBy().isEmpty())
		{
			orderBy().setSQLDialect(dialect);
			orderBy().setIgnorColon(ignorColon);
			sql.append(orderBy().getSQL(dialect));
		}
		return sql.toString();
	}

	
	@Override
	public String getListParameterSQL() {
		SQLStringBuilder sql = new SQLStringBuilder();
		if(this.isEmpty()) {
			return "";
		}
		sql.append(SQLKeyword.SELECT);
		if(this.fields.size()==0) {
			sql.append(SQLKeyword.SELECT_STAR);
		} else {
			for(int i=0;i<this.fields.size();i++)
			{
				this.fields.get(i).setIgnorColon(ignorColon);
				sql.append((this.fieldsPrefix.get(i)==null?"":this.fieldsPrefix.get(i)+SQLKeyword.DOT.toString())+this.fields.get(i).getListParameterSQL(),(this.fieldsAliases.get(i)==null?"":this.fieldsAliases.get(i)));
				if(i<this.fields.size()-1) {
					sql.append(SQLKeyword.COMMA);
				}
			}
		}
		sql.append(SQLKeyword.FROM);
		for(int i=0;i<this.tables.size();i++) {
			tables.get(i).setIgnorColon(ignorColon);
			String sub=bracketSQL(this.tables.get(i).getListParameterSQL());
			
			sql.append(sub,(this.tableAliases.get(i)==null?"":this.tableAliases.get(i)));
			if(i<this.tables.size()-1) {
				sql.append(SQLKeyword.COMMA);
			}
		}
		//
		if(!this.where().isEmpty())
		{
			where().setIgnorColon(ignorColon);
			sql.append(where().getListParameterSQL());
		}
		if(!this.groupBy().isEmpty())
		{
			groupBy().setIgnorColon(ignorColon);
			sql.append(groupBy().getListParameterSQL());
		}
		if(!this.orderBy().isEmpty())
		{
			orderBy().setIgnorColon(ignorColon);
			sql.append(orderBy().getListParameterSQL());
		}
		return sql.toString();
	}

	
	@Override
	public Object[] getListParameters() {
		if(this.isEmpty()) {
			return new Object[]{};
		}
		ArrayList<Object> ps=new ArrayList<Object>();
		for(int i=0;i<this.fields.size();i++)
		{
			fields.get(i).setIgnorColon(ignorColon);
			ps.addAll(Utils.toList(this.fields.get(i).getListParameters()));
		}
		for(int i=0;i<this.tables.size();i++)
		{
			tables.get(i).setIgnorColon(ignorColon);
			ps.addAll(Utils.toList(this.tables.get(i).getListParameters()));
		}
		if(!this.where().isEmpty())
		{
			this.where().setIgnorColon(ignorColon);
			ps.addAll(Utils.toList(this.where().getListParameters()));
		}
		if(!this.groupBy().isEmpty())
		{
			groupBy().setIgnorColon(ignorColon);
			ps.addAll(Utils.toList(this.groupBy().getListParameters()));
		}
		if(!this.orderBy().isEmpty())
		{
			orderBy().setIgnorColon(ignorColon);
			ps.addAll(Utils.toList(this.orderBy().getListParameters()));
		}
		return ps.toArray(new Object[ps.size()]);
	}

	
	@Override
	public String getNamedParameterSQL() {
		SQLStringBuilder sql = new SQLStringBuilder();
		if(this.isEmpty()) {
			return "";
		}
		this.beginParamNameSQL();
		sql.append(SQLKeyword.SELECT);
		if(this.fields.size()==0) {
			sql.append(SQLKeyword.SELECT_STAR);
		} else {
			for(int i=0;i<this.fields.size();i++)
			{
				fields.get(i).setIgnorColon(ignorColon);
				sql.append((this.fieldsPrefix.get(i)==null?"":this.fieldsPrefix.get(i)+SQLKeyword.DOT.toString())+this.fields.get(i).getNamedParameterSQL(),(this.fieldsAliases.get(i)==null?"":this.fieldsAliases.get(i)));
				if(i<this.fields.size()-1) {
					sql.append(SQLKeyword.COMMA);
				}
			}
		}
		sql.append(SQLKeyword.FROM);
		for(int i=0;i<this.tables.size();i++)
		{
			tables.get(i).setIgnorColon(ignorColon);
			String sub=bracketSQL(this.tables.get(i).getNamedParameterSQL());
			sql.append(sub,(this.tableAliases.get(i)==null?"":this.tableAliases.get(i)));
			if(i<this.tables.size()-1) {
				sql.append(SQLKeyword.COMMA);
			}
		}
		if(!this.where().isEmpty())
		{
			where().setIgnorColon(ignorColon);
			sql.append(where().getNamedParameterSQL());
		}
		if(!this.groupBy().isEmpty())
		{
			groupBy().setIgnorColon(ignorColon);
			sql.append(groupBy().getNamedParameterSQL());
		}
		if(!this.orderBy().isEmpty())
		{
			orderBy().setIgnorColon(ignorColon);
			sql.append(orderBy().getNamedParameterSQL());
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
		for(int i=0;i<this.fields.size();i++)
		{
			fields.get(i).setIgnorColon(ignorColon);
			ps.putAll(this.fields.get(i).getNamedParameters());
		}
		for(int i=0;i<this.tables.size();i++)
		{
			tables.get(i).setIgnorColon(ignorColon);
			ps.putAll(this.tables.get(i).getNamedParameters());
		}
		if(!this.where().isEmpty())
		{
			where().setIgnorColon(ignorColon);
			ps.putAll(this.where().getNamedParameters());
		}
		if(!this.groupBy().isEmpty())
		{
			groupBy().setIgnorColon(ignorColon);
			ps.putAll(this.groupBy().getNamedParameters());
		}
		if(!this.orderBy().isEmpty())
		{
			orderBy().setIgnorColon(ignorColon);
			ps.putAll(this.orderBy().getNamedParameters());
		}
		this.endParamNameSQL();
		return ps;
	}

	/**
	 * 判断是否为空
	 * 表数量为 0 , 判定为 empty
	 * @return 是否空
	 * */
	@Override
	public boolean isEmpty() {
		return this.tables.size()==0;
	}

	
	@Override
	public boolean isAllParamsEmpty() {
 
		for(int i=0;i< this.fields.size();i++)
		{
			fields.get(i).setIgnorColon(ignorColon);
			if(!this.fields.get(i).isAllParamsEmpty()) {
				return false;
			}
		}
		for(int i=0;i<this.tables.size();i++)
		{
			tables.get(i).setIgnorColon(ignorColon);
			if(!this.tables.get(i).isAllParamsEmpty()) {
				return false;
			}
		}
		
		where().setIgnorColon(ignorColon);
		if(!this.where().isAllParamsEmpty()) {
			return false;
		}
 
		groupBy().setIgnorColon(ignorColon);
		if(!this.groupBy().isAllParamsEmpty()) {
			return false;
		}

		orderBy().setIgnorColon(ignorColon);
		if(!this.orderBy().isAllParamsEmpty()) {
			return false;
		}
		
		return true;
	}
	
	
	private transient ExprDAO dao = null;

	@Override
	public ExprDAO getDAO() {
		return dao;
	}

	@Override
	public Select setDAO(ExprDAO dao) {
		this.dao = dao;
		return this;
	}
	
	@Override
	public ExprRcdSet query() {
		return getDAO().query(this);
	}
	
	
	@Override
	public <T> List<T> queryEntities(Class<T> type) {
		return getDAO().queryEntities(type, this);
	};
	
	
	@Override
	public <T> ExprPagedList<T> queryPagedEntities(Class<T> type,int pageSize,int pageIndex) {
		return getDAO().queryPagedEntities(type, pageSize,pageIndex,this);
	};
	
	@Override
	public ExprRcdSet queryPage(int pageSize,int pageIndex)
	{
		return getDAO().queryPage(this, pageSize, pageIndex);
	}
 
	@Override
	public ExprRcd queryRecord() {
		return getDAO().queryRecord(this);
	}

	@Override
	public Integer queryInteger() {
		return getDAO().queryInteger(this);
	}

	@Override
	public String queryString() {
		return getDAO().queryString(this);
	}

	@Override
	public Long queryLong() {
		return getDAO().queryLong(this);
	}

	@Override
	public Date queryDate() {
		return getDAO().queryDate(this);
	}

	@Override
	public BigDecimal queryBigDecimal() {
		return getDAO().queryBigDecimal(this);
	}
	
	@Override
	public Double queryDouble() {
		return getDAO().queryDouble(this);
	}
	
	@Override
	public Timestamp queryTimestamp() {
		return getDAO().queryTimestamp(this);
	}

	/**
	 * 使用 setDAO()方法指定的DAO执行当前语句
	 * */
	@Override
	public Integer execute()
	{
		return getDAO().execute(this);
	}
 
}
