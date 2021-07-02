package com.github.foxnic.commons.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * 分页列表
 * */
public interface IPagedList<T> extends Iterable<T>  {
 
	
	Iterator<T> iterator();

	 ArrayList<T> getList();
	
	 int size();

	Stream<T> stream();

	int getPageSize();

	int getPageIndex() ;

	int getPageCount() ;

	int getTotalRowCount();
	
}
