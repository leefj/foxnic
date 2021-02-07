package com.github.foxnic.dao.entity;

public class Entity {
 
	/**
	 * 获得被设置过值的属性清单(无论值变化与否)
	 * */
	public boolean hasBeSetProperties() {return false;};
	/**
	 * 获得被修改过，且值被改变的属性清单
	 * */
	public boolean hasDirtyProperties() {return false;};
	
	/**
	 * 获得被设置过值的属性清单(无论值变化与否)
	 * */
	public String[] getBeSetProperties() {return null;};
	/**
	 * 获得被修改过，且值被改变的属性清单
	 * */
	public String[] getDirtyProperties() {return null;};
	/**
	 * 重置修改状态，标记所有字段为未修改、未被设置过值的状态
	 * */
	public void clearModifies() {};
	/**
	 * 该析构函数，必须<br>
	 * 销毁时被调用并通知容器清空缓存
	 * */
	public void finalize( ) throws Throwable { super.finalize(); }
	
	
	
}
