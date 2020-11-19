package com.github.foxnic.sql.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.github.foxnic.sql.dialect.SQLDialect;

/**
 * group by 子句
 * @author fangjieli
 *
 */
public class GroupBy extends SubSQL
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5301937897600459098L;

	private ArrayList<Expr> ses=new ArrayList<Expr>();
	
	private Having having=new Having();
	
	public GroupBy()
	{
		this.having.setParent(this);
	}
	
	
	public Having having()
	{
		return having;
	}
	
	public GroupBy bys(String... fld)
	{
		for (String f : fld) {
			this.by(f);
		}
		return this;
	}
	
	
	public GroupBy by(Expr se)
	{
		ses.add(se);
		se.setParent(this);
		return this;
	}
	
	public GroupBy by(String se,Object... ps)
	{
		return by(Expr.get(se,ps));
	}
	
	 
	
	
	
	
	@Override
	public String getSQL(SQLDialect dialect) {
		if(this.isEmpty()) {
			return "";
		}
		SQLStringBuilder sql = new SQLStringBuilder();
		sql.append(SQLKeyword.GROUP,SQLKeyword.GROUP$BY);
		for(int i=0;i<ses.size();i++) {
			ses.get(i).setIgnorColon(ignorColon);
			sql.append(ses.get(i).getSQL(dialect));
			if(i<ses.size()-1) {
				sql.append(SQLKeyword.COMMA);
			}
		}
		if(!having().isEmpty()) {
			having().setIgnorColon(ignorColon);
			sql.append( having().getSQL(dialect) );
		}
		return sql.toString();
	}

	/**
	 * 返回顶层语句
	 * */
	@Override
	public Select top()
	{
		return (Select)super.top();
	}
	
	@Override
	public String getListParameterSQL() {
		if(this.isEmpty()) {
			return "";
		}
		SQLStringBuilder sql = new SQLStringBuilder();
		sql.append(SQLKeyword.GROUP,SQLKeyword.GROUP$BY);
		for(int i=0;i<ses.size();i++) {
			ses.get(i).setIgnorColon(ignorColon);
			sql.append(ses.get(i).getListParameterSQL());
			if(i<ses.size()-1) {
				sql.append(SQLKeyword.COMMA);
			}
		}
		if(!having().isEmpty())
		{
			having().setIgnorColon(ignorColon);
			sql.append(having().getListParameterSQL() );
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
		if(!this.having().isEmpty())
		{
			having().setIgnorColon(ignorColon);
			ps.addAll(Utils.toList(this.having().getListParameters()));
		}
		return ps.toArray(new Object[ps.size()]);
	}

	
	@Override
	public String getNameParameterSQL() {
		if(this.isEmpty()) {
			return "";
		}
		this.beginParamNameSQL();
		SQLStringBuilder sql = new SQLStringBuilder();
		sql.append(SQLKeyword.GROUP,SQLKeyword.GROUP$BY);
		for(int i=0;i<ses.size();i++) {
			ses.get(i).setIgnorColon(ignorColon);
			sql.append(ses.get(i).getNameParameterSQL());
			if(i<ses.size()-1) {
				sql.append(SQLKeyword.COMMA);
			}
		}
		if(!having().isEmpty())
		{
			having().setIgnorColon(ignorColon);
			sql.append(having().getNameParameterSQL());
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
		for(int i=0;i<ses.size();i++)
		{
			ses.get(i).setIgnorColon(ignorColon);
			ps.putAll(ses.get(i).getNameParameters());
		}
		if(!this.having().isEmpty())
		{
			having().setIgnorColon(ignorColon);
			ps.putAll(this.having().getNameParameters());
		}
		this.endParamNameSQL();
		return ps;
	}

	/**
	 * 判断是否为空
	 * 分组字段个数为0 , 判定为 empty
	 * @return 是否空
	 * */
	@Override
	public boolean isEmpty() {
		return ses.size()==0;
	}

	
	@Override
	public Select parent()
	{
		return (Select)super.parent();
	}


	
	@Override
	public boolean isAllParamsEmpty() {
 
		 
		for(int i=0; i<ses.size();i++)
		{
			ses.get(i).setIgnorColon(ignorColon);
			if(!ses.get(i).isAllParamsEmpty()) {
				return false;
			}
		}
 
		if(!this.having().isAllParamsEmpty()) {
			return false;
		}
 
		return true;
	}
 
}
