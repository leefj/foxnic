package com.github.foxnic.dao.dataperm.model;

import com.alibaba.fastjson.JSONArray;
import com.github.foxnic.api.dataperm.ConditionNodeType;
import com.github.foxnic.api.dataperm.ExprType;
import com.github.foxnic.api.dataperm.LogicType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DataPermCondition {

    private String id;
    private String parentId;
    private String queryProperty;
    private String queryField;
    private ConditionNodeType nodeType;
    private LogicType logicType;
    private ExprType exprType;
    private Integer sort;
    private JSONArray varibales;

    private String title;
    private String notes;

    private DataPermCondition parent;
    private List<DataPermCondition> children=new ArrayList<>();

    public DataPermCondition getParent() {
        return parent;
    }

    public void setParent(DataPermCondition parent) {
        this.parent = parent;
    }

    public List<DataPermCondition> getChildren() {
        return children;
    }

    public void addChildren(DataPermCondition child) {
        this.children.add(child);
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

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public JSONArray getVaribales() {
        return varibales;
    }

    public void setVaribales(JSONArray varibales) {
        this.varibales = varibales;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public void sortChildren() {
        this.children.sort(new Comparator<DataPermCondition>() {
            @Override
            public int compare(DataPermCondition a, DataPermCondition b) {
                if(a.sort==null && b.sort==null) {
                    return 0;
                } else if(a.sort!=null && b.sort==null) {
                    return 1;
                } else if(a.sort==null && b.sort!=null) {
                    return -1;
                } else if(a.sort > b.sort) {
                    return 1;
                } else if(a.sort < b.sort) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }
}
