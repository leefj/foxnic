package com.github.foxnic.dao.dataperm.model;

import com.github.foxnic.api.dataperm.ConditionNodeType;
import com.github.foxnic.api.dataperm.ExprType;
import com.github.foxnic.api.dataperm.LogicType;

import java.util.List;

public class DataPermCondition {

    private String id;
    private String parentId;
    private String queryProperty;
    private String queryField;
    private ConditionNodeType nodeType;
    private LogicType logicType;
    private ExprType exprType;

    private DataPermCondition parent;
    private List<DataPermCondition> children;

    public DataPermCondition getParent() {
        return parent;
    }

    public void setParent(DataPermCondition parent) {
        this.parent = parent;
    }

    public List<DataPermCondition> getChildren() {
        return children;
    }

    public void setChildren(List<DataPermCondition> children) {
        this.children = children;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getQueryProperty() {
        return queryProperty;
    }

    public void setQueryProperty(String queryProperty) {
        this.queryProperty = queryProperty;
    }

    public String getQueryField() {
        return queryField;
    }

    public void setQueryField(String queryField) {
        this.queryField = queryField;
    }

    public ConditionNodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(ConditionNodeType nodeType) {
        this.nodeType = nodeType;
    }

    public LogicType getLogicType() {
        return logicType;
    }

    public void setLogicType(LogicType logicType) {
        this.logicType = logicType;
    }

    public ExprType getExprType() {
        return exprType;
    }

    public void setExprType(ExprType exprType) {
        this.exprType = exprType;
    }



}
