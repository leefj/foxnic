package com.github.foxnic.dao.excel;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.dao.data.AbstractSet;
import com.github.foxnic.dao.data.QueryMetaData;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.data.RcdSet;
import com.github.foxnic.dao.excel.wrapper.RowWrapper;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBMapping;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.dao.sql.SQLParser;
import com.github.foxnic.sql.data.DataNameFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Excel结构描述
 * @author fangjieli
 * */
public class ExcelStructure {

	public static  interface RowHandler {
		void handle(int rowIndex, ExcelStructure es, Rcd r, RowWrapper row);
	}

	private int dataRowEndDelta=0;

	private  RowHandler rowHandler;

	public RowHandler getRowHandler() {
		return rowHandler;
	}

	public void setRowHandler(RowHandler rowHandler) {
		this.rowHandler = rowHandler;
	}

	/**
	 * 末行数据行偏移量：Excel读取是最末尾的行可能是汇总，备注之类的，通过设置这个值不读取最末尾的几行数据
	 *
	 * @return 获得末行数据行偏移量，默认0
	 * */
	public int getDataRowEndDelta() {
		return dataRowEndDelta;
	}

	/**
	 * 设置末尾n行数据不读取；末行数据行偏移量：Excel读取是最末尾的行可能是汇总，备注之类的，通过设置这个值不读取最末尾的几行数据
	 * @param dataRowEndDelta  末行数据行偏移量
	 * @return ExcelStructure
	 * */
	public ExcelStructure setDataRowEndDelta(int dataRowEndDelta) {

		if(dataRowEndDelta<0)
		{
			throw new IllegalArgumentException("dataRowEndDelta 必须大于 0");
		}

		this.dataRowEndDelta = dataRowEndDelta;

		return this;
	}

	private int dataRowBegin=1;

	/**
	 * 数据起始行,取值与Excel中的行标一致
	 * @return 数据起始行
	 * */
	public int getDataRowBegin() {
		return dataRowBegin;
	}


	/**
	 * 数据起始行, 读取与写入时用
	 * @param dataRowBegin 数据起始行
	 * @return ExcelStructure
	 * */
	public ExcelStructure setDataRowBegin(int dataRowBegin) {
		this.dataRowBegin = dataRowBegin;
		return this;
	}

	/**
	 * 按字段设置列名
	 * @param field 字段
	 * @param title 列标题
	 * @return ExcelStructure
	 * */
	public ExcelStructure setColumnTitleByField(String field,String title)
	{
		this.getColumnByField(field).setTitle(title);
		return this;
	}

	/**
	 * 按Excel列的字符索引设置标题
	 * @param charIndex 字符索引位置
	 * @param title 列标题
	 * @return ExcelStructure
	 * */
	public ExcelStructure setColumnTitleCharIndex(String charIndex,String title)
	{
		this.getColumn(charIndex).setTitle(title);
		return this;
	}

	/**
	 * 按Excel列的数字索引设置标题
	 * @param index 数字索引位置
	 * @param title 列标题
	 * @return ExcelStructure
	 * */
	public ExcelStructure setColumnTitleIndex(int index,String title)
	{
		this.getColumn(index).setTitle(title);
		return this;
	}

	/**
	 * 数据结束列,仅读取时用
	 * @return 最末列位置
	 * */
	public int getColumnReadEndIndex() {
		return columnReadEndIndex;
	}

//	/**
//	 * 数据结束列,仅读取时用
//	 * @param dataColumnEnd 最末列位置
//	 * @return ExcelStructure
//	 * */
//	public ExcelStructure setDataColumnEnd(int dataColumnEnd) {
//		this.dataColumnEnd = dataColumnEnd;
//		return this;
//	}

//	/**
//	 * 数据结束列
//	 * @param  excelCharIndex excel 本身的列头
//	 * @return ExcelStructure
//	 * */
//	public ExcelStructure setDataColumnEnd(String excelCharIndex) {
//		setDataColumnEnd(fromExcel26(excelCharIndex));
//		return this;
//	}

	/**
	 * 设置读取的列范围
	 * @param range  只读取A列, 读取B列到G列数据  B:G
	 * @return ExcelStructure
	 * */
	public ExcelStructure setColummReadRange(String range)
	{
		if(range.indexOf(':')==-1)
		{
			this.columnReadBeginIndex=ExcelUtil.fromExcel26(range);
			this.columnReadEndIndex=ExcelUtil.fromExcel26(range);
		}
		else
		{
			String[] r=range.split(":");
			this.columnReadBeginIndex=ExcelUtil.fromExcel26(r[0]);
			this.columnReadEndIndex=ExcelUtil.fromExcel26(r[1]);
		}
		return this;
	}

	public ExcelStructure setColummReadRange(String beginColumn,String endColumn)
	{
		return setColummReadRange(beginColumn+":"+endColumn);
	}


	/**
	 * 数据起始列，可用于设置读取范围
	 * @return 起始列
	 * */
	public int getColumnReadBeginIndex() {
		return columnReadBeginIndex;
	}

	public void setColumnReadBeginIndex(int columnReadBeginIndex) {
		this.columnReadBeginIndex = columnReadBeginIndex;
	}

	public void setColumnReadBeginIndex(String columnReadBeginIndex) {
		this.columnReadBeginIndex = ExcelUtil.fromExcel26(columnReadBeginIndex);
	}

	public void setColumnReadEndIndex(int columnReadEndIndex) {
		this.columnReadEndIndex = columnReadEndIndex;
	}

	public void setColumnReadEndIndex(String columnReadEndIndex) {
		this.columnReadEndIndex = ExcelUtil.fromExcel26(columnReadEndIndex);
	}

	private int columnReadEndIndex =-1;
	private int columnReadBeginIndex =1;

	private int columnMoveDelta = 0;

	/**
	 * 在标准列基础上的偏移量
	 * */
	public void setColumnMoveDelta(int columnMoveDelta) {
		this.columnMoveDelta = columnMoveDelta;
	}

	public int getColumnMoveDelta() {
		return columnMoveDelta;
	}

	private HashMap<String,ExcelColumn> columns=new HashMap<String,ExcelColumn>();

	/**
	 * 添加列
	 * @param charIndex 字符索引
	 * @param field 字段名
	 * @param type 数据类型
	 * @return  ExcelColumn
	 * */
	public ExcelColumn addColumn(String charIndex,String field,Class type)
	{
		charIndex=charIndex.trim().toUpperCase();
		ExcelColumn column=columns.get(charIndex);
		if(column==null)
		{
			column=new ExcelColumn();
		}

		int index=ExcelUtil.fromExcel26(charIndex);
		column.setCharIndex(charIndex);
		column.setIndex(index);
		column.setDataType(type);
		column.setField(field);

		columns.put(charIndex, column);

		//自动设置读取的起止列
		int begin=this.getColumnReadBeginIndex();
		if(begin==-1) begin=index;
		else
		{
			if(begin>index)
			{
				begin=index;
			}
		}
		this.columnReadBeginIndex =begin;

		int end=this.getColumnReadEndIndex();
		if(end==-1) end=index;
		else
		{
			if(end<index)
			{
				end=index;
			}
		}
		this.columnReadEndIndex = end;

		return column;
	}

	/**
	 * 添加列
	 * @param index 数字索引
	 * @param field 字段名
	 * @return  ExcelColumn
	 * */
	public ExcelColumn addColumn(int index,String field)
	{
		return addColumn(ExcelUtil.toExcel26(index),field,Object.class);
	}

	/**
	 * 添加列
	 * @param charIndex 字符索引
	 * @param field 字段名
	 * @return  ExcelColumn
	 * */
	public ExcelColumn addColumn(String charIndex,String field)
	{
		return addColumn(charIndex,field,Object.class);
	}

	/**
	 * 添加列
	 * @param charIndex 字符索引
	 * @param field 字段名
	 * @return  ExcelColumn
	 * */
	public ExcelColumn addColumn(String charIndex,String field,String title)
	{
		ExcelColumn column=addColumn(charIndex,field,Object.class);
		column.setTitle(title);
		return column;
	}

	/**
	 * 添加列
	 * @param charIndex 字符索引
	 * @param field 字段名
	 * @return  ExcelColumn
	 * */
	public ExcelColumn addColumn(String charIndex, String field, String title, ExcelColumn.CellReader cellReader)
	{
		ExcelColumn column=addColumn(charIndex,field,Object.class);
		column.setTitle(title);
		column.setCellReader(cellReader);
		return column;
	}

	/**
	 * 添加列
	 * @param column ExcelColumn
	 * @return  ExcelColumn
	 * */
	public ExcelStructure addColumn(ExcelColumn column)
	{
		columns.put(column.getCharIndex(), column);
		return this;
	}

	/**
	 * 添加列
	 * @param charIndex 字符索引
	 * @return  ExcelColumn
	 * */
	public ExcelColumn getColumn(String charIndex)
	{
		return columns.get(charIndex.toUpperCase().trim());
	}

	/**
	 * 通过字段名获得 ExcelColumn 对象
	 * @param field 字段
	 * @return ExcelColumn
	 * */
	public ExcelColumn getColumnByField(String field)
	{
		for (ExcelColumn ec : columns.values()) {
			if(field.equalsIgnoreCase(ec.getField())) {
				return ec;
			}
		}
		return null;
	}

	/**
	 * 通过字段名获得 ExcelColumn 对象清单
	 * @param field 字段
	 * @return ExcelColumn 对象清单
	 * */
	public List<ExcelColumn> getColumnsByField(String field)
	{
		ArrayList<ExcelColumn> list=new ArrayList<ExcelColumn>();
		for (ExcelColumn ec : columns.values()) {
			if(field.equalsIgnoreCase(ec.getField()))
			{
				list.add(ec);
			}
		}
		return list;
	}

	/**
	 * 按序号获得 ExcelColumn
	 * @param index 字段
	 * @return ExcelColumn 对象清单
	 * */
	public ExcelColumn getColumn(int index)
	{
		String charIndex=ExcelUtil.toExcel26(index);
		return columns.get(charIndex);
	}

	/**
	 * 是否有校验错误的数据
	 * @return 逻辑值
	 * */
	public boolean hasValidateError()
	{
		for (ExcelColumn column : columns.values()) {
			if(column.getValidateResults().size()>0) return true;
		}
		return false;
	}

	/**
	 * 获得校验结果
	 * @return 校验结果
	 * */
	public List<ValidateResult> getValidateResults() {

		List<ValidateResult> results=new ArrayList<>();
		for (ExcelColumn column : columns.values()) {
			results.addAll(column.getValidateResults());
		}
		return results;

	}




	/**
	 * 从 RcdSet 转换
	 * @param rs 记录集
	 * @param useCommentAsHeader  是否使用列注释作为列名。列注释规则，用空格，逗号，分号，等隔开的后半部分字符串被认为是字段标签
	 * @return ExcelStructure
	 * */
	public static ExcelStructure parse(RcdSet rs,boolean useCommentAsHeader) {
		return parse(DAO.getInstance(rs),rs.getMetaData(),rs.getDataNameFormat(),useCommentAsHeader);
	}

	/**
	 * 从 RcdSet 转换,不使用列注释作为列名
	 * @param rs 记录集
	 * @return ExcelStructure
	 * */
	public static ExcelStructure parse(RcdSet rs) {
		return parse(DAO.getInstance(rs),rs.getMetaData(),rs.getDataNameFormat(),false);
	}

	/**
	 * 从 QueryMetaData 转换
	 * @param dao DAO对象
	 * @param m QueryMetaData对象
	 * @param dataNameFormat  DataNameFormat对象
	 * @param useCommentAsHeader  是否使用列注释作为列名。列注释规则，用空格，逗号，分号，等隔开的后半部分字符串被认为是字段标签
	 * @return ExcelStructure
	 * */
	public static ExcelStructure parse(DAO dao,QueryMetaData m,DataNameFormat dataNameFormat,boolean useCommentAsHeader) {

		ExcelStructure es =new ExcelStructure();

		//es.setDataColumnBegin(0);
		//es.setDataColumnEnd(m.getColumnCount());
		es.setDataRowBegin(0);

		String tableName=null;
		List<String> tables=null;
		DBTableMeta tm=null;
		String title=null;
		for (int i = 0; i < m.getColumnCount(); i++) {
			ExcelColumn ec=new ExcelColumn();
			title=null;
			if(useCommentAsHeader)
			{
				tableName=m.getTableName(i);
				if(!StringUtil.hasContent(tableName) && tables==null) tables=SQLParser.getAllTables(m.getSQL().getListParameterSQL(), DBMapping.getDruidDBType(dao.getDBType().getSQLDialect()));
				if(tables!=null && tables.size()==1) tableName=tables.get(0);
				if(StringUtil.hasContent(tableName) && dao!=null) {
					tm=dao.getTableMeta(tableName);
					if(tm!=null) {
						DBColumnMeta cm = tm.getColumn(m.getColumnLabel(i));
						if(cm!=null) title=cm.getLabel();
					}
				}
			}

			ec.setField(m.getColumnLabel(i));
			ec.setIndex(i);

			if(!StringUtil.hasContent(title)) {
				title=AbstractSet.convertDataName(m.getColumnLabel(i),dataNameFormat);
			}
			ec.setTitle(title);
			es.addColumn(ec);
		}

		return es;

	}

	/**
	 * 获得总列数
	 * @return 总列数
	 * */
	public int getColumnCount()
	{
		return columns.size();
	}

	private ExcelColumn.CellWriter cellWriter= null;

	public ExcelColumn.CellWriter getCellWriter() {
		return cellWriter;
	}

	public void setCellWriter(ExcelColumn.CellWriter cellWriter) {
		this.cellWriter = cellWriter;
	}



}
