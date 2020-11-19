package com.github.foxnic.dao.data;

import java.io.Serializable;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import com.github.foxnic.commons.busi.id.IDGenerator;
import com.github.foxnic.dao.GlobalSettings;
import com.github.foxnic.dao.excel.ExcelColumn;
import com.github.foxnic.dao.excel.ExcelStructure;
import com.github.foxnic.sql.data.DataNameFormat;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;
import com.github.foxnic.sql.expr.SQL;

 

/**
 * @author 李方捷
 * */
public abstract class AbstractSet  implements Serializable {

	public static abstract class AbstractDataReader
	{
		public void applyRawMetaData(AbstractSet rs,QueryMetaData meta)
		{
			rs.setRawMetaData(meta);
		}
	}
	
	
	public static abstract class ExcelDataReader extends AbstractDataReader
	{
		public QueryMetaData applyMetaDataDetail(String sheetName, ExcelStructure es) {
			QueryMetaData meta = new QueryMetaData();
			ExcelColumn column = null;
			int columnCount = 0;
			if(es.getDataColumnEnd()<=0)
			{
				throw new IllegalArgumentException("请设置读取的列范围");
			}
			for (int i = es.getDataColumnBegin(); i <= es.getDataColumnEnd(); i++) {
				column = es.getColumn(i);
				if (column == null) {
					continue;
				}
				meta.addCatalogName("ExcelCatalog");
				meta.addColumnClassName(column.getClass().getName());
				meta.addColumnLabel(column.getField());
				meta.addColumnType(-1);
				meta.addColumnTypeName(column.getClass().getSimpleName());
				meta.addSchemaName("Excel");
				meta.addTableName(sheetName);
				meta.setMap(column.getField(), columnCount);
				columnCount++;
			}
			meta.setColumnCount(columnCount);
			return meta;
		}
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6468003432102467478L;

	
	
 
	private DataNameFormat dataNameFormat=null;
	
	/**
	 * 设置JSON的属性格式
	 * @return DataNameFormat
	 * */
	public DataNameFormat getDataNameFormat() {
		if(dataNameFormat==null)
		{
			return GlobalSettings.DEFAULT_DATA_NAME_FORMAT;
		}
		else
		{
			return dataNameFormat;
		}
	}

	/**
	 * 设置 DataNameFormat
	 * @param dataNameFormat 字段名格式规则
	 * */
	public void setDataNameFormat(DataNameFormat dataNameFormat) {
		this.dataNameFormat = dataNameFormat;
	}
	
	


	private String dbIdentity = null;
	
	private String id = null;

	/**
	 * 获得结果集ID,
	 * @return id
	 * */
	public String getId() {
		return id;
	}

	

	/**
	 * 设置数据库标识
	 * @param daoIdentity 数据库标识
	 * */
	public void setDBConnectionIdentity(String daoIdentity) {
		this.dbIdentity = daoIdentity;
		id=IDGenerator.getUUID();
	}

	/**
	 * 记录数量
	 * @return 记录数量
	 * */
	public abstract int size();

	
	private ArrayList<Long> timepoints=new ArrayList<Long>();
	
	/**
	 * 这里 getRawMetaData/setRawMetaData 是序列化的需要
	 * @return 返回原始元数据
	 * */
	protected abstract QueryMetaData getRawMetaData();
	/**
	 * 设置原始元数据
	 * @param meta 原始元数据
	 * */
	protected abstract void setRawMetaData(QueryMetaData meta);
	 
	/**
	 * 做一个时间标记
	 * */
	public void flagTimePoint()
	{
		QueryMetaData metaData = this.getRawMetaData();
		timepoints.add(System.currentTimeMillis());
		long t=0;
		if(timepoints.size()==2)
		{
			t=timepoints.get(1)-timepoints.get(0);
			if(metaData!=null)
			{
				metaData.setSqlTime(t);
			}
		}
		if(timepoints.size()==3)
		{
			t=timepoints.get(2)-timepoints.get(1);
			if(metaData!=null)
			{
				metaData.setDataTime(t);
			}
			
//			Logger.info("查询耗时:"+metaData.getSqlTime()+"ms ;数据处理耗时:"+metaData.getDataTime()+"ms ;总耗时:"+metaData.getQueryTime()+"ms ,总行数:"+this.size());
			
		}
	}
	
	/**
	 * 设置分页查询语句
	 * @param pagedSQL 分页查询语句
	 * */
	public void setPagedSQL(SQL pagedSQL)
	{
		QueryMetaData metaData = this.getRawMetaData();
		if (metaData != null && metaData.getPagedSQL()==null) {
			metaData.setPagedSQL(pagedSQL);
		}
	}
 
	/**
	 * 设置数据时间
	 * @param dataTime 数据时间
	 * */
	public void setDataTime(long dataTime)
	{
		QueryMetaData metaData = this.getRawMetaData();
		if (metaData != null && metaData.getSqlTime()==0) {
			metaData.setDataTime(dataTime);
		}
	}
	
	/**
	 * 设置分页信息
	 * @param pageSize 分页大小
	 * @param pageIndex 页码
	 * @param totalRowCount 总行数
	 * @param pageCount 总页数
	 * @param sql sql语句
	 * */
	public void setPageInfos(int pageSize, int pageIndex, int totalRowCount, int pageCount, SQL sql)
	{
		QueryMetaData metaData = this.getRawMetaData();
		if (metaData != null && metaData.getSQL()==null) {
			metaData.setSQL(sql);
		}
		this.pageCount = pageCount;
		this.pageSize = pageSize;
		this.pageIndex = pageIndex;
		this.totalRowCount = totalRowCount;
	}
	
	/**
	 * 元数据初始化是否完成
	 * @return 逻辑值
	 * */
	public boolean isMetaDataInited()
	{
		QueryMetaData metaData = this.getRawMetaData();
		return metaData != null;
	}
	
	/**
	 * 取得查询的元数据
	 * @return QueryMetaData
	 * */
	public QueryMetaData getMetaData()
	{
		QueryMetaData metaData = this.getRawMetaData();
		if(!isMetaDataInited())
		{
			try
			{
				initeMetaData(null);
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		
		return metaData;
	}
	
	private int pageSize = 0;
	private int pageIndex = 0;
	private int pageCount = 0;
	private int totalRowCount = 0;

	/**
	 * @return 页面大小
	 * */
	public int getPageSize() {
		return pageSize;
	}
	
	/**
	 * @return 页码
	 * */
	public int getPageIndex() {
		return pageIndex;
	}
	
	/**
	 * @return 总页数
	 * */
	public int getPageCount() {
		return pageCount;
	}
	/**
	 * 数据总行数，如果不分页则和size()一致，如果分页则返回未分页查询的总记录数
	 * @return 总行数
	 * */
	public int getTotalRowCount() {
		return totalRowCount;
	}
	
	private static DefaultNameConvertor defaultNameConvertor=new DefaultNameConvertor();
	
	/**
	 * 数据名转换
	 * @param name 数据名 
	 * @param format 格式
	 * @return 转换后的数据名
	 * */
	public static String convertDataName(String name,DataNameFormat format)
	{
		if(name==null) {
			return null;
		}
		
		if(format==null) format=GlobalSettings.DEFAULT_DATA_NAME_FORMAT;
		if(format==null) format=DataNameFormat.NONE;
		
		
		if(format==DataNameFormat.NONE)
		{
			return name;
		}
		else if(format==DataNameFormat.DB_LOWER_CASE)
		{
			return name.toLowerCase();
		}
		else if(format==DataNameFormat.DB_UPPER_CASE)
		{
			return name.toUpperCase();
		}
		else if(format==DataNameFormat.POJO_PROPERTY)
		{
			return defaultNameConvertor.getPropertyName(name);
		}
		else
		{
			return name;
		}
			
			
	}

	/**
	 * 初始化MetaData
	 * @param meta ResultSetMetaData
	 * @exception SQLException  SQL异常，可以有ResultSet的某些方法抛出
	 * */
	protected void initeMetaData(ResultSetMetaData meta) throws SQLException
	{
		QueryMetaData metaData = new QueryMetaData();
		if(meta==null) {
			return;
		}
 
		int ct=meta.getColumnCount();
		metaData.setColumnCount(ct);

		for (int colIndex = 1; colIndex <= ct; colIndex++)
		{
			metaData.addCatalogName((meta.getCatalogName(colIndex)));
			metaData.addColumnClassName(meta.getColumnClassName(colIndex));
			metaData.addColumnLabel((meta.getColumnLabel(colIndex)));
			metaData.addColumnType(meta.getColumnType(colIndex));
			metaData.addColumnTypeName(meta.getColumnTypeName(colIndex));
			metaData.addSchemaName(meta.getSchemaName(colIndex));
			metaData.addTableName((meta.getTableName(colIndex)));
			metaData.setMap(meta.getColumnLabel(colIndex), colIndex - 1);
			
		}
		
		this.setRawMetaData(metaData);
		
	}
	
	/**
	 * 设置列，用于手动创建的情况
	 * @param columns 列清单
	 * */
	public void setColumns(ArrayList<String> columns)
	{
		QueryMetaData metaData  = new QueryMetaData();
	 
		int ct=columns.size();
		metaData.setColumnCount(ct);
		int colIndex=0;
		for (String c : columns) {
			metaData.addCatalogName((c));
			metaData.addColumnClassName("java.lang.String");
			metaData.addColumnLabel((c));
//			metaData.addColumnType(String.class);
//			metaData.addColumnTypeName(meta.getColumnTypeName(colIndex));
//			metaData.addSchemaName(meta.getSchemaName(colIndex));
//			metaData.addTableName(meta.getTableName(colIndex));
			metaData.setMap(c, colIndex);
			colIndex++;
		}
		
		this.setRawMetaData(metaData);
	}

	public String getDBIdentity() {
		return dbIdentity;
	}

}
