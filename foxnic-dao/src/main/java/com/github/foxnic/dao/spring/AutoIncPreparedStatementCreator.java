package com.github.foxnic.dao.spring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;

import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.meta.DBType;

class AutoIncPreparedStatementCreator implements PreparedStatementCreator
{
	private Connection conn=null;
	private PreparedStatement stmt = null;
	private boolean reverseAutoCommit=false;
	private DAO dao=null;
	private Long autoAIKey=null;
	
	public Long getAutoAIKey() {
		return autoAIKey;
	}

	public AutoIncPreparedStatementCreator(DAO dao,Connection conn,String sql,Object[] params) throws SQLException
	{
		this.dao=dao;
		this.conn = conn;
		if(dao.getDBType()==DBType.DB2) {
			this.stmt = conn.prepareStatement(sql);
		} else {
			this.stmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
		}
		
		ArgumentPreparedStatementSetter setter=new ArgumentPreparedStatementSetter(params);
		setter.setValues(stmt);
		
//		for (int i = 0; i < params.length; i++) {
//           	setParameter(i+1,params[i],this.stmt);
//		}

		if(dao.getDBType()==DBType.DB2 && conn.getAutoCommit()) {
			conn.setAutoCommit(false);
		}
	}

	@Override
	public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
		return stmt;
	}
	
	public void close()
	{
		if(dao.getDBType()==DBType.DB2) {
			try {
				PreparedStatement stmt=conn.prepareStatement("values  identity_val_local()");
				ResultSet rs=stmt.executeQuery();
				if(rs.next()) {
					autoAIKey=DataParser.parseLong(rs.getObject(1));
				}
			} catch (SQLException e) {
				Logger.exception(e);
			}
			
			try {
				this.conn.commit();
			} catch (SQLException e1) {
				Logger.exception(e1);
			}
			try {
				if(reverseAutoCommit) {
					this.conn.setAutoCommit(false);
				}
			} catch (SQLException e1) {
				Logger.exception(e1);
			}
		}
		
		//
		if(stmt!=null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				Logger.exception(e);
			}
		}
		
		if(conn!=null) {
			try {
				conn.close();
			} catch (SQLException e) {
				Logger.exception(e);
			}
		}
	}
	
}