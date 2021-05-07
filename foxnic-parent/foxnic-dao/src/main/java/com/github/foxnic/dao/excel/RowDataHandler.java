package com.github.foxnic.dao.excel;

import com.github.foxnic.dao.data.RcdSet;

/**
 * 数据行处理器
 * @author lifangjie
 * */
public abstract class RowDataHandler {

	protected int limitSize = 100;
	
	/**
	 * 每次返回的最大行数
	 * @return 数值
	 * */
	public int getLimitSize()
	{
		return limitSize;
	}
	/**
	 * 逐行处理Excel数据，使整过处理过程占用较小的内存
	 * @param rs 记录集
	 * @param rowIndex 行号
	 * @param totalRowCount 总行数，OPC模式无法获取正确的总行数
	 * */
	public abstract void process(RcdSet rs,int rowIndex,int totalRowCount);
 
}
