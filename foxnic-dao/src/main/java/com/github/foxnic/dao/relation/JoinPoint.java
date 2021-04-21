package com.github.foxnic.dao.relation;

import java.util.HashSet;
import java.util.Set;

import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;

public class JoinPoint {

	private DBTable table;
	private DBField[] fields;
	private Set<String> fieldIds;
	
	public JoinPoint(DBField... field) {
		if(field==null || field.length==0) {
			throw new IllegalArgumentException("JoinPoint 至少需要一个字段");
		}
		fieldIds=new HashSet<>();
		for (DBField f : field) {
			
			//检查重复的字段
			if(fieldIds.contains(f.getId())) {
				throw new IllegalArgumentException("JoinPoint 不允许同名字段 "+f.table().name()+"."+f.name());
			}
			
			fieldIds.add(f.getId());
			//设置表
			if(table==null) {
				table=f.table();
				continue;
			}
			//校验表
			if(!table.name().equalsIgnoreCase(f.table().name())) {
				throw new IllegalArgumentException("JoinPoint 表名不一致 : "+table.name()+","+f.table().name());
			}
		}
	}
	
	/**
	 * 检查两个 JoinPoint 是否匹配
	 * */
	public boolean match(JoinPoint joinPoint) {
		//如果表名不一致，则不匹配
		if(!joinPoint.table.name().equalsIgnoreCase(this.table.name())) return false;
		//如果字段数量不一致，则不匹配
		if(joinPoint.fields.length!=this.fields.length) return false;
		for (DBField f : this.fields) {
			if(!this.fieldIds.contains(f.getId())) return false;
		}
		return true;
	}
	
	
}
