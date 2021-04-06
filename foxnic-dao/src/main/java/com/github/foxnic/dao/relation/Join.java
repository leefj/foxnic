package com.github.foxnic.dao.relation;

import java.util.ArrayList;
import java.util.List;

public class Join {

    private String sourceTable;
    private List<String> sourceTableFields=new ArrayList<>();

    private String targetTable;
    private List<String> targetTableFields=new ArrayList<>();

    private JoinType joinType=JoinType.JOIN;

    public Join join(String sourceTable, String targetTable) {
        this.sourceTable=sourceTable;
        this.targetTable=targetTable;
        this.joinType=JoinType.JOIN;
        return this;
    }

    public Join leftJoin(String sourceTable, String targetTable) {
        this.sourceTable=sourceTable;
        this.targetTable=targetTable;
        this.joinType=JoinType.LEFT_JOIN;
        return this;
    }

    public Join rightJoin(String sourceTable, String targetTable) {
        this.sourceTable=sourceTable;
        this.targetTable=targetTable;
        this.joinType=JoinType.RIGHT_JOIN;
        return this;
    }

    public Join on(String sourceTableField, String targetTableField) {
        this.sourceTableFields.add(sourceTableField);
        this.targetTableFields.add(targetTableField);
        return this;
    }

	public String getSourceTable() {
		return sourceTable;
	}

	public void setSourceTable(String sourceTable) {
		this.sourceTable = sourceTable;
	}

	public List<String> getSourceTableFields() {
		return sourceTableFields;
	}

	public void setSourceTableFields(List<String> sourceTableFields) {
		this.sourceTableFields = sourceTableFields;
	}

	public String getTargetTable() {
		return targetTable;
	}

	public void setTargetTable(String targetTable) {
		this.targetTable = targetTable;
	}

	public List<String> getTargetTableFields() {
		return targetTableFields;
	}

	public void setTargetTableFields(List<String> targetTableFields) {
		this.targetTableFields = targetTableFields;
	}

	public JoinType getJoinType() {
		return joinType;
	}

	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}

	
	private Join revertJoin=null;
	
	public Join getRevertJoin() {
		if(revertJoin!=null) return revertJoin;
		revertJoin=new Join();
		revertJoin.sourceTable=this.targetTable;
		revertJoin.sourceTableFields=this.targetTableFields;
		revertJoin.targetTable=this.sourceTable;
		revertJoin.targetTableFields=this.sourceTableFields;
		revertJoin.joinType=JoinType.JOIN;
		return revertJoin;
	}







}
