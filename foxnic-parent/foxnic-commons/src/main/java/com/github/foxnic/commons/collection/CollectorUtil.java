package com.github.foxnic.commons.collection;


import com.github.foxnic.commons.lang.ArrayUtil;

import java.util.*;
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

	public static <T,R>  List<T> distinct(List<T> list,Function<? super T, ? extends R> key) {
	 	Set<R> keys=new HashSet<>();
		List<T> distinctList = new ArrayList<>();
		for (T t : list) {
			R keyValue=key.apply(t);
			if(keys.contains(keyValue)) continue;
			keys.add(keyValue);
			distinctList.add(t);
		}
		return distinctList;
	}

	/**
	 * 排序
	 * */
	public static <T,R>  List<T> sort(List<T> list,Function<? super T, ? extends R> key,boolean asc,boolean nullsLast) {
		list.sort(new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				R v1=key.apply(o1);
				R v2=key.apply(o2);
				int cp=0;
				if(v1==null && v2==null) cp=0;
				else if(v1!=null && v2==null) {
					cp = nullsLast? -1:1;
				}
				else if(v1==null && v2!=null) {
					cp = nullsLast? 1: -1;
				} else if(v1!=null && v2!=null) {
					if(v1 instanceof Comparable) {
						cp =((Comparable) v1).compareTo((Comparable) v2);
					} else {
						String s1=v1.toString();
						String s2=v2.toString();
						cp = s1.compareTo(s2);
					}
					if(!asc) {
						cp= -1 * cp;
					}
				}
				return cp;
			}
		});
		 return list;
	}


}
