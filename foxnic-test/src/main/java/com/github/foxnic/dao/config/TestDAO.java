package com.github.foxnic.dao.config;

public interface TestDAO {

	public void setTableName(String normalTable,String pkTable,String clobTable,String allTypeTable);
	public String getNormalTableName();
	public String getPKTableName();
	public String getClobTableName();
	public void createTables();
	public void dropTables();
	
}
