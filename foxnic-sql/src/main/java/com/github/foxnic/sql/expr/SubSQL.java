package com.github.foxnic.sql.expr;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.GlobalSettings;
import com.github.foxnic.sql.dialect.SQLDialect;

/**
 * @author fangjieli
 * */
public abstract class SubSQL implements SQL {
 
	
	private SQL parent=null;
	private int nameIndex=0;
	
	protected boolean ignorColon=false;
	
	/**
	 * IgnorColon 是否忽略冒号
	 * */
	@Override
	public SubSQL setIgnorColon(boolean b)
	{
		ignorColon=b;
		return this;
	}
	
	
	/**
	 * 是否替换空值
	 * */
	protected boolean replaceNull=false;

	public void setReplaceNull(boolean replaceNull) {
		this.replaceNull = replaceNull;
	}
	 
	 
//	@Override
//	public boolean isAllParamsEmpty()
//	{
//		return isAllParamsEmpty(false);
//	}
	

	@Override
	public SQL parent()
	{
		return parent;
	}
	
	
	@Override
	public String toString()
	{
		String sql=null;
		try {
			sql = this.getSQL();
		} catch (Exception e) {
			sql=StringUtil.toString(e);
		}
		if(StringUtil.isBlank(sql)) {
			return "SQL Is Empty"; 
		}
		return sql;
	}

	@Override
	public void setParent(SQL sql)
	{
		 this.parent=sql;
	}

	private SQL currentTop;
	@Override
	public void beginParamNameSQL()
	{
		currentTop=this.top();
		if(!this.equals(currentTop)) {
			return;
		}
		nameIndex=0;
	}

	
	 
	@Override
	public String getNextParamName(boolean withColon)
	{
		if(!this.equals(currentTop)) {
			return currentTop.getNextParamName(withColon);
		} else
		{
			nameIndex++;
			return (withColon?SQLKeyword.COLON:"")+PNAME_PREFIX+"_"+nameIndex;
		}
	}
 
	@Override
	public void endParamNameSQL()
	{
		if(!this.equals(currentTop)) {
			return;
		}
		currentTop=null;
	}
	
	 
	@Override
	public SQL top()
	{
		SQL se=this;
		while(se.parent()!=null) {
			se=se.parent();
		}
		return se;
	}
 
	private SQLDialect dialect=null;
	
	@Override
	public SQLDialect getSQLDialect()
	{
		
		if(dialect==null)
		{
			if(this.parent==null)
			{
				return GlobalSettings.DEFAULT_SQL_DIALECT;
			}
			else
			{
				return this.parent.getSQLDialect();
			}
		}
		return dialect;
	}
	
	@Override
	public void setSQLDialect(SQLDialect dialect)
	{
		this.dialect=dialect;
	}
	
	@Override
	public String getSQL() {
		return getSQL(this.getSQLDialect());
	}
	
	@Override
	public String getNamedParameterSQL() {
		return this.getSQL();
	}
	
	@Override
	public String getListParameterSQL() {
		return this.getSQL();
	}
	
}
