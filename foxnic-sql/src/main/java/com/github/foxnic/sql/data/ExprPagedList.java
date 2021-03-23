package com.github.foxnic.sql.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * 分页列表
 * */
public interface ExprPagedList<T> extends Iterable<T>  {
 
	
	Iterator<T> iterator();

	 ArrayList<T> getList();
	
	 int size();

	Stream<T> stream();
	
}
