package com.github.foxnic.dao.meta;

import com.github.foxnic.commons.lang.ArrayUtil;

public class DBIndexMeta {

	public String table;
	public String name;
	private boolean unique;
	public boolean isPrimary() {
		return primary;
	}



	private boolean primary;
	private String[] fields;


	public String getTable() {
		return table;
	}

	public String getName() {
		return name;
	}

	public boolean isUnique() {
		return unique;
	}

	public String[] getFields() {
		return fields.clone();
	}

	public int getColumnOrder(String column)
	{
		return ArrayUtil.indexOf(this.fields, column, true);
	}

	public DBIndexMeta(String name,String table,boolean primary,boolean unique,String[] fields)
	{
		this.table=table;
		this.name=name;
		this.primary=primary;
		this.unique=unique;
		this.fields=fields;
	}

}
