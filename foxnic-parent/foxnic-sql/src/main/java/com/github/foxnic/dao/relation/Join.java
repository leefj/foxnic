package com.github.foxnic.dao.relation;

import com.github.foxnic.sql.meta.DBField;

public class Join {

    private JoinPoint sourcePoint;

    private JoinPoint targetPoint;

    private JoinType joinType=JoinType.JOIN;

	public Join(JoinType joinType,DBField... sourceField) {
		this.joinType=joinType;
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
}
