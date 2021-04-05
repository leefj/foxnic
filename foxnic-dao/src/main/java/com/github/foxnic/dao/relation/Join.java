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








}
