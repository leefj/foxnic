package com.github.foxnic.dao.dataperm.model;

import java.util.ArrayList;
import java.util.List;

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


}
