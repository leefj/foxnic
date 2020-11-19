package com.github.foxnic.dao.meta.builder;

import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.spec.DAO;

public abstract class DBMetaAdaptor {
	
	/**
	 * 查询所有表与视图
	 * */
	public abstract RcdSet queryAllTableAndViews(DAO dao,String schema);
	
	
	/**
	 * 查询表或视图的列
	 * */
	public abstract RcdSet queryTableColumns(DAO dao,String schema,String tableName);
	
	/**
	 * 查询表或视图的列
	 * */
	public abstract RcdSet queryTableIndexs(DAO dao,String schema,String tableName);
	
	
}
