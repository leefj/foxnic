package com.github.foxnic.dao.meta;

import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.meta.DBDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库元数据
 * @author fangjieli
 *
 */
public abstract class DBMetaData {
	
	/**
	 * 是否构建索引元数据
	 * */
	public static boolean BUILD_INDEX_META=false;
	
	private static  HashMap<String, DBTableMeta> TABLE_METADATAS=new HashMap<>();
	private static  HashMap<String, String[]> TABLES=new HashMap<String, String[]>();
	private static  HashMap<String, Map<Object, Rcd>> TABLES_INFOS=new HashMap<String, Map<Object, Rcd>>();
	
	/**
	 * 使Meta信息失效
	 * @param dao DAO
	 * */
	public static void invalid(DAO dao) {
		TABLE_METADATAS.remove(dao.getDBConnectionIdentity());
		TABLES.remove(dao.getDBConnectionIdentity());
		TABLES_INFOS.remove(dao.getDBConnectionIdentity());
	}
	
	/**
	 * 获得全部数据表的表名，如果数据库表有变动，需重启应用
	 * @param dao DAO
	 * @return 表名清单
	 * */
	public static String[] getAllTableNames(DAO dao)
	{
		String[] arr=TABLES.get(dao.getDBConnectionIdentity());
		if(arr!=null) {
			return arr;
		}
		//
		String schema=dao.getSchema();
		RcdSet rs=DBMapping.getDBMetaAdaptor(dao.getSQLDialect()).queryAllTableAndViews(dao, schema);
		 //
		for (Rcd r : rs) {
			r.setValue("TABLE_NAME", r.getString("TABLE_NAME").toLowerCase());
		}
		//
		arr= rs.getValueArray("TABLE_NAME",String.class);
		TABLES.put(dao.getDBConnectionIdentity(),arr);
		Map<Object, Rcd> infos=rs.getMappedRcds("TABLE_NAME");
		TABLES_INFOS.put(dao.getDBConnectionIdentity(),infos);
		return arr;
	}
	
	/**
	 * 获得表定义的相关信息
	 * @param dao DAO
	 * @param tableName 表名
	 * @return DBTableMeta
	 * */
	public static DBTableMeta getTableMetaData(DAO dao,String tableName)
	{
		String key=dao.getDBConnectionIdentity()+"."+tableName;
		key=key.toLowerCase();
		DBTableMeta meta=TABLE_METADATAS.get(key);
		if(meta!=null) {
			return meta;
		}
		return getTableMeta(key,dao,tableName);
	}
	
	private static synchronized DBTableMeta getTableMeta(String key,DAO dao,String tableName)
	{
		if(tableName==null) {
			System.out.println();
		}
		tableName=tableName.toLowerCase();
		String schema=dao.getSchema();
		if(TABLES.size()==0) {
			getAllTableNames(dao);
		}
 
		RcdSet rs=DBMapping.getDBMetaAdaptor(dao.getSQLDialect()).queryTableColumns(dao, schema, tableName);
		
		if(rs.size()==0) {
			return null;
		}
 
		if(TABLES.get(dao.getDBConnectionIdentity())==null)
		{
			getAllTableNames(dao);
			return getTableMeta(key, dao, tableName);
		}
 
		String tableComment=TABLES_INFOS.get(dao.getDBConnectionIdentity()).get(tableName).getString("TC");
		String tableType=TABLES_INFOS.get(dao.getDBConnectionIdentity()).get(tableName).getString("TABLE_TYPE");
		DBTableMeta meta=new DBTableMeta(tableName,tableComment,"VIEW".equals(tableType) );
		String table;
		String column;
		String dbType;
		DBDataType dbTypeCatagery;
		String comment;
		String keyType;
		Integer dataLength;
		Integer charLength;
		Integer precision;
		Integer scale;
		boolean autoIncrease;
		Boolean nullable;
		String defaultValue=null;
		for (Rcd r : rs) {
			
			table=r.getString("TABLE_NAME");
			table=table.toUpperCase();
			column=r.getString("COLUMN_NAME");
			dbType=r.getString("DATA_TYPE");
 
		
			comment=r.getString("COMMENTS");
			keyType=r.getString("KEY_TYPE");
			dataLength=r.getInteger("DATA_LENGTH");
			charLength=r.getInteger("CHAR_LENGTH");
 
			
			precision=r.getInteger("NUM_PRECISION");
			scale=r.getInteger("NUM_SCALE");
			
			dbTypeCatagery=DBDataType.parseFromDBInfo(table,column,dao.getDBTreaty(),dao.getDBType(),dbType,dataLength==null?-1:dataLength,precision,scale,comment);
			
			nullable=r.getBoolean("NULLABLE");
			
			autoIncrease=r.getBoolean("AUTO_INCREASE"); 
			
			defaultValue=r.getString("COLUMN_DEFAULT");
			
			DBColumnMeta clmn=new DBColumnMeta(dao.getDBType(),table,column,dataLength,charLength,"PRI".equalsIgnoreCase(keyType),dbType,dbTypeCatagery,comment,nullable,autoIncrease,precision,scale,defaultValue);
			meta.addColumn(clmn);
//			Logger.debug("db-column-meta-raw-["+table+"]"+r.toJSONObject());
//			Logger.debug("db-column-meta-["+table+"]"+JSON.toJSONString(clmn));

		}
 
		
		//建立索引的Meta
		if(BUILD_INDEX_META)
		{
			meta=buildIndex(dao, tableName, meta);
		}
		TABLE_METADATAS.put(key, meta);
		
		return meta;
		
	}
	
	
	private static DBTableMeta buildIndex(DAO dao, String tableName, DBTableMeta meta) {
 
		RcdSet rs = DBMapping.getDBMetaAdaptor(dao.getSQLDialect()).queryTableIndexs(dao, dao.getSchema(), tableName);
		
		Map<Object,List<Rcd>> group=rs.getGroupedMap("index_name");
	 
		for (Object indexName : group.keySet()) {
			List<Rcd> fields=group.get(indexName);
			ArrayList<String> fieldNames=new ArrayList<>();
			boolean primary=false;
			boolean unique=false;
			for (Rcd f : fields) {
				fieldNames.add(f.getString("column_name"));
				primary="PRIMARY KEY".equalsIgnoreCase (f.getString("CONSTRAINT_TYPE"));
				unique="UNIQUE".equalsIgnoreCase(f.getString("CONSTRAINT_TYPE"));
			}
			meta.addIndex(new DBIndexMeta(indexName+"", tableName, primary,primary || unique, fieldNames.toArray(new String[fieldNames.size()])));
		}
		
		return meta;
	}
  
	/**
	 * 获得对应表的列元数据
	 * @param dao DAO
	 * @param tableName 表名，不区分大小写
	 * @param column 列名，不区分大小写
	 * @return DBColumnMeta
	 * */
	public static DBColumnMeta getDBColumn(DAO dao,String tableName,String column)
	{
		DBTableMeta tableMeta=getTableMetaData(dao, tableName);
		if(tableMeta==null) {
			return null;
		}
		DBColumnMeta colm=tableMeta.getColumn(column);
		return colm;
	}
 
}
