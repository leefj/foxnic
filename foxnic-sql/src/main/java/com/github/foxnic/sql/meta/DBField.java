package com.github.foxnic.sql.meta;

public class DBField {
	
	private DBTable table;
	private String name;
	private String var;
	private String label;
	private String detail;
	
	public DBField(String name,String var,String label,String detail) {
		this.name=name;
		this.label=label;
		this.detail=detail;
		this.var=var;
	}
	
	public DBTable table() {
		return table;
	}
	
	void setTable(DBTable table) {
		this.table = table;
	}
	
	public String name() {
		return name;
	}
	public String label() {
		return label;
	}
	public String detail() {
		return detail;
	}

	/**
	 * 变量名
	 * */
	public String var() {
		return var;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
}
