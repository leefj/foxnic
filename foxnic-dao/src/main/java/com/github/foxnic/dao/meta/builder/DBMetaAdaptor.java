package com.github.foxnic.dao.meta.builder;

import com.github.foxnic.sql.dao.DAO;
import com.github.foxnic.sql.data.AbstractRcdSet;

public abstract class DBMetaAdaptor {
	
	/**
	 * 查询所有表与视图
	 * */
	public abstract AbstractRcdSet queryAllTableAndViews(DAO dao,String schema);
	
	
	/**
	 * 查询表或视图的列 
	 * */
	public abstract AbstractRcdSet queryTableColumns(DAO dao,String schema,String tableName);
	
	/**
	 * 查询表或视图的列
	 * */
	public abstract AbstractRcdSet queryTableIndexs(DAO dao,String schema,String tableName);
	
	
}
