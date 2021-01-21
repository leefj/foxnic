package com.github.foxnic.dao.sql;

import com.alibaba.druid.DbType;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.meta.DBType;

public class DruidUtils {
	
	public static DbType getDbType(DBType dbType) {
		return DbType.valueOf(dbType.getDruidDbType());
	}
	
	public static DbType getDbType(SQLDialect dialect) {
		return DbType.valueOf(dialect.getDBType().getDruidDbType());
	}

}
