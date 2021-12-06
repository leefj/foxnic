package com.github.foxnic.dao.relation;

import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.meta.DBField;

import java.util.ArrayList;
import java.util.List;

public class Join {

    private JoinPoint masterPoint;

    private JoinPoint slavePoint;

    private JoinType joinType=JoinType.JOIN;


	private String key = null;

	public Join clone() {
		Join join=new Join();
		join.masterPoint =this.masterPoint.clone();
		join.slavePoint =this.slavePoint.clone();
		join.joinType=this.joinType;
		return join;
	}


	public String getKey(){
		if(this.key!=null) return key;
		List<String> parts=new ArrayList<>();

		parts.add(masterPoint.getKey());
		parts.add(slavePoint.getKey());
		parts.add(joinType.name());

		key= StringUtil.join(parts);
		key= MD5Util.encrypt16(key);
		return key;
	}

	private Join() {}

	public Join(DBField... sourceField) {
		this.masterPoint =new JoinPoint(sourceField);
	}




    public void slave(DBField... targetField) {
        this.slavePoint =new JoinPoint(targetField);
    }

	public JoinPoint getMasterPoint() {
		return masterPoint;
	}

	public JoinPoint getSlavePoint() {
		return slavePoint;
	}

	public JoinType getJoinType() {
		return joinType;
	}

	@Override
	public String toString() {
		return this.masterPoint.toString()+"  <"+this.joinType.name()+">  "+this.slavePoint.toString();
	}

	public String getSourceTable() {
		return masterPoint.table().name();
	}

	public String getSlaveTable() {
		return slavePoint.table().name();
	}


	public DBField[] getMasterFields() {
		return masterPoint.fields();
	}

	public DBField[] getTargetFields() {
		return slavePoint.fields();
	}

	public void setJoinType(JoinType joinType) {
		this.joinType=joinType;
	}

	public String getTargetJoinKey() {
		List<String> fs=new ArrayList<>();
		for (DBField targetField : this.getTargetFields()) {
			fs.add(targetField.name());
		}
		return this.getSlaveTable()+":"+ StringUtil.join(fs);
	}
}
