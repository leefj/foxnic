package com.github.foxnic.dao.entity;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Transient;

import io.swagger.annotations.ApiModelProperty;

public class Entity implements Serializable {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 被修改的属性清单
	 * */
	@Transient
	@ApiModelProperty(hidden = true)
	private final Set<String> $$dirtys=new HashSet<>();
	/**
	 * 被设置过值的属性清单
	 * */
	@Transient
	@ApiModelProperty(hidden = true)
	private final Set<String> $$besets=new HashSet<>();
	
	/**
	 * @param field 字段名
	 * @param oldValue 旧值
	 * @param newValue 新值
	 * */
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
	 * 判断属性是否有被设置过(无论值变化与否)
	 * */
	public final boolean isBeSetProperty(String propertyName) {
		return $$besets.contains(propertyName);
	}
	
	/**
	 * 获得被修改过，且值被改变的属性清单
	 * */
	public final boolean hasDirtyProperties() {
		return !$$dirtys.isEmpty();
	}
	
	/**
	 * 判断属性是否有被被修改过，且值被改变
	 * */
	public final boolean isDirtyProperty(String propertyName) {
		return $$dirtys.contains(propertyName);
	}
	
	/**
	 * 获得被设置过值的属性清单(无论值变化与否)
	 * */
	@ApiModelProperty(hidden = true)
	public final Set<String> getBeSetProperties() {
		return Collections.unmodifiableSet($$besets);
	}
	
	/**
	 * 获得被修改过，且值被改变的属性清单
	 * */
	@ApiModelProperty(hidden = true)
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
