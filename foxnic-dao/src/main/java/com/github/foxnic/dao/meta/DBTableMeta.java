package com.github.foxnic.dao.meta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 数据表元数据
 * @author fangjieli
 *
 */
public class DBTableMeta implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7157261205405198885L;
	
	private String tableName=null;
	
	public String getTableName() {
		return tableName;
	}

	private LinkedHashMap<String,DBColumnMeta> columns=new LinkedHashMap<String,DBColumnMeta>();
	private ArrayList<DBColumnMeta> columnList=new ArrayList<DBColumnMeta>();
	
	private LinkedHashMap<String,DBColumnMeta> pkColumns=new LinkedHashMap<String,DBColumnMeta>();
	private ArrayList<DBColumnMeta> pkColumnList=new ArrayList<DBColumnMeta>();
	
	private LinkedHashMap<String,DBIndexMeta> indexs=new LinkedHashMap<String,DBIndexMeta>();
	private ArrayList<DBIndexMeta> indexList=new ArrayList<DBIndexMeta>();
	
	private String comments=null;
	
	/**
	 * 表注释
	 * @return 表注释
	 * */
	public String getComments() {
		return comments;
	}

 
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	private boolean isView=false;

	/**
	 * 是否视图
	 * @return 逻辑值
	 * */
	public boolean isView() {
		return isView;
	}

 
	public DBTableMeta(String tableName,String comments,boolean isView)
	{
		this.tableName=tableName;
		this.comments=comments;
		this.isView=isView;
	}
	
	public void addColumn(DBColumnMeta column)
	{
		columns.put(column.getColumn().toLowerCase(), column);
		columnList.add(column);
		if(column.isPK())
		{
			pkColumns.put(column.getColumn().toLowerCase(), column);
			pkColumnList.add(column);
		}
	}
	
	/**
	 * 获得表的所有列
	 * @return 列清单
	 * */
	public List<DBColumnMeta> getColumns()
	{
		return columnList;
	}
	
	/**
	 * 按列名获取列
	 * @param columnName 列名，不区分大小写
	 * @return DBColumnMeta
	 * */
	public DBColumnMeta getColumn(String columnName)
	{
		return columns.get(columnName.toLowerCase());
	}

	/**
	 * @return 主键字段个数
	 * */
	public int getPKColumnCount() {
		return pkColumnList.size();
	}
	
	/**
	 * 是否为当前表的一个字段
	 * @param field 字段名
	 * @return 逻辑值
	 * */
	public boolean isColumnExists(String field)
	{
		return columns.containsKey(field.toLowerCase());
	}
	
	/**
	 * 返回主键字段清单
	 * @return  主键字段清单
	 * */
	public List<DBColumnMeta> getPKColumns()
	{
		 return  Collections.unmodifiableList(this.pkColumnList);
	}
	
	/**
	 * 是否为PK字段中的一个，请结合 checkPK 方法
	 * @param field 字段名，去区分大小写
	 * @return 逻辑值
	 * */
	public boolean isPK(String field)
	{
		DBColumnMeta c=pkColumns.get(field.toLowerCase());
		if(c==null) {
			return false;
		} else {
			return true;
		}
	}
	/**
	 * 确认传入的数组是否为正确且完整pk字段列表
	 * @param fields 字段
	 * @return 逻辑值
	 * */
	public boolean checkPK(String... fields)
	{
		if(fields.length!=this.getPKColumnCount()) {
			return false;
		}
		HashMap<String,String> map=new HashMap<>();
		for (String string : fields) {
			DBColumnMeta c=pkColumns.get(string.toLowerCase());
			if(c==null) {
				return false;
			}
			map.put(string.toLowerCase(), "X");
		}
		return map.size()==this.getPKColumnCount();
	}
 
	public void addIndex(DBIndexMeta index)
	{
		indexs.put(index.getName().toLowerCase(), index);
		indexList.add(index);
	}
	
	/**
	 * 获得表上的索引信息
	 * @return 索引信息
	 * */
	public List<DBIndexMeta> getIndexs()
	{
		return indexList;
	}
	
	/**
	 * 获得表上的索引信息
	 * @param indexName 索引名称
	 * @return 索引信息
	 * */
	public DBIndexMeta getIndex(String indexName)
	{
		return indexs.get(indexName.toLowerCase());
	}
	
	
}
