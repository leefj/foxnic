package com.github.foxnic.dao.dataperm.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataPermRule {
    private String id;
    private String code;
    private String poType;
    private String name;

    /**
     * 规则应用的时间，可以用于核实规则是否是最新版本
     * */
    private Date applyTime;

    private List<DataPermRange> ranges=new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPoType() {
        return poType;
    }

    public void setPoType(String poType) {
        this.poType = poType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DataPermRange> getRanges() {
        return ranges;
    }

    public void addRanges(DataPermRange range) {
        this.ranges.add(range);
    }

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

}
