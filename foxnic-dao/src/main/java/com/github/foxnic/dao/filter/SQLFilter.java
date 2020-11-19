package com.github.foxnic.dao.filter;

import com.github.foxnic.dao.spec.DAO;

/**
 * SQL 过滤器
 * @author fangjieli
 * */
public abstract class SQLFilter {
	private String name;
	
	private DAO dao;
	
	public DAO getDAO() {
		return dao;
	}

	void setDAO(DAO dao) {
		this.dao = dao;
	}

	public String getName() {
		return name;
	}

	/**
	 * 优先级，数字越大越优先处理
	 * @return 优先级
	 * */
	public int getPriority() {
		return priority;
	}

	public SQLFilterChain getFilterList() {
		return chain;
	}

	void setChain(SQLFilterChain filterList) {
		this.chain = filterList;
	}

	private int priority=0;
	private SQLFilterChain chain=null;
	
	/**
	 * @param name 唯一名称，
	 * @param priority 优先级 0-9 ，数值越大越优先处理
	 * */
	public SQLFilter(String name,int priority)
	{
		this.name=name;
		this.priority=priority;
	}
	
 
	
	public SQLFilterObject doStatementFilter(SQLFilterObject sqlobj)
	{
		return sqlobj;
	}
	
	public SQLFilterObject doSelectFilter(SQLFilterObject sqlobj)
	{
		return sqlobj;
	}
	
	public SQLFilterObject doInsertFilter(SQLFilterObject sqlobj) {
		return sqlobj;
	}
	
	public  SQLFilterObject doUpdateFilter(SQLFilterObject sqlobj)
	{
		return sqlobj;
	}
	
	public  SQLFilterObject doDeleteFilter(SQLFilterObject sqlobj)
	{
		return sqlobj;
	}

}
