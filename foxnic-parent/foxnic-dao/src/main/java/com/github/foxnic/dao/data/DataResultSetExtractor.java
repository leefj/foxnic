package com.github.foxnic.dao.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;

/**
 * @author 李方捷
 * */
public class DataResultSetExtractor extends  RowMapperResultSetExtractor {

	private DataRowMapper rowmap=null;
	public DataResultSetExtractor(DataRowMapper map) {
		super(map);
		this.rowmap=map;
	}
	 
	
	@Override
	public List<DataRowMapper> extractData(ResultSet rs) throws SQLException {
		this.rowmap.begin(rs,0);
		List<DataRowMapper> r=super.extractData(rs);
		this.rowmap.done();
		return r;
	}

	 

}
