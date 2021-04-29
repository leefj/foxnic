package com.github.foxnic.dao.sql.loader;

class TQL {
	
	public TQL(String id,String sql,String location)
	{
		this.id=id;
		this.sql=sql;
		this.location=location;
	}
	
	private String id;
	private String sql;
	private String location;
	
	
	private long calls=0;
 
	public void increaseCalls()
	{
		calls++;
	}
	
	/**
	 * 调用次数
	 * */
	public long getCalls() {
		return calls;
	}

	public String getId() {
		return id;
	}
	
	public String getLocation() {
		return location;
	}

	public String getSql() {
		return sql;
	}
	
	
	
}
