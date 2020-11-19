package com.github.foxnic.dao.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.foxnic.dao.excel.DataException;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.dao.spring.Db2DAO;

/**
 * 记录 RowMapper 
 * @author fangjieli
 *
 */
public class RcdRowMapper implements RowMapper<Rcd>
{

	private RcdSet ownerSet;
	private int limit=-1;
	private int begin=-1;
	public RcdRowMapper(RcdSet set,int begin,int limit)
	{
		ownerSet = set;
		this.limit=limit;
		this.begin=begin;
	}
	
	public void begin(ResultSet rs, int row) throws SQLException {
		ownerSet.flagTimePoint();
		ownerSet.initeMetaData(rs.getMetaData());
		if(this.begin>0) {
			if(  DAO.getInstance(this.ownerSet) instanceof Db2DAO)
			{
				rs.first();
				rs.relative(begin-1);
			}
		}
		this.columnCount=ownerSet.getMetaData().getColumnCount();
	}
	
	private int columnCount=0;

	@Override
	public Rcd mapRow(ResultSet rs, int row) throws SQLException
	{
		if(limit>0 && ownerSet.size()>limit)
		{
			throw new DataException("查询结果行数超过 queryLimit 限制，当前限制为 "+limit);
		}
		
		Rcd r = new Rcd(ownerSet);
		for(int i=1;i<=columnCount;i++)
		{
			r.setValueInternal(i-1, rs.getObject(i));
		}
		r.setNextSaveAction(SaveAction.UPDATE);
		ownerSet.add(r);
		r.clearDitryFields();
		return r;
	}

	public void done() {
		ownerSet.flagTimePoint();
	}

	

	 

}
