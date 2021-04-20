package com.github.foxnic.dao.relation;

import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;

public class JoinPoint {

	private DBTable table;
	private DBField[] fields;
	
	public JoinPoint(DBField... field) {
		if(field==null || field.length==0) {
			throw new IllegalArgumentException("JoinPoint 至少需要一个字段");
		}
		for (DBField f : field) {
			if(table==null) {
				table=f.table();
				continue;
			}
			//
			if(!table.name().equalsIgnoreCase(f.table().name())) {
				throw new IllegalArgumentException("JoinPoint 表名不一致 : "+table.name()+","+f.table().name());
			}
		}
	}
	
	public boolean match(JoinPoint joinPoint) {
		//如果表名不一致，则不匹配
		if(!joinPoint.table.name().equalsIgnoreCase(this.table.name())) return false;
		//如果字段数量不一致，则不匹配
		if(joinPoint.fields.length!=this.fields.length) return false;
		for (DBField f : this.fields) {
			
		}
		return true;
	}
	
	
}
