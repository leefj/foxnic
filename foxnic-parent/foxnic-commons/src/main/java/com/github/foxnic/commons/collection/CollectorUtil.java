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

	/**
	 * key 指代的get方法返回 R 的 List ，把这些 List 合并后返回
	 * */
	public static <T,R>  List<R> collectMergedList(IPagedList<T> list,Function<? super T, ? extends List<R>> key) {
		List<List<R>> lists=list.stream().map(key).collect(Collectors.toList());
		List<R> els=new ArrayList<>();
		lists.stream().forEach((ls)->{els.addAll(ls);});
		return els;
	}

	/**
	 * key 指代的get方法返回 R 的 List ，把这些 List 合并后返回
	 * */
	public static <T,R>  List<R> collectMergedList(List<T> list,Function<? super T, ? extends List<R>> key) {
		List<List<R>> lists=list.stream().map(key).collect(Collectors.toList());
		List<R> els=new ArrayList<>();
		lists.stream().forEach((ls)->{els.addAll(ls);});
		return els;
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

	public static <T,R,V>  Map<R,List<V>> groupBy(List<T> list,Function<? super T, ? extends R> key,Function<? super T, ? extends V> value) {
		Map<R,List<T>> map=list.stream().collect(Collectors.groupingBy(key));
		Map<R,List<V>> result=new HashMap<>();
		for (Map.Entry<R, List<T>> entry : map.entrySet()) {
			List<T> input=entry.getValue();
			List<V> output=new ArrayList<>();
			for (T t : input) {
				V v=value.apply(t);
				output.add(v);
			}
			result.put(entry.getKey(),output);
		}
		return result;
	}

	public static <T,R>  Map<R,List<T>> groupBy(IPagedList<T> list,Function<? super T, ? extends R> key) {
		return list.stream().collect(Collectors.groupingBy(key));
	}

	public static <T,R,V>  Map<R,List<V>> groupBy(IPagedList<T> list,Function<? super T, ? extends R> key,Function<? super T, ? extends V> value) {
		return groupBy(list.getList(),key,value);
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

	/**
	 * 将 S 的集合转为 T 的集合
	 * */
	public static <S,T> List<T> cast(List<S> list, DataCreateHandler<S,T> handler) {
		List<T> result=new ArrayList<>();
		for (S s : list) {
			T t=handler.handle(s);
			result.add(t);
		}
		return result;
	}


	public static  interface DataUpdateHandler<S,T> {
		T handle(S source,T target);
	}

	public static  interface DataCreateHandler<S,T> {
		T handle(S source);
	}

	public static class CompareResult<S, T> {

		private List<S> source;

		private List<T> target;
		/**
		 * source 比 target 多的部分
		 * */
		private List<T> sourceDiff;
		/**
		 * 交集
		 * */
		private List<T> intersection;
		/**
		 * target 比 source 多的部分
		 * */
		private List<T> targetDiff;

		public List<S> getSource() {
			return source;
		}

		public List<T> getTarget() {
			return target;
		}

		/**
		 * target 比 source 多的部分
		 * */
		public List<T> getTargetDiff() {
			return targetDiff;
		}

		/**
		 * source 比 target 多的部分
		 * */
		public List<T> getSourceDiff() {
			return sourceDiff;
		}

		/**
		 * 交集
		 * */
		public List<T> getIntersection() {
			return intersection;
		}
	}

	/**
	 * 比较两个集合，并返回比较后的结果
	 * @param source 源数据集合
	 * @param target 目标数据集合
	 * @param sourceKey 源数据中用于比较的key
	 * @param targetKey 目标数据中用于比较的key
	 * @param handleSourceDiff 处理 sourceDiff 部分
	 * @param handleIntersection 处理 intersection 部分
	 * @return CompareResult 返回比较与处理后的结果
	 * */
	public static <S,T,K> CompareResult compare(List<S> source, List<T> target, Function<? super S, ? extends K> sourceKey,
										 Function<? super T, ? extends K> targetKey,DataCreateHandler<S,T> handleSourceDiff,DataUpdateHandler<S,T> handleIntersection,DataCreateHandler<T,T> handleTargetDiff) {

		Map<K,S> sourceMap=CollectorUtil.collectMap(source,sourceKey,(e)->{return e;});
		Map<K,T> targetMap=CollectorUtil.collectMap(target,targetKey,(e)->{return e;});

		// 比对差异
		List<T> intersection=new ArrayList<>();
		List<T> sourceDiff=new ArrayList<>();
		List<T> targetDiff=new ArrayList<>();

		// 循环源
		for (S s : source) {
			if(s==null) continue;
			T t=targetMap.get(sourceKey.apply(s));
			// 如果在 camunda 已经存在
			if(t!=null) {
				if(handleIntersection!=null) {
					t=handleIntersection.handle(s,t);
				}
				intersection.add(t);
			} else {
				if(handleSourceDiff!=null) {
					t = handleSourceDiff.handle(s);
				}
				if(t!=null) {
					sourceDiff.add(t);
				}
			}
		}

		// 循环目标
		for (T t : target) {
			if(t==null) continue;
			S s=sourceMap.get(targetKey.apply(t));
			if(s!=null) {
				if(handleIntersection!=null) {
					t=handleIntersection.handle(s,t);
				}
				if(t!=null && !intersection.contains(t)) {
					intersection.add(t);
				}
			} else {
				if(handleTargetDiff!=null) {
					t=handleTargetDiff.handle(t);
				}
				if(t!=null) {
					targetDiff.add(t);
				}
			}
		}

		CompareResult result=new CompareResult();

		result.source=source;
		result.target=target;
		result.intersection=intersection;
		result.sourceDiff=sourceDiff;
		result.targetDiff=targetDiff;

		return result;


	}



}
