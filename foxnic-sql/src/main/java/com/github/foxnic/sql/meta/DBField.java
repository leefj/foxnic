package com.github.foxnic.sql.meta;

public class DBField {
	
	private DBTable table;
	private String name;
	private String var;
	private String label;
	private String detail;
	private String id;
	
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
		if(this.table!=null) {
			throw new IllegalArgumentException("请勿重复设置 table");
		}
		this.table = table;
		this.id=(this.table.name()+"."+this.name).toLowerCase();
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

	/**
	 * 表名与字段名的拼接，大写，形式如： SYS_USER.NAME
	 * */
	public String getId() {
		return id;
	}
	
	public boolean equals(DBField field) {
		return this.id.equals(field.id);
	}
	
}
