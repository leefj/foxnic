package com.github.foxnic.dao.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.meta.DBDataType;

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
			
			dbTypeCatagery=DBDataType.parseFromDBInfo(table,column,dao.getDBTreaty(),dao.getDBType(),dbType,dataLength==null?-1:dataLength,precision,scale);
			
			nullable=r.getBoolean("NULLABLE");
			
			autoIncrease=r.getBoolean("AUTO_INCREASE"); 
			
			defaultValue=r.getString("COLUMN_DEFAULT");
			
			DBColumnMeta clmn=new DBColumnMeta(table,column,dataLength,charLength,"PRI".equalsIgnoreCase(keyType),dbType,dbTypeCatagery,comment,nullable,autoIncrease,precision,scale,defaultValue);
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
//		RcdSet rs;
//		SE  se=null;
//		if(dao instanceof MySqlDAO)
//		{
//			se=new SE("SELECT a.table_name,a.non_unique,a.index_name,a.seq_in_index sort,a.column_name,ifnull(b.CONSTRAINT_TYPE,'NORMAL') CONSTRAINT_TYPE FROM information_schema.STATISTICS a " + 
//					"left join information_schema.TABLE_CONSTRAINTS b on a.Table_schema=b.Table_schema and a.table_name=b.table_name and a.index_name=b.CONSTRAINT_NAME " + 
//					"where a.Table_schema in (?,upper(?))  and a.table_name in (?,upper(?))  " + 
//					"order by a.table_name,a.index_name,a.SEQ_IN_INDEX",dao.getSchema(),dao.getSchema(),tableName,tableName);
//			
//			//另外的一种方式查询索引
//			//SELECT * FROM mysql.innodb_index_stats a WHERE a.`database_name` = 'titydb' and a.table_name = 'qrtz_blob_triggers';
//		
//		}
//		else if(dao instanceof OracleDAO)
//		{
//			String[] sqls= {
//					"select i.index_name,i.table_owner,i.table_name,i.uniqueness non_unique,i.tablespace_name,c.column_name,c.column_position sort,c.column_length,i.uniqueness CONSTRAINT_TYPE",
//					" from user_indexes i, user_ind_columns c",
//					" where i.index_name = c.index_name and  table_owner in  (?,upper(?))  and i.table_name in  (?,upper(?))  ",
//					" order by index_name,COLUMN_POSITION asc"
//			};
//			se=new SE(dao.joinSQLs(sqls),dao.getSchema(),dao.getSchema(),tableName,tableName);
//		}
//		else if(dao instanceof Db2DAO)
//		{
//			String[] sqls= {
//					"SELECT  INDSCHEMA table_owner,INDNAME index_name ,TABSCHEMA table_owner,UNIQUERULE ,COLNAMES,'' CONSTRAINT_TYPE,'' COLUMN_NAME ",
//					" FROM syscat.indexes A WHERE TABSCHEMA in  (?,upper(?)) AND TABNAME in (?,upper(?))"
//			};
//			se=new SE(dao.joinSQLs(sqls),dao.getSchema(),dao.getSchema(),tableName,tableName);
//		}
//
//
//		rs=dao.query(se);
//		
//		// DB2 需要额外处理
//		if (dao instanceof Db2DAO) {
//			
//			ArrayList<Rcd> nrs=new ArrayList<>();
//			for (Rcd r : rs) {
//				
//				String uType=r.getString("UNIQUERULE"); //
//				if("P".equalsIgnoreCase(uType)) {
//					r.set("CONSTRAINT_TYPE", "PRIMARY KEY");
//				} else if("U".equalsIgnoreCase(uType)) {
//					r.set("CONSTRAINT_TYPE", "UNIQUE");
//				} else if("D".equalsIgnoreCase(uType)) {
//					r.set("CONSTRAINT_TYPE", "NON-UNIQUE");
//				}
//				String colstr=r.getString("COLNAMES");
//				colstr=StringUtil.removeFirst(colstr, "+");
//				String[] cols=colstr.split("\\+");
//				if(cols.length==1) {
//					r.set("COLUMN_NAME", cols[0]);
//				} else {
//					for (int i = 1; i < cols.length; i++) {
//						Rcd nr=r.clone();
//						nr.set("COLUMN_NAME", cols[i]);
//						nrs.add(nr);
//					}
//				}
//				
//			}
//			//
//			for (Rcd r : nrs) {
//				rs.add(r);
//			}
//		}
		
		RcdSet rs = DBMapping.getDBMetaAdaptor(dao.getSQLDialect()).queryTableIndexs(dao, dao.getSchema(), tableName);
		
		Map<Object,List<Rcd>> group=rs.getGroupedMap("index_name");
	 
		for (Object indexName : group.keySet()) {
			List<Rcd> fields=group.get(indexName);
			ArrayList<String> fieldNames=new ArrayList<>();
			boolean primary=false;
			boolean unique=false;
			for (Rcd f : fields) {
				fieldNames.add(f.getString("column_name"));
				primary="PRIMARY KEY".equals(f.getString("CONSTRAINT_TYPE"));
				unique="UNIQUE".equals(f.getString("CONSTRAINT_TYPE"));
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
