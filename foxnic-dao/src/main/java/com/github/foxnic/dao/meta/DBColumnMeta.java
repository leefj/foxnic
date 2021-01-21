package com.github.foxnic.dao.meta;

import java.io.Serializable;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;
import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.meta.DBType;

/**
 * 数据库列描述信息
 * @author fangjieli
 *
 */
public class DBColumnMeta implements Serializable {
	
 
	private static final long serialVersionUID = -5951299851547384353L;

	private static final DefaultNameConvertor NC=new DefaultNameConvertor();

	private  static final String[] SEPS=  {",","."," ","　","|","。","，","\n","\t","\r","-","_","；",";"};
	
	static String[] depart(String comment) {
		if(comment==null) {
			return new String[] {null,null};
		}
		if(StringUtil.isBlank(comment)) {
			return new String[] {"",""};
		}
		
		int i = Integer.MAX_VALUE, j;
		for (String s : SEPS) {
			j = comment.indexOf(s);
			if (j >= 0 && j < i) {
				i = j;
			}
		}
		
		String label,detail;

		if (i == Integer.MAX_VALUE) {
			label = comment.trim();
			detail = comment.trim();
		} else {
			label = comment.substring(0, i).trim();
			detail = comment.substring(i + 1, comment.length()).trim();
		}
 
		detail = detail.trim();
		detail = detail.replaceAll("\n", ",");
		detail = detail.replaceAll("\r", ",");
		
		return new String[] {label,detail};
	}
 
	private DBType dbType;
	
	public DBColumnMeta(DBType dbType,String table,String column,Integer dataLength,Integer charLength,boolean isPK,String localDataType,DBDataType dbDataType,String comment,boolean nullable,boolean autoIncrease,Integer precision,Integer scale,String defaultValue)
	{
	 
 
		this.dbType=dbType;
		this.table = table;
		this.column = column;
		this.isPK = isPK;
		this.localDataType = localDataType;
		this.dbDataType = dbDataType;
		this.comment = comment;
		this.dataLength = dataLength;
		this.charLength = charLength;
		this.precision = precision;
		this.scale = scale;
		this.autoIncrease = autoIncrease;
		this.nullable = nullable;
		this.defaultValue=defaultValue;
		
		String[] cmts=depart(this.comment);
		this.label=cmts[0];
		this.detail=cmts[1];
 
	}
	private String table;
	private String column;
	private boolean isPK=false;
	private Integer dataLength=0;
	private Integer charLength=0;
	private boolean nullable=true;
	private boolean autoIncrease=false;
	private String defaultValue=null;
	
	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	private Integer precision;
	
	public Integer getPrecision() {
		return precision;
	}

	private Integer scale;
	
	/**
	 * 是否自增
	 * @return 逻辑值
	 * */
	public boolean isAutoIncrease() {
		return autoIncrease;
	}
	
	public Integer getScale() {
		return scale;
	}

	public boolean isNullable() {
		if(isPK)
		{
			return false;
		}
		return nullable;
	}
 
	public Integer getDataLength() {
		return dataLength;
	}
	
	public Integer getCharLength() {
		return charLength;
	}



	/**
	 * 是否主键
	 * @return 逻辑值
	 * */
	public boolean isPK() {
		return isPK;
	}

	private String localDataType;
	private DBDataType dbDataType;
	private String comment;
	private String label;
	private String detail;
	
	public String getKey()
	{
		return table+"-"+column;
	}
	
	
	
	public String getTable() {
		return table;
	}
	public String getColumn() {
		return column;
	}
	
	/**
	 * 获取JAVA风格的命名，如 NICK_NAME 返回  nickName 
	 * @return 获取java命名风格的列名称
	 * */
	public String getColumnVarName() {
		return NC.getPropertyName(column);
	}
	
	public String getLocalDataType() {
		return localDataType;
	}
	
	public DBDataType getDBDataType() {
		return dbDataType;
	}
	public String getComment() {
		return comment;
	}
	
	/**
	 * 获得字段标签，可用于给用户显示的字段名称等<br>
	 * 源于数据库字段注释，字段指数中用空格，逗号，分号，等隔开的前半部分字符串被认为是字段标签<br>
	 * 如无这些符号，则取全部注释，如无注释则返回字段名
	 * @return 标签
	 * */
	public String getLabel()
	{
		if(StringUtil.isBlank(this.label)) {
			if(StringUtil.isBlank(this.comment)) {
				return this.column;
			} else {
				return this.comment;
			}
		}
		return this.label;
	}
	
	/**
	 * 获得字段标签后的文本内容，用于给用户显示提示信息<br>
	 * 源于数据库字段注释，字段指数中用空格，逗号，分号，等隔开的后半部分字符串被认为是字段标签<br>
	 * 如无这些符号，则取全部注释，如无注释则返回字段名
	 * @return 字符串
	 * */
	public String getDetail()
	{
		if(StringUtil.isBlank(this.detail)) {
			if(StringUtil.isBlank(this.comment)) {
				return this.column;
			} else {
				return this.comment;
			}
		}
		return this.detail;
	}
	
	public String getJDBCDataType() {
		return dbType.getJDBCType(localDataType);
	}
	
	
}
