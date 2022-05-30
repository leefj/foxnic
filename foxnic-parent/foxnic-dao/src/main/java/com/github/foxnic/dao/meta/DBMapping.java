package com.github.foxnic.dao.meta;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.DbType;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.meta.builder.*;
import com.github.foxnic.dao.sql.tablefinder.*;
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
		ITEMS.add(new Item(SQLDialect.SQLite,DBType.SQLITE,DbType.sqlite,SQLiteTableNameFinder.class,null));
		ITEMS.add(new Item(SQLDialect.PSQL,DBType.PG,DbType.postgresql,PGTableNameFinder.class,new PGMetaAdaptor()));
		ITEMS.add(new Item(SQLDialect.DMSQL,DBType.DM,DbType.dm , DmTableNameFinder.class,new DMMetaAdaptor()));
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
		try {
			return MAP.get("DbType:"+dbType.name()).finderType.newInstance();
		} catch (Exception e) {
			Logger.error("create ITableNameFinder Error",e);
			return null;
		}
	}




}
