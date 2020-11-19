package com.github.foxnic.dao.sql.tablefinder;

import java.util.List;

import com.alibaba.druid.sql.visitor.SQLASTVisitor;

/**
 * @author fangjieli
 * */
public interface ITableNameFinder extends SQLASTVisitor {
	public List<String> getAllTables();
}
