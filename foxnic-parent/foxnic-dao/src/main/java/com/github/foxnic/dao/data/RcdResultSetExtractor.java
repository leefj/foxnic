package com.github.foxnic.dao.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapperResultSetExtractor;

/**
 * ResultSet 数据提取器
 * @author fangjieli
 *
 */
public class RcdResultSetExtractor extends  RowMapperResultSetExtractor {

	private RcdRowMapper rowmap=null;
	public RcdResultSetExtractor(RcdRowMapper map) {
		super(map);
		this.rowmap=map;
	}
	 
	
	@Override
	public List<RcdRowMapper> extractData(ResultSet rs) throws SQLException {
		this.rowmap.begin(rs,0);
		List<RcdRowMapper> r=super.extractData(rs);
		this.rowmap.done();
		return r;
	}

	 

}
