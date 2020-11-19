package com.github.foxnic.dao.meta;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.DbType;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.meta.builder.DB2MetaAdaptor;
import com.github.foxnic.dao.meta.builder.DBMetaAdaptor;
import com.github.foxnic.dao.meta.builder.MySQLMetaAdaptor;
import com.github.foxnic.dao.meta.builder.OracleMetaAdaptor;
import com.github.foxnic.dao.sql.tablefinder.DB2TableNameFinder;
import com.github.foxnic.dao.sql.tablefinder.ITableNameFinder;
import com.github.foxnic.dao.sql.tablefinder.MySQLTableNameFinder;
import com.github.foxnic.dao.sql.tablefinder.OracleTableNameFinder;
import com.github.foxnic.dao.sql.tablefinder.SQLServerTableNameFinder;
import com.github.foxnic.dao.sql.tablefinder.SQLiteTableNameFinder;
import com.github.foxnic.sql.dialect.SQLDialect;
import com.github.foxnic.sql.meta.DBType;

/**
 * 仅内部使用
 * 
 * */
public class DBMapping {
	
	private static class Item {
		
		private SQLDialect dialect;
		private DBType dbType;
		private DbType druidDbType;
		private Class<? extends ITableNameFinder> finderType;
		private DBMetaAdaptor dbMetaAdaptor;
		
		private Item(SQLDialect dialect,DBType dbType,DbType druidDbType,Class<? extends ITableNameFinder> finderType,DBMetaAdaptor dbMetaAdaptor) {
			this.dialect=dialect;
			this.dbType=dbType;
			this.druidDbType=druidDbType;
			this.finderType=finderType;
			this.dbMetaAdaptor=dbMetaAdaptor;
		}
		
	}
	
	private static List<Item> ITEMS=null;
	
	private static Map<String,Item> MAP=null;
	
	static {
		ITEMS=new ArrayList<DBMapping.Item>();
		ITEMS.add(new Item(SQLDialect.PLSQL,DBType.ORACLE,DbType.oracle , OracleTableNameFinder.class,new OracleMetaAdaptor()));
		ITEMS.add(new Item(SQLDialect.MySQL,DBType.MYSQL,DbType.mysql,MySQLTableNameFinder.class,new MySQLMetaAdaptor()));
		ITEMS.add(new Item(SQLDialect.DB2,DBType.DB2,DbType.db2,DB2TableNameFinder.class,new DB2MetaAdaptor()));
		ITEMS.add(new Item(SQLDialect.TSQL,DBType.SQLSVR,DbType.sqlserver,SQLServerTableNameFinder.class,null));
		ITEMS.add(new Item(SQLDialect.SQLite,DBType.SQLLIT,DbType.sqlite,SQLiteTableNameFinder.class,null));
		MAP=new HashMap<>();
		//
		for (Item item : ITEMS) {
			MAP.put("SQLDialect:"+item.dialect.name(), item);
		}
		for (Item item : ITEMS) {
			MAP.put("DbType:"+item.druidDbType.name(), item);
		}
		for (Item item : ITEMS) {
			MAP.put("DBType:"+item.dbType.name(), item);
		}
	}
 
	public static DbType getDruidDBType(SQLDialect dialect) {
		return MAP.get("SQLDialect:"+dialect.name()).druidDbType;
	}
	
	
	public static DBMetaAdaptor getDBMetaAdaptor(SQLDialect dialect) {
		try {
			return MAP.get("SQLDialect:"+dialect.name()).dbMetaAdaptor;
		} catch (Exception e) {
			Logger.error("create ITableNameFinder Error",e);
			return null;
		}
	}
	
	public static DbType getDruidDBType(DBType dbType) {
		return MAP.get("DBType:"+dbType.name()).druidDbType;
	}
 
	public static ITableNameFinder getDruidDBType(DbType dbType) {
		StringWriter out = new StringWriter();
		try {
			return MAP.get("DbType:"+dbType.name()).finderType.getConstructor(Appendable.class).newInstance(out);
		} catch (Exception e) {
			Logger.error("create ITableNameFinder Error",e);
			return null;
		}
	}
 
	
	

}
