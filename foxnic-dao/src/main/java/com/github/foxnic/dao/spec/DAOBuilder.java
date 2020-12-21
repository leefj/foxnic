package com.github.foxnic.dao.spec;

import java.sql.DatabaseMetaData;

import javax.sql.DataSource;

import com.github.foxnic.dao.spring.MySqlDAO;
import com.github.foxnic.dao.spring.OracleDAO;

public class DAOBuilder {

	private DataSource datasource;

	/**
	 * 设置数据源
	 * */
	public DAOBuilder datasource(DataSource ds) {
		this.datasource = ds;
		return this;
	}
	
	/**
	 * 设置TQL扫描范围
	 * */
	public DAOBuilder scanRange(String packages) {
		return this;
	}

	public DAO build() throws Exception {

		try {
			DatabaseMetaData dm = this.datasource.getConnection().getMetaData();
			String productName = dm.getDatabaseProductName().toLowerCase();
			DAO dao = null;
			if (productName.contains("oracle")) {
				dao = new OracleDAO();
			} else if (productName.contains("mysql")) {
				dao = new MySqlDAO();
			}
			if (dao == null) {
				throw new Exception("not support db type : " + productName);
			}
			dao.setDataSource(this.datasource);
			return dao;
		} catch (Exception e) {
			throw e;
		}
	}
	
	
	
	
}
