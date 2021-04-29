package com.github.foxnic.dao.data;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.foxnic.dao.excel.DataException;

/**
 * @author 李方捷
 * */
public class DataRowMapper implements RowMapper<Object>
{
	
	
	
	private DataSet ownerSet;
	private int limit=-1;
	
	public DataRowMapper(DataSet set,int limit)
	{
		ownerSet = set;
		this.limit=limit;
	}
	
	public void begin(ResultSet rs, int row) throws SQLException {
		ownerSet.flagTimePoint();
		ownerSet.initeMetaData(rs.getMetaData());
		this.columnCount=ownerSet.getMetaData().getColumnCount();
		 
	}

	private int columnCount=0;
	private String columnType=null;
	
	
	private Object objectValue=null;
	private String stringValue=null;
	private Date dateValue=null;
	private double doubleRawValue=0.0;
	private int row;
	@Override
	public Object mapRow(ResultSet rs, int row) throws SQLException
	{
 
		if(limit>0 && ownerSet.size()>limit)
		{
			throw new DataException("查询结果行数超过 queryLimit 限制，当前限制为 "+limit);
		}
		
		this.row=row;
		for(int i=1;i<=columnCount;i++)
		{
			columnType=ownerSet.getMetaData().getColumnClassName(i-1);
			switch (columnType) {
			case DataSet.TYPE_BIGDECIMAL:
				doubleRawValue=rs.getDouble(i); 
				//如果报null异常,则使用 Double.NaN 值,改分支未覆盖
				ownerSet.addValueInternal(row,i-1,doubleRawValue);
				break;
			case DataSet.TYPE_TIMESTAMP:
				dateValue=rs.getDate(i);
				ownerSet.addValueInternal(row,i-1,dateValue);
				break;
			case DataSet.TYPE_STRING:
				stringValue=rs.getString(i);
				ownerSet.addValueInternal(row,i-1,stringValue);
				break;
			default:
				objectValue=rs.getObject(i);
				ownerSet.addValueInternal(row,i-1,objectValue);
				break;
			}
			 
			
		}
 
		return null;
	}

	public void done() {
		ownerSet.setRealSizeInternal(row);
		ownerSet.flagTimePoint();
	}

	 

}
