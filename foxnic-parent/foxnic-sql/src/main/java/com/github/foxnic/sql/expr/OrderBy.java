package com.github.foxnic.sql.expr;

import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.meta.DBField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * OrderBy子句
 * @author fangjieli
 *
 */
public class OrderBy<T extends OrderBy> extends SubSQL
{

	public static OrderBy byAscNullsLast(String expr,Object... params)
	{
		OrderBy ob=new OrderBy();
		ob.ascNL(expr, params);
		return ob;
	}

	public static OrderBy byDescNullsLast(String expr,Object... params)
	{
		OrderBy ob=new OrderBy();
		ob.descNL(expr, params);
		return ob;
	}

	public static OrderBy byAsc(DBField field)
	{
		return byAsc(field.name());
	}

	public static OrderBy byAsc(String expr,Object... params)
	{
		OrderBy ob=new OrderBy();
		ob.asc(expr, params);
		return ob;
	}

	public static OrderBy byDesc(DBField field)
	{
		return byDesc(field.name());
	}

	public static OrderBy byDesc(String expr,Object... params)
	{
		OrderBy ob=new OrderBy();
		ob.desc(expr, params);
		return ob;
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 5256268143580031683L;

	private ArrayList<Expr> ses=new ArrayList<Expr>();

	private ArrayList<SQLKeyword> sortTypes=new ArrayList<SQLKeyword>();

	private ArrayList<SQLKeyword> nulls=new ArrayList<SQLKeyword>();


	public T by(Expr se,SQLKeyword sortType,SQLKeyword nulls)
	{
		ses.add(se);
		se.setParent(this);
		sortTypes.add(sortType);
		this.nulls.add(nulls);
		return (T)this;
	}


	public T asc(Expr se)
	{
		 return by(se,SQLKeyword.ASC,null);
	}

	public T desc(Expr se)
	{
		return by(se,SQLKeyword.DESC,null);
	}

	public T ascNL(Expr se)
	{
		return by(se,SQLKeyword.ASC,SQLKeyword.LAST);
	}

	public T descNL(Expr se)
	{
		return by(se,SQLKeyword.DESC,SQLKeyword.LAST);
	}

	public T ascNF(Expr se)
	{
		return by(se,SQLKeyword.ASC,SQLKeyword.FIRST);
	}

	public T descNF(Expr se)
	{
		return by(se,SQLKeyword.DESC,SQLKeyword.FIRST);
	}

	public T asc(String se,Object... ps)
	{
		return asc(Expr.create(se,ps));
	}

	public T desc(String se,Object... ps)
	{
		 return desc(Expr.create(se,ps));
	}

	public T ascNL(String se,Object... ps)
	{
		 return ascNL(Expr.create(se,ps));
	}

	public T descNL(String se,Object... ps)
	{
		 return descNL(Expr.create(se,ps));
	}

	public T ascNF(String se,Object... ps)
	{
		 return ascNF(Expr.create(se,ps));
	}

	public T descNF(String se,Object... ps)
	{
		 return descNF(Expr.create(se,ps));
	}


	@Override
	public String getSQL(SQLDialect dialect) {
		if(this.isEmpty()) {
			return "";
		}
		SQLStringBuilder sql = new SQLStringBuilder();
		sql.append(SQLKeyword.ORDER,SQLKeyword.ORDER$BY);
		for(int i=0;i<ses.size();i++) {
			ses.get(i).setIgnorColon(ignorColon);
			if(this.getSQLDialect()==SQLDialect.PLSQL || this.getSQLDialect()==SQLDialect.PSQL || this.getSQLDialect()==SQLDialect.DMSQL) {
				sql.append(ses.get(i).getSQL(dialect),sortTypes.get(i).toString(),(nulls.get(i)==null?"":SQLKeyword.NULLS.toString()+SQLKeyword.SPACER.toString()+nulls.get(i).toString()));
			} else if(this.getSQLDialect()==SQLDialect.MySQL) {
				SQLKeyword nul=nulls.get(i);
				if(nul==SQLKeyword.LAST) {
					sql.append("ifnull(", ses.get(i).getSQL(dialect),",1) -1 asc," , ses.get(i).getSQL(dialect),sortTypes.get(i).toString()  );
				} else {
					 //MySQL 默认就是 NULLS FIRST 模式
					sql.append(ses.get(i).getSQL(dialect),sortTypes.get(i).toString());
				}
			}
			if(i<ses.size()-1) {
				sql.append(SQLKeyword.COMMA);
			}
		}
		return sql.toString();
	}


	@Override
	public String getListParameterSQL() {
		if(this.isEmpty()) {
			return "";
		}
		SQLStringBuilder sql = new SQLStringBuilder();
		sql.append(SQLKeyword.ORDER,SQLKeyword.ORDER$BY);
		for(int i=0;i<ses.size();i++) {
			ses.get(i).setIgnorColon(ignorColon);
			if(this.getSQLDialect()==SQLDialect.PLSQL || this.getSQLDialect()==SQLDialect.PSQL || this.getSQLDialect()==SQLDialect.DMSQL) {
				sql.append(ses.get(i).getListParameterSQL(),sortTypes.get(i).toString(),(nulls.get(i)==null?"":SQLKeyword.NULLS.toString()+SQLKeyword.SPACER.toString()+nulls.get(i).toString()));
			} else if(this.getSQLDialect()==SQLDialect.MySQL) {
				SQLKeyword nul=nulls.get(i);
				if(nul==SQLKeyword.LAST) {
					sql.append("ifnull(", ses.get(i).getListParameterSQL(),",1) -1 "+sortTypes.get(i).toString()+"," , ses.get(i).getListParameterSQL(),sortTypes.get(i).toString());
				} else {
					//MySQL 默认就是 NULLS FIRST 模式
					sql.append(ses.get(i).getListParameterSQL(),sortTypes.get(i).toString());
				}
			}
			if(i<ses.size()-1) {
				sql.append(SQLKeyword.COMMA);
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
		this.beginParamNameSQL();
		SQLStringBuilder sql = new SQLStringBuilder();
		sql.append(SQLKeyword.ORDER,SQLKeyword.ORDER$BY);
		for(int i=0;i<ses.size();i++) {
			ses.get(i).setIgnorColon(ignorColon);
			if(this.getSQLDialect()==SQLDialect.PLSQL || this.getSQLDialect()==SQLDialect.PSQL || this.getSQLDialect()==SQLDialect.DMSQL) {
				sql.append(ses.get(i).getNamedParameterSQL(),sortTypes.get(i).toString(),(nulls.get(i)==null?"":SQLKeyword.NULLS.toString()+SQLKeyword.SPACER.toString()+nulls.get(i).toString()));
			} else if(this.getSQLDialect()==SQLDialect.MySQL) {
				SQLKeyword nul=nulls.get(i);
				if(nul==SQLKeyword.LAST) {
					sql.append("ifnull(", ses.get(i).getListParameterSQL(),",1) -1 asc," , ses.get(i).getListParameterSQL(),sortTypes.get(i).toString());
				} else {
					 //MySQL 默认就是 NULLS FIRST 模式
					sql.append(ses.get(i).getListParameterSQL(),sortTypes.get(i).toString());
				}
			}
			if(i<ses.size()-1) {
				sql.append(SQLKeyword.COMMA);
			}
		}
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
	 * 排序字段数为0 , 判定为 empty
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

		for(int i=0;i<ses.size();i++)
		{
			ses.get(i).setIgnorColon(ignorColon);
			if(!ses.get(i).isAllParamsEmpty()) {
				return false;
			}
		}

		return true;
	}


//	@Override
//	public boolean isAllParamsEmpty(boolean isCE)
//	{
//		return isAllParamsEmpty();
//	}
}
