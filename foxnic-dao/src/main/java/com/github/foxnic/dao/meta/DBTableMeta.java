package com.github.foxnic.dao.meta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.github.foxnic.commons.lang.StringUtil;

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
	
	private LinkedHashMap<String,DBColumnMeta> aiColumns=new LinkedHashMap<String,DBColumnMeta>();
	private ArrayList<DBColumnMeta> aiColumnList=new ArrayList<DBColumnMeta>();
	
	private String comments=null;
	
	private String topic;
	private String detail;
	
	private boolean hasAutoIncreaseColumn=false;
	
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
		String[] cmts=DBColumnMeta.depart(this.comments);
		this.topic=cmts[0];
		this.detail=cmts[1];
	}
	
	public void addColumn(DBColumnMeta column)
	{
		columns.put(column.getColumn().toLowerCase(), column);
		columnList.add(column);
		if(column.isPK())
		{
			pkColumns.put(column.getColumn().toLowerCase(), column);
			pkColumnList.add(column);
			if(column.isAutoIncrease()) {
				this.hasAutoIncreaseColumn=true;
			}
		}
		if(column.isAutoIncrease())
		{
			aiColumns.put(column.getColumn().toLowerCase(), column);
			aiColumnList.add(column);
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

	/**
	 * 获得数据表主题<br>
	 * 源于数据库字段注释，字段指数中用空格，逗号，分号，等隔开的前半部分字符串被认为是字段标签<br>
	 * 如无这些符号，则取全部注释，如无注释则返回字段名
	 * @return 标签
	 * */
	public String getTopic() {
		if(StringUtil.isBlank(this.topic)) {
			if(StringUtil.isBlank(this.comments)) {
				return this.tableName;
			} else {
				return this.comments;
			}
		}
		return this.topic;
	}
	
	/**
	 * 获得数据表说明的后半部分，用于给用户显示提示信息<br>
	 * 源于数据库字段注释，字段指数中用空格，逗号，分号，等隔开的后半部分字符串被认为是字段标签<br>
	 * 如无这些符号，则取全部注释，如无注释则返回字段名
	 * @return 字符串
	 * */
	public String getDetail()
	{
		if(StringUtil.isBlank(this.detail)) {
			if(StringUtil.isBlank(this.comments)) {
				return this.tableName;
			} else {
				return this.comments;
			}
		}
		return this.detail;
	}


	public List<DBColumnMeta> getAIColumns() {
		return this.aiColumnList;
	}


	public boolean hasAutoIncreaseColumn() {
		return hasAutoIncreaseColumn;
	}
	
	
}
