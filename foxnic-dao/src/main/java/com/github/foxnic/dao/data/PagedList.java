package com.github.foxnic.dao.data;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * 分页列表
 * */
public class PagedList<T> implements Iterable<T> {
	
	
	private ArrayList<T> list;
	
	@Transient
	private QueryMetaData meta;
	
	private int pageSize = 0;
	private int pageIndex = 0;
	private int pageCount = 0;
	private int totalRowCount = 0;
	
	
	public PagedList(List<T> entities,QueryMetaData meta,int pageSize,int pageIndex,int pageCount,int totalRowCount) {
		this.list=new ArrayList<T>(entities.size());
		this.list.addAll(entities);
		this.meta=meta;
		this.pageSize=pageSize;
		this.pageIndex=pageIndex;
		this.pageCount=pageCount;
		this.totalRowCount=totalRowCount;
	}

	public QueryMetaData getMeta() {
		return meta;
	}
	
	public void clearMeta() {
		this.meta=null;
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
 
	@Override
	public Iterator<T> iterator() {
		return this.list.iterator();
	}

	public ArrayList<T> getList() {
		return list;
	}
	
	public int size() {
		return list.size();
	}

	public Stream<T> stream() {
		return this.list.stream();
	}
	
}
