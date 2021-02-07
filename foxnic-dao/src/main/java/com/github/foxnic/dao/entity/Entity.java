package com.github.foxnic.dao.entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Entity {
 
	/**
	 * 被修改的属性清单
	 * */
	private final Set<String> $$dirtys=new HashSet<>();
	/**
	 * 被设置过值的属性清单
	 * */
	private final Set<String> $$besets=new HashSet<>();
	
	protected final void change(String field,Object oldValue,Object newValue) {

		boolean isModified=false;
		if(oldValue==null && newValue==null) {
			isModified=false;
		} else if(oldValue==null && newValue!=null) {
			isModified=true;
		} else if(oldValue!=null && newValue==null) {
			isModified=true;
		} else {
			isModified=!oldValue.equals(newValue);
		}
		
		//设置是否被修改
		if(isModified) {
			$$dirtys.add(field);
		}
		//是否被设置过
		$$besets.add(field);
		
	}
	
	/**
	 * 获得被设置过值的属性清单(无论值变化与否)
	 * */
	public final boolean hasBeSetProperties() {
		return !$$besets.isEmpty();
	}
	/**
	 * 获得被修改过，且值被改变的属性清单
	 * */
	public final boolean hasDirtyProperties() {
		return !$$dirtys.isEmpty();
	}
	
	/**
	 * 获得被设置过值的属性清单(无论值变化与否)
	 * */
	public final Set<String> getBeSetProperties() {
		return Collections.unmodifiableSet($$besets);
	}
	
	/**
	 * 获得被修改过，且值被改变的属性清单
	 * */
	public final Set<String> getDirtyProperties() {
		return Collections.unmodifiableSet($$dirtys);
	}
	/**
	 * 重置修改状态，标记所有字段为未修改、未被设置过值的状态
	 * */
	public final void clearModifies() {
		$$besets.clear();
		$$dirtys.clear();
	};
 
}
