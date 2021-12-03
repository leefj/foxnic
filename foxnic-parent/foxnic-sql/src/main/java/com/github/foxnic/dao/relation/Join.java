package com.github.foxnic.dao.relation;

import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.meta.DBField;

import java.util.ArrayList;
import java.util.List;

public class Join {

    private JoinPoint sourcePoint;

    private JoinPoint targetPoint;

    private JoinType joinType=JoinType.JOIN;


	private String key = null;

	public Join clone() {
		Join join=new Join();
		join.sourcePoint=this.sourcePoint.clone();
		join.targetPoint=this.targetPoint.clone();
		join.joinType=this.joinType;
		return join;
	}


	public String getKey(){
		if(this.key!=null) return key;
		List<String> parts=new ArrayList<>();

		parts.add(sourcePoint.getKey());
		parts.add(targetPoint.getKey());
		parts.add(joinType.name());

		key= StringUtil.join(parts);
		key= MD5Util.encrypt16(key);
		return key;
	}

	private Join() {}

	public Join(DBField... sourceField) {
		this.sourcePoint=new JoinPoint(sourceField);
	}




    public void target(DBField... targetField) {
        this.targetPoint=new JoinPoint(targetField);
    }

	public JoinPoint getSourcePoint() {
		return sourcePoint;
	}

	public JoinPoint getTargetPoint() {
		return targetPoint;
	}

	public JoinType getJoinType() {
		return joinType;
	}

	@Override
	public String toString() {
		return this.sourcePoint.toString()+"  <"+this.joinType.name()+">  "+this.targetPoint.toString();
	}

	public String getSourceTable() {
		return sourcePoint.table().name();
	}

	public String getTargetTable() {
		return targetPoint.table().name();
	}


	public DBField[] getSourceFields() {
		return sourcePoint.fields();
	}

	public DBField[] getTargetFields() {
		return targetPoint.fields();
	}

	public void setJoinType(JoinType joinType) {
		this.joinType=joinType;
	}

	public String getTargetJoinKey() {
		List<String> fs=new ArrayList<>();
		for (DBField targetField : this.getTargetFields()) {
			fs.add(targetField.name());
		}
		return this.getTargetTable()+":"+ StringUtil.join(fs);
	}
}
