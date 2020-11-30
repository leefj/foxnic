package com.github.foxnic.dao.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页列表
 * */
public class PagedList<T> extends ArrayList<T> {
	
	private QueryMetaData meta;
	private int pageSize = 0;
	private int pageIndex = 0;
	private int pageCount = 0;
	private int totalRowCount = 0;
	
	
	public PagedList(List<T> entities,QueryMetaData meta,int pageSize,int pageIndex,int pageCount,int totalRowCount) {
		this.addAll(entities);
		this.meta=meta;
		this.pageSize=pageSize;
		this.pageIndex=pageIndex;
		this.pageCount=pageCount;
		this.totalRowCount=totalRowCount;
	}

	public QueryMetaData getMeta() {
		return meta;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public int getPageCount() {
		return pageCount;
	}

	public int getTotalRowCount() {
		return totalRowCount;
	}
	
}
