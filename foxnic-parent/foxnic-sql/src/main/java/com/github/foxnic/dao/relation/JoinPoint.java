package com.github.foxnic.dao.relation;

import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.expr.ConditionExpr;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JoinPoint {

	public static class SelelctFieldPair {

		public DBField getField() {
			return field;
		}

		public String getAlias() {
			return alias;
		}

		private DBField field;
		private String alias;

		public SelelctFieldPair(DBField field, String alias) {
			this.field=field;
			this.alias=alias;
		}
	}

	private DBTable table;
	private DBField[] fields;
	private Set<String> fieldIds;

	private JoinPoint() {}

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
		this.fields=field;
	}

	/**
	 * 检查两个 JoinPoint 是否匹配
	 * */
	public boolean match(JoinPoint joinPoint) {
		//如果表名不一致，则不匹配
		if(!joinPoint.table.name().equalsIgnoreCase(this.table.name())) return false;

		//如果字段数量不一致，则不匹配
		if(joinPoint.fields.length!=this.fields.length) return false;

		for (DBField f : joinPoint.fields) {
			if(!this.fieldIds.contains(f.getId())) return false;
		}
		return true;
	}

	public DBTable table() {
		return table;
	}

	public DBField[] fields() {
		return fields;
	}


	private List<ConditionExpr> conditions =new ArrayList<>();

	void addCondition(ConditionExpr ce) {
		conditions.add(ce);
	}

	public List<ConditionExpr> getConditions() {
		return new ArrayList<>(conditions);
	}

	@Override
	public String toString() {
		String str=this.table().name()+"( ";
		for (int i = 0; i < fields.length; i++) {
			str+=fields[i].name()+((i<fields.length-1)?" , ":"");
		}
		str+=" )";
		return str;
	}

	public List<SelelctFieldPair> getSelectFields() {
		return selectFields;
	}

	private  List<SelelctFieldPair> selectFields=new ArrayList<>();

    public void addSelectFields(DBField fields,String alias) {
		selectFields.add(new SelelctFieldPair(fields,alias));
    }

    private String key = null;

    public JoinPoint clone() {
    	JoinPoint point=new JoinPoint();
    	point.key=this.key;
    	point.table=this.table;
    	point.fields=this.fields.clone();
    	point.fieldIds=new HashSet<>();
    	point.fieldIds.addAll(this.fieldIds);
    	point.conditions=new ArrayList<>();
    	point.conditions.addAll(this.conditions);
    	point.selectFields=new ArrayList<>();
    	point.selectFields.addAll(this.selectFields);
    	return point;
	}

    public String getKey(){
    	if(this.key!=null) return key;
    	List<String> parts=new ArrayList<>();

		parts.add(table.name());
		parts.add("fields:");
		for (DBField field : fields) {
			parts.add(field.name());
		}
		parts.add("fieldIds:");
		for (String fieldId : fieldIds) {
			parts.add(fieldId);
		}
		parts.add("conditions:");
		for (ConditionExpr condition : conditions) {
			try {
				parts.add(condition.getSQL());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		parts.add("selectFields:");
		for (SelelctFieldPair selectField : selectFields) {
			parts.add(selectField.alias+":"+selectField.getAlias());
		}

    	key= StringUtil.join(parts);
    	key= MD5Util.encrypt16(key);
    	return key;
	}


}
