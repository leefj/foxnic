package com.github.foxnic.commons.collection;


import com.github.foxnic.commons.lang.ArrayUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectorUtil {
	
	public static <T,R>  List<R> collectList(List<T> list,Function<? super T, ? extends R> key) {
		return list.stream().map(key).collect(Collectors.toList());
	}

	public static <T,R>  R[] collectArray(List<T> list,Function<? super T, ? extends R> key,Class<? extends R> type) {
		List<R> result=collectList(list,key);
		R[] c=ArrayUtil.createArray(type,0);
		return (R[])result.toArray(c);
	}

	public static <T,R> Set<R> collectSet(List<T> list, Function<? super T, ? extends R> key) {
		return list.stream().map(key).collect(Collectors.toSet());
	}
	
	public static <T,R>  List<R> collectList(IPagedList<T> list,Function<? super T, ? extends R> key) {
		return list.stream().map(key).collect(Collectors.toList());
	}

	public static <T,K,V> Map<K,V> collectMap(List<T> list,Function<? super T, ? extends K> key,Function<? super T, ? extends V> value) {
		return list.stream().collect(Collectors.toMap(key, value));
	}

	public static <T,K,V> Map<K,V> collectMap(IPagedList<T> list,Function<? super T, ? extends K> key,Function<? super T, ? extends V> value) {
		return list.stream().collect(Collectors.toMap(key, value));
	}

	public static <T,R>  Map<R,List<T>> groupBy(List<T> list,Function<? super T, ? extends R> key) {
		return list.stream().collect(Collectors.groupingBy(key));
	}
	
	public static <T,R>  Map<R,List<T>> groupBy(IPagedList<T> list,Function<? super T, ? extends R> key) {
		return list.stream().collect(Collectors.groupingBy(key));
	}

}
