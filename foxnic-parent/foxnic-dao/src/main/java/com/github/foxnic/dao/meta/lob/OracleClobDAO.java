package com.github.foxnic.dao.meta.lob;

import java.io.Writer;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.sql.DataSource;

import com.github.foxnic.sql.expr.Insert;
import com.github.foxnic.sql.expr.Where;

/**
 * @author 李方捷
 * */
public class OracleClobDAO implements IClobDAO {


	private DataSource db = null;
	private Class  clobClass=null;

	public OracleClobDAO(DataSource db) {
		this.db=db;
		try {
			clobClass=Class.forName("oracle.sql.CLOB");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insert(String table, String clobField,String content,HashMap<String,Object> otherfields,String[] pkFields) throws SQLException
	{
		Connection conn = null;


		conn = db.getConnection();

		PreparedStatement pstmt = null;

		Insert ins=new Insert(table);
		ins.setExpr(clobField, "empty_clob()");
		for(String key:otherfields.keySet())
		{
			ins.set(key, otherfields.get(key));
		}

		int i=0;
		try {
			pstmt = conn.prepareStatement(ins.getSQL());
			i=pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e1) {
			try {
				conn.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			e1.printStackTrace();
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return;
		}

		if(i==0)
		{
			return;
		}


		Where where=new Where();
		for(String pk:pkFields)
		{
			where.and(pk+"=?",otherfields.get(pk));
		}

		update(conn, table, clobField, where, content);

		try {
			conn.commit();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

	}


	private void update(Connection conn,String table, String field, Where where, String content) {

		PreparedStatement pstmt = null;
		try {
			char[] contents = content.toCharArray();
			conn.setAutoCommit(false);
			String sql = "update " + table + " set " + field + "=empty_clob() "
					+ where.getSQL();
			pstmt = conn.prepareStatement(sql);
			int i1 = pstmt.executeUpdate();
			pstmt.close();
			if (i1 == 0) {
				return;
			}

			ResultSet rs = null;
			Clob clob = null;
			String sql1 = "select " + field + " from " + table + " "
					+ where.getSQL() + " for update";
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				clob =  rs.getClob(1);
			}

			if (clob == null)
			 {
				return;
			//
			}

			//Writer writer =    clob.getCharacterOutputStream();
			Writer writer  = (Writer)clobClass.getMethod("getCharacterOutputStream").invoke(clob);
			writer.write(contents);
			writer.flush();
			writer.close();
			//
			rs.close();
			pstmt.close();

		} catch (Exception e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void update(String table, String field, Where where, String content) throws SQLException {

		Connection conn = null;


		conn = db.getConnection();


		update(conn, table, field, where, content);

		try {
			conn.commit();
		} catch (SQLException e1) {
			e1.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}



	}

	@Override
	public String getText(String table, String field, Where where) {
		Connection conn=null;
		PreparedStatement pstmt=null;
		String content = "";
		try {

			conn = db.getConnection();
			conn.setAutoCommit(false);
			ResultSet rs = null;
			Clob clob = null;
			String sql = "select " + field + " from " + table + " "
					+ where.getSQL();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				clob = rs.getClob(1);
			}
			if (clob != null && clob.length() != 0) {
				content = clob.getSubString((long) 1, (int) clob.length());
			}
			rs.close();
			conn.commit();
			pstmt.close();
			//conn.close();
			return content;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		finally
		{
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		}
	}

}
