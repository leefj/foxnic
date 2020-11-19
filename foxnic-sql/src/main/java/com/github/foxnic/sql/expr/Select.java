package com.github.foxnic.sql.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.github.foxnic.sql.dialect.SQLDialect;

/**
 * @author fangjieli
 * */
public class Select extends DML  
{
	
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
	private OrderBy orderBy=new OrderBy();
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
	
	public OrderBy orderBy()
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
		return from(Expr.get(table,ps),alias);
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
		return from(Expr.get(table),null);
	}
	
	public Select froms(String... table)
	{
		for(String tab:table)
		{
			Utils.validateDBIdentity(tab);
			from(Expr.get(tab),null);
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
			
			from(Expr.get(table),alias); 
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
		 return this.select(Expr.get(fld,ps), "");
	}

	public Select select(String fld,String alias,Object... ps)
	{
		 return this.select(Expr.get(fld,ps), alias);
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
	public String getNameParameterSQL() {
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
				sql.append((this.fieldsPrefix.get(i)==null?"":this.fieldsPrefix.get(i)+SQLKeyword.DOT.toString())+this.fields.get(i).getNameParameterSQL(),(this.fieldsAliases.get(i)==null?"":this.fieldsAliases.get(i)));
				if(i<this.fields.size()-1) {
					sql.append(SQLKeyword.COMMA);
				}
			}
		}
		sql.append(SQLKeyword.FROM);
		for(int i=0;i<this.tables.size();i++)
		{
			tables.get(i).setIgnorColon(ignorColon);
			String sub=bracketSQL(this.tables.get(i).getNameParameterSQL());
			sql.append(sub,(this.tableAliases.get(i)==null?"":this.tableAliases.get(i)));
			if(i<this.tables.size()-1) {
				sql.append(SQLKeyword.COMMA);
			}
		}
		if(!this.where().isEmpty())
		{
			where().setIgnorColon(ignorColon);
			sql.append(where().getNameParameterSQL());
		}
		if(!this.groupBy().isEmpty())
		{
			groupBy().setIgnorColon(ignorColon);
			sql.append(groupBy().getNameParameterSQL());
		}
		if(!this.orderBy().isEmpty())
		{
			orderBy().setIgnorColon(ignorColon);
			sql.append(orderBy().getNameParameterSQL());
		}
		this.endParamNameSQL();
		return sql.toString();
	}

	
	@Override
	public Map<String, Object> getNameParameters() {
		HashMap<String, Object> ps=new HashMap<String, Object>();
		if(this.isEmpty()) {
			return ps;
		}
		this.beginParamNameSQL();
		for(int i=0;i<this.fields.size();i++)
		{
			fields.get(i).setIgnorColon(ignorColon);
			ps.putAll(this.fields.get(i).getNameParameters());
		}
		for(int i=0;i<this.tables.size();i++)
		{
			tables.get(i).setIgnorColon(ignorColon);
			ps.putAll(this.tables.get(i).getNameParameters());
		}
		if(!this.where().isEmpty())
		{
			where().setIgnorColon(ignorColon);
			ps.putAll(this.where().getNameParameters());
		}
		if(!this.groupBy().isEmpty())
		{
			groupBy().setIgnorColon(ignorColon);
			ps.putAll(this.groupBy().getNameParameters());
		}
		if(!this.orderBy().isEmpty())
		{
			orderBy().setIgnorColon(ignorColon);
			ps.putAll(this.orderBy().getNameParameters());
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
 
}
