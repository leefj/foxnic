package com.github.foxnic.dao.dataperm.model;

import com.github.foxnic.commons.collection.CollectorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataPermRange {
    private String id;
    private String name;
    private List<DataPermCondition> conditions=new ArrayList<>();

    private DataPermCondition root;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DataPermCondition> getConditions() {
        return conditions;
    }

    public void addConditions(DataPermCondition condition) {
        this.conditions.add(condition);
    }

    public void buildTreeIf() {
        if(this.root!=null) return;
        Map<String,DataPermCondition> map= CollectorUtil.collectMap(this.conditions,(c)->{return c.getId();},(c)->{return c;});
        for (DataPermCondition condition : this.conditions) {
            DataPermCondition parent=map.get(condition.getParentId());
            if(parent==null && this.root==null) {
                this.root = condition;
                continue;
            }
            parent.addChildren(condition);
        }

        for (DataPermCondition condition : this.conditions) {
            condition.sortChildren();
        }

    }

    public DataPermCondition getRoot() {
        return root;
    }


}
